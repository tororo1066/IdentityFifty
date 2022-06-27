package tororo1066.identityfifty

import com.destroystokyo.paper.event.player.PlayerJumpEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Door
import org.bukkit.block.data.type.Stairs
import org.bukkit.block.data.type.Stairs.Shape
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Cow
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Sheep
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockCanBuildEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Team
import tororo1066.identityfifty.data.GeneratorData
import tororo1066.identityfifty.data.MapData
import tororo1066.identityfifty.data.SurvivorData
import tororo1066.tororopluginapi.otherPlugin.SWorldGuardAPI
import tororo1066.tororopluginapi.sEvent.SEvent
import tororo1066.tororopluginapi.sItem.SItem
import java.time.Duration
import java.util.UUID
import java.util.function.Consumer
import kotlin.random.Random

class IdentityFiftyTask(val map: MapData) : Thread() {



    private fun allPlayerAction(action: (UUID)->Unit){
        IdentityFifty.survivors.forEach {
            action.invoke(it.key)
        }

        IdentityFifty.hunters.forEach {
            action.invoke(it.key)
        }
    }

    private fun onlinePlayersAction(action: (Player) -> Unit){
        Bukkit.getOnlinePlayers().forEach {
            action.invoke(it)
        }
    }

    private fun broadcast(string: String){
        Bukkit.broadcast(Component.text(string),Server.BROADCAST_CHANNEL_USERS)
    }

    private fun runTask(unit: ()->Unit){
        Bukkit.getScheduler().runTask(IdentityFifty.plugin, Runnable {
            unit.invoke()
        })
    }

    fun allPlayerSound(loc: Location, sound: Sound, volume: Float, pitch: Float){
        allPlayerAction {
            val p = Bukkit.getPlayer(it)!!
            p.playSound(loc,sound, volume, pitch)
        }
    }

    private fun taskSurvivor(task: (Map.Entry<UUID,SurvivorData>)->Unit){
        IdentityFifty.survivors.forEach {
            if (it.value.getHealth() < 1)return@forEach
            task.invoke(it)
        }
    }

    private fun changeBlockFace(blockFace: BlockFace): BlockFace {
        return when(blockFace){
            BlockFace.WEST-> BlockFace.EAST
            BlockFace.EAST-> BlockFace.WEST
            BlockFace.NORTH-> BlockFace.SOUTH
            BlockFace.SOUTH-> BlockFace.NORTH
            else-> blockFace
        }
    }

    fun end(){
        end = true
        sEvent.unregisterAll()
        generatorUUID.forEach {
            runTask {
                Bukkit.getEntity(it)?.remove()
            }
        }

        escapeGeneratorUUID.forEach {
            runTask {
                Bukkit.getEntity(it)?.remove()
            }
        }

        prisonUUID.forEach {
            runTask {
                Bukkit.getEntity(it)?.remove()
            }
        }

        map.escapeGenerators.forEach {
            val getBlock = map.world.getBlockAt(it.doorLocation)
            val blockData = getBlock.blockData as Door
            blockData.isOpen = false
        }

        IdentityFifty.survivors.forEach {
            it.value.healBossBar.removeAll()
            it.value.helpBossBar.removeAll()
        }

        IdentityFifty.survivors.clear()
        IdentityFifty.hunters.clear()



        Bukkit.getOnlinePlayers().forEach {
            it.walkSpeed = 0.2f
            it.playSound(it.location,Sound.UI_TOAST_CHALLENGE_COMPLETE,1f,1f)
        }
        if (escapedSurvivor.size > survivorSize/2){

            onlinePlayersAction {
                it.showTitle(Title.title(Component.text("§e§lサバイバーの勝ち！"), Component.text(""), Title.Times.of(
                    Duration.ZERO, Duration.ofSeconds(3), Duration.ZERO)))
            }
        } else {
            if (escapedSurvivor.size == survivorSize){
                onlinePlayersAction {
                    it.showTitle(Title.title(Component.text("§b§l引き分け！"), Component.text(""), Title.Times.of(
                    Duration.ZERO, Duration.ofSeconds(3), Duration.ZERO)))
                }
            } else {
                onlinePlayersAction {
                    it.showTitle(Title.title(Component.text("§c§lハンターの勝ち！"), Component.text(""), Title.Times.of(
                    Duration.ZERO, Duration.ofSeconds(3), Duration.ZERO)))
                }
            }
        }
    }

    private var remainingGenerator = map.generatorGoal
    private var worldGuard = SWorldGuardAPI()
    private val sbManager = Bukkit.getScoreboardManager()
    private val scoreboard = sbManager.newScoreboard
    private val generatorUUID = ArrayList<UUID>()
    private val escapeGeneratorUUID = ArrayList<UUID>()
    private val prisonUUID = ArrayList<UUID>()
    private val woodPlateUUID = ArrayList<UUID>()
    private val sEvent = SEvent(IdentityFifty.plugin)
    private val survivorSize = IdentityFifty.survivors.size
    private var survivorCount = IdentityFifty.survivors.size
    private val escapedSurvivor = ArrayList<UUID>()
    private var end = false

    val survivorTeam = scoreboard.registerNewTeam("Survivor")
    val hunterTeam = scoreboard.registerNewTeam("Hunter")


    init {
        survivorTeam.setOption(Team.Option.NAME_TAG_VISIBILITY,Team.OptionStatus.NEVER)
        survivorTeam.setCanSeeFriendlyInvisibles(true)

        hunterTeam.setOption(Team.Option.NAME_TAG_VISIBILITY,Team.OptionStatus.NEVER)
        hunterTeam.setCanSeeFriendlyInvisibles(true)
    }

    override fun run() {
        broadcast("ゲームをスタート中...")

        val survivorSpawnList = ArrayList<Location>(map.survivorSpawnLocations)
        survivorSpawnList.shuffle()
        val hunterSpawnList = ArrayList<Location>(map.hunterSpawnLocations)
        hunterSpawnList.shuffle()
        val generatorList = ArrayList<GeneratorData>(map.generators)
        generatorList.shuffle()
        val escapeGeneratorList = ArrayList<GeneratorData>(map.escapeGenerators)
        escapeGeneratorList.shuffle()

        IdentityFifty.survivors.forEach { (uuid, data) ->
            val p = Bukkit.getPlayer(uuid)!!

            runTask {
                p.walkSpeed = 0.2f
                p.foodLevel = 20
                p.gameMode = GameMode.SURVIVAL
                p.inventory.clear()
                p.teleport(survivorSpawnList.removeAt(0))
                data.survivorClass.onStart(p)
            }

            IdentityFifty.stunEffect(p)
        }

        IdentityFifty.hunters.forEach { (uuid, data) ->
            val p = Bukkit.getPlayer(uuid)!!

            runTask {
                p.walkSpeed = 0.28f
                p.foodLevel = 20
                p.gameMode = GameMode.SURVIVAL
                p.inventory.clear()
                p.teleport(hunterSpawnList.removeAt(0))
                data.hunterClass.onStart(p)
            }
            IdentityFifty.stunEffect(p)
        }

        map.prisons.forEach {
            runTask {
                it.key.world.spawn(it.key,ArmorStand::class.java) { stand ->
                    stand.persistentDataContainer.set(NamespacedKey(IdentityFifty.plugin,"PrisonLoc"), PersistentDataType.INTEGER_ARRAY,
                        intArrayOf(it.key.blockX,it.key.blockY,it.key.blockZ))
                    stand.isInvisible = true
                    stand.isInvulnerable = true
                    prisonUUID.add(stand.uniqueId)
                }
            }
        }

        map.woodPlates.forEach {  (_,data) ->

            runTask {
                var block = data.loc.block
                for (i in 0 until data.length) {
                    block.type = Material.AIR
                    block = block.getRelative(data.face)
                }

                val loc = data.loc.clone()
                val centerValue = (data.length / 2.0)
                when (data.face) {
                    BlockFace.NORTH -> {
                        loc.add(0.0, 0.0, -centerValue)
                    }
                    BlockFace.SOUTH -> {
                        loc.add(0.0, 0.0, centerValue)
                    }
                    BlockFace.WEST -> {
                        loc.add(-centerValue, 0.0, 0.0)
                    }
                    BlockFace.EAST -> {
                        loc.add(centerValue, 0.0, 0.0)
                    }
                    else -> {}
                }

                map.world.spawn(loc, ArmorStand::class.java) { stand ->
                    stand.persistentDataContainer.set(
                        NamespacedKey(IdentityFifty.plugin, "PlateLoc"), PersistentDataType.INTEGER_ARRAY,
                        intArrayOf(data.loc.blockX, data.loc.blockY, data.loc.blockZ)
                    )
                    stand.isInvisible = true
                    stand.isInvulnerable = true
                    woodPlateUUID.add(stand.uniqueId)
                }

                var upBlock = data.loc.block
                for (i in 0 until data.length) {
                    upBlock.type = Material.OAK_STAIRS
                    val blockData = upBlock.blockData as Stairs
                    blockData.shape = Shape.STRAIGHT
                    blockData.half = Bisected.Half.TOP
                    blockData.facing = changeBlockFace(data.face)
                    upBlock.blockData = blockData

                    upBlock = upBlock.getRelative(BlockFace.UP)

                }
            }
        }

        for (i in 1..map.generatorLimit){
            val data = generatorList.removeAt(0)
            runTask {
                map.world.spawn(data.location,Sheep::class.java) {
                    it.setAI(false)
                    it.isSilent = true
                    it.color = DyeColor.WHITE
                    generatorUUID.add(it.uniqueId)
                    it.customName = "§f§l羊型発電機§5(§e${data.health}§f/§b${data.health}§5)"
                    it.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.baseValue = data.health.toDouble()
                    it.health = data.health.toDouble()
                    it.persistentDataContainer.set(NamespacedKey(IdentityFifty.plugin,"Generator"), PersistentDataType.INTEGER,1)
                }
            }
        }



        IdentityFifty.survivors.forEach {
            survivorTeam.addEntry(it.value.name)
        }

        IdentityFifty.hunters.forEach {
            hunterTeam.addEntry(it.value.name)
        }





        val bossBar = Bukkit.createBossBar("§d残り暗号機§f：§c§l${map.generatorLimit}§f個",BarColor.YELLOW,BarStyle.SOLID)
        bossBar.isVisible = true
        Bukkit.getOnlinePlayers().forEach {
            bossBar.addPlayer(it)
        }

        sEvent.register(FoodLevelChangeEvent::class.java) { e ->
            if (IdentityFifty.survivors.containsKey(e.entity.uniqueId) || IdentityFifty.hunters.containsKey(e.entity.uniqueId)){
                e.isCancelled = true
            }
        }

        sEvent.register(EntityDeathEvent::class.java) { e ->
            if (e.entity.type != EntityType.SHEEP)return@register
            if (!e.entity.persistentDataContainer.has(NamespacedKey(IdentityFifty.plugin,"Generator"),
                    PersistentDataType.INTEGER))return@register
            if (remainingGenerator == 0)return@register
            e.drops.clear()
            generatorUUID.remove(e.entity.uniqueId)
            remainingGenerator--
            bossBar.setTitle("§d残り暗号機§f：§c§l${remainingGenerator}§f個")
            bossBar.progress = remainingGenerator.toDouble() / map.generatorLimit.toDouble()
            IdentityFifty.hunters.forEach {
                runTask {
                    it.value.hunterClass.onFinishedGenerator(e.entity.location,remainingGenerator,Bukkit.getPlayer(it.key)!!)
                }
            }
            taskSurvivor {
                runTask {
                    it.value.survivorClass.onFinishedGenerator(e.entity.location,remainingGenerator,Bukkit.getPlayer(it.key)!!)
                }
            }
            if (remainingGenerator != 0)return@register
            bossBar.progress = 1.0
            bossBar.setTitle("§dゲートを開けよう！")
            bossBar.color = BarColor.RED

            broadcast("§e§lゲート付近の発電機が開かれた！")

            Bukkit.getOnlinePlayers().forEach {
                it.playSound(it.location,Sound.ENTITY_WITHER_SPAWN,1f,1f)
            }

            for (i in 1..map.escapeGeneratorLimit){
                val data = escapeGeneratorList.removeAt(0)
                runTask {
                    map.world.spawn(data.location,Cow::class.java) {
                        it.setAI(false)
                        it.isSilent = true
                        it.customName = "§f§l牛型発電機§5(§e${it.health.toInt()}§f/§b${data.health}§5)"
                        it.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.baseValue = data.health.toDouble()
                        it.health = data.health.toDouble()
                        it.persistentDataContainer.set(NamespacedKey(IdentityFifty.plugin,"EscapeGenerator"), PersistentDataType.INTEGER,1)
                        escapeGeneratorUUID.add(it.uniqueId)
                    }
                }
            }


            sEvent.register(EntityDeathEvent::class.java) second@{ e2 ->

                if (e2.entity.type != EntityType.COW)return@second
                if (!e2.entity.persistentDataContainer.has(NamespacedKey(IdentityFifty.plugin,"EscapeGenerator"),
                        PersistentDataType.INTEGER))return@second

                escapeGeneratorUUID.remove(e2.entity.uniqueId)
                e.drops.clear()

                val loc = e2.entity.location.toBlockLocation()
                loc.yaw = 0f
                loc.pitch = 0f

                val data = map.escapeGenerators.find { it.location == loc }!!

                val getBlock = map.world.getBlockAt(data.doorLocation)
                val blockData = getBlock.blockData as Door

                blockData.isOpen = true

                taskSurvivor {
                    runTask {
                        it.value.survivorClass.onFinishedEscapeGenerator(loc,Bukkit.getPlayer(it.key)!!)
                    }
                }

                IdentityFifty.hunters.forEach {
                    runTask {
                        it.value.hunterClass.onFinishedEscapeGenerator(loc,Bukkit.getPlayer(it.key)!!)
                    }
                }

            }


            generatorUUID.forEach {
                runTask {
                    Bukkit.getEntity(it)?.remove()
                }
                generatorUUID.remove(it)
            }



        }

        sEvent.register(PlayerToggleSneakEvent::class.java) { e ->
            if (!IdentityFifty.survivors.containsKey(e.player.uniqueId))return@register
            if (e.isSneaking){
                e.player.location.getNearbyEntitiesByType(ArmorStand::class.java,3.0).forEach {
                    if (it.persistentDataContainer.has(NamespacedKey(IdentityFifty.plugin,"PrisonLoc"),
                            PersistentDataType.INTEGER_ARRAY)){
                        val intArray = it.persistentDataContainer[NamespacedKey(IdentityFifty.plugin,"PrisonLoc"), PersistentDataType.INTEGER_ARRAY]!!
                        val prisonData = map.prisons[Location(map.world,intArray[0].toDouble(),intArray[1].toDouble(),intArray[2].toDouble())]!!
                        val helperData = IdentityFifty.survivors[e.player.uniqueId]!!

                        if (prisonData.inPlayer.isEmpty())return@forEach

                        var helpTime = 0
                        helpTime += helperData.helpTick
                        for (uuid in prisonData.inPlayer){
                            val data = IdentityFifty.survivors[uuid]!!
                            helpTime += data.otherPlayerHelpDelay
                        }

                        for (uuid in prisonData.inPlayer){
                            val data = IdentityFifty.survivors[uuid]!!
                            helpTime += (data.otherPlayerHelpDelayPercentage * helpTime).toInt()
                        }

                        val helpTimeInitial = helpTime

                        helperData.helpBossBar = Bukkit.createBossBar("§e救助中...",BarColor.BLUE,BarStyle.SOLID)
                        helperData.helpBossBar.progress = 0.0
                        helperData.helpBossBar.addPlayer(e.player)


                        Bukkit.getScheduler().runTaskTimer(IdentityFifty.plugin, Consumer { task ->
                            if (!e.player.location.getNearbyEntitiesByType(ArmorStand::class.java,3.0).contains(it) || !e.player.isSneaking){
                                helperData.helpBossBar.removeAll()
                                task.cancel()
                                return@Consumer
                            } else {
                                if (helpTime <= 0) {

                                    prisonData.lastPressUUID = e.player.uniqueId
                                    val block = map.world.getBlockAt(prisonData.doorLoc)
                                    val blockData = block.blockData as Door
                                    blockData.isOpen = true
                                    block.blockData = blockData
                                    helperData.helpBossBar.removeAll()
                                    map.world.playSound(block.location,Sound.BLOCK_IRON_DOOR_OPEN,1f,1f)
                                    Bukkit.getScheduler().runTaskLater(IdentityFifty.plugin, Consumer {
                                        blockData.isOpen = false
                                        block.blockData = blockData
                                        map.world.playSound(block.location,Sound.BLOCK_IRON_DOOR_CLOSE,1f,1f)
                                    },60)
                                    task.cancel()
                                }
                                helpTime--
                                helperData.helpBossBar.progress = 1.0 - (helpTime.toDouble() / helpTimeInitial.toDouble())
                            }
                        },0,1)
                        return@register
                    }

                }

                e.player.location.getNearbyPlayers(3.0).forEach {
                    if (it == e.player)return@forEach
                    if (!IdentityFifty.survivors.containsKey(it.uniqueId))return@forEach
                    val helperData = IdentityFifty.survivors[e.player.uniqueId]!!
                    val playerData = IdentityFifty.survivors[it.uniqueId]!!

                    if (playerData.getHealth() == 5){
                        return@forEach
                    }

                    if (playerData.healingPlayers.isNotEmpty()){
                        playerData.healingPlayers[helperData.uuid] = helperData
                        playerData.healBossBar.addPlayer(e.player)
                        return@forEach
                    }
                    var healTime = 0
                    healTime += helperData.healTick

                    healTime += playerData.otherPlayerHealDelay

                    healTime += (playerData.otherPlayerHealDelayPercentage * healTime).toInt()

                    playerData.healBossBar = Bukkit.createBossBar("§d${playerData.name}§eを回復中...",BarColor.GREEN,BarStyle.SOLID)
                    playerData.healBossBar.progress = playerData.healProcess
                    playerData.healBossBar.addPlayer(e.player)

                    playerData.healingPlayers[helperData.uuid] = helperData


                    Bukkit.getScheduler().runTaskTimer(IdentityFifty.plugin, Consumer { task ->
                        if (playerData.healProcess >= 1.0){
                            playerData.healProcess = 0.0
                            playerData.healingPlayers.clear()
                            playerData.healBossBar.removeAll()
                            map.world.playSound(it.location,Sound.ENTITY_PLAYER_LEVELUP,1f,1f)
                            playerData.setHealth(playerData.getHealth() + 2)
                            task.cancel()
                            return@Consumer
                        }
                        val players = it.location.getNearbyPlayers(3.0)
                        playerData.healingPlayers.forEach { (key, _)->
                            val p = Bukkit.getPlayer(key)!!
                            if (!players.contains(p)){
                                playerData.healingPlayers.remove(key)
                                playerData.healBossBar.removePlayer(p)
                            }
                        }

                        var allPlayerHealTick = 0
                        var healPlayers = 0
                        players.forEach PlayersForEach@ { p ->
                            if (!playerData.healingPlayers.containsKey(p.uniqueId))return@PlayersForEach
                            if (!p.isSneaking){
                                playerData.healingPlayers.remove(p.uniqueId)
                                playerData.healBossBar.removePlayer(p)
                                return@PlayersForEach
                            }
                            val data = playerData.healingPlayers[p.uniqueId]!!
                            healPlayers++
                            allPlayerHealTick += data.healTick

                        }

                        if (healPlayers == 0){
                            task.cancel()
                            return@Consumer
                        }

                        healTime = allPlayerHealTick / healPlayers
                        healTime = (healTime / (1 + (0.5 * (healPlayers - 1)))).toInt()
                        healTime += playerData.otherPlayerHealDelay
                        healTime += (playerData.otherPlayerHealDelayPercentage * healTime).toInt()

                        playerData.healBossBar.progress = playerData.healProcess

                        playerData.healProcess += 1.0 / healTime

                    },0,1)
                }
            }
        }

        sEvent.register(PlayerJumpEvent::class.java) { e ->
            if (IdentityFifty.hunters.containsKey(e.player.uniqueId)){
                e.isCancelled = true
            }
        }

        sEvent.register(BlockBreakEvent::class.java) { e ->
            if (IdentityFifty.survivors.containsKey(e.player.uniqueId)){
                e.isCancelled = true
                return@register
            }

            if (IdentityFifty.hunters.containsKey(e.player.uniqueId)){
                if (e.block.type != Material.OAK_STAIRS){
                    e.isCancelled = true
                    return@register
                }
            }
        }

        sEvent.register(BlockCanBuildEvent::class.java) { e ->
            val p = e.player?:return@register
            if (IdentityFifty.survivors.containsKey(p.uniqueId) || IdentityFifty.hunters.containsKey(p.uniqueId)){
                e.isBuildable = false
            }
        }


        sEvent.register(PlayerMoveEvent::class.java) { e ->
            if (!IdentityFifty.survivors.containsKey(e.player.uniqueId))return@register
            val from = e.from.clone()
            from.yaw = 0f
            from.pitch = 0f
            val to = e.to.clone()
            to.yaw = 0f
            to.pitch = 0f

            if (from == to)return@register



            val loc = to.toBlockLocation()
            if (map.prisons.containsKey(loc)){
                if (IdentityFifty.survivors.containsKey(e.player.uniqueId)) {
                    val data = map.prisons[loc]!!
                    if (data.inPlayer.contains(e.player.uniqueId)) {
                        data.inPlayer.remove(e.player.uniqueId)
                        val playerData = IdentityFifty.survivors[e.player.uniqueId]!!
                        val helperData = IdentityFifty.survivors[data.lastPressUUID]!!
                        helperData.survivorClass.onHelp(Bukkit.getPlayer(helperData.uuid)!!,e.player)
                        playerData.survivorClass.onGotHelp(e.player,Bukkit.getPlayer(helperData.uuid)!!)
                        IdentityFifty.hunters.forEach {
                            it.value.hunterClass.onSurvivorHelp(Bukkit.getPlayer(helperData.uuid)!!,e.player,Bukkit.getPlayer(it.key)!!)
                        }
                        playerData.setHealth(3,true)
                    }
                }
            }

            val data = IdentityFifty.survivors[e.player.uniqueId]!!
            var footprints = data.footprintsTime

            if (data.footprintsCount >= 5){
                data.footprintsCount = 0
                Bukkit.getScheduler().runTaskTimer(IdentityFifty.plugin, Consumer {
                    if (footprints <= 0){
                        it.cancel()
                    }

                    if (!e.player.isSneaking){
                        IdentityFifty.hunters.forEach { (uuid,_) ->
                            Bukkit.getPlayer(uuid)?.spawnParticle(Particle.REDSTONE,e.to,2,
                                Random.nextDouble(-0.2,0.2),0.2,
                                Random.nextDouble(-0.2,0.2), Particle.DustTransition(Color.RED,Color.WHITE,1f))
                            Bukkit.getPlayer(uuid)?.spawnParticle(Particle.REDSTONE,e.to,2,
                                Random.nextDouble(-0.2,0.2),0.2,
                                Random.nextDouble(-0.2,0.2), Particle.DustTransition(Color.BLACK,Color.WHITE,1f))
                        }
                    }

                    footprints--
                },0,20)
            } else {
                data.footprintsCount++
            }



            val entities = e.player.location.getNearbyEntitiesByType(ArmorStand::class.java,3.0)

            if (entities.isEmpty()){
                if (e.player.inventory.itemInOffHand.type != Material.AIR){
                    e.player.inventory.setItemInOffHand(ItemStack(Material.AIR))
                }
            } else {
                if (e.player.inventory.itemInOffHand.type == Material.AIR){
                    entities.forEach {

                        if (it.persistentDataContainer.has(NamespacedKey(IdentityFifty.plugin,"PlateLoc"),
                                PersistentDataType.INTEGER_ARRAY)){
                                    val intArray = it.persistentDataContainer[NamespacedKey(IdentityFifty.plugin,"PlateLoc"), PersistentDataType.INTEGER_ARRAY]!!
                                    val plateData = map.woodPlates[Location(map.world,intArray[0].toDouble(),intArray[1].toDouble(),intArray[2].toDouble())]!!
                                    val item = IdentityFifty.interactManager.createSInteractItem(SItem(Material.WOODEN_HOE).setDisplayName("§e板倒し").setCustomModelData(3),true).setInteractEvent { e, item ->
                                        var upBlock = plateData.loc.block
                                        for (i in 0 until plateData.length) {
                                            upBlock.type = Material.AIR
                                            upBlock = upBlock.getRelative(BlockFace.UP)
                                        }

                                        var faceBlock = plateData.loc.block

                                        for (i in 0 until plateData.length){
                                            faceBlock.type = Material.OAK_STAIRS
                                            val blockData = faceBlock.blockData as Stairs
                                            blockData.shape = Shape.STRAIGHT
                                            blockData.half = Bisected.Half.TOP
                                            blockData.facing = plateData.face
                                            faceBlock.blockData = blockData
                                            faceBlock = faceBlock.getRelative(plateData.face)
                                        }

                                        map.world.playSound(it.location,Sound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR,1f,0.8f)
                                        it.location.getNearbyPlayers(3.0).forEach second@ { p ->
                                            if (!IdentityFifty.hunters.containsKey(p.uniqueId))return@second
                                            map.world.playSound(it.location,Sound.BLOCK_ANVIL_PLACE,0.2f,0.5f)
                                            map.world.spawnParticle(Particle.ELECTRIC_SPARK,p.location.add(0.0,0.5,0.0),30)
                                            IdentityFifty.stunEffect(p)
                                        }
                                        woodPlateUUID.remove(it.uniqueId)
                                        it.remove()
                                        e.player.inventory.setItemInOffHand(SItem(Material.AIR))
                                        item.delete()
                                    }

                            e.player.inventory.setItemInOffHand(item)


                        }
                    }
                }

            }


            if (!worldGuard.inRegion(e.player,map.goalRegions))return@register
            escapedSurvivor.add(e.player.uniqueId)
            survivorCount--
            data.setHealth(-1,true)
            e.player.gameMode = GameMode.SPECTATOR
            broadcast("§e§l${e.player.name}§a§lは脱出に成功した！")
            if (survivorCount == 0){
                end()
            }
        }

        sEvent.register(EntityDamageEvent::class.java) { e ->

            if (IdentityFifty.hunters.containsKey(e.entity.uniqueId)){
                e.isCancelled = true
            }

            if (IdentityFifty.survivors.containsKey(e.entity.uniqueId)){
                if (e.cause == EntityDamageEvent.DamageCause.ENTITY_ATTACK || e.cause == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK){
                    return@register
                }
                e.isCancelled = true
            }

        }

        sEvent.register(EntityDamageByEntityEvent::class.java) { e ->
            if (IdentityFifty.hunters.containsKey(e.entity.uniqueId)){
                e.isCancelled = true
                return@register
            }
            if (IdentityFifty.survivors.containsKey(e.entity.uniqueId)){
                e.damage = 0.0
                if (IdentityFifty.hunters.containsKey(e.damager.uniqueId)){
                    val survivorData = IdentityFifty.survivors[e.entity.uniqueId]!!
                    val hunterData = IdentityFifty.hunters[e.damager.uniqueId]!!
                    if (survivorData.getHealth() == 0)return@register
                    var damage = hunterData.hunterClass.onAttack(e.entity as Player, e.damager as Player,remainingGenerator == 0)
                    if (damage <= 0)return@register
                    if (survivorData.getHealth() < damage) damage = survivorData.getHealth()
                    e.entity.world.playSound(e.entity.location,Sound.BLOCK_ANVIL_PLACE,1f,0.5f)
                    if (!survivorData.survivorClass.onDamage(survivorData.getHealth()-damage,e.damager as Player, e.entity as Player)){
                        IdentityFifty.stunEffect(e.damager as Player)
                        return@register
                    }

                    IdentityFifty.stunEffect(e.damager as Player)
                    map.world.spawnParticle(Particle.ELECTRIC_SPARK,e.entity.location,30)
                    survivorData.setHealth(survivorData.getHealth() - damage)
                    if (survivorData.getHealth() == 1){
                        broadcast("${survivorData.name}が捕まった！")
                        val prisons = map.prisons.filter { it.value.inPlayer.size == 0 }.entries.shuffled()
                        if (prisons.isNotEmpty()){
                            val data = prisons[0]
                            data.value.inPlayer.add(e.entity.uniqueId)
                            runTask { e.entity.teleport(data.value.spawnLoc) }

                        } else {
                            val data = map.prisons.entries.shuffled()[0]
                            data.value.inPlayer.add(e.entity.uniqueId)
                            runTask { e.entity.teleport(data.value.spawnLoc) }
                        }
                    }
                }
            }

            if (e.damager !is Player)return@register

            if (!IdentityFifty.survivors.containsKey(e.damager.uniqueId)){
                e.isCancelled = true
                return@register
            }

            val p = e.damager as Player
            val survivorData = IdentityFifty.survivors[e.damager.uniqueId]!!

            if (e.entity.type == EntityType.SHEEP && e.entity.persistentDataContainer.has(NamespacedKey(IdentityFifty.plugin,"Generator"), PersistentDataType.INTEGER)){
                val sheep = e.entity as Sheep
                e.damage = survivorData.survivorClass.sheepGeneratorModify(e.damage,remainingGenerator,sheep.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value,sheep.health,p)
                e.entity.customName = "§f§l羊型発電機§5(§e${sheep.health.toInt()}§f/§b${sheep.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value.toInt()}§5)"
            }

            if (e.entity.type == EntityType.COW && e.entity.persistentDataContainer.has(NamespacedKey(IdentityFifty.plugin,"EscapeGenerator"), PersistentDataType.INTEGER)){
                val cow = e.entity as Cow
                e.damage = survivorData.survivorClass.cowGeneratorModify(e.damage, cow.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value,cow.health,p)
                e.entity.customName = "§f§l牛型発電機§5(§e${cow.health.toInt()}§f/§b${cow.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value.toInt()}§5)"
            }
        }

        sleep(3000)

        allPlayerAction {
            Thread {
                val p = Bukkit.getPlayer(it)!!
                for (i in 5 downTo 1){
                    p.showTitle(Title.title(Component.text("§e－－－－§f§l${i}§e－－－－"), Component.text(""), Title.Times.of(
                        Duration.ZERO,Duration.ofSeconds(1),Duration.ZERO)))
                    p.playSound(p.location, Sound.UI_BUTTON_CLICK,1f,2f)
                    sleep(1000)
                }

                p.showTitle(Title.title(Component.text("§c§lSTART!"), Component.text(""), Title.Times.of(
                    Duration.ZERO,Duration.ofSeconds(2),Duration.ofSeconds(1))))
                p.playSound(p.location,Sound.ENTITY_WITHER_SPAWN,0.7f,1f)
            }.start()
        }

        Bukkit.getScheduler().runTaskTimer(IdentityFifty.plugin, Consumer {
            IdentityFifty.hunters.forEach {
                val playerLoc = Bukkit.getPlayer(it.key)!!.location

                playerLoc.getNearbyPlayers(25.0).forEach PlayersForEach@ { p ->
                    if (!IdentityFifty.survivors.containsKey(p.uniqueId))return@PlayersForEach
                    if (escapedSurvivor.contains(p.uniqueId))return@PlayersForEach
                    val data = IdentityFifty.survivors[p.uniqueId]!!
                    data.heartProcess += 0.2
                }

                playerLoc.getNearbyPlayers(20.0).forEach PlayersForEach@ { p ->
                    if (!IdentityFifty.survivors.containsKey(p.uniqueId))return@PlayersForEach
                    if (escapedSurvivor.contains(p.uniqueId))return@PlayersForEach
                    val data = IdentityFifty.survivors[p.uniqueId]!!
                    data.heartProcess += 0.1
                }

                playerLoc.getNearbyPlayers(15.0).forEach PlayersForEach@ { p ->
                    if (!IdentityFifty.survivors.containsKey(p.uniqueId))return@PlayersForEach
                    if (escapedSurvivor.contains(p.uniqueId))return@PlayersForEach
                    val data = IdentityFifty.survivors[p.uniqueId]!!
                    data.heartProcess += 0.2
                }

                playerLoc.getNearbyPlayers(10.0).forEach PlayersForEach@ { p ->
                    if (!IdentityFifty.survivors.containsKey(p.uniqueId))return@PlayersForEach
                    if (escapedSurvivor.contains(p.uniqueId))return@PlayersForEach
                    val data = IdentityFifty.survivors[p.uniqueId]!!
                    data.heartProcess += 0.5
                }

            }

            IdentityFifty.survivors.forEach { (uuid,data) ->
                if (data.heartProcess >= 1.0){
                    data.heartProcess = 0.0
                    val p = Bukkit.getPlayer(uuid)!!
                    p.playSound(p.location,"identity.heart",1f,1f)
                    p.spawnParticle(Particle.SPELL_WITCH,p.location,20,0.0,0.5,0.0)
                }
            }
        },0,10)


        Bukkit.getScheduler().runTaskTimer(IdentityFifty.plugin, Consumer {
            if (end){
                it.cancel()
                return@Consumer
            }

            for (survivor in IdentityFifty.survivors.values){
                val p = Bukkit.getPlayer(survivor.uuid)?:continue
                if (survivor.getHealth() == 1){
                    if (survivor.remainingTime <= 0){
                        survivor.setHealth(0,true)
                        survivorCount--
                        p.gameMode = GameMode.SPECTATOR
                        broadcast("§c§l${survivor.name}§4§lは死んでしまった...")

                        if (survivorCount == 0){
                            end()
                            it.cancel()
                            return@Consumer
                        }

                        taskSurvivor { data ->
                            if (data.key != p.uniqueId) runTask { data.value.survivorClass.onDieOtherSurvivor(p,survivorCount,Bukkit.getPlayer(data.key)!!) }
                        }
                        runTask { survivor.survivorClass.onDie(p) }

                        continue
                    }
                    survivor.remainingTime--
                }
            }


            scoreboard.getObjective("IdentityFifty")?.unregister()
            val ob = scoreboard.registerNewObjective("IdentityFifty","Dummy", Component.text("IdentityFifty"))


            for (survivor in IdentityFifty.survivors.values){
                val p = Bukkit.getPlayer(survivor.uuid)?:continue
                val prefix = when(survivor.getHealth()){
                    5-> "§a"
                    4-> "§6"
                    3-> "§c"
                    2-> "§4"
                    1-> "§0"
                    0-> "§f"
                    -1-> "§e"
                    else-> "Prefix Error"
                }

                ob.getScore(prefix + p.name).score = survivor.remainingTime
            }

            ob.displaySlot = DisplaySlot.SIDEBAR

            Bukkit.getOnlinePlayers().forEach { p ->
                p.scoreboard = scoreboard
            }




        },0,20)


    }
}
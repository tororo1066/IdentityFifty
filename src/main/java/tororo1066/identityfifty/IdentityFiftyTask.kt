package tororo1066.identityfifty

import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.*
import org.bukkit.Particle.DustOptions
import org.bukkit.attribute.Attribute
import org.bukkit.block.BlockFace
import org.bukkit.block.data.AnaloguePowerable
import org.bukkit.block.data.Powerable
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.entity.Cow
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Sheep
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scoreboard.DisplaySlot
import org.checkerframework.checker.units.qual.Volume
import tororo1066.identityfifty.data.GeneratorData
import tororo1066.identityfifty.data.MapData
import tororo1066.tororopluginapi.otherPlugin.SWorldGuardAPI
import tororo1066.tororopluginapi.sEvent.SEvent
import java.time.Duration
import java.util.UUID
import java.util.function.Consumer
import kotlin.random.Random

class IdentityFiftyTask(val map: MapData) : Thread() {

    fun allPlayerAction(action: (UUID)->Unit){
        IdentityFifty.survivors.forEach {
            action.invoke(it.key)
        }

        IdentityFifty.hunters.forEach {
            action.invoke(it.key)
        }
    }

    fun broadcast(string: String){
        Bukkit.broadcast(Component.text(string),Server.BROADCAST_CHANNEL_USERS)
    }

    fun runTask(unit: ()->Unit){
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

    fun end(){
        end = true
        sEvent.unregisterAll()
        generatorUUID.forEach {
            runTask {
                Bukkit.getEntity(it)?.remove()
            }
        }
        Bukkit.getOnlinePlayers().forEach {
            it.playSound(it.location,Sound.UI_TOAST_CHALLENGE_COMPLETE,1f,1f)
        }
        if (escapedSurvivor.size > survivorSize/2){
            allPlayerAction {
                val p = Bukkit.getPlayer(it)!!
                p.showTitle(Title.title(Component.text("§e§lサバイバーの勝ち！"), Component.text(""), Title.Times.of(
                    Duration.ZERO, Duration.ofSeconds(3), Duration.ZERO)))
            }
        } else {
            if (escapedSurvivor.size == survivorSize){
                allPlayerAction {
                    val p = Bukkit.getPlayer(it)!!
                    p.showTitle(Title.title(Component.text("§b§l引き分け！"), Component.text(""), Title.Times.of(
                        Duration.ZERO, Duration.ofSeconds(3), Duration.ZERO)))
                }
            } else {
                allPlayerAction {
                    val p = Bukkit.getPlayer(it)!!
                    p.showTitle(Title.title(Component.text("§c§lハンターの勝ち！"), Component.text(""), Title.Times.of(
                        Duration.ZERO, Duration.ofSeconds(3), Duration.ZERO)))
                }
            }
        }
    }

    var remainingGenerator = map.generatorLimit
    var worldGuard = SWorldGuardAPI()
    val sbManager = Bukkit.getScoreboardManager()
    val generatorUUID = ArrayList<UUID>()
    val escapeGeneratorUUID = ArrayList<UUID>()
    val sEvent = SEvent(IdentityFifty.plugin)
    val survivorSize = IdentityFifty.survivors.size
    var survivorCount = IdentityFifty.survivors.size
    val escapedSurvivor = ArrayList<UUID>()
    var end = false

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
            runTask { p.teleport(survivorSpawnList.removeAt(0)) }
            data.survivorClass.onStart(p)
            IdentityFifty.stunEffect(p)
        }

        IdentityFifty.hunters.forEach { (uuid, data) ->
            val p = Bukkit.getPlayer(uuid)!!
            runTask { p.teleport(hunterSpawnList.removeAt(0)) }
            runTask { data.hunterClass.onStart(p) }
            IdentityFifty.stunEffect(p)
        }

        for (i in 1..map.generatorLimit){
            val data = generatorList.removeAt(0)
            runTask {
                map.world.spawn(data.location,Sheep::class.java) {
                    it.setAI(false)
                    generatorUUID.add(it.uniqueId)
                    it.customName = "§f§l羊型発電機§5(§e${data.health}§f/§b${data.health}§5)"
                    it.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.baseValue = data.health.toDouble()
                    it.health = data.health.toDouble()
                    it.persistentDataContainer.set(NamespacedKey(IdentityFifty.plugin,"Generator"), PersistentDataType.INTEGER,1)
                }
            }
        }

        val bossBar = Bukkit.createBossBar("残り暗号機：§c§l${map.generatorLimit}§f個",BarColor.YELLOW,BarStyle.SOLID)
        bossBar.isVisible = true
        Bukkit.getOnlinePlayers().forEach {
            bossBar.addPlayer(it)
        }


        sEvent.register(EntityDeathEvent::class.java) { e ->
            if (e.entity.type != EntityType.SHEEP)return@register
            if (!e.entity.persistentDataContainer.has(NamespacedKey(IdentityFifty.plugin,"Generator"),
                    PersistentDataType.INTEGER))return@register
            if (remainingGenerator == 0)return@register
            generatorUUID.remove(e.entity.uniqueId)
            bossBar.setTitle("残り暗号機：§c§l${remainingGenerator}§f個")
            bossBar.progress = remainingGenerator.toDouble() / map.generatorLimit.toDouble()
            IdentityFifty.hunters.forEach {
                it.value.hunterClass.onFinishedGenerator(e.entity.location,Bukkit.getPlayer(it.key)!!)
            }
            IdentityFifty.survivors.forEach {
                it.value.survivorClass.onFinishedGenerator(e.entity.location,Bukkit.getPlayer(it.key)!!)
            }
            remainingGenerator--
            if (remainingGenerator != 0)return@register

            broadcast("§e§lゲート付近の発電機が開かれた！")

            Bukkit.getOnlinePlayers().forEach {
                it.playSound(it.location,Sound.ENTITY_WITHER_SPAWN,1f,1f)
            }

            for (i in 1..map.escapeGeneratorLimit){
                val data = escapeGeneratorList.removeAt(0)
                runTask {
                    map.world.spawn(data.location,Cow::class.java) {
                        it.setAI(false)
                        it.customName = "§f§l牛型発電機"
                        it.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.baseValue = data.health.toDouble()
                        it.health = data.health.toDouble()
                        it.persistentDataContainer.set(NamespacedKey(IdentityFifty.plugin,"EscapeGenerator"), PersistentDataType.INTEGER,1)
                        escapeGeneratorUUID.add(it.uniqueId)
                    }
                }
            }


            sEvent.register(EntityDeathEvent::class.java) second@{

                if (e.entity.type != EntityType.COW)return@second
                if (!e.entity.persistentDataContainer.has(NamespacedKey(IdentityFifty.plugin,"EscapeGenerator"),
                        PersistentDataType.INTEGER))return@second

                escapeGeneratorUUID.remove(e.entity.uniqueId)

                




            }


            generatorUUID.forEach {
                runTask {
                    Bukkit.getEntity(it)?.remove()
                }
                generatorUUID.remove(it)
            }



        }


        sEvent.register(PlayerMoveEvent::class.java){ e ->
            if (!IdentityFifty.survivors.containsKey(e.player.uniqueId))return@register
            val from = e.from.clone()
            from.yaw = 0f
            from.pitch = 0f
            val to = e.to.clone()
            to.yaw = 0f
            to.pitch = 0f

            if (from == to)return@register

            if (IdentityFifty.plateSaver.contains(e.player.uniqueId) && to.block.getRelative(BlockFace.DOWN).type != Material.HEAVY_WEIGHTED_PRESSURE_PLATE){
                IdentityFifty.plateSaver.remove(e.player.uniqueId)
            }

            val loc = to.toBlockLocation()
            if (map.prisons.containsKey(loc)){
                if (IdentityFifty.survivors.containsKey(e.player.uniqueId)) {
                    val data = map.prisons[loc]!!
                    if (data.inPlayer.contains(e.player.uniqueId)) {
                        data.inPlayer.remove(e.player.uniqueId)
                        val playerData = IdentityFifty.survivors[e.player.uniqueId]!!
                        val helperData = IdentityFifty.survivors[data.lastPressUUID]!!
                        helperData.survivorClass.onHelp(e.player,Bukkit.getPlayer(helperData.uuid)!!)
                        playerData.survivorClass.onGotHelp(Bukkit.getPlayer(helperData.uuid)!!,e.player)
                        IdentityFifty.hunters.forEach {
                            it.value.hunterClass.onSurvivorHelp(Bukkit.getPlayer(helperData.uuid)!!,e.player,Bukkit.getPlayer(it.key)!!)
                        }
                        playerData.setHealth(3,true)
                    }
                }
            }

            val data = IdentityFifty.survivors[e.player.uniqueId]!!
            var footprints = data.footprintsTime

            Bukkit.getScheduler().runTaskTimer(IdentityFifty.plugin, Consumer {
                if (footprints <= 0){
                    it.cancel()
                }

                IdentityFifty.hunters.forEach { (uuid,data) ->
                    Bukkit.getPlayer(uuid)?.spawnParticle(Particle.REDSTONE,e.to,2,
                        Random.nextDouble(-0.2,0.2),0.2,
                        Random.nextDouble(-0.2,0.2), Particle.DustTransition(Color.RED,Color.WHITE,1f))
                    Bukkit.getPlayer(uuid)?.spawnParticle(Particle.REDSTONE,e.to,2,
                        Random.nextDouble(-0.2,0.2),0.2,
                        Random.nextDouble(-0.2,0.2), Particle.DustTransition(Color.BLACK,Color.WHITE,1f))
                }

                footprints--
            },0,20)

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

        sEvent.register(EntityDamageByEntityEvent::class.java) { e ->
            if (IdentityFifty.survivors.containsKey(e.entity.uniqueId)){
                e.damage = 0.0
                if (IdentityFifty.hunters.containsKey(e.damager.uniqueId)){
                    val survivorData = IdentityFifty.survivors[e.entity.uniqueId]!!
                    val hunterData = IdentityFifty.hunters[e.damager.uniqueId]!!
                    if (survivorData.getHealth() == 0)return@register
                    var damage = hunterData.hunterClass.onAttack(e.entity as Player, e.damager as Player)
                    if (damage <= 0)return@register
                    if (survivorData.getHealth() < damage) damage = survivorData.getHealth()
                    e.entity.world.playSound(e.entity.location,Sound.BLOCK_ANVIL_PLACE,1f,0.5f)
                    if (!survivorData.survivorClass.onDamage(survivorData.getHealth()-damage,e.damager as Player, e.entity as Player)){
                        IdentityFifty.stunEffect(e.damager as Player)
                        return@register
                    }

                    IdentityFifty.stunEffect(e.damager as Player)
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

            if (e.entity.type == EntityType.SHEEP && e.entity.persistentDataContainer.has(NamespacedKey(IdentityFifty.plugin,"Generator"), PersistentDataType.INTEGER)){
                e.entity.customName = "§f§l羊型発電機§5(§e${(e.entity as Sheep).health.toInt()}§f/§b${(e.entity as Sheep).getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value.toInt()}§5)"
            }
        }

        SEvent(IdentityFifty.plugin).register(PlayerInteractEvent::class.java) { e ->

            if (e.action != Action.PHYSICAL)return@register
            if (e.clickedBlock?.type != Material.HEAVY_WEIGHTED_PRESSURE_PLATE)return@register
            if (!IdentityFifty.survivors.containsKey(e.player.uniqueId))return@register
            if (IdentityFifty.plateSaver.contains(e.player.uniqueId)){
                e.isCancelled = true
                return@register
            }
            val plate = e.clickedBlock!!.blockData as AnaloguePowerable

            if (plate.power == 0)return@register


            e.isCancelled = true
            IdentityFifty.plateSaver.add(e.player.uniqueId)

            val helperData = IdentityFifty.survivors[e.player.uniqueId]!!

            val loc = e.clickedBlock!!.location.toBlockLocation()
            loc.yaw = 0f
            loc.pitch = 0f
            val prisonData = map.prisons.entries.find { it.value.plateLoc == loc }!!





            var helpTime = 0
            helpTime += helperData.helpTick
            for (uuid in prisonData.value.inPlayer){
                val data = IdentityFifty.survivors[uuid]!!
                helpTime += data.otherPlayerHelpDelay
            }

            for (uuid in prisonData.value.inPlayer){
                val data = IdentityFifty.survivors[uuid]!!
                helpTime += data.otherPlayerHelpDelayPercentage * helpTime
            }

            Bukkit.getScheduler().runTaskTimer(IdentityFifty.plugin, Consumer {
                if (e.player.location.block.type != Material.HEAVY_WEIGHTED_PRESSURE_PLATE){
                    it.cancel()
                    IdentityFifty.plateSaver.remove(e.player.uniqueId)
                    return@Consumer
                } else {
                    if (helpTime <= 0) {
                        plate.power = 1
                        prisonData.value.lastPressUUID = e.player.uniqueId
                        it.cancel()
                    }
                    helpTime--
                }
            },0,1)



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
                            return@Consumer
                        }
                        continue
                    }
                    survivor.remainingTime--
                }
            }


            val scoreboard = sbManager.newScoreboard
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
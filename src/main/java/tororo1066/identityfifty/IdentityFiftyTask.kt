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
import org.bukkit.entity.Arrow
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
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitTask
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Team
import tororo1066.identityfifty.data.GeneratorData
import tororo1066.identityfifty.data.MapData
import tororo1066.identityfifty.data.SurvivorData
import tororo1066.tororopluginapi.lang.SLang.Companion.sendTranslateMsg
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.otherPlugin.SWorldGuardAPI
import tororo1066.tororopluginapi.sEvent.SEvent
import tororo1066.tororopluginapi.sItem.SItem
import tororo1066.tororopluginapi.utils.toPlayer
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

    private fun taskLivingSurvivor(task: (Map.Entry<UUID,SurvivorData>)->Unit){
        IdentityFifty.survivors.forEach {
            if (it.value.getHealth() < 1)return@forEach
            task.invoke(it)
        }
    }

    //板の処理で使用
    private fun changeBlockFace(blockFace: BlockFace): BlockFace {
        return when(blockFace){
            BlockFace.WEST-> BlockFace.EAST
            BlockFace.EAST-> BlockFace.WEST
            BlockFace.NORTH-> BlockFace.SOUTH
            BlockFace.SOUTH-> BlockFace.NORTH
            else-> blockFace
        }
    }

    //終了処理
    fun end(){
        end = true
        sEvent.unregisterAll()

        bossBar.isVisible = false

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
            map.prisons.forEach {
                it.value.inPlayer.clear()
            }
        }

        map.escapeGenerators.forEach {
            val getBlock = map.world.getBlockAt(it.doorLocation)
            val blockData = getBlock.blockData as Door
            blockData.isOpen = false
            getBlock.blockData = blockData
        }

        woodPlateUUID.forEach {
            runTask {
                Bukkit.getEntity(it)?.remove()
            }
        }

        runTask {
            hatchUUID?.let { Bukkit.getEntity(it)?.remove() }
            hatchUUID = null
        }

        //サバイバーハンター両方のパッシブで走らせるタスク終了 現在はDasherが該当
        IdentityFifty.survivors.values.forEach {
            val p = it.uuid.toPlayer()
            if (p != null){
                it.survivorClass.onEnd(p)
                it.talentClasses.values.forEach { clazz ->
                    clazz.onEnd(p)
                    clazz.tasks.forEach { task ->
                        task.cancel()
                    }
                    clazz.tasks.clear()
                }
            }

            it.survivorClass.tasks.forEach {  task ->
                task.cancel()
            }
            it.survivorClass.tasks.clear()
        }

        IdentityFifty.hunters.values.forEach {
            val p = it.uuid.toPlayer()

            if (p != null){
                it.hunterClass.onEnd(p)
                it.talentClasses.values.forEach { clazz ->
                    clazz.onEnd(p)
                    clazz.tasks.forEach { task ->
                        task.cancel()
                    }
                    clazz.tasks.clear()
                }
            }

            it.hunterClass.tasks.forEach { task ->
                task.cancel()
            }
            it.hunterClass.tasks.clear()
        }

        IdentityFifty.survivors.clear()
        IdentityFifty.hunters.clear()



        Bukkit.getOnlinePlayers().forEach {
            it.walkSpeed = 0.2f
            it.playSound(it.location,Sound.UI_TOAST_CHALLENGE_COMPLETE,0.8f,1f)
        }

        //人数によって結果を変える
        if (escapedSurvivor.size > survivorSize/2){

            onlinePlayersAction {
                it.showTitle(Title.title(Component.text("§e§lサバイバーの勝ち！"), Component.text(""), Title.Times.of(
                    Duration.ZERO, Duration.ofSeconds(3), Duration.ofSeconds(1))))
            }
        } else {
            if (escapedSurvivor.size == survivorSize){
                onlinePlayersAction {
                    it.showTitle(Title.title(Component.text("§b§l引き分け！"), Component.text(""), Title.Times.of(
                    Duration.ZERO, Duration.ofSeconds(3), Duration.ofSeconds(1))))
                }
            } else {
                onlinePlayersAction {
                    it.showTitle(Title.title(Component.text("§c§lハンターの勝ち！"), Component.text(""), Title.Times.of(
                    Duration.ZERO, Duration.ofSeconds(3), Duration.ofSeconds(1))))
                }
            }
        }

        //スキルアイテムの処理全て削除
        IdentityFifty.interactManager.items.clear()

        //タスク削除
        IdentityFifty.identityFiftyTask = null
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
    private val bossBar = Bukkit.createBossBar("§d残り暗号機§f：§c§l${map.generatorGoal}§f個",BarColor.YELLOW,BarStyle.SOLID)
    val escapedSurvivor = ArrayList<UUID>()
    val deadSurvivor = ArrayList<UUID>()
    private var end = false
    //一撃死 全発電機発電後発動(60s)
    private var noOne = false

    private var hatchUUID: UUID? = null

    val survivorTeam = scoreboard.registerNewTeam("Survivor")
    val hunterTeam = scoreboard.registerNewTeam("Hunter")

    fun aliveSurvivors(): List<UUID>{
        return IdentityFifty.survivors.filter { !escapedSurvivor.contains(it.key) && !deadSurvivor.contains(it.key) }.map { it.key }
    }


    init {
        survivorTeam.setOption(Team.Option.NAME_TAG_VISIBILITY,Team.OptionStatus.NEVER)
        survivorTeam.setCanSeeFriendlyInvisibles(true)

        hunterTeam.setOption(Team.Option.NAME_TAG_VISIBILITY,Team.OptionStatus.NEVER)
        hunterTeam.setCanSeeFriendlyInvisibles(true)
    }

    override fun run() {

        broadcast("ゲームをスタート中...")

        //新しいリストに入れて初期化+シャッフル
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
                data.talentClasses.values.forEach { clazz ->
                    clazz.parameters(data)
                }
//                data.survivorClass.onStart(p)
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
                data.talentClasses.values.forEach { clazz ->
                    clazz.parameters(data)
                }
//                data.hunterClass.onStart(p)
            }
            IdentityFifty.stunEffect(p)
        }

        //牢屋の脱出判定のlocationにアマスタを出す この周囲でシフトすることで救助できる
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

        //板のlocationにアマスタを出す この周囲で右クリックすることで板を倒すことができる
        map.woodPlates.forEach {  (_,data) ->

            runTask {
                var block = data.loc.block
                for (i in 0 until data.length) {
                    block.type = Material.AIR
                    block = block.getRelative(data.face)
                }

                val loc = data.loc.clone()
                //アマスタの位置を板中央辺りに置く
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
                    stand.isMarker = true
                    woodPlateUUID.add(stand.uniqueId)
                }

                //階段の設置処理
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

        //羊型発電機のスポーン
        for (i in 1..map.generatorLimit){
            val data = generatorList.removeAt(0)
            runTask {
                map.world.spawn(data.location,Sheep::class.java) {
                    it.setAI(false)
                    it.isSilent = true
                    it.color = DyeColor.WHITE
                    generatorUUID.add(it.uniqueId)
                    it.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.baseValue = data.health.toDouble()
                    it.health = data.health.toDouble()
                    it.customName = "§f§l羊型発電機§5(§e${data.health}§f/§b${data.health}§5)"
                    it.noDamageTicks = 0
                    it.persistentDataContainer.set(NamespacedKey(IdentityFifty.plugin,"Generator"), PersistentDataType.INTEGER,1)
                }
            }
        }



        //チーム参加処理 遅くない...?w
        IdentityFifty.survivors.forEach {
            survivorTeam.addEntry(it.value.name)
        }

        IdentityFifty.hunters.forEach {
            hunterTeam.addEntry(it.value.name)
        }

        //残り発電機の視覚化
        bossBar.isVisible = true
        Bukkit.getOnlinePlayers().forEach {
            bossBar.addPlayer(it)
        }

        //以下イベント

        //腹減る必要がないからcancel
        sEvent.register(FoodLevelChangeEvent::class.java) { e ->
            if (IdentityFifty.survivors.containsKey(e.entity.uniqueId) || IdentityFifty.hunters.containsKey(e.entity.uniqueId)){
                e.isCancelled = true
            }
        }

        //羊型発電機が倒されたときに走る処理
        sEvent.register(EntityDeathEvent::class.java) { e ->
            if (e.entity.type != EntityType.SHEEP)return@register
            if (!e.entity.persistentDataContainer.has(NamespacedKey(IdentityFifty.plugin,"Generator"),
                    PersistentDataType.INTEGER))return@register
            if (remainingGenerator == 0)return@register
            e.drops.clear()
            onlinePlayersAction {
                it.playSound(it.location,Sound.ENTITY_BLAZE_DEATH,2f,1f)
                it.sendMessage(IdentityFifty.prefix + "§b暗号機が壊された！")
            }
            generatorUUID.remove(e.entity.uniqueId)
            remainingGenerator--
            bossBar.setTitle("§d残り暗号機§f：§c§l${remainingGenerator}§f個")
            bossBar.progress = remainingGenerator.toDouble() / map.generatorGoal.toDouble()
            IdentityFifty.hunters.forEach {
                it.value.hunterClass.onFinishedGenerator(e.entity.location,remainingGenerator,Bukkit.getPlayer(it.key)!!)
                it.value.talentClasses.values.forEach { clazz ->
                    clazz.onFinishedGenerator(e.entity.location,remainingGenerator,Bukkit.getPlayer(it.key)!!)
                }

            }
            taskLivingSurvivor {
                val p = Bukkit.getPlayer(it.key)!!
                it.value.survivorClass.onFinishedGenerator(e.entity.location,remainingGenerator,p)
                it.value.talentClasses.values.forEach { clazz ->
                    clazz.onFinishedGenerator(e.entity.location,remainingGenerator,p)
                }
            }
            if (map.generatorGoal-remainingGenerator >= map.needSummonHatchGenerator && hatchUUID == null){
                val random = map.hatches.entries.random()
                map.world.spawn(random.value.location,ArmorStand::class.java) {
                    hatchUUID = it.uniqueId
                    it.isInvulnerable = true
                    it.isInvisible = true
                    it.setDisabledSlots(EquipmentSlot.CHEST,EquipmentSlot.FEET,EquipmentSlot.HEAD,EquipmentSlot.LEGS)
                    it.setItem(EquipmentSlot.HEAD,SItem(Material.STICK).setCustomModelData(18))
                    if (survivorCount == 1){
                        it.persistentDataContainer.set(NamespacedKey(IdentityFifty.plugin,"hatch"), PersistentDataType.INTEGER,1)
                        it.setItem(EquipmentSlot.HEAD,SItem(Material.STICK).setCustomModelData(19))
                    }
                }
            }
            if (remainingGenerator != 0)return@register
            //残り暗号機が0だったら 一撃死状態にする(60秒)
            bossBar.progress = 1.0
            bossBar.setTitle("§dゲートを開けよう！")
            bossBar.color = BarColor.RED

            broadcast("§e§lゲート付近の発電機が開かれた！")

            noOne = true
            var count = 60
            Bukkit.getScheduler().runTaskTimer(IdentityFifty.plugin, Consumer {
                count--
                if (count <= 0){
                    noOne = false
                    bossBar.progress = 0.0
                    it.cancel()
                }
                bossBar.progress = count.toDouble() / 60.0
            },0,20)

            Bukkit.getOnlinePlayers().forEach {
                it.playSound(it.location,Sound.ENTITY_WITHER_SPAWN,1f,1f)
            }

            //牛型発電機のスポーン
            for (i in 1..map.escapeGeneratorLimit){
                val data = escapeGeneratorList.removeAt(0)
                runTask {
                    map.world.spawn(data.location,Cow::class.java) {
                        it.setAI(false)
                        it.isSilent = true
                        it.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.baseValue = data.health.toDouble()
                        it.health = data.health.toDouble()
                        it.customName = "§f§l牛型発電機§5(§e${it.health.toInt()}§f/§b${data.health}§5)"
                        it.noDamageTicks = 0
                        it.persistentDataContainer.set(NamespacedKey(IdentityFifty.plugin,"EscapeGenerator"), PersistentDataType.INTEGER,1)
                        escapeGeneratorUUID.add(it.uniqueId)
                    }
                }
            }


            //牛型発電機が倒された時の処理
            sEvent.register(EntityDeathEvent::class.java) second@{ e2 ->
                if (e2.entity.type != EntityType.COW)return@second
                if (!e2.entity.persistentDataContainer.has(NamespacedKey(IdentityFifty.plugin,"EscapeGenerator"),
                        PersistentDataType.INTEGER))return@second
                escapeGeneratorUUID.remove(e2.entity.uniqueId)
                e2.drops.clear()

                val loc = e2.entity.location.toBlockLocation()
                loc.yaw = 0f
                loc.pitch = 0f

                //モブのロケーションから発電機、ドアの位置を取得する + ドアを開く
                val data = map.escapeGenerators.find { it.location == loc }!!
                val getBlock = map.world.getBlockAt(data.doorLocation)
                val blockData = getBlock.blockData as Door

                blockData.isOpen = true
                getBlock.blockData = blockData
                map.world.playSound(getBlock.location,Sound.BLOCK_IRON_DOOR_OPEN,1f,1f)

                taskLivingSurvivor {
                    val p = Bukkit.getPlayer(it.key)!!
                    it.value.survivorClass.onFinishedEscapeGenerator(loc,p)
                    it.value.talentClasses.values.forEach { clazz ->
                        clazz.onFinishedEscapeGenerator(loc,p)
                    }
                }

                IdentityFifty.hunters.forEach {
                    val p = Bukkit.getPlayer(it.key)!!
                    it.value.hunterClass.onFinishedEscapeGenerator(loc,p)
                    it.value.talentClasses.values.forEach { clazz ->
                        clazz.onFinishedEscapeGenerator(loc,p)
                    }
                }

            }

            //牛型発電機が出たら羊型発電機はもういらないので削除
            generatorUUID.forEach {
                runTask {
                    Bukkit.getEntity(it)?.remove()
                }
            }
            generatorUUID.clear()

        }

        //スニークをした時の処理
        sEvent.register(PlayerToggleSneakEvent::class.java) { e ->
            if (!IdentityFifty.survivors.containsKey(e.player.uniqueId))return@register
            if (!e.isSneaking)return@register
            //牢屋の処理
            e.player.location.getNearbyEntitiesByType(ArmorStand::class.java,3.0).forEach {
                if (it.persistentDataContainer.has(NamespacedKey(IdentityFifty.plugin,"PrisonLoc"),
                        PersistentDataType.INTEGER_ARRAY)){
                    //locationから牢屋のデータ取得
                    val intArray = it.persistentDataContainer[NamespacedKey(IdentityFifty.plugin,"PrisonLoc"), PersistentDataType.INTEGER_ARRAY]!!
                    val prisonData = map.prisons[Location(map.world,intArray[0].toDouble(),intArray[1].toDouble(),intArray[2].toDouble())]!!
                    val helperData = IdentityFifty.survivors[e.player.uniqueId]!!

                    //中に誰もいなかったら処理を回す必要がない
                    if (prisonData.inPlayer.isEmpty())return@forEach

                    //自分で開けられないようにする
                    if (prisonData.inPlayer.contains(e.player.uniqueId))return@forEach

                    var helpTime = 0 //これが助けるのに必要な時間(tick)
                    helpTime += helperData.helpTick //助ける側の必要時間をまず入れる(tick)

                    //サバイバーの救助される時間が延長されるパッシブの計算
                    for (uuid in prisonData.inPlayer){
                        val data = IdentityFifty.survivors[uuid]!!
                        helpTime += data.otherPlayerHelpDelay //助けられる側の他のサバイバーからの救助時間の遅延(tick)
                    }

                    //サバイバーの救助される時間が延長されるパッシブ(割合)の計算
                    for (uuid in prisonData.inPlayer){
                        val data = IdentityFifty.survivors[uuid]!!
                        helpTime += (data.otherPlayerHelpDelayPercentage * helpTime).toInt() //助けられる側の他のサバイバーからの救助時間の遅延(パーセンテージ:0.00~1.00)
                    }

                    //helpTimeは数値が変わるので先に動かない値を作る
                    val helpTimeInitial = helpTime

                    //bossbar作成+追加
                    helperData.helpBossBar = Bukkit.createBossBar("§e救助中...",BarColor.BLUE,BarStyle.SOLID)
                    helperData.helpBossBar.progress = 0.0
                    helperData.helpBossBar.addPlayer(e.player)


                    Bukkit.getScheduler().runTaskTimer(IdentityFifty.plugin, Consumer { task ->
                        //場を離れたりシフト解除すると救助キャンセル
                        if (!e.player.location.getNearbyEntitiesByType(ArmorStand::class.java,3.0).contains(it) || !e.player.isSneaking){
                            helperData.helpBossBar.removeAll()
                            task.cancel()
                            return@Consumer
                        } else {
                            if (helpTime <= 0) {
                                prisonData.lastPressUUID = e.player.uniqueId //最後に扉を開けたプレイヤーの代入(helper)
                                //ドアを開ける+bossbar削除
                                val block = map.world.getBlockAt(prisonData.doorLoc)
                                val blockData = block.blockData as Door
                                blockData.isOpen = true
                                block.blockData = blockData
                                helperData.helpBossBar.removeAll()
                                map.world.playSound(block.location,Sound.BLOCK_IRON_DOOR_OPEN,1f,1f)
                                //3秒後にはドアを閉じる
                                Bukkit.getScheduler().runTaskLater(IdentityFifty.plugin, Consumer {
                                    blockData.isOpen = false
                                    block.blockData = blockData
                                    map.world.playSound(block.location,Sound.BLOCK_IRON_DOOR_CLOSE,1f,1f)
                                },60)
                                task.cancel()
                            }
                            helpTime--
                            //1.0 - (残り必要時間[tick] / 救助必要時間[tick])が1.0より小さかったらbossbarを更新する(バグ防ぎ)
                            if (1.0 - (helpTime.toDouble() / helpTimeInitial.toDouble()) < 1.0){
                                helperData.helpBossBar.progress = 1.0 - (helpTime.toDouble() / helpTimeInitial.toDouble())
                            }
                        }
                    },0,1)
                    return@register
                }

            }

            if (!map.prisons.filter { it.value.inPlayer.contains(e.player.uniqueId) }.none())return@register

            //回復の処理
            e.player.location.getNearbyPlayers(3.0).forEach {
                if (it == e.player)return@forEach
                if (!IdentityFifty.survivors.containsKey(it.uniqueId))return@forEach
                if (it.gameMode == GameMode.SPECTATOR)return@forEach
                val helperData = IdentityFifty.survivors[e.player.uniqueId]!!
                val playerData = IdentityFifty.survivors[it.uniqueId]!!

                //ヘルスがマックスなら処理をしない
                if (playerData.getHealth() == 5){
                    return@forEach
                }

                //既にそのサバイバーを回復しているサバイバーがいるなら回復しているプレイヤー一覧とbossbarに追加するだけ
                if (playerData.healingPlayers.isNotEmpty()){
                    playerData.healingPlayers[helperData.uuid] = helperData
                    playerData.healBossBar.addPlayer(e.player)
                    return@forEach
                }
                var healTime = 0 //これが助けるのに必要な時間(tick)
                healTime += helperData.healTick //回復する側の必要時間をまず入れる(tick)

                healTime += playerData.otherPlayerHealDelay //回復される側の他のサバイバーからの回復時間の遅延(tick)

                healTime += (playerData.otherPlayerHealDelayPercentage * healTime).toInt() //回復される側の他のサバイバーからの回復時間の遅延(パーセンテージ:0.00~1.00)

                //bossbar作成+追加
                playerData.healBossBar = Bukkit.createBossBar("§d${playerData.name}§eを回復中...",BarColor.GREEN,BarStyle.SOLID)
                playerData.healBossBar.progress = playerData.healProcess
                playerData.healBossBar.addPlayer(e.player)

                //回復中のプレイヤーに追加
                playerData.healingPlayers[helperData.uuid] = helperData

                Bukkit.getScheduler().runTaskTimer(IdentityFifty.plugin, Consumer { task ->
                    if (playerData.healProcess >= 1.0){
                        playerData.healProcess = 0.0
                        playerData.healingPlayers.clear()
                        playerData.healBossBar.removeAll()
                        map.world.playSound(it.location,Sound.ENTITY_PLAYER_LEVELUP,1f,1f)
                        playerData.setHealth(playerData.getHealth() + 2)
                        runTask {
                            IdentityFifty.hunters.forEach { clazz ->
                                clazz.value.hunterClass.onSurvivorHeal(it.player!!, e.player, clazz.key.toPlayer()!!)
                            }
                        }
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

                    var allPlayerHealTick = 0 //全ての回復中のプレイヤーの合計時間
                    var healPlayers = 0 //回復中のプレイヤー数
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


                    healTime = allPlayerHealTick / healPlayers //回復合計時間 / 回復人数
                    healTime = (healTime / (1 + (0.5 * (healPlayers - 1)))).toInt() //平均回復時間 / (1 + (0.5 * (回復人数 - 1)))
                    healTime += playerData.otherPlayerHealDelay
                    healTime += (playerData.otherPlayerHealDelayPercentage * healTime).toInt()

                    playerData.healProcess += 1.0 / healTime //SurvivorDataに直接代入することで一度回復をやめても途中からできる

                    if (playerData.healProcess <= 1.0){
                        playerData.healBossBar.progress = playerData.healProcess
                    }

                },0,1)
            }

            e.player.location.getNearbyEntitiesByType(ArmorStand::class.java,1.5).forEach {
                if (!it.persistentDataContainer.has(NamespacedKey(IdentityFifty.plugin,"hatch"), PersistentDataType.INTEGER))return@forEach

                val data = IdentityFifty.survivors[e.player.uniqueId]!!
                data.hatchBossBar = Bukkit.createBossBar("§d逃走中...",BarColor.PURPLE,BarStyle.SOLID)
                data.hatchBossBar.progress = 0.0
                data.hatchBossBar.addPlayer(e.player)
                var hatchProgress = data.hatchTick
                val hatchTimeInitial = data.hatchTick

                Bukkit.getScheduler().runTaskTimer(IdentityFifty.plugin, Consumer { task ->
                    //場を離れたりシフト解除すると救助キャンセル
                    if (!e.player.location.getNearbyEntitiesByType(ArmorStand::class.java,1.5).contains(it) || !e.player.isSneaking){
                        data.hatchBossBar.removeAll()
                        task.cancel()
                        return@Consumer
                    } else {
                        if (hatchProgress <= 0) {
                            escapedSurvivor.add(e.player.uniqueId)
                            survivorCount--
                            data.setHealth(-1,true)
                            e.player.gameMode = GameMode.SPECTATOR
                            onlinePlayersAction { online ->
                                online.playSound(online.location,Sound.ENTITY_FIREWORK_ROCKET_BLAST,1f,1f)
                            }
                            broadcast(IdentityFifty.prefix + translate("success_escape",e.player.name))
                            end()
                            task.cancel()
                        }
                        hatchProgress--
                        //1.0 - (残り必要時間[tick] / 救助必要時間[tick])が1.0より小さかったらbossbarを更新する(バグ防ぎ)
                        if (1.0 - (hatchProgress.toDouble() / hatchTimeInitial.toDouble()) < 1.0){
                            data.hatchBossBar.progress = 1.0 - (hatchProgress.toDouble() / hatchTimeInitial.toDouble())
                        }
                    }
                },0,1)
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
                e.block.location.getNearbyEntitiesByType(ArmorStand::class.java,3.0).forEach {
                    if (it.persistentDataContainer.has(NamespacedKey(IdentityFifty.plugin,"PlateLoc"),
                            PersistentDataType.INTEGER_ARRAY)){
                        e.isCancelled = true
                        return@register
                    }
                }
                e.isDropItems = false
            }
        }

        sEvent.register(BlockCanBuildEvent::class.java) { e ->
            val p = e.player?:return@register
            if (IdentityFifty.survivors.containsKey(p.uniqueId) || IdentityFifty.hunters.containsKey(p.uniqueId)){
                e.isBuildable = false
            }
        }

        sEvent.register(PlayerMoveEvent::class.java) { e ->
            if (e.player.gameMode == GameMode.SPECTATOR)return@register
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
                        val helperPlayer = Bukkit.getPlayer(helperData.uuid)!!

                        helperData.survivorClass.onHelp(e.player,helperPlayer)
                        helperData.talentClasses.values.forEach { clazz ->
                            clazz.onHelp(e.player,helperPlayer)
                        }
                        playerData.survivorClass.onGotHelp(helperPlayer,e.player)
                        playerData.talentClasses.values.forEach { clazz ->
                            clazz.onGotHelp(helperPlayer,e.player)
                        }

                        IdentityFifty.hunters.forEach {
                            it.key.toPlayer()?.sendTranslateMsg("helped_survivor",e.player.name)

                            val p = Bukkit.getPlayer(it.key)!!
                            it.value.hunterClass.onSurvivorHelp(helperPlayer,e.player,p)
                            it.value.talentClasses.values.forEach { clazz ->
                                clazz.onSurvivorHelp(helperPlayer,e.player,p)
                            }
                        }

                        playerData.setHealth(3,true)
                    }
                }
            }

            val data = IdentityFifty.survivors[e.player.uniqueId]!!
            var footprints = data.footprintsTime

            if (data.footprintsCount >= 4){
                data.footprintsCount = 0
                Bukkit.getScheduler().runTaskTimer(IdentityFifty.plugin, Consumer {
                    if (footprints <= 0){
                        it.cancel()
                    }

                    if (!e.player.isSneaking){
                        IdentityFifty.hunters.forEach { (uuid,_) ->
                            (1..2).forEach { _ ->
                                Bukkit.getPlayer(uuid)?.spawnParticle(Particle.REDSTONE,e.to,2,
                                    Random.nextDouble(-0.2,0.2),0.2,
                                    Random.nextDouble(-0.2,0.2), Particle.DustTransition(Color.RED,Color.WHITE,1.2f))
                            }
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
                                    val item = IdentityFifty.interactManager.createSInteractItem(SItem(Material.STICK).setDisplayName("§e板倒し").setCustomModelData(3),true).setInteractEvent { e, item ->
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

                                        data.talentClasses.values.forEach { clazz ->
                                            clazz.onWoodPlate(it.location,data.uuid.toPlayer()!!)
                                        }
                                        it.location.getNearbyPlayers(2.5).forEach second@ { p ->
                                            if (!IdentityFifty.hunters.containsKey(p.uniqueId))return@second
                                            val hunterData = IdentityFifty.hunters[p.uniqueId]!!
                                            if (!hunterData.hunterClass.onDamagedWoodPlate(e.player, it.location, p))return@second
                                            hunterData.talentClasses.values.forEach { clazz ->
                                                if (!clazz.onDamagedWoodPlate(e.player, it.location, p))return@second
                                            }
                                            map.world.playSound(it.location,Sound.BLOCK_ANVIL_PLACE,0.2f,0.5f)
                                            map.world.spawnParticle(Particle.ELECTRIC_SPARK,p.location.add(0.0,0.5,0.0),30)
                                            var modify = data.survivorClass.onHitWoodPlate(p, it.location, e.player)
                                            data.talentClasses.values.forEach { clazz ->
                                                modify = clazz.onHitWoodPlate(p, it.location,modify.first,modify.second,e.player)
                                            }
                                            IdentityFifty.stunEffect(p,modify.first,modify.second)
                                        }
                                        woodPlateUUID.remove(it.uniqueId)
                                        it.remove()
                                        e.player.inventory.setItemInOffHand(SItem(Material.AIR))
                                        item.delete()
                                        return@setInteractEvent true
                                    }

                            e.player.inventory.setItemInOffHand(item)

                        }
                    }
                }

            }

            if (escapedSurvivor.contains(e.player.uniqueId))return@register
            if (!worldGuard.inRegion(e.player,map.goalRegions))return@register
            escapedSurvivor.add(e.player.uniqueId)
            survivorCount--
            data.setHealth(-1,true)
            e.player.gameMode = GameMode.SPECTATOR
            onlinePlayersAction {
                it.playSound(it.location,Sound.ENTITY_FIREWORK_ROCKET_BLAST,1f,1f)
            }
            broadcast(IdentityFifty.prefix + translate("success_escape",e.player.name))
            if (survivorCount == 0){
                end()
                return@register
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
                    if (survivorData.getHealth() <= 1)return@register
                    var damage = hunterData.hunterClass.onAttack(e.entity as Player, e.damager as Player,noOne)
                    if (damage <= 0)return@register
                    hunterData.talentClasses.values.forEach {
                        it.onAttack(e.entity as Player, e.damager as Player,noOne)
                    }
                    if (survivorData.getHealth() < damage) damage = survivorData.getHealth()
                    e.entity.world.playSound(e.entity.location,Sound.ENTITY_ELDER_GUARDIAN_CURSE,1f,2f)
                    val onDamage = survivorData.survivorClass.onDamage(damage,survivorData.getHealth()-damage,e.damager as Player, e.entity as Player)
                    if (onDamage.first){
                        IdentityFifty.stunEffect(e.damager as Player)
                    }
                    map.world.spawnParticle(Particle.ELECTRIC_SPARK,e.entity.location,30)
                    survivorData.setHealth(survivorData.getHealth() - onDamage.second)
                    if (survivorData.getHealth() == 1){
                        broadcast(IdentityFifty.prefix + translate("send_jail",e.entity.name))
                        survivorData.healProcess = 0.0
                        val prisons = map.prisons.filter { it.value.inPlayer.size == 0 }.entries.shuffled()
                        if (prisons.isNotEmpty()){
                            val data = prisons[0]
                            survivorData.survivorClass.onJail(data.value,e.entity as Player)
                            survivorData.talentClasses.values.forEach {
                                it.onJail(data.value,e.entity as Player)
                            }
                            hunterData.hunterClass.onSurvivorJail(e.entity as Player, data.value, e.damager as Player)
                            hunterData.talentClasses.values.forEach {
                                it.onSurvivorJail(e.entity as Player, data.value, e.damager as Player)
                            }
                            data.value.inPlayer.add(e.entity.uniqueId)
                            e.entity.teleport(data.value.spawnLoc)
                        } else {
                            val data = map.prisons.entries.shuffled()[0]
                            survivorData.survivorClass.onJail(data.value,e.entity as Player)
                            survivorData.talentClasses.values.forEach {
                                it.onJail(data.value,e.entity as Player)
                            }
                            hunterData.hunterClass.onSurvivorJail(e.entity as Player, data.value, e.damager as Player)
                            hunterData.talentClasses.values.forEach {
                                it.onSurvivorJail(e.entity as Player, data.value, e.damager as Player)
                            }
                            data.value.inPlayer.add(e.entity.uniqueId)
                            e.entity.teleport(data.value.spawnLoc)
                        }
                        if (IdentityFifty.survivors.filter { it.value.getHealth() <= 1 }.size == IdentityFifty.survivors.size){
                            end()
                        }
                    }
                } else {
                    if (e.damager is Arrow)return@register
                    e.isCancelled = true
                }
            }

            if (e.damager !is Player)return@register

            if (!IdentityFifty.survivors.containsKey(e.damager.uniqueId)){
                return@register
            }

            val p = e.damager as Player
            val survivorData = IdentityFifty.survivors[e.damager.uniqueId]!!

            if (e.entity.type == EntityType.SHEEP && e.entity.persistentDataContainer.has(NamespacedKey(IdentityFifty.plugin,"Generator"), PersistentDataType.INTEGER)){
                val sheep = e.entity as Sheep
                val survivors = sheep.location.getNearbyPlayers(5.0).filter { aliveSurvivors().contains(it.uniqueId) && it.uniqueId != p.uniqueId }.size
                var multiply = 1 - (survivors * 0.2)
                if (multiply < 0.3) multiply = 0.3
                val maxHealth = sheep.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value
                e.damage = survivorData.survivorClass.sheepGeneratorModify(e.damage,remainingGenerator,maxHealth,sheep.health,p) * multiply
                survivorData.talentClasses.values.forEach { clazz ->
                    e.damage = clazz.sheepGeneratorModify(e.damage,remainingGenerator,maxHealth,sheep.health,p) * multiply
                }
                Bukkit.getScheduler().runTaskLater(IdentityFifty.plugin, Runnable {
                    sheep.noDamageTicks = 0
                }, 0)
                e.entity.customName = "§f§l羊型発電機§5(§e${sheep.health.toInt()}§f/§b${maxHealth.toInt()}§5)"
            }

            if (e.entity.type == EntityType.COW && e.entity.persistentDataContainer.has(NamespacedKey(IdentityFifty.plugin,"EscapeGenerator"), PersistentDataType.INTEGER)){
                val cow = e.entity as Cow
                val survivors = cow.location.getNearbyPlayers(5.0).filter { aliveSurvivors().contains(it.uniqueId) && it.uniqueId != p.uniqueId }.size
                var multiply = 1 - (survivors * 0.2)
                if (multiply < 0.3) multiply = 0.3
                val maxHealth = cow.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value
                e.damage = survivorData.survivorClass.cowGeneratorModify(e.damage,maxHealth,cow.health,p) * multiply
                survivorData.talentClasses.values.forEach { clazz ->
                    e.damage = clazz.cowGeneratorModify(e.damage,maxHealth,cow.health,p) * multiply
                }
                Bukkit.getScheduler().runTaskLater(IdentityFifty.plugin, Runnable {
                    cow.noDamageTicks = 0
                }, 0)
                e.entity.customName = "§f§l牛型発電機§5(§e${cow.health.toInt()}§f/§b${cow.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value.toInt()}§5)"
            }
        }

        sEvent.register(PlayerInteractEvent::class.java) { e ->
            if (e.hand == EquipmentSlot.OFF_HAND)return@register
            if (IdentityFifty.hunters.containsKey(e.player.uniqueId)){
                if (!e.action.isLeftClick)return@register
                if (e.hasBlock()){
                    if (e.clickedBlock!!.type == Material.OAK_STAIRS){
                        return@register
                    }
                    if (e.clickedBlock!!.blockData is Door){
                        return@register
                    }
                }
                if (e.player.getTargetEntity(4)?.type == EntityType.PLAYER)return@register
                e.player.playSound(e.player.location,Sound.ENTITY_PLAYER_ATTACK_SWEEP,1f,1f)
                e.player.addPotionEffect(PotionEffect(PotionEffectType.SLOW,20,5,false,false,true))
                e.player.addPotionEffect(PotionEffect(PotionEffectType.SLOW_DIGGING,20,5,false,false,true))
                e.player.addPotionEffect(PotionEffect(PotionEffectType.WEAKNESS,20,5,false,false,true))
            }
        }

        sEvent.register(PlayerDropItemEvent::class.java) { e ->
            if (IdentityFifty.survivors.containsKey(e.player.uniqueId) || IdentityFifty.hunters.containsKey(e.player.uniqueId)){
                e.isCancelled = true
            }
        }

        sleep(3000)

        fun titleTask(p: Player, action: ()->Unit){
            Thread {
                for (i in 5 downTo 1){
                    p.showTitle(Title.title(Component.text("§e--------§f§l${i}§e--------"), Component.text(""), Title.Times.of(
                        Duration.ZERO,Duration.ofSeconds(1),Duration.ZERO)))
                    p.playSound(p.location, Sound.UI_BUTTON_CLICK,1f,2f)
                    sleep(1000)
                }

                p.showTitle(Title.title(Component.text("§c§lSTART!"), Component.text(""), Title.Times.of(
                    Duration.ZERO,Duration.ofSeconds(2),Duration.ofSeconds(1))))
                p.playSound(p.location,Sound.ENTITY_WITHER_SPAWN,0.7f,1f)
                runTask(action)
            }.start()
        }

        IdentityFifty.survivors.forEach { (uuid, data) ->
            val p = Bukkit.getPlayer(uuid)!!
            titleTask(p){
                data.survivorClass.onStart(p)
                data.talentClasses.values.forEach {
                    it.onStart(p)
                }
            }
        }

        IdentityFifty.hunters.forEach { (uuid, data) ->
            val p = Bukkit.getPlayer(uuid)!!
            titleTask(p){
                data.hunterClass.onStart(p)
            }
        }

        Bukkit.getScheduler().runTaskTimer(IdentityFifty.plugin, Consumer {
            IdentityFifty.survivors.forEach { (uuid, data) ->
                val playerLoc = (uuid.toPlayer()?:return@forEach).location
                data.heartProcessRules.forEach { pair ->
                    playerLoc.getNearbyPlayers(pair.first).forEach PlayersForEach@ { p ->
                        if (!IdentityFifty.hunters.containsKey(p.uniqueId))return@PlayersForEach
                        data.heartProcess += pair.second
                    }
                }

                if (data.heartProcess >= 1.0){
                    data.heartProcess = 0.0
                    val p = Bukkit.getPlayer(uuid)!!
                    p.playSound(p.location,"identity.heart",1f,1f)
                    p.spawnParticle(Particle.SPELL_WITCH,p.location,20,0.0,0.5,0.0)
                }
            }
//            IdentityFifty.hunters.forEach {
//                val playerLoc = Bukkit.getPlayer(it.key)!!.location
//
//                playerLoc.getNearbyPlayers(25.0).forEach PlayersForEach@ { p ->
//                    if (!IdentityFifty.survivors.containsKey(p.uniqueId))return@PlayersForEach
//                    if (escapedSurvivor.contains(p.uniqueId))return@PlayersForEach
//                    val data = IdentityFifty.survivors[p.uniqueId]!!
//                    data.heartProcess += 0.2
//                }
//
//                playerLoc.getNearbyPlayers(20.0).forEach PlayersForEach@ { p ->
//                    if (!IdentityFifty.survivors.containsKey(p.uniqueId))return@PlayersForEach
//                    if (escapedSurvivor.contains(p.uniqueId))return@PlayersForEach
//                    val data = IdentityFifty.survivors[p.uniqueId]!!
//                    data.heartProcess += 0.1
//                }
//
//                playerLoc.getNearbyPlayers(15.0).forEach PlayersForEach@ { p ->
//                    if (!IdentityFifty.survivors.containsKey(p.uniqueId))return@PlayersForEach
//                    if (escapedSurvivor.contains(p.uniqueId))return@PlayersForEach
//                    val data = IdentityFifty.survivors[p.uniqueId]!!
//                    data.heartProcess += 0.2
//                }
//
//                playerLoc.getNearbyPlayers(10.0).forEach PlayersForEach@ { p ->
//                    if (!IdentityFifty.survivors.containsKey(p.uniqueId))return@PlayersForEach
//                    if (escapedSurvivor.contains(p.uniqueId))return@PlayersForEach
//                    val data = IdentityFifty.survivors[p.uniqueId]!!
//                    data.heartProcess += 0.5
//                }
//
//            }
//
//            IdentityFifty.survivors.forEach { (uuid,data) ->
//                if (data.heartProcess >= 1.0){
//                    data.heartProcess = 0.0
//                    val p = Bukkit.getPlayer(uuid)!!
//                    p.playSound(p.location,"identity.heart",1f,1f)
//                    p.spawnParticle(Particle.SPELL_WITCH,p.location,20,0.0,0.5,0.0)
//                }
//            }
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
                        deadSurvivor.add(survivor.uuid)
                        survivorCount--
                        p.gameMode = GameMode.SPECTATOR
                        broadcast("§c§l${survivor.name}§4§lは死んでしまった...")

                        if (survivorCount == 0){
                            end()
                            it.cancel()
                            return@Consumer
                        }

                        if (survivorCount == 1 && hatchUUID != null){
                            val armorStand = Bukkit.getEntity(hatchUUID!!) as ArmorStand
                            armorStand.setItem(EquipmentSlot.HEAD,SItem(Material.STICK).setCustomModelData(19))
                            armorStand.persistentDataContainer.set(NamespacedKey(IdentityFifty.plugin,"hatch"), PersistentDataType.INTEGER,1)
                        }

                        runTask {
                            taskLivingSurvivor { data ->
                                if (data.key != p.uniqueId){
                                    val livingPlayer = Bukkit.getPlayer(data.key)!!
                                    data.value.survivorClass.onDieOtherSurvivor(p,survivorCount,livingPlayer)
                                    data.value.talentClasses.values.forEach { clazz ->
                                        clazz.onDieOtherSurvivor(p,survivorCount,livingPlayer)
                                    }
                                }

                            }

                            survivor.survivorClass.onDie(p)
                            survivor.talentClasses.values.forEach { clazz ->
                                clazz.onDie(p)
                            }
                        }


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
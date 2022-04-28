package tororo1066.identityfifty

import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.entity.EntityType
import org.bukkit.entity.Sheep
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import tororo1066.identityfifty.data.GeneratorData
import tororo1066.identityfifty.data.MapData
import tororo1066.tororopluginapi.otherPlugin.SWorldGuardAPI
import tororo1066.tororopluginapi.sEvent.SEvent
import java.time.Duration
import java.util.UUID

class IdentityFiftyTask(val map: MapData) : Thread() {

    fun allPlayerAction(action: (UUID)->Unit){
        IdentityFifty.survivors.forEach {
            action.invoke(it.key)
        }
    }

    fun broadcast(string: String){
        Bukkit.broadcast(Component.text(string),Server.BROADCAST_CHANNEL_USERS)
    }

    var finishedGenerator = 0
    var worldGuard = SWorldGuardAPI()
    val sbManager = Bukkit.getScoreboardManager()

    override fun run() {
        broadcast("ゲームをスタート中...")

        val survivorSpawnList = ArrayList<Location>(map.survivorSpawnLocations)
        survivorSpawnList.shuffle()
        val hunterSpawnList = ArrayList<Location>(map.hunterSpawnLocations)
        hunterSpawnList.shuffle()
        val generatorList = ArrayList<GeneratorData>(map.generators)
        generatorList.shuffle()

        IdentityFifty.survivors.forEach { (uuid, data) ->
            val p = Bukkit.getPlayer(uuid)!!
            p.teleport(survivorSpawnList.removeAt(0))
            data.survivorClass.onStart(p)
            IdentityFifty.stunEffect(p)
        }

        IdentityFifty.hunters.forEach { (uuid, data) ->
            val p = Bukkit.getPlayer(uuid)!!
            p.teleport(hunterSpawnList.removeAt(0))
            data.hunterClass.onStart(p)
            IdentityFifty.stunEffect(p)
        }

        for (i in 1..map.generatorLimit){
            val data = generatorList.removeAt(0)
            map.world.spawn(data.location,Sheep::class.java) {
                it.setAI(false)
                it.customName = "§f§l羊型発電機"
                it.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.baseValue = data.health.toDouble()
                it.persistentDataContainer.set(NamespacedKey(IdentityFifty.plugin,"Generator"), PersistentDataType.INTEGER,1)
            }
        }

        SEvent(IdentityFifty.plugin).register(EntityDeathEvent::class.java) { e ->
            if (e.entity.type != EntityType.SHEEP)return@register
            if (!e.entity.persistentDataContainer.has(NamespacedKey(IdentityFifty.plugin,"Generator"),
                    PersistentDataType.INTEGER))return@register
            IdentityFifty.hunters.forEach {
                it.value.hunterClass.onFinishedGenerator(e.entity.location,Bukkit.getPlayer(it.key)!!)
            }
            finishedGenerator++
        }

        SEvent(IdentityFifty.plugin).register(PlayerMoveEvent::class.java){ e ->
            if (!IdentityFifty.survivors.containsKey(e.player.uniqueId))return@register
            if (!worldGuard.inRegion(e.player,map.goalRegions))return@register
            IdentityFifty.survivors.remove(e.player.uniqueId)
            e.player.gameMode = GameMode.SPECTATOR
            broadcast("§e§l${e.player.name}§a§lは脱出に成功した！")
        }

        sleep(3000)



        allPlayerAction {
            val p = Bukkit.getPlayer(it)!!
            for (i in 5 downTo 1){
                p.showTitle(Title.title(Component.text("§eー－－－§f§l${i}§eー－－－"), Component.text(""), Title.Times.of(
                    Duration.ZERO,Duration.ofSeconds(1),Duration.ZERO)))
                p.playSound(p.location, Sound.UI_BUTTON_CLICK,1f,2f)
                sleep(1000)
            }

            p.showTitle(Title.title(Component.text("§eー－－－§f§lSTART!§eー－－－"), Component.text(""), Title.Times.of(
                Duration.ZERO,Duration.ofSeconds(2),Duration.ofSeconds(1))))
            p.playSound(p.location,Sound.ENTITY_WITHER_SPAWN,0.7f,1f)
        }

        while (true){
            val scoreboard = sbManager.newScoreboard
            val ob = scoreboard.registerNewObjective("IdentityFifty","Dummy", Component.text("IdentityFifty"))

            for (survivor in IdentityFifty.survivors.values){
                val p = Bukkit.getPlayer(survivor.uuid)?:continue
                val prefix = when(survivor.health){
                    4-> "§a§l"
                    3-> "§6§l"
                    2-> "§c§l"
                    1-> "§4§l"
                    0-> "§0§l"
                    else-> "Prefix Error"
                }

            }
        }


    }
}
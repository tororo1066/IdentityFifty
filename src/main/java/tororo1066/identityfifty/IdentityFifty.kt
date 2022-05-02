package tororo1066.identityfifty

import org.bukkit.*
import org.bukkit.block.data.AnaloguePowerable
import org.bukkit.block.data.Powerable
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import org.inventivetalent.glow.GlowAPI
import tororo1066.identityfifty.character.hunter.AbstractHunter
import tororo1066.identityfifty.character.hunter.Dasher
import tororo1066.identityfifty.character.survivor.AbstractSurvivor
import tororo1066.identityfifty.character.survivor.Nurse
import tororo1066.identityfifty.commands.IdentityCommand
import tororo1066.identityfifty.data.HunterData
import tororo1066.identityfifty.data.MapData
import tororo1066.identityfifty.data.SurvivorData
import tororo1066.tororopluginapi.SConfig
import tororo1066.tororopluginapi.SJavaPlugin
import tororo1066.tororopluginapi.otherUtils.UsefulUtility
import tororo1066.tororopluginapi.sEvent.SEvent
import tororo1066.tororopluginapi.sItem.SInteractItemManager
import tororo1066.tororopluginapi.sItem.SItem
import java.io.File
import java.util.UUID
import java.util.function.Consumer
import kotlin.random.Random

class IdentityFifty : SJavaPlugin() {

    companion object {
        val survivorsData = HashMap<String,AbstractSurvivor>()
        val huntersData = HashMap<String,AbstractHunter>()
        val survivors = HashMap<UUID,SurvivorData>()
        val hunters = HashMap<UUID,HunterData>()
        val maps = HashMap<String,MapData>()
        val plateSaver = ArrayList<UUID>()
        lateinit var interactManager: SInteractItemManager
        lateinit var plugin: SJavaPlugin
        lateinit var sConfig: SConfig

        const val prefix = "§b[§cIdentity§eFifty§b]§r"

        fun stunEffect(p: Player){
            Bukkit.getScheduler().runTask(plugin, Runnable {
                p.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS,120,0,true,false,false))
                p.addPotionEffect(PotionEffect(PotionEffectType.SLOW,160,200,true,false,false))
                p.addPotionEffect(PotionEffect(PotionEffectType.JUMP,160,200,true,false,false))
                p.addPotionEffect(PotionEffect(PotionEffectType.WEAKNESS,160,200,true,false,false))
            })
        }

        fun CommandSender.prefixMsg(s: String){
            this.sendMessage(prefix + s)
        }
    }

    private fun register(string: String, abstractSurvivor: AbstractSurvivor){
        survivorsData[string] = abstractSurvivor
    }

    private fun register(string: String, abstractHunter: AbstractHunter){
        huntersData[string] = abstractHunter
    }

    private fun registerAll(){
        register("nurse",Nurse())
        register("dasher",Dasher())

    }

    override fun onEnable() {
        saveDefaultConfig()
        plugin = this
        interactManager = SInteractItemManager(this)
        sConfig = SConfig(this)
        SEvent(this).register(PlayerJoinEvent::class.java){
            it.player.inventory.setItem(0,IdentityFifty.interactManager.createSInteractItem(SItem(Material.DIAMOND).setDisplayName("test"),true).setInteractEvent { e ->
                GlowAPI.setGlowing(Bukkit.getPlayer("tororo_1066")!!,GlowAPI.Color.BLUE,e.player)
                Bukkit.getScheduler().runTaskLater(this, Runnable {
                    GlowAPI.setGlowing(Bukkit.getPlayer("tororo_1066")!!,false,e.player)
                },100)
            }.setInitialCoolDown(100))
        }

        registerAll()

        for (file in File(dataFolder.path + "/map/").listFiles()!!){
            maps[file.nameWithoutExtension] = MapData.loadFromYml(YamlConfiguration.loadConfiguration(file))
        }
        IdentityCommand()



    }


}
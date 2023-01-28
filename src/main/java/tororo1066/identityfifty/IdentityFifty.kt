package tororo1066.identityfifty

import de.slikey.effectlib.EffectManager
import org.bukkit.Bukkit
import org.bukkit.Particle
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.inventivetalent.glow.GlowAPI
import tororo1066.identityfifty.character.hunter.AbstractHunter
import tororo1066.identityfifty.character.hunter.AreaMan
import tororo1066.identityfifty.character.hunter.Dasher
import tororo1066.identityfifty.character.hunter.Gambler
import tororo1066.identityfifty.character.survivor.*
import tororo1066.identityfifty.commands.IdentityCommand
import tororo1066.identityfifty.data.HunterData
import tororo1066.identityfifty.data.MapData
import tororo1066.identityfifty.data.SurvivorData
import tororo1066.tororopluginapi.SJavaPlugin
import tororo1066.tororopluginapi.config.SConfig
import tororo1066.tororopluginapi.lang.SLang
import tororo1066.tororopluginapi.sItem.SInteractItemManager
import java.io.File
import java.util.*

class IdentityFifty : SJavaPlugin() {

    companion object {
        val survivorsData = HashMap<String,AbstractSurvivor>()
        val huntersData = HashMap<String,AbstractHunter>()
        val survivors = HashMap<UUID,SurvivorData>()
        val hunters = HashMap<UUID,HunterData>()
        val maps = HashMap<String,MapData>()
        lateinit var interactManager: SInteractItemManager
        lateinit var effectManager: EffectManager
        lateinit var plugin: SJavaPlugin
        lateinit var sConfig: SConfig
        lateinit var sLang: SLang
        var identityFiftyTask: IdentityFiftyTask? = null

        const val prefix = "§b[§cIdentity§eFifty§b]§r"

        fun stunEffect(p: Player){
            stunEffect(p,140,160)
        }

        fun stunEffect(p: Player, blindTime: Int, slowTime: Int){
            p.world.spawnParticle(Particle.ELECTRIC_SPARK,p.location.add(0.0,0.5,0.0),20)
            Bukkit.getScheduler().runTask(plugin, Runnable {
                p.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS,blindTime,0,true,false,false))
                p.addPotionEffect(PotionEffect(PotionEffectType.SLOW,slowTime,200,true,false,false))
                p.addPotionEffect(PotionEffect(PotionEffectType.JUMP,slowTime,200,true,false,false))
                p.addPotionEffect(PotionEffect(PotionEffectType.WEAKNESS,slowTime,200,true,false,false))
                p.addPotionEffect(PotionEffect(PotionEffectType.SLOW_DIGGING,slowTime,200,true,false,false))
            })
        }

        fun CommandSender.prefixMsg(s: String){
            this.sendMessage(prefix + s)
        }
    }

    private fun register(abstractSurvivor: AbstractSurvivor){
        survivorsData[abstractSurvivor.name] = abstractSurvivor
    }

    private fun register(abstractHunter: AbstractHunter){
        huntersData[abstractHunter.name] = abstractHunter
    }

    private fun registerAll(){
        register(Nurse())
        register(Dasher())
        register(RunAway())
        register(Searcher())
        register(Helper())
        register(AreaMan())
        register(DisguisePlayer())
        register(Gambler())
    }

    override fun onLoad() {
        plugin = this
        sLang = SLang(this, prefix)
    }
    override fun onStart() {
        saveDefaultConfig()

        interactManager = SInteractItemManager(this)
        sConfig = SConfig(this)

        effectManager = EffectManager(this)
        registerAll()

        for (file in File(dataFolder.path + "/map/").listFiles()!!){
            maps[file.nameWithoutExtension] = MapData.loadFromYml(YamlConfiguration.loadConfiguration(file))
        }
        IdentityCommand()

        GlowAPI.TEAM_TAG_VISIBILITY = "never"
    }


}
package tororo1066.identityfifty

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import de.slikey.effectlib.EffectManager
import org.bukkit.Bukkit
import org.bukkit.Particle
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
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
import tororo1066.tororopluginapi.sEvent.SEvent
import tororo1066.tororopluginapi.sItem.SInteractItemManager
import java.io.File
import java.net.InetSocketAddress
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

        var resourceUrl = ""
        var resourceSha1 = ""
        var http: HttpServer? = null

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
        register(Mechanic())
    }

    override fun onStart() {
        saveDefaultConfig()

        plugin = this
        sLang = SLang(this, prefix)

        interactManager = SInteractItemManager(this)
        sConfig = SConfig(this)

        effectManager = EffectManager(this)
        registerAll()

        for (file in File(dataFolder.path + "/map/").listFiles()!!){
            maps[file.nameWithoutExtension] = MapData.loadFromYml(YamlConfiguration.loadConfiguration(file))
        }
        IdentityCommand()

        val packLines = File(dataFolder.path + "/secrecy/resourcePackUrl.txt").readLines()
        resourceUrl = packLines[0]
        resourceSha1 = packLines[1]
        http = HttpServer.create(InetSocketAddress(8000), 0)
        saveResource("IdentityFifty.zip",true)
        http?.createContext("/Resource",FileHandler(File(dataFolder.path + "/IdentityFifty.zip")))
        http?.start()
        SEvent(this).register(PlayerJoinEvent::class.java) { e ->
            if (e.player.name == "tororo_1066"){
                e.player.setResourcePack("http://localhost:8000/Resource")
            } else {
                e.player.setResourcePack(resourceUrl, resourceSha1)
            }
        }

    }

    override fun onDisable() {
        http?.stop(0)
    }

    class FileHandler(private val file: File): HttpHandler {
        override fun handle(exchange: HttpExchange) {
            val bytes = file.readBytes()
            exchange.sendResponseHeaders(200,bytes.size.toLong())
            val writer = exchange.responseBody
            writer.write(bytes)
            writer.close()
            exchange.close()
        }
    }


}
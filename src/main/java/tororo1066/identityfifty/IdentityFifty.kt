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
import tororo1066.identityfifty.character.hunter.*
import tororo1066.identityfifty.character.survivor.*
import tororo1066.identityfifty.commands.IdentityCommand
import tororo1066.identityfifty.data.HunterData
import tororo1066.identityfifty.data.MapData
import tororo1066.identityfifty.data.SurvivorData
import tororo1066.tororopluginapi.SJavaPlugin
import tororo1066.tororopluginapi.config.SConfig
import tororo1066.tororopluginapi.lang.SLang
import tororo1066.tororopluginapi.otherUtils.UsefulUtility
import tororo1066.tororopluginapi.sEvent.SEvent
import tororo1066.tororopluginapi.sItem.SInteractItemManager
import java.io.File
import java.net.InetSocketAddress
import java.util.*

class IdentityFifty : SJavaPlugin() {

    companion object {
        /** サバイバークラスのデータ クローンして使う方がいいかも **/
        val survivorsData = HashMap<String,AbstractSurvivor>()
        /** ハンタークラスのデータ クローンして使う方がいいかも **/
        val huntersData = HashMap<String,AbstractHunter>()
        /** サバイバーのデータ ゲーム終わり時に削除 **/
        val survivors = HashMap<UUID,SurvivorData>()
        /** ハンターのデータ ゲーム終わり時に削除 **/
        val hunters = HashMap<UUID,HunterData>()
        /** マップのデータ IdentityFifty/map/に入ってる **/
        val maps = HashMap<String,MapData>()
        /** 主にスキル用のマネージャー **/
        lateinit var interactManager: SInteractItemManager
        /** エフェクトのマネージャー **/
        lateinit var effectManager: EffectManager
        /** このプラグイン **/
        lateinit var plugin: IdentityFifty
        /** コンフィグマネージャー **/
        lateinit var sConfig: SConfig
        /** 言語マネージャー **/
        lateinit var sLang: SLang
        lateinit var util: UsefulUtility
        /** 稼働中のゲームの変数 **/
        var identityFiftyTask: IdentityFiftyTask? = null

        const val prefix = "§b[§cIdentity§eFifty§b]§r"

        /** リソパのurl **/
        var resourceUrl = ""
        /** リソパのサーバー **/
        var http: HttpServer? = null

        /** スタンのエフェクト(デフォルトの時間) **/
        fun stunEffect(p: Player){
            stunEffect(p,140,160)
        }

        /** スタンのエフェクト(時間指定) **/
        fun stunEffect(p: Player, blindTime: Int, slowTime: Int){
            p.world.spawnParticle(Particle.ELECTRIC_SPARK,p.location.add(0.0,0.5,0.0),20)
            Bukkit.getScheduler().runTask(plugin, Runnable {
                p.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS,blindTime,3,true,false,false))
                p.addPotionEffect(PotionEffect(PotionEffectType.SLOW,slowTime,200,true,false,false))
                p.addPotionEffect(PotionEffect(PotionEffectType.JUMP,slowTime,200,true,false,false))
                p.addPotionEffect(PotionEffect(PotionEffectType.WEAKNESS,slowTime,200,true,false,false))
                p.addPotionEffect(PotionEffect(PotionEffectType.SLOW_DIGGING,slowTime,200,true,false,false))
            })
        }

        /** prefix付きでメッセージを送信 **/
        fun CommandSender.prefixMsg(s: String){
            this.sendMessage(prefix + s)
        }
    }

    /** クラスのデータを登録する(サバイバー) **/
    private fun register(abstractSurvivor: AbstractSurvivor){
        survivorsData[abstractSurvivor.name] = abstractSurvivor
    }

    /** クラスのデータを登録する(ハンター) **/
    private fun register(abstractHunter: AbstractHunter){
        huntersData[abstractHunter.name] = abstractHunter
    }

    /** クラスのデータを全て登録する **/
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
        register(Fader())
        register(Offense())
    }

    override fun onStart() {
        saveDefaultConfig()
        
        plugin = this
        sLang = SLang(this, prefix)
        util = UsefulUtility(this)

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
        http = HttpServer.create(InetSocketAddress(8000), 0)
        saveResource("IdentityFifty.zip",true)
        http?.createContext("/Resource",FileHandler(File(dataFolder.path + "/IdentityFifty.zip")))
        http?.start()
        SEvent(this).register(PlayerJoinEvent::class.java) { e ->
            if (e.player.name == "tororo_1066"){
                e.player.setResourcePack("http://localhost:8000/Resource")
            } else {
                e.player.setResourcePack(resourceUrl)
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
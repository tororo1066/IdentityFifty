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
import tororo1066.identityfifty.data.SpectatorData
import tororo1066.identityfifty.data.SurvivorData
import tororo1066.identityfifty.discord.DiscordClient
import tororo1066.identityfifty.enumClass.AllowAction
import tororo1066.identityfifty.enumClass.StunState
import tororo1066.identityfifty.talent.TalentSQL
import tororo1066.nmsutils.SNms
import tororo1066.tororopluginapi.SJavaPlugin
import tororo1066.tororopluginapi.config.SConfig
import tororo1066.tororopluginapi.lang.SLang
import tororo1066.tororopluginapi.otherUtils.UsefulUtility
import tororo1066.tororopluginapi.sEvent.SEvent
import tororo1066.tororopluginapi.sItem.SInteractItemManager
import tororo1066.tororopluginapi.utils.toPlayer
import java.io.File
import java.net.InetSocketAddress
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

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
        val spectators = HashMap<UUID,SpectatorData>()
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
        /** 便利なユーティリティ **/
        lateinit var util: UsefulUtility
        lateinit var sNms: SNms
        /** データベース **/
        lateinit var talentSQL: TalentSQL
        /** 稼働中のゲームの変数 **/
        var identityFiftyTask: IdentityFiftyTask? = null

        lateinit var discordClient: DiscordClient

        lateinit var characterLogSQL: IdentityFiftyCharacterLogSQL

        val allowSpectatorActions = ArrayList<AllowAction>()

        var loggerMode = true

        const val prefix = "§b[§cIdentity§eFifty§b]§r"

        /** リソパのurl **/
        var resourceUrl = ""
        /** リソパのサーバー **/
        var http: HttpServer? = null

        /** スタンのエフェクト(デフォルトの時間) **/
        fun stunEffect(p: Player){
            stunEffect(p,120,150,StunState.DAMAGED)
        }

        /** スタンのエフェクト(時間指定) **/
        fun stunEffect(p: Player, blindTime: Int, slowTime: Int, state: StunState){
            p.world.spawnParticle(Particle.ELECTRIC_SPARK,p.location.add(0.0,0.5,0.0),50)
            val data = hunters[p.uniqueId]
            var stunTime = Pair(blindTime,slowTime)
            if (data != null){
                stunTime = data.hunterClass.onStun(stunTime.first, stunTime.second, state, p)
                data.talentClasses.values.forEach { clazz ->
                    stunTime = clazz.onStun(stunTime.first, stunTime.second, state, p)
                }
            }
            Bukkit.getScheduler().runTask(plugin, Runnable {
                p.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS,stunTime.first,3,true,false,false))
                p.addPotionEffect(PotionEffect(PotionEffectType.SLOW,stunTime.second,200,true,false,false))
                p.addPotionEffect(PotionEffect(PotionEffectType.JUMP,stunTime.second,200,true,false,false))
                p.addPotionEffect(PotionEffect(PotionEffectType.WEAKNESS,stunTime.second,200,true,false,false))
                p.addPotionEffect(PotionEffect(PotionEffectType.SLOW_DIGGING,stunTime.second,200,true,false,false))
            })
        }

        /** prefix付きでメッセージを送信 **/
        fun CommandSender.prefixMsg(s: String){
            this.sendMessage(prefix + s)
        }

        fun broadcastSpectators(s: String, action: AllowAction){
            spectators.values.filter { it.actions.contains(action) }.forEach {
                it.uuid.toPlayer()?.sendMessage(prefix + s)
            }
            if (loggerMode){
                plugin.logger.info(prefix + s)
            }
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

    private fun register(vararg abstractClass: Any){
        for (clazz in abstractClass){
            if (clazz is AbstractSurvivor){
                register(clazz)
            }else if (clazz is AbstractHunter){
                register(clazz)
            }
        }
    }

    /** クラスのデータを全て登録する **/
    private fun registerAll(){
        register(Nurse(),Dasher(),RunAway(),Searcher(),Helper(),AreaMan(),
            DisguisePlayer(),Gambler(),Mechanic(),Fader(),Offense(),Marker(),
            Coffin(),SerialKiller(),Controller())
    }
    override fun onStart() {
        saveDefaultConfig()
        
        plugin = this
        sLang = SLang(this, prefix)
        util = UsefulUtility(this)
        sNms = SNms.newInstance()

        interactManager = SInteractItemManager(this)
        sConfig = SConfig(this)
        talentSQL = TalentSQL()

        effectManager = EffectManager(this)
        registerAll()

        for (file in File(dataFolder.path + "/map/").listFiles()!!){
            maps[file.nameWithoutExtension] = MapData.loadFromYml(YamlConfiguration.loadConfiguration(file))
        }
        allowSpectatorActions.addAll(config.getStringList("allowSpectatorActions")
            .map { AllowAction.valueOf(it.uppercase()) })
        IdentityCommand()

        if (config.getBoolean("discord.enabled")){
            discordClient = DiscordClient()
        }

        val packLines = File(dataFolder.path + "/secrecy/resourcePackUrl.txt").readLines()
        resourceUrl = packLines[0]
        http = HttpServer.create(InetSocketAddress(8000), 0)
        saveResource("IdentityFifty.zip",true)
        http?.createContext("/Resource",FileHandler(File(dataFolder.path + "/IdentityFifty.zip")))
        http?.start()

        characterLogSQL = IdentityFiftyCharacterLogSQL()

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
        discordClient.jda.shutdown()
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
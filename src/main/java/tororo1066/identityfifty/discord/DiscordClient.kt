package tororo1066.identityfifty.discord

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.GatewayIntent
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.IdentityFiftyTask
import tororo1066.tororopluginapi.utils.toPlayer
import java.util.*
import kotlin.collections.HashMap
import kotlin.random.Random

class DiscordClient: ListenerAdapter(), Listener {

    val jda: JDA
    val guild: Long
    val commandChannel: Long
    val infoChannel: Long
    val entryPermission: Long
    val opPermission: Long
    val map: String
    val discordSQL = DiscordMinecraftSQL()

    var thread: Thread? = null
    val himo = HashMap<Int, Pair<UUID,String>>()
    var himoMode = false

    var canEntry = false

    companion object {
        val survivors = HashMap<UUID,Long>()
        val hunters = HashMap<UUID,Long>()
        val spectators = HashMap<UUID,Long>()
        var enable = false
    }

    init {
        val config = IdentityFifty.plugin.config.getConfigurationSection("discord")!!
        jda = JDABuilder.createDefault(config.getString("token"))
            .addEventListeners(this)
            .enableIntents(GatewayIntent.MESSAGE_CONTENT,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_MEMBERS).build()
        guild = config.getLong("guild")
        commandChannel = config.getLong("commandChannel")
        infoChannel = config.getLong("infoChannel")
        entryPermission = config.getLong("entryPermission")
        opPermission = config.getLong("opPermission")
        map = config.getString("map","")!!

        Bukkit.getPluginManager().registerEvents(this,IdentityFifty.plugin)

        if (config.getBoolean("autoStart")){
            enable = true
            run()
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun event(e: PlayerLoginEvent){
        val uuid = e.player.uniqueId
        if (himoMode && (!survivors.containsKey(uuid) && !hunters.containsKey(uuid)
                    && !spectators.containsKey(uuid)) && discordSQL.getFromUUID(uuid) == null){
            var random = Random.nextInt(100000,999999)
            while (himo.containsKey(random)){
                random = Random.nextInt(100000,999999)
            }
            himo[random] = uuid to e.player.name
            e.disallow(PlayerLoginEvent.Result.KICK_OTHER, Component.text("${e.player.name}のコード $random"))
            return
        }
        if (!enable)return
        if ((!survivors.containsKey(uuid) && !hunters.containsKey(uuid)
                    && !spectators.containsKey(uuid))
                    && !e.player.hasPermission("identity.op")){
            e.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, Component.text("Discord: https://discord.gg/yTubxkj"))
            return
        }
        e.allow()
        val isSurvivor = survivors.containsKey(uuid)
        Bukkit.getScheduler().runTaskLater(IdentityFifty.plugin, Runnable {
            if (IdentityFifty.spectators.containsKey(e.player.uniqueId)){
                e.player.performCommand("identity spectator")
                return@Runnable
            }
            if (isSurvivor){
                e.player.sendMessage("§aサバイバーを選択してください")
            } else {
                e.player.sendMessage("§aハンターを選択してください")
            }
        },20)
    }

    fun run(){
        thread = Thread {
            val channel = jda.getTextChannelById(infoChannel)?:return@Thread
            while (true){
                Thread.sleep(5000)
                while (IdentityFifty.identityFiftyTask != null){
                    Thread.sleep(1)
                }

                Thread.sleep(5000)

                Bukkit.getScheduler().runTask(IdentityFifty.plugin, Runnable {
                    (survivors + hunters + spectators).forEach {
                        it.key.toPlayer()?.kick(Component.text("遊んでくれてありがとう:heart:"))
                    }
                })

                Thread.sleep(5000)

                IdentityFifty.survivors.clear()
                IdentityFifty.hunters.clear()
                IdentityFifty.spectators.clear()
                survivors.clear()
                hunters.clear()
                spectators.clear()
                channel.sendMessage("参加可能になりました 0/4 0/1").queue()
                canEntry = true

                while (survivors.size != 4 || hunters.size != 1){
                    Thread.sleep(1)
                }

                canEntry = false

                val usersAsMention = survivors.values.plus(hunters.values).map { jda.retrieveUserById(it).complete()?.asMention?:"不明" }

                channel.sendMessage("7分以内にサーバーに参加してキャラを選択してください" +
                        " /gwarp xtororo" +
                        "\n${usersAsMention.joinToString(" ")}")
                    .queue()

                var count = 1000 * 60 * 7
                while (IdentityFifty.survivors.size != 4 || IdentityFifty.hunters.size != 1){
                    if (count <= 0){
                        channel.sendMessage("7分経過したため取り消しました")
                        Bukkit.getScheduler().runTask(IdentityFifty.plugin, Runnable {
                            (survivors + hunters + spectators).keys.forEach {
                                it.toPlayer()?.kick(Component.text("5分経過したため取り消しました"))
                            }
                        })
                        continue
                    }
                    Thread.sleep(1)
                    count--
                }

                Bukkit.getScheduler().runTask(IdentityFifty.plugin, Runnable {
                    IdentityFifty.identityFiftyTask = IdentityFiftyTask(IdentityFifty.maps[map]!!.clone())
                    IdentityFifty.identityFiftyTask?.start()
                })

            }
        }
        thread?.start()
    }

    fun end(){
        thread?.interrupt()
    }

    override fun onMessageReceived(e: MessageReceivedEvent) {
        if (e.author.isBot)return
        if (e.message.channel.idLong != commandChannel)return
        if (!e.message.contentRaw.startsWith("!"))return

        val content = e.message.contentRaw.replaceFirst("!","").split(" ")

        val member = e.member?:return
        val command = content[0]
        val args = content.drop(1)

        when(command){
            ""->{
                e.message.reply("""
                    !entry (survivor or hunter or spectator or unregister) ゲームにエントリーします
                    !himo discordアカウントとminecraftを紐づけします
                """.trimIndent()).queue()
            }

            "entry"->{
                if (!member.roles.map { it.idLong }.contains(entryPermission)){
                    e.message.reply("権限がありません").queue()
                    return
                }

                if (args.size != 1){
                    e.message.reply("!entry (hunter or survivor or spectator or unregister)").queue()
                    return
                }

                if (!enable){
                    e.message.reply("開始されていません").queue()
                    return
                }

                if (!canEntry){
                    e.message.reply("現在エントリーできません").queue()
                    return
                }

                if (args[0].lowercase() !in listOf("survivor","hunter","spectator","unregister")){
                    e.message.reply("!entry (hunter or survivor or spectator or unregister)").queue()
                    return
                }

                if (args[0].lowercase() == "unregister"){
                    val minecraftData = discordSQL.getFromDiscordId(member.idLong)
                    if (minecraftData == null){
                        e.message.reply("マインクラフトとdiscordを紐付けしてください !himo").queue()
                        return
                    }
                    val uuid = UUID.fromString(minecraftData.getString("uuid"))
                    hunters.remove(uuid)
                    survivors.remove(uuid)
                    spectators.remove(uuid)
                    IdentityFifty.hunters.remove(uuid)
                    IdentityFifty.survivors.remove(uuid)
                    IdentityFifty.spectators.remove(uuid)
                    e.message.reply("登録解除しました").queue()
                    jda.getTextChannelById(infoChannel)!!
                        .sendMessage("${minecraftData.getString("mcid")}が" +
                                "登録解除しました\n" +
                                "${survivors.size}/4 ${hunters.size}/1").queue()

                    Bukkit.getScheduler().runTask(IdentityFifty.plugin, Runnable {
                        Bukkit.getPlayer(uuid)?.kick(Component.text("登録解除"))
                    })

                    return
                }


                if (hunters.containsValue(member.idLong) || survivors.containsValue(member.idLong)
                    || spectators.containsValue(member.idLong)){
                    e.message.reply("既に参加済みです").queue()
                    return
                }

                val minecraftData = discordSQL.getFromDiscordId(member.idLong)
                if (minecraftData == null){
                    e.message.reply("マインクラフトとdiscordを紐付けしてください !himo").queue()
                    return
                }

                if (args[0].lowercase() == "spectator"){
                    spectators[UUID.fromString(minecraftData.getString("uuid"))] = member.idLong
                    e.message.reply("観戦者として参加しました").queue()
                    return
                }

                val isSurvivor = args[0].lowercase() == "survivor"

                if (isSurvivor && survivors.size >= 4){
                    e.message.reply("サバイバーは満員です").queue()
                    return
                }
                if (!isSurvivor && hunters.size >= 1){
                    e.message.reply("ハンターは満員です").queue()
                    return
                }

                if (isSurvivor){
                    survivors[UUID.fromString(minecraftData.getString("uuid"))] = member.idLong
                } else {
                    hunters[UUID.fromString(minecraftData.getString("uuid"))] = member.idLong
                }

                e.message.reply("参加しました\n<#1119575979241766975> を読むことを推奨します").queue()
                jda.getTextChannelById(infoChannel)!!
                    .sendMessage("${minecraftData.getString("mcid")}が" +
                            "${if (isSurvivor) "サバイバー" else "ハンター"}として参加しました\n" +
                            "${survivors.size}/4 ${hunters.size}/1").queue()

            }

            "himo"->{
                if (!member.roles.map { it.idLong }.contains(entryPermission)){
                    e.message.reply("権限がありません").queue()
                    return
                }

                if (args.size == 1){
                    val code = args[0].toIntOrNull()
                    if (code == null){
                        e.message.reply("コードは6桁の数字です").queue()
                        return
                    }

                    if (!himo.containsKey(code)){
                        e.message.reply("コードが間違っています").queue()
                        return
                    }


                    val data = himo.remove(code)!!
                    val player = discordSQL.getFromUUID(data.first)
                    if (player != null){
                        e.message.reply("このプレイヤーはすでに紐づけされています")
                        return
                    }
                    if (discordSQL.insertData(member.idLong, data.first, data.second)){
                        e.message.reply("紐づけに成功しました！").queue()
                    } else {
                        e.message.reply("紐づけに失敗しました tororo_1066に連絡してください")
                    }

                    return
                } else if (args.isNotEmpty()){
                    e.message.reply("!himo").queue()
                    return
                }

                if (himoMode){
                    e.message.reply("紐づけモードがオンです").queue()
                    return
                }

                if (discordSQL.getFromDiscordId(member.idLong) != null){
                    e.message.reply("既に紐づけ済みです").queue()
                    return
                }

                himoMode = true
                e.message.reply("紐づけモードがオンになりました 5分以内にxtororoに入ってコードを取得し、!himo <コード>を入力してください").queue()
                Bukkit.getScheduler().runTaskLater(IdentityFifty.plugin, Runnable {
                    himoMode = false
                    e.channel.sendMessage("紐づけモードがオフになりました").queue()
                },6000)
            }

            "enable"->{
                if (!member.roles.map { it.idLong }.contains(opPermission))return
                if (args.size != 1){
                    e.message.reply("!enable <bool>").queue()
                    return
                }

                val bool = args[0].toBoolean()
                if (bool){
                    if (enable){
                        e.message.reply("既にtrueです").queue()
                        return
                    }
                    enable = true
                    run()
                    e.message.reply("trueにしました").queue()
                } else {
                    if (!enable){
                        e.message.reply("既にfalseです").queue()
                        return
                    }
                    enable = false
                    end()
                    e.message.reply("falseにしました").queue()
                }
            }
        }
    }
}
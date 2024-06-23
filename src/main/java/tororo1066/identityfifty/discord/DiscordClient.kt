package tororo1066.identityfifty.discord

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.requests.GatewayIntent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.inventory.ItemStack
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.IdentityFiftyTask
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.utils.toPlayer
import java.awt.Color
import java.util.UUID
import kotlin.random.Random

class DiscordClient: ListenerAdapter(), Listener {

    val jda: JDA
    val guild: Long
    val commandChannel: Long
    val infoChannel: Long
    val entryPermission: Long
    val opPermission: Long
    val map: String
    val discordSQL = DiscordMinecraftDB()

    var thread: Thread? = null
    val himo = HashMap<Int, Pair<UUID,String>>()
    var himoMode = false

    var canEntry = false

    companion object {
        val survivors = HashMap<UUID,Long>()
        val hunters = HashMap<UUID,Long>()
        val spectators = HashMap<UUID,Long>()
        val entries = ArrayList<UUID>()
        var enable = false
        var enableTalent = false
    }

    init {
        val config = IdentityFifty.plugin.config.getConfigurationSection("discord")!!
        jda = JDABuilder.createDefault(config.getString("token"))
            .addEventListeners(this)
            .enableIntents(GatewayIntent.MESSAGE_CONTENT,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_MEMBERS).build()
        jda.awaitReady()
        guild = config.getLong("guild")
        commandChannel = config.getLong("commandChannel")
        infoChannel = config.getLong("infoChannel")
        entryPermission = config.getLong("entryPermission")
        opPermission = config.getLong("opPermission")
        map = config.getString("map","")!!
        jda.getGuildById(guild)!!.updateCommands().addCommands(
            Commands.slash("entry","ゲームにエントリーします")
                .addOptions(
                    OptionData(OptionType.STRING, "type", "タイプ", true)
                        .addChoice("サバイバー", "survivor")
                        .addChoice("ハンター", "hunter")
                        .addChoice("観戦者", "spectator")
                        .addChoice("登録解除", "unregister")
                ),
            Commands.slash("himo", "discordアカウントとminecraftを紐づけします")
                .addOptions(OptionData(OptionType.INTEGER, "code", "コード")),
            Commands.slash("info","キャラクターの情報を表示します")
                .addSubcommands(
                    SubcommandData("survivor","サバイバーの情報を表示します")
                        .addOptions(
                            OptionData(OptionType.STRING, "character", "キャラクター", true)
                                .addChoices(
                                    IdentityFifty.survivorsData.keys.map { Command.Choice(translate(it), it) }
                                        .plus(Command.Choice("list","list"))
                                )
                        ),
                    SubcommandData("hunter","ハンターの情報を表示します")
                        .addOptions(
                            OptionData(OptionType.STRING, "character", "キャラクター", true)
                                .addChoices(
                                    IdentityFifty.huntersData.keys.map { Command.Choice(translate(it), it) }
                                        .plus(Command.Choice("list","list"))
                                )
                        ),
                ),
            Commands.slash("enable", "有効/無効を切り替える")
                .addSubcommands(
                    SubcommandData("game", "ゲームの有効/無効を切り替える")
                        .addOptions(OptionData(OptionType.BOOLEAN, "value", "有効にするか", true)),
                    SubcommandData("talent", "天賦の有効/無効を切り替える")
                        .addOptions(OptionData(OptionType.BOOLEAN, "value", "有効にするか", true))
                )
        ).queue()

        Bukkit.getPluginManager().registerEvents(this,IdentityFifty.plugin)

        if (config.getBoolean("autoStart")){
            enable = true
            run()
        }
        enableTalent = config.getBoolean("enableTalent")
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
            e.allow()
            Bukkit.getScheduler().runTask(IdentityFifty.plugin, Runnable {
                e.player.sendMessage(Component.text("§d${e.player.name}のコード $random(クリックでコピー)").clickEvent(
                    ClickEvent.copyToClipboard(random.toString())
                ))
                e.player.sendMessage("§c10秒後に切断されます...")
            })
            Bukkit.getScheduler().runTaskLater(IdentityFifty.plugin, Runnable {
                e.player.kick(Component.text("コードを入力してください $random"))
            },200)
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
            if (spectators.containsKey(e.player.uniqueId)){
                e.player.performCommand("identity spectator")
                e.player.gameMode = GameMode.SPECTATOR
                return@Runnable
            }
            if (isSurvivor){
                e.player.sendMessage("§aサバイバー${if (enableTalent) "と§c天賦§a" else ""}を選択してください")
            } else {
                e.player.sendMessage("§aハンター${if (enableTalent) "と§c天賦§a" else ""}を選択してください")
            }
        },20)
    }

    fun run(){
        thread = Thread {
            val channel = jda.getTextChannelById(infoChannel)?:return@Thread
            game@ while (true) {
                Thread.sleep(5000)
                while (IdentityFifty.identityFiftyTask != null){
                    Thread.sleep(1)
                }

                Thread.sleep(7000)

                Bukkit.getScheduler().runTask(IdentityFifty.plugin, Runnable {
                    (survivors + hunters + spectators).forEach {
                        it.key.toPlayer()?.kick(Component.text("遊んでくれてありがとう:heart:"))
                    }
                })

                Thread.sleep(3000)

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
                while ((IdentityFifty.survivors.size != 4 || IdentityFifty.hunters.size != 1)
                    && (!enableTalent || entries.size != 5)) {
                    if (count <= 0){
                        channel.sendMessage("7分経過したため取り消しました")
                        Bukkit.getScheduler().runTask(IdentityFifty.plugin, Runnable {
                            (survivors + hunters + spectators).keys.forEach {
                                it.toPlayer()?.kick(Component.text("7分経過したため取り消しました"))
                            }
                        })
                        continue@game
                    }
                    Thread.sleep(1)
                    count--
                }

                Bukkit.getScheduler().runTask(IdentityFifty.plugin, Runnable {
                    IdentityFifty.identityFiftyTask = IdentityFiftyTask(IdentityFifty.maps[map]!!.clone(), true)
                    IdentityFifty.identityFiftyTask?.start()
                })

            }
        }
        thread?.start()
    }

    fun end(){
        thread?.interrupt()
    }

    override fun onSlashCommandInteraction(e: SlashCommandInteractionEvent) {
        if (e.user.isBot)return
        if (e.channel.idLong != commandChannel)return
        val member = e.member?:return
        IdentityFifty.plugin.logger.info(e.name)
        when (e.name) {
            "entry" -> {
                if (!member.roles.map { it.idLong }.contains(entryPermission)) {
                    e.reply("権限がありません <#800272407578411029> で権限をもらってください").queue()
                    return
                }

                if (!enable) {
                    e.reply("開始されていません").queue()
                    return
                }

                val type = e.getOption("type") { it.asString } ?:return

                val minecraftData = discordSQL.getFromDiscordId(member.idLong)
                if (minecraftData == null) {
                    e.reply("マインクラフトとdiscordを紐付けしてください /himo").queue()
                    return
                }

                val uuid = UUID.fromString(minecraftData.getString("uuid"))

                if (type == "spectator") {
                    spectators[uuid] = member.idLong
                    e.reply("観戦者として参加しました").queue()
                    return
                }

                if (!canEntry) {
                    e.reply("現在エントリーできません").queue()
                    return
                }

                if (type == "unregister") {
                    hunters.remove(uuid)
                    survivors.remove(uuid)
                    spectators.remove(uuid)
                    IdentityFifty.hunters.remove(uuid)
                    IdentityFifty.survivors.remove(uuid)
                    IdentityFifty.spectators.remove(uuid)
                    e.reply("登録解除しました").queue()
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
                    || spectators.containsValue(member.idLong)) {
                    e.reply("既に参加済みです").queue()
                    return
                }

                val isSurvivor = type == "survivor"

                if (isSurvivor && survivors.size >= 4) {
                    e.reply("サバイバーは満員です").queue()
                    return
                }

                if (!isSurvivor && hunters.size >= 1) {
                    e.reply("ハンターは満員です").queue()
                    return
                }

                if (isSurvivor) {
                    survivors[uuid] = member.idLong
                } else {
                    hunters[uuid] = member.idLong
                }

                e.reply("参加しました\n<#1119575979241766975> を読むことを推奨します").queue()
                jda.getTextChannelById(infoChannel)!!
                    .sendMessage("${minecraftData.getString("mcid")}が" +
                            "${if (isSurvivor) "サバイバー" else "ハンター"}として参加しました\n" +
                            "${survivors.size}/4 ${hunters.size}/1").queue()
            }

            "himo" -> {
                if (!member.roles.map { it.idLong }.contains(entryPermission)) {
                    e.reply("権限がありません <#800272407578411029> で権限をもらってください").setEphemeral(true).queue()
                    return
                }

                val option = e.getOption("code") { it.asLong }
                if (option != null) {
                    val code = option.toInt()
                    if (!himo.containsKey(code)) {
                        e.reply("コードが間違っています").queue()
                        return
                    }

                    val data = himo.remove(code)!!
                    val player = discordSQL.getFromUUID(data.first)
                    if (player != null) {
                        e.reply("このプレイヤーはすでに紐づけされています")
                        return
                    }
                    if (discordSQL.insertData(member.idLong, data.first, data.second)) {
                        e.reply("紐づけに成功しました！").queue()
                    } else {
                        e.reply("紐づけに失敗しました tororo_1066に連絡してください")
                    }

                    return
                }

                if (himoMode) {
                    e.reply("紐づけモードがオンです xtororoに入ってコードを取得し(10秒でキックされます)、!himo <コード>を入力してください").queue()
                    return
                }

                if (discordSQL.getFromDiscordId(member.idLong) != null) {
                    e.reply("既に紐づけ済みです").queue()
                    return
                }

                himoMode = true

                e.reply("紐づけモードがオンになりました 5分以内にxtororoに入ってコードを取得し(10秒でキックされます)、!himo <コード>を入力してください").queue()
            }

            "info" -> {
                if (!member.roles.map { it.idLong }.contains(entryPermission))return
                val type = e.subcommandName
                val character = e.getOption("character") { it.asString } ?:return

                if (character == "list"){
                    val data = if (type == "survivor") IdentityFifty.survivorsData else IdentityFifty.huntersData
                    val embed = EmbedBuilder()
                        .setTitle(if (type == "survivor") "サバイバー" else "ハンター")
                    data.entries.forEach {
                        embed.appendDescription("\n" + translate(it.key))
                    }
                    e.replyEmbeds(embed.build()).queue()
                    return
                }

                val infoItems = ArrayList<ItemStack>()

                if (type == "survivor") {
                    val data = IdentityFifty.survivorsData[character]
                    if (data == null){
                        e.reply("そのキャラクターは存在しません").queue()
                        return
                    }
                    infoItems.addAll(data.info())
                } else {
                    val data = IdentityFifty.huntersData[character]
                    if (data == null){
                        e.reply("そのキャラクターは存在しません").queue()
                        return
                    }
                    infoItems.addAll(data.info())
                }

                val embeds = ArrayList<EmbedBuilder>()

                val embed = EmbedBuilder()
                    .setTitle(translate(character))

                infoItems[0].lore?.forEach {
                    embed.appendDescription("\n" + (ChatColor.stripColor(it)?:"Error"))
                }

                embed.setColor(if (type == "survivor") Color.GREEN else Color.RED)

                embeds.add(embed)

                fun getFirstColor(string: String): Color {
                    if (!string.contains("§")) return Color.WHITE
                    val color = string.split("§")[1].substring(0, 1).firstOrNull()?:'f'
                    return ChatColor.getByChar(color)?.asBungee()?.color?:Color.WHITE
                }

                val others = infoItems.drop(1)
                others.forEach {
                    val embedOther = EmbedBuilder()
                        .setTitle(ChatColor.stripColor(it.itemMeta.displayName))
                    it.lore?.forEach { lore ->
                        embedOther.appendDescription("\n" + (ChatColor.stripColor(lore)?:"Error"))
                    }
                    embedOther.setColor(getFirstColor(it.itemMeta.displayName))
                    embeds.add(embedOther)
                }

                e.replyEmbeds(embeds.map {
                    it.build()
                }).queue()
            }

            "enable" -> {
                if (!member.roles.map { it.idLong }.contains(opPermission))return
                val bool = e.getOption("value") { it.asBoolean } ?:return
                if (e.subcommandName == "talent") {
                    if (bool) {
                        if (enableTalent) {
                            e.reply("既にtrueです").queue()
                            return
                        }
                        enableTalent = true
                        IdentityFifty.plugin.config.set("discord.enableTalent", true)
                        IdentityFifty.plugin.saveConfig()
                        e.reply("trueにしました").queue()
                    } else {
                        if (!enableTalent) {
                            e.reply("既にfalseです").queue()
                            return
                        }
                        enableTalent = false
                        IdentityFifty.plugin.config.set("discord.enableTalent", false)
                        IdentityFifty.plugin.saveConfig()
                        e.reply("falseにしました").queue()
                    }
                } else {
                    if (bool) {
                        if (enable) {
                            e.reply("既にtrueです").queue()
                            return
                        }
                        enable = true
                        run()
                        e.reply("trueにしました").queue()
                    } else {
                        if (!enable) {
                            e.reply("既にfalseです").queue()
                            return
                        }
                        enable = false
                        end()
                        e.reply("falseにしました").queue()
                    }
                }
            }
        }


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

                if (args[0].lowercase() !in listOf("survivor","hunter","spectator","unregister")){
                    e.message.reply("!entry (hunter or survivor or spectator or unregister)").queue()
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

                if (!canEntry){
                    e.message.reply("現在エントリーできません").queue()
                    return
                }

                if (args[0].lowercase() == "unregister"){
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
                    e.message.reply("紐づけモードがオンです xtororoに入ってコードを取得し(10秒でキックされます)、!himo <コード>を入力してください").queue()
                    return
                }

                if (discordSQL.getFromDiscordId(member.idLong) != null){
                    e.message.reply("既に紐づけ済みです").queue()
                    return
                }

                himoMode = true
                e.message.reply("紐づけモードがオンになりました 5分以内にxtororoに入ってコードを取得し(10秒でキックされます)、!himo <コード>を入力してください").queue()
                Bukkit.getScheduler().runTaskLater(IdentityFifty.plugin, Runnable {
                    himoMode = false
                    e.channel.sendMessage("紐づけモードがオフになりました").queue()
                },6000)
            }

            "info" -> {
                if (!member.roles.map { it.idLong }.contains(entryPermission))return
                if (args.size < 2){
                    e.message.reply("!info (survivor or hunter) <キャラクター(or list)>").queue()
                    return
                }
                val type = args[0]
                val character = args[1]
                if (type !in listOf("survivor","hunter","list")){
                    e.message.reply("!info (survivor or hunter) <キャラクター(or list)>").queue()
                    return
                }

                if (character == "list"){
                    val data = if (type == "survivor") IdentityFifty.survivorsData else IdentityFifty.huntersData
                    val embed = EmbedBuilder()
                        .setTitle(if (type == "survivor") "サバイバー" else "ハンター")
                    data.entries.forEach {
                        embed.appendDescription("\n" + translate(it.key))
                    }
                    e.message.replyEmbeds(embed.build()).queue()
                    return
                }

                val infoItems = ArrayList<ItemStack>()

                if (type == "survivor") {
                    val data = IdentityFifty.survivorsData.entries.find { translate(it.key) == character }?.value
                    if (data == null){
                        e.message.reply("そのキャラクターは存在しません").queue()
                        return
                    }
                    infoItems.addAll(data.info())
                } else {
                    val data = IdentityFifty.huntersData.entries.find { translate(it.key) == character }?.value
                    if (data == null){
                        e.message.reply("そのキャラクターは存在しません").queue()
                        return
                    }
                    infoItems.addAll(data.info())
                }

                val embeds = ArrayList<EmbedBuilder>()

                val embed = EmbedBuilder()
                    .setTitle(translate(character))

                infoItems[0].lore?.forEach {
                    embed.appendDescription("\n" + (ChatColor.stripColor(it)?:"Error"))
                }

                embeds.add(embed)

                val others = infoItems.drop(1)
                others.forEach {
                    val embedOther = EmbedBuilder()
                        .setTitle(ChatColor.stripColor(it.itemMeta.displayName))
                    it.lore?.forEach { lore ->
                        embedOther.appendDescription("\n" + (ChatColor.stripColor(lore)?:"Error"))
                    }
                    embeds.add(embedOther)
                }

                e.message.replyEmbeds(embeds.map { it.build() }).queue()
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
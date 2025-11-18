package tororo1066.identityfifty.discord

import io.papermc.paper.event.player.PlayerServerFullCheckEvent
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.requests.GatewayIntent
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.IdentityFiftyTask
import tororo1066.tororopluginapi.SStr
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.utils.sendMessage
import tororo1066.tororopluginapi.utils.toPlayer
import java.awt.Color
import java.util.UUID

class DiscordClient: ListenerAdapter(), Listener {

    val jda: JDA
    private val guild: Long
    private val commandChannel: Long
    val infoChannel: Long
    private val entryPermission: Long
    private val opPermission: Long
    val map: String
    private val discordSQL = DiscordMinecraftDB()

    private var thread: Thread? = null
    private val himo = HashMap<Int, Pair<UUID,String>>()
    private var himoMode = false

    var canEntry = false

    companion object {
        val survivors = HashMap<UUID,Long>()
        val hunters = HashMap<UUID,Long>()
        val spectators = HashMap<UUID,Long>()
        val entries = ArrayList<UUID>()
        val invite = HashMap<UUID,ArrayList<UUID>>() // key: inviter value: invited
        var enable = false
        var enableTalent = false
        var enableInvite = false
        var enableSpectator = true
        var hunterLimit = 1
        var survivorLimit = 4
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
            Commands.slash("help","ヘルプを表示します"),
            Commands.slash("entry","ゲームにエントリーします")
                .addOptions(
                    OptionData(OptionType.STRING, "type", "タイプ", true)
                        .addChoice("サバイバー", "survivor")
                        .addChoice("ハンター", "hunter")
                        .addChoice("観戦者", "spectator")
                        .addChoice("登録解除", "unregister")
                ),
            Commands.slash("invite","discordと連携していないプレイヤーを招待します")
                .addOptions(OptionData(OptionType.STRING, "name", "プレイヤー名", true)),
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
                        .addOptions(OptionData(OptionType.BOOLEAN, "value", "有効にするか", true)),
                    SubcommandData("invite", "招待の有効/無効を切り替える")
                        .addOptions(OptionData(OptionType.BOOLEAN, "value", "有効にするか", true)),
                    SubcommandData("spectator", "観戦者の有効/無効を切り替える")
                        .addOptions(OptionData(OptionType.BOOLEAN, "value", "有効にするか", true)),
                )
        ).queue()

        Bukkit.getPluginManager().registerEvents(this,IdentityFifty.plugin)

        if (config.getBoolean("autoStart")){
            enable = true
            run()
        }
        enableTalent = config.getBoolean("enableTalent")
        enableInvite = config.getBoolean("enableInvite")
        enableSpectator = config.getBoolean("enableSpectator", true)

        hunterLimit = config.getInt("hunterLimit", 1)
        survivorLimit = config.getInt("survivorLimit", 4)
    }

//    @EventHandler(priority = EventPriority.HIGHEST)
//    fun event(e: PlayerLoginEvent){
//        val uuid = e.player.uniqueId
//        if (himoMode && (!survivors.containsKey(uuid) && !hunters.containsKey(uuid)
//                    && !spectators.containsKey(uuid)) && discordSQL.getFromUUID(uuid) == null){
//            var random = Random.nextInt(100000,999999)
//            while (himo.containsKey(random)){
//                random = Random.nextInt(100000,999999)
//            }
//            himo[random] = uuid to e.player.name
//            e.allow()
//            Bukkit.getScheduler().runTask(IdentityFifty.plugin, Runnable {
//                e.player.sendMessage(Component.text("§d${e.player.name}のコード $random(クリックでコピー)").clickEvent(
//                    ClickEvent.copyToClipboard(random.toString())
//                ))
//                e.player.sendMessage("§c10秒後に切断されます...")
//            })
//            Bukkit.getScheduler().runTaskLater(IdentityFifty.plugin, Runnable {
//                e.player.kick(Component.text("コードを入力してください $random"))
//            },200)
//            return
//        }
//        if (!enable)return
//        if ((!survivors.containsKey(uuid) && !hunters.containsKey(uuid)
//                    && !spectators.containsKey(uuid)
//                    && (!invite.any { it.value.contains(uuid) } || !enableInvite))
//                    && !e.player.hasPermission("identity.op")){
//            e.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, Component.text("Discord: https://discord.gg/yTubxkj"))
//            return
//        }
//        e.allow()
//        Bukkit.getScheduler().runTaskLater(IdentityFifty.plugin, Runnable {
//            val map = IdentityFifty.maps[this.map]!!
//            map.lobbyLocation?.let {
//                if (IdentityFifty.identityFiftyTask == null) {
//                    e.player.teleport(it)
//                }
//            }
//            if (invite.any { it.value.contains(uuid) }){
//                val inviter = invite.entries.first { it.value.contains(uuid) }.key
//                val p = e.player
//                p.sendMessage("§aあなたは${Bukkit.getOfflinePlayer(inviter).name}に招待されました")
//                p.sendMessage("§d何でエントリーするか選択してください")
//                p.sendMessage(
//                    SStr("§b§l[サバイバー(${survivors.size}/$survivorLimit)]")
//                        .commandText("/identity acceptInvite survivor")
//                        .hoverText("サバイバーを選択します")
//                        .append(
//                            SStr(" §c§l[ハンター(${hunters.size}/$hunterLimit)]")
//                                .commandText("/identity acceptInvite hunter")
//                                .hoverText("ハンターを選択します")
//                        )
//                        .append(
//                            SStr(" §7§l[観戦者]")
//                                .commandText("/identity acceptInvite spectator")
//                                .hoverText("観戦者を選択します")
//                        )
//                )
//
//                return@Runnable
//            }
//            if (spectators.containsKey(e.player.uniqueId)){
//                e.player.performCommand("identity spectator")
//                e.player.gameMode = GameMode.SPECTATOR
//                return@Runnable
//            }
//            if (survivors.containsKey(uuid)){
//                e.player.sendMessage("§aサバイバー${if (enableTalent) "と§c天賦§a" else ""}を選択してください")
//            } else if (hunters.containsKey(uuid)){
//                e.player.sendMessage("§aハンター${if (enableTalent) "と§c天賦§a" else ""}を選択してください")
//            }
//        },20)
//    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun event(e: PlayerServerFullCheckEvent) {
        val uuid = e.playerProfile.id
        val name = e.playerProfile.name
        if (uuid == null || name == null) {
            e.deny(Component.text("You can't join the server"))
            return
        }
        if (himoMode && (!survivors.containsKey(uuid) && !hunters.containsKey(uuid))
                    && !spectators.containsKey(uuid) && discordSQL.getFromUUID(uuid) == null) {
            var random = (100000..999999).random()
            while (himo.containsKey(random)) {
                random = (100000..999999).random()
            }
            himo[random] = uuid to name
            e.deny(Component.text("${name}のコード: $random"))
            return
        }

        if (!enable) return

        val offlinePlayer = Bukkit.getOfflinePlayer(uuid)

        if ((!survivors.containsKey(uuid) && !hunters.containsKey(uuid)
                    && !spectators.containsKey(uuid)
                    && (!invite.any { it.value.contains(uuid) } || !enableInvite))
                    && !IdentityFifty.permissionManager.playerHas(null, offlinePlayer, "identity.op")) {
            e.deny(Component.text("Discord: https://discord.gg/yTubxkj"))
            return
        }
        e.allow(true)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerJoin(e: PlayerJoinEvent) {
        val uuid = e.player.uniqueId
        if (!enable)return
        Bukkit.getScheduler().runTaskLater(IdentityFifty.plugin, Runnable {
            val map = IdentityFifty.maps[this.map]!!
            map.lobbyLocation?.let {
                if (IdentityFifty.identityFiftyTask == null) {
                    e.player.teleport(it)
                }
            }
            if (invite.any { it.value.contains(uuid) }){
                val inviter = invite.entries.first { it.value.contains(uuid) }.key
                val p = e.player
                p.sendMessage("§aあなたは${Bukkit.getOfflinePlayer(inviter).name}に招待されました")
                p.sendMessage("§d何でエントリーするか選択してください")
                p.sendMessage(
                    SStr("§b§l[サバイバー(${survivors.size}/$survivorLimit)]")
                        .commandText("/identity acceptInvite survivor")
                        .hoverText("サバイバーを選択します")
                        .append(
                            SStr(" §c§l[ハンター(${hunters.size}/$hunterLimit)]")
                                .commandText("/identity acceptInvite hunter")
                                .hoverText("ハンターを選択します")
                        )
                        .append(
                            SStr(" §7§l[観戦者]")
                                .commandText("/identity acceptInvite spectator")
                                .hoverText("観戦者を選択します")
                        )
                )
                return@Runnable
            }
            if (spectators.containsKey(e.player.uniqueId)){
                e.player.performCommand("identity spectator")
                return@Runnable
            }
            if (survivors.containsKey(uuid)){
                e.player.sendMessage("§aサバイバー${if (enableTalent) "と§c天賦§a" else ""}を選択してください")
            } else if (hunters.containsKey(uuid)){
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
                entries.clear()
                invite.clear()
                channel.sendMessage("参加可能になりました 0/${survivorLimit} 0/${hunterLimit}").queue()
                canEntry = true

                while (survivors.size != survivorLimit || hunters.size != hunterLimit){
                    Thread.sleep(1)
                }

                canEntry = false

                val usersAsMention = survivors.values.plus(hunters.values).map {
                    try {
                        jda.retrieveUserById(it).complete()?.asMention?:"不明"
                    } catch (e: Exception) {
                        "不明"
                    }
                }

                channel.sendMessage("7分以内にサーバーに参加してキャラを選択してください" +
                        " /gwarp xtororo" +
                        "\n${usersAsMention.joinToString(" ")}")
                    .queue()

                var count = 1000 * 60 * 7
                while ((IdentityFifty.survivors.size != survivorLimit || IdentityFifty.hunters.size != hunterLimit)
                    && (!enableTalent || entries.size != survivorLimit + hunterLimit)) {
                    if (count <= 0){
                        channel.sendMessage("7分経過したため取り消しました").queue()
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
        IdentityFifty.plugin.logger.info(e.commandString)
        when (e.name) {
            "help" -> {
                e.reply("""
                    /entry (サバイバー or ハンター or 観戦者 or 登録解除) ゲームにエントリーします
                    /himo discordアカウントとminecraftを紐づけします
                    /info (survivor or hunter) <キャラクター名(or list)> キャラクターの情報を表示します
                """.trimIndent())
            }

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
                    if (!enableSpectator) {
                        e.reply("観戦が無効になっています").queue()
                        return
                    }
                    if (hunters.containsValue(member.idLong) || survivors.containsValue(member.idLong)) {
                        e.reply("既に参加済みです").queue()
                        return
                    }
                    spectators[uuid] = member.idLong
                    e.reply("観戦者として参加しました").queue()
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
                                "${survivors.size}/${survivorLimit} ${hunters.size}/${hunterLimit}").queue()
                    Bukkit.getScheduler().runTask(IdentityFifty.plugin, Runnable {
                        Bukkit.getPlayer(uuid)?.kick(Component.text("登録解除"))
                    })
                    return
                }

                if (!canEntry) {
                    e.reply("現在エントリーできません").queue()
                    return
                }

                if (hunters.containsValue(member.idLong) || survivors.containsValue(member.idLong)
                    || spectators.containsValue(member.idLong)) {
                    e.reply("既に参加済みです").queue()
                    return
                }

                val isSurvivor = type == "survivor"

                if (isSurvivor && survivors.size >= survivorLimit) {
                    e.reply("サバイバーは満員です").queue()
                    return
                }

                if (!isSurvivor && hunters.size >= hunterLimit) {
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
                            "${survivors.size}/${survivorLimit} ${hunters.size}/${hunterLimit}").queue()
            }

            "invite" -> {
                if (!member.roles.map { it.idLong }.contains(entryPermission)) {
                    e.reply("権限がありません <#800272407578411029> で権限をもらってください").setEphemeral(true).queue()
                    return
                }

                if (!enableInvite) {
                    e.reply("招待が無効になっています").queue()
                    return
                }

                val name = e.getOption("name") { it.asString } ?:return
                Bukkit.getScheduler().runTaskAsynchronously(IdentityFifty.plugin, Runnable {
                    val self = discordSQL.getFromDiscordId(member.idLong)?.getString("uuid")
                        .let {
                            if (it == null) {
                                e.reply("マインクラフトとdiscordを紐付けしてください /himo").queue()
                                return@Runnable
                            }
                            UUID.fromString(it)
                        }

                    val player = Bukkit.getOfflinePlayer(name)
                    if (player.name == null) {
                        e.reply("そのプレイヤーは存在しません").queue()
                        return@Runnable
                    }

                    discordSQL.getFromUUID(player.uniqueId)?.let {
                        e.reply("そのプレイヤーはdiscordと連携されています").queue()
                        return@Runnable
                    }

                    if (invite.any { it.value.contains(player.uniqueId) }) {
                        e.reply("既に招待されています").queue()
                        return@Runnable
                    }

                    invite.computeIfAbsent(self) { ArrayList() }.add(player.uniqueId)
                    e.reply("${player.name}に招待を送信しました").queue()
                })
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
                    e.reply("紐づけモードがオンです xtororoに入ってコードを取得し(10秒でキックされます)、/himo <コード>を入力してください").queue()
                    return
                }

                if (discordSQL.getFromDiscordId(member.idLong) != null) {
                    e.reply("既に紐づけ済みです").queue()
                    return
                }

                himoMode = true

                e.reply("紐づけモードがオンになりました xtororoに入ってコードを取得し(10秒でキックされます)、/himo <コード>を入力してください").queue()
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
                when(e.subcommandName) {
                    "talent" -> {
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
                    }

                    "game" -> {
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

                    "invite" -> {
                        if (bool) {
                            if (enableInvite) {
                                e.reply("既にtrueです").queue()
                                return
                            }
                            enableInvite = true
                            IdentityFifty.plugin.config.set("discord.enableInvite", true)
                            IdentityFifty.plugin.saveConfig()
                            e.reply("trueにしました").queue()
                        } else {
                            if (!enableInvite) {
                                e.reply("既にfalseです").queue()
                                return
                            }
                            enableInvite = false
                            IdentityFifty.plugin.config.set("discord.enableInvite", false)
                            IdentityFifty.plugin.saveConfig()
                            e.reply("falseにしました").queue()
                        }
                    }

                    "spectator" -> {
                        if (bool) {
                            if (enableSpectator) {
                                e.reply("既にtrueです").queue()
                                return
                            }
                            enableSpectator = true
                            IdentityFifty.plugin.config.set("discord.enableSpectator", true)
                            IdentityFifty.plugin.saveConfig()
                            e.reply("trueにしました").queue()
                        } else {
                            if (!enableSpectator) {
                                e.reply("既にfalseです").queue()
                                return
                            }
                            enableSpectator = false
                            IdentityFifty.plugin.config.set("discord.enableSpectator", false)
                            IdentityFifty.plugin.saveConfig()
                            e.reply("falseにしました").queue()
                        }
                    }
                }
            }
        }


    }
}
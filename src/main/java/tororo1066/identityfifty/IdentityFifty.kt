package tororo1066.identityfifty

import de.slikey.effectlib.EffectManager
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.UseCooldown
import net.kyori.adventure.key.Key
import net.milkbowl.vault.permission.Permission
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
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
import tororo1066.identityfifty.talent.TalentSQLV2
import tororo1066.nmsutils.PacketListener
import tororo1066.nmsutils.SNms
import tororo1066.tororopluginapi.Proxy
import tororo1066.tororopluginapi.SJavaPlugin
import tororo1066.tororopluginapi.lang.SLang
import tororo1066.tororopluginapi.otherUtils.UsefulUtility
import tororo1066.tororopluginapi.sItem.SInteractItem
import tororo1066.tororopluginapi.sItem.SInteractItemManager
import tororo1066.tororopluginapi.sItem.SItem
import tororo1066.tororopluginapi.utils.toPlayer
import java.io.File
import java.util.UUID

class IdentityFifty : SJavaPlugin(UseOption.SInput) {

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
        lateinit var permissionManager: Permission
        /** このプラグイン **/
        lateinit var plugin: IdentityFifty
        /** 言語マネージャー **/
        lateinit var sLang: SLang
        /** 便利なユーティリティ **/
        lateinit var util: UsefulUtility
        /** データベース **/
        lateinit var talentSQL: TalentSQLV2
        lateinit var sNms: SNms
        lateinit var packetListener: PacketListener
        const val CHANNEL_NAME = "identityfifty"
        /** 稼働中のゲームの変数 **/
        var identityFiftyTask: IdentityFiftyTask? = null

        lateinit var discordClient: DiscordClient

        lateinit var characterLogSQL: IdentityFiftyCharacterLogSQL

        val allowSpectatorActions = ArrayList<AllowAction>()

        private var loggerMode = true

        const val PREFIX = "§b[§cIdentity§eFifty§b]§r"

        /** スタンのエフェクト(デフォルトの時間) **/
        fun stunEffect(p: Player) {
            stunEffect(p,110,120,StunState.DAMAGED)
        }

        /** スタンのエフェクト(時間指定) **/
        fun stunEffect(p: Player, blindTime: Int, slowTime: Int, state: StunState) {
            p.world.spawnParticle(Particle.ELECTRIC_SPARK,p.location.add(0.0,0.5,0.0),75,1.0,1.0,1.0)
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
                p.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS,stunTime.second,200,true,false,false))
                p.addPotionEffect(PotionEffect(PotionEffectType.JUMP_BOOST,stunTime.second,200,true,false,false))
                p.addPotionEffect(PotionEffect(PotionEffectType.WEAKNESS,stunTime.second,200,true,false,false))
                p.addPotionEffect(PotionEffect(PotionEffectType.MINING_FATIGUE,stunTime.second,200,true,false,false))
            })
        }

        fun speedModifier(p: Player, speed: Double, duration: Int,
                          type: AttributeModifier.Operation = AttributeModifier.Operation.ADD_NUMBER
        ): BukkitRunnable {
            val speedAttribute = p.getAttribute(Attribute.MOVEMENT_SPEED)
            val uuid = UUID.randomUUID()
            val modifier = AttributeModifier(NamespacedKey(plugin, uuid.toString()), speed, type)
            fun removeModifier() {
                speedAttribute?:return
                speedAttribute.removeModifier(modifier)
            }
            speedAttribute?.addModifier(modifier)
            return object : BukkitRunnable() {
                init {
                    runTaskLater(plugin, duration.toLong())
                }
                override fun run() {
                    removeModifier()
                }

                override fun cancel() {
                    removeModifier()
                    super.cancel()
                }
            }
        }

        @Suppress("UnstableApiUsage")
        fun createSInteractItem(sItem: SItem): SInteractItem {
            val itemStack = sItem.build()
            itemStack.setData(
                DataComponentTypes.USE_COOLDOWN,
                UseCooldown.useCooldown(0.1f)
                    .cooldownGroup(Key.key("identityfifty", "${UUID.randomUUID()}"))
            )
            return interactManager.createSInteractItem(itemStack, true)
        }

        /** prefix付きでメッセージを送信 **/
        fun CommandSender.prefixMsg(s: String) {
            this.sendMessage(PREFIX + s)
        }

        fun broadcastSpectators(s: String, vararg action: AllowAction) {
            spectators.values.filter { it.actions.any { any -> action.contains(any) } }.forEach {
                it.uuid.toPlayer()?.sendMessage(PREFIX + s)
            }
            if (loggerMode){
                Bukkit.getConsoleSender().sendMessage(PREFIX + s)
            }
        }


    }

    /** クラスのデータを登録する(サバイバー) **/
    private fun register(abstractSurvivor: AbstractSurvivor) {
        survivorsData[abstractSurvivor.name] = abstractSurvivor
    }

    /** クラスのデータを登録する(ハンター) **/
    private fun register(abstractHunter: AbstractHunter) {
        huntersData[abstractHunter.name] = abstractHunter
    }

    private fun register(vararg abstractClass: Any) {
        for (clazz in abstractClass) {
            if (clazz is AbstractSurvivor) {
                register(clazz)
            } else if (clazz is AbstractHunter) {
                register(clazz)
            }
        }
    }

    /** クラスのデータを全て登録する **/
    private fun registerAll() {
        register(Nurse(),Dasher(),RunAway(),Searcher(),Helper(),AreaMan(),
            DisguisePlayer(),Gambler(),Mechanic(),Fader(),Offense(),Marker(),
            Coffin(),SerialKiller(),Controller(),Swapper(),Fixer(),TraceCollector())
    }
    override fun onStart() {
        saveDefaultConfig()
        
        plugin = this

        val permissionProvider = server.servicesManager.getRegistration(Permission::class.java)
        if (permissionProvider == null){
            logger.severe("This plugin requires Vault to run. Disabling...")
            server.pluginManager.disablePlugin(this)
            return
        }

        sLang = SLang(this, PREFIX)
        util = UsefulUtility(this)
        sNms = getSNms()
        packetListener = Proxy(this, "tororo1066.nmsutils").getProxy(PacketListener::class.java)

        interactManager = SInteractItemManager(this)
        interactManager.setOnSetCoolDownEvent { cooldown, sInteractItem ->
            val itemStack = sInteractItem.itemStack
            Bukkit.getOnlinePlayers().forEach { player ->
                player.setCooldown(itemStack, cooldown)
            }
        }
        talentSQL = TalentSQLV2()

        effectManager = EffectManager(this)
        permissionManager = permissionProvider.provider

        registerAll()

        for (file in File(dataFolder.path + "/map/").listFiles()!!) {
            maps[file.nameWithoutExtension] = MapData.loadFromYml(YamlConfiguration.loadConfiguration(file))
        }
        allowSpectatorActions.addAll(config.getStringList("allowSpectatorActions")
            .map { AllowAction.valueOf(it.uppercase()) })
        IdentityCommand()

        if (config.getBoolean("discord.enabled")) {
            discordClient = DiscordClient()
        }

        characterLogSQL = IdentityFiftyCharacterLogSQL()
    }

    override fun onEnd() {
        discordClient.jda.shutdownNow()
    }


}
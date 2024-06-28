package tororo1066.identityfifty.character.survivor

import me.libraryaddict.disguise.disguisetypes.PlayerDisguise
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Chicken
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitTask
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.data.GlowManager
import tororo1066.identityfifty.data.PrisonData
import tororo1066.identityfifty.data.SurvivorData
import tororo1066.identityfifty.enumClass.AllowAction
import tororo1066.identityfifty.enumClass.StunState
import tororo1066.nmsutils.items.GlowColor
import tororo1066.tororopluginapi.lang.SLang.Companion.sendTranslateMsg
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sItem.SItem
import tororo1066.tororopluginapi.utils.toPlayer
import java.util.UUID

class DisguisePlayer: AbstractSurvivor("disguise") {

    private var latentEntity: Chicken? = null
    private var entityGlowManager: GlowManager? = null
    private var glowTasks = mutableListOf<Int>()
    private var searchTask: BukkitTask? = null

    override fun onStart(p: Player) {
        super.onStart(p)
        val passiveItem = SItem(Material.STICK).setDisplayName(translate("passive")).setCustomModelData(8)
            .addLore(translate("disguise_passive_lore_1"))
            .addLore(translate("disguise_passive_lore_2"))
            .addLore(translate("disguise_passive_lore_3"))
            .addLore(translate("disguise_passive_lore_4"))

        val disguiseSkill = SItem(Material.STICK).setDisplayName(translate("disguise_skill")).setCustomModelData(10)
            .addLore(translate("disguise_skill_lore_1"))
            .addLore(translate("disguise_skill_lore_2"))
            .addLore(translate("disguise_skill_lore_3"))

        val disguiseSkillItem = IdentityFifty.interactManager.createSInteractItem(disguiseSkill,true).setInteractEvent { e, _ ->
            val player = e.player
            val target = player.getTargetEntity(4)?:return@setInteractEvent false
            if (target !is Player)return@setInteractEvent false
            if (!IdentityFifty.survivors.containsKey(target.uniqueId))return@setInteractEvent false
            val data = IdentityFifty.survivors[player.uniqueId]!!
            data.skinModifier.disguise(target)
            player.sendTranslateMsg("disguise_skill_start",target.name)
            IdentityFifty.broadcastSpectators(translate("spec_disguise_skill_used",player.name,target.name),
                AllowAction.RECEIVE_SURVIVORS_ACTION)
            player.world.playSound(player.location, Sound.ENTITY_ENDER_DRAGON_HURT, 1f, 1f)
            tasks.add(Bukkit.getScheduler().runTaskLater(IdentityFifty.plugin, Runnable {
                if (data.skinModifier.isDisguise()){
                    data.skinModifier.unDisguise()
                    player.sendTranslateMsg("disguise_skill_end")
                    IdentityFifty.broadcastSpectators(translate("spec_disguise_skill_end",player.name),
                        AllowAction.RECEIVE_SURVIVORS_ACTION)
                    player.world.playSound(player.location, Sound.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, 1f, 1f)
                }
            },700))
            return@setInteractEvent true
        }.setInitialCoolDown(1000)

        p.inventory.addItem(passiveItem)
        p.inventory.addItem(disguiseSkillItem)

    }

    override fun parameters(data: SurvivorData): SurvivorData {
        data.survivorClass = this
        return data
    }

    override fun onDamage(damage: Int, toHealth: Int, damager: Player, p: Player): Pair<Boolean, Int> {
        val data = IdentityFifty.survivors[p.uniqueId]!!
        if (data.skinModifier.isDisguise()){
            data.skinModifier.unDisguise()
            IdentityFifty.stunEffect(damager,20,60,StunState.OTHER)
            p.sendTranslateMsg("disguise_skill_end")
            IdentityFifty.broadcastSpectators(translate("spec_disguise_skill_end",p.name),
                AllowAction.RECEIVE_SURVIVORS_ACTION)
            p.world.playSound(p.location, Sound.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, 1f, 1f)
            return Pair(false,0)
        }
        return super.onDamage(damage, toHealth, damager, p)
    }

    override fun onHelp(helpedPlayer: Player, p: Player) {
        val helpedPlayerData = IdentityFifty.survivors[helpedPlayer.uniqueId]!!
        val uuid = UUID.randomUUID()
        helpedPlayerData.footprintsModify += uuid to 0.0
        helpedPlayer.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY,100,1,true,false))
        Bukkit.getScheduler().runTaskLater(IdentityFifty.plugin, Runnable {
            helpedPlayerData.footprintsModify -= uuid
        }, 100)
    }

    override fun onJail(prisonData: PrisonData, p: Player) {
        Bukkit.getScheduler().runTaskLater(IdentityFifty.plugin, Runnable {
            val map = IdentityFifty.identityFiftyTask?.map ?: return@Runnable
            val randomJail = map.prisons.filter { it.value.inPlayer.isEmpty() }
                .values.randomOrNull() ?: return@Runnable
            map.world.spawn(randomJail.spawnLoc, Chicken::class.java) { chicken ->
                chicken.setAI(false)
                chicken.isSilent = true
                val disguise = PlayerDisguise(p)
                    .setEntity(chicken)
                    .setNameVisible(false)
                disguise.isSelfDisguiseVisible = false
                disguise.startDisguise()

                latentEntity = chicken
                entityGlowManager = GlowManager(chicken.uniqueId).also {
                    glowTasks.addAll(it.glow(mutableSetOf(p), GlowColor.YELLOW, 1000000))
                }
                searchTask = Bukkit.getScheduler().runTaskTimer(IdentityFifty.plugin, Runnable second@ {
                    val players = chicken.location.getNearbyPlayers(12.0)
                        .filter { IdentityFifty.hunters.containsKey(it.uniqueId) }
                    if (players.isEmpty())return@second

                    val receivers = IdentityFifty.survivors.mapNotNull { it.key.toPlayer() }.toMutableList()
                    players.forEach { hunter ->
                        val data = IdentityFifty.hunters[hunter.uniqueId]!!
                        data.glowManager.glow(receivers, GlowColor.BLUE, 21)
                    }
                }, 0, 20)
            }
        }, 10)
    }

    override fun onGotHelp(helper: Player, p: Player): ReturnAction {
        latentEntity?.let {
            it.location.world.playSound(it.location, Sound.ENTITY_SHEEP_DEATH, 2f, 2f)
        }
        clear(p)
        return super.onGotHelp(helper, p)
    }

    override fun onDie(p: Player) {
        clear(p)
    }

    override fun onEnd(p: Player) {
        clear(p)
    }

    private fun clear(p: Player){
        searchTask?.cancel()
        glowTasks.forEach { entityGlowManager?.cancelTask(it) }
        glowTasks.clear()
        entityGlowManager = null
        latentEntity?.remove()
        latentEntity = null
        IdentityFifty.survivors[p.uniqueId]?.skinModifier?.unDisguise()
    }

    override fun info(): ArrayList<ItemStack> {
        val passiveItem = SItem(Material.STICK).setDisplayName(translate("passive")).setCustomModelData(8)
            .addLore(translate("disguise_passive_lore_1"))
            .addLore(translate("disguise_passive_lore_2"))
            .addLore(translate("disguise_passive_lore_3"))
            .addLore(translate("disguise_passive_lore_4"))

        val disguiseSkill = SItem(Material.STICK).setDisplayName(translate("disguise_skill")).setCustomModelData(10)
            .addLore(translate("disguise_skill_lore_1"))
            .addLore(translate("disguise_skill_lore_2"))
            .addLore(translate("disguise_skill_lore_3"))

        return arrayListOf(passiveItem,disguiseSkill)
    }

    override fun description(): String {
        return translate("disguise_description")
    }
}
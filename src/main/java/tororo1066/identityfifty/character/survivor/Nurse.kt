package tororo1066.identityfifty.character.survivor

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.data.SurvivorData
import tororo1066.identityfifty.enumClass.AllowAction
import tororo1066.nmsutils.items.GlowColor
import tororo1066.tororopluginapi.lang.SLang.Companion.sendTranslateMsg
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sItem.SItem
import tororo1066.tororopluginapi.utils.toPlayer

class Nurse : AbstractSurvivor("nurse") {

    private val glowTasks = ArrayList<Int>()
    private var selfHealCooldown = 0

    override fun onStart(p: Player) {
        super.onStart(p)
        val passiveItem = SItem(Material.STICK).setDisplayName(translate("passive")).setCustomModelData(8)
            .addLore(translate("nurse_passive_lore_1"))
            .addLore(translate("nurse_passive_lore_2"))

        val speedUpItem = SItem(Material.STICK).setDisplayName(translate("syringe")).setCustomModelData(2)
            .addLore(translate("syringe_lore_1"))
            .addLore(translate("syringe_lore_2"))
        val speedUpSkillItem = IdentityFifty.interactManager.createSInteractItem(speedUpItem,true).setInteractEvent { _, _ ->
            val data = IdentityFifty.survivors[p.uniqueId]!!
            if (data.getHealth() <= 2){
                p.sendTranslateMsg("nurse_syringe_cant_use")
                return@setInteractEvent false
            }
            p.world.playSound(p.location,Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE,1f,1f)
            data.setHealth(data.getHealth() - 1)
            p.addPotionEffect(PotionEffect(PotionEffectType.SPEED,80,1))
            IdentityFifty.broadcastSpectators(translate("spec_syringe_used",p.name),AllowAction.RECEIVE_SURVIVORS_ACTION)
            return@setInteractEvent true
        }.setInitialCoolDown(1000)

        tasks.add(Bukkit.getScheduler().runTaskTimer(IdentityFifty.plugin, Runnable {
            if (selfHealCooldown > 0) selfHealCooldown--
        },0,20))

        p.inventory.addItem(passiveItem, speedUpSkillItem)
    }

    override fun onTryHeal(healPlayer: Player, p: Player): Boolean {
        val data = IdentityFifty.survivors[p.uniqueId]!!
        val hunters = IdentityFifty.hunters.values.mapNotNull { it.uuid.toPlayer() }
        if (healPlayer == p){
            if (selfHealCooldown > 0){
                return false
            }
            glowTasks.addAll(data.glowManager.glow(ArrayList(hunters), GlowColor.GREEN,1000))
            hunters.forEach { it.sendTranslateMsg("nurse_healing") }
        }
        return true
    }

    override fun onHeal(isCancelled: Boolean, heal: Int, healedPlayer: Player, p: Player) {
        val data = IdentityFifty.survivors[p.uniqueId]!!
        if (healedPlayer == p){
            glowTasks.forEach { data.glowManager.cancelTask(it) }
            glowTasks.clear()
            if (!isCancelled) selfHealCooldown = 70
        }
    }

    override fun parameters(data: SurvivorData): SurvivorData {
        data.survivorClass = this
        data.healTick = 140
        data.canHealSelf = true
        return data
    }

    override fun scoreboards(p: Player): ArrayList<Pair<Int, String>> {
        return if (selfHealCooldown > 0){
            arrayListOf(Pair(-1, translate("nurse_scoreboard", selfHealCooldown)))
        } else {
            arrayListOf(Pair(-1, translate("nurse_scoreboard_usable")))
        }
    }

    override fun info(): ArrayList<ItemStack> {
        val passiveItem = SItem(Material.STICK).setDisplayName(translate("passive")).setCustomModelData(8)
            .addLore(translate("nurse_passive_lore_1"))
            .addLore(translate("nurse_passive_lore_2"))
        val speedUpItem = SItem(Material.STICK).setDisplayName(translate("syringe")).setCustomModelData(2)
            .addLore(translate("syringe_lore_1"))
            .addLore(translate("syringe_lore_2"))

        return arrayListOf(passiveItem,speedUpItem)
    }

}
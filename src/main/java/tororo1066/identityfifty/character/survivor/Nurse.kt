package tororo1066.identityfifty.character.survivor

import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.data.SurvivorData
import tororo1066.identityfifty.enumClass.AllowAction
import tororo1066.tororopluginapi.lang.SLang.Companion.sendTranslateMsg
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sItem.SItem
import tororo1066.tororopluginapi.utils.toPlayer
import java.util.UUID

class Nurse : AbstractSurvivor("nurse") {

    var healModifier: UUID? = null

    override fun onStart(p: Player) {
        super.onStart(p)
        val passiveItem = SItem(Material.STICK).setDisplayName(translate("passive")).setCustomModelData(8)
            .addLore(translate("nurse_passive_lore_1"))
            .addLore(translate("nurse_passive_lore_2"))
            .addLore(translate("nurse_passive_lore_3"))
        p.inventory.addItem(passiveItem)
        val speedUpItem = SItem(Material.STICK).setDisplayName(translate("syringe")).setCustomModelData(2)
            .addLore(translate("syringe_lore_1"))
            .addLore(translate("syringe_lore_2"))
        val speedUpSkillItem = IdentityFifty.interactManager.createSInteractItem(speedUpItem,true).setInteractEvent { e, item ->
            val data = IdentityFifty.survivors[p.uniqueId]!!
            if (data.getHealth() <= 2){
                p.sendTranslateMsg("nurse_syringe_cant_use")
                return@setInteractEvent false
            }
            p.world.playSound(p.location,Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE,1f,1f)
            data.setHealth(data.getHealth() - 1)
            p.addPotionEffect(PotionEffect(PotionEffectType.SPEED,160,0))
            IdentityFifty.broadcastSpectators(translate("spec_syringe_used",p.name),AllowAction.RECEIVE_SURVIVORS_ACTION)
            return@setInteractEvent true
        }.setInitialCoolDown(1000)
        p.inventory.addItem(speedUpSkillItem)
    }

    override fun onTryHeal(healPlayer: Player, p: Player): Boolean {
        val data = IdentityFifty.survivors[p.uniqueId]!!
        healModifier?.let { data.healTickModify.remove(it) }
        if (healPlayer == p){
            healModifier = UUID.randomUUID()
            data.healTickModify[healModifier!!] = 2.0
        }
        return true
    }

    override fun parameters(data: SurvivorData): SurvivorData {
        data.survivorClass = this
        data.healTick = 140
        data.healSmallHealth = true
        data.canHealSelf = true
        return data
    }

    override fun info(): ArrayList<ItemStack> {
        val passiveItem = SItem(Material.STICK).setDisplayName(translate("passive")).setCustomModelData(8)
            .addLore(translate("nurse_passive_lore_1"))
            .addLore(translate("nurse_passive_lore_2"))
            .addLore(translate("nurse_passive_lore_3"))
        val speedUpItem = SItem(Material.STICK).setDisplayName(translate("syringe")).setCustomModelData(2)
            .addLore(translate("syringe_lore_1"))
            .addLore(translate("syringe_lore_2"))

        return arrayListOf(passiveItem,speedUpItem)
    }

}
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
import tororo1066.tororopluginapi.lang.SLang.Companion.sendTranslateMsg
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sItem.SItem
import tororo1066.tororopluginapi.utils.addItem
import java.util.UUID

class Nurse : AbstractSurvivor("nurse") {

    private var healModifyUUID = UUID.randomUUID()
    private var selfHealCooldown = 0

    override fun onStart(p: Player) {
        super.onStart(p)
        val passiveItem = SItem(Material.STICK).setDisplayName(translate("passive")).setCustomModelData(8)
            .addLore(translate("nurse_passive_lore_1"))
            .addLore(translate("nurse_passive_lore_2"))

        val speedUpItem = SItem(Material.STICK).setDisplayName(translate("syringe")).setCustomModelData(2)
            .addLore(translate("syringe_lore_1"))
            .addLore(translate("syringe_lore_2"))
        val speedUpSkillItem = IdentityFifty.interactManager.createSInteractItem(speedUpItem,true).setInteractEvent { e, _ ->
            val player = e.player
            val data = IdentityFifty.survivors[player.uniqueId]!!
            if (data.getHealth() <= 2){
                player.sendTranslateMsg("nurse_syringe_cant_use")
                return@setInteractEvent false
            }
            player.world.playSound(player.location,Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE,1f,1f)
            data.setHealth(data.getHealth() - 1)
            player.addPotionEffect(PotionEffect(PotionEffectType.SPEED,80,1))
            IdentityFifty.broadcastSpectators(translate("spec_syringe_used",player.name),AllowAction.RECEIVE_SURVIVORS_ACTION)
            return@setInteractEvent true
        }.setInitialCoolDown(1000)

        tasks.add(Bukkit.getScheduler().runTaskTimer(IdentityFifty.plugin, Runnable {
            if (selfHealCooldown > 0) selfHealCooldown--
        },0,20))

        p.inventory.addItem(passiveItem, speedUpSkillItem)
    }

    override fun onTryHeal(healPlayer: Player, p: Player): Boolean {
        val data = IdentityFifty.survivors[p.uniqueId]!!
        if (healPlayer == p){
            if (selfHealCooldown > 0){
                return false
            }
            data.healTickModify += healModifyUUID to 3.0
        }
        return true
    }

    override fun onHeal(isCancelled: Boolean, heal: Int, healedPlayer: Player, p: Player) {
        val data = IdentityFifty.survivors[p.uniqueId]!!
        if (healedPlayer == p){
            data.healTickModify -= healModifyUUID
            selfHealCooldown = 70
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
            .build()
        val speedUpItem = SItem(Material.STICK).setDisplayName(translate("syringe")).setCustomModelData(2)
            .addLore(translate("syringe_lore_1"))
            .addLore(translate("syringe_lore_2"))
            .build()

        return arrayListOf(passiveItem,speedUpItem)
    }

}
package tororo1066.identityfifty.character.survivor

import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.data.SurvivorData
import tororo1066.identityfifty.enumClass.AllowAction
import tororo1066.identityfifty.enumClass.StunState
import tororo1066.tororopluginapi.lang.SLang.Companion.sendTranslateMsg
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sItem.SItem

class RunAway : AbstractSurvivor("runaway") {

    private val blindSkillRadius = 12.0
    private val blindSkillStun = 20
    private val blindSkillBlind = 140
    private val blindSkillCoolDown = 1200

    private val footprintsModify = 0.75

    private val passiveItem = SItem(Material.STICK).setDisplayName(translate("passive")).setCustomModelData(8)
        .addLore(translate("runaway_passive_lore_1"))
        .addLore(translate("runaway_passive_lore_2"))
        .addLore(translate("runaway_passive_lore_3"))
    private val blindSkillItem = SItem(Material.STICK).setDisplayName(translate("camouflage")).setCustomModelData(6)
        .addLore(translate("camouflage_lore_1"))
        .addLore(translate("camouflage_lore_2", blindSkillCoolDown / 20))


    override fun onStart(p: Player) {
        super.onStart(p)
        val blindSkill = IdentityFifty.interactManager.createSInteractItem(blindSkillItem,true).setInteractEvent { e, _ ->
            val player = e.player
            val entities = player.location.getNearbyPlayers(blindSkillRadius)
            if (entities.isEmpty()){
                IdentityFifty.broadcastSpectators(translate("spec_camouflage_miss",player.name), AllowAction.RECEIVE_SURVIVORS_ACTION)
                player.sendTranslateMsg("camouflage_miss")
                return@setInteractEvent true
            }
            player.world.playSound(player.location, Sound.ENTITY_COW_DEATH,1f,2f)
            entities.forEach {
                if (!IdentityFifty.hunters.containsKey(it.uniqueId))return@forEach
                IdentityFifty.stunEffect(it,0,blindSkillStun,StunState.OTHER)
                it.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS,blindSkillBlind,3,false,false,true))
                it.isSprinting = false
                it.sendTranslateMsg("camouflage_hit_hunter")
                player.sendTranslateMsg("camouflage_hit_survivor",it.name)
                IdentityFifty.broadcastSpectators(translate("spec_camouflage_hit",player.name,it.name), AllowAction.RECEIVE_SURVIVORS_ACTION)
            }
            return@setInteractEvent true
        }.setInitialCoolDown(blindSkillCoolDown)
        p.inventory.addItem(passiveItem)
        p.inventory.addItem(blindSkill)
    }

    override fun parameters(data: SurvivorData): SurvivorData {
        data.survivorClass = this
        data.footprintsTime *= footprintsModify
        return data
    }

    override fun onGotHelp(helper: Player, p: Player): ReturnAction {
        IdentityFifty.speedModifier(p, 0.4, 100, AttributeModifier.Operation.MULTIPLY_SCALAR_1)
        return super.onGotHelp(helper, p)
    }

    override fun onDamage(damage: Int, toHealth: Int, damager: Player, p: Player): Pair<Boolean, Int> {
        IdentityFifty.speedModifier(p, 0.4, 100, AttributeModifier.Operation.MULTIPLY_SCALAR_1)
        return Pair(true,damage)
    }

    override fun info(): ArrayList<ItemStack> {
        return arrayListOf(passiveItem,blindSkillItem)
    }

}
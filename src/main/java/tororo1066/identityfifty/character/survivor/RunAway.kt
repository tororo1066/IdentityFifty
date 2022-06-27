package tororo1066.identityfifty.character.survivor

import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.IdentityFifty.Companion.prefixMsg
import tororo1066.identityfifty.data.SurvivorData
import tororo1066.tororopluginapi.lang.SLang.Companion.sendTranslateMsg
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sItem.SItem

class RunAway : AbstractSurvivor("runaway") {
    override fun onStart(p: Player) {
        val passiveItem = SItem(Material.STICK).setDisplayName(p.translate("passive"))
            .addLore(p.translate("runaway_passive_lore_1"))
            .addLore(p.translate("runaway_passive_lore_2"))
        val blindSkillItem = SItem(Material.STICK).setDisplayName(p.translate("camouflage")).setCustomModelData(6)
            .addLore(p.translate("camouflage_lore_1"))
            .addLore(p.translate("camouflage_lore_2"))
        val blindSkill = IdentityFifty.interactManager.createSInteractItem(blindSkillItem,true).setInteractEvent { _, _ ->
            p.location.getNearbyPlayers(15.0).forEach {
                if (!IdentityFifty.hunters.containsKey(it.uniqueId))return@forEach
                it.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS,140,1,false,false,true))
                it.sendTranslateMsg("camouflage_hit_hunter")
                p.sendTranslateMsg("camouflage_hit_survivor",it.name)
                it.playSound(it.location, Sound.ENTITY_COW_DEATH,1f,1f)
            }
        }.setInitialCoolDown(1200)
        p.inventory.addItem(passiveItem)
        p.inventory.addItem(blindSkill)
    }

    override fun parameters(data: SurvivorData): SurvivorData {
        data.survivorClass = this
        return data
    }

    override fun onGotHelp(helper: Player, p: Player) {
        p.addPotionEffect(PotionEffect(PotionEffectType.SPEED,100,1))
    }

    override fun onDamage(toHealth: Int, damager: Player, p: Player): Boolean {
        p.addPotionEffect(PotionEffect(PotionEffectType.SPEED,100,1))
        return true
    }

}
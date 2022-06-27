package tororo1066.identityfifty.character.hunter

import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.data.HunterData
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sItem.SItem

class Dasher : AbstractHunter("dasher") {

    override fun onStart(p: Player) {
        val passiveItem = SItem(Material.STICK).setDisplayName(p.translate("hunter_passive")).setCustomModelData(1)
            .addLore(p.translate("dasher_passive_lore_1"))
        val firstSkillItem = SItem(Material.STICK).setDisplayName(p.translate("hyper_engine")).setCustomModelData(4)
            .addLore(p.translate("hyper_engine_lore_1"))
            .addLore(p.translate("hyper_engine_lore_2"))
        val firstSkill = IdentityFifty.interactManager.createSInteractItem(firstSkillItem,true).setInteractEvent { _, _ ->
            p.addPotionEffect(PotionEffect(PotionEffectType.SPEED,60,1))
            p.playSound(p.location, Sound.ENTITY_WITHER_SHOOT,1f,1.5f)
        }.setInitialCoolDown(800)

        p.inventory.setItem(0,passiveItem)
        p.inventory.setItem(1,firstSkill)
    }

    override fun parameters(data: HunterData): HunterData {
        data.hunterClass = this
        return data
    }


    override fun onSurvivorHelp(helper: Player, gotHelpPlayer: Player, p: Player) {
        p.addPotionEffect(PotionEffect(PotionEffectType.SPEED,100,1,true,false,true))
        p.playSound(p.location, Sound.ENTITY_WITHER_SHOOT,1f,1.5f)
    }
}
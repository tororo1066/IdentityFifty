package tororo1066.identityfifty.character.hunter

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.data.HunterData
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sItem.SItem

class Dasher : AbstractHunter("dasher") {

    override fun onStart(p: Player) {
        val passiveItem = SItem(Material.STICK).setDisplayName(translate("hunter_passive")).setCustomModelData(1)
            .addLore(translate("dasher_passive_lore_1"))
            .addLore(translate("dasher_passive_lore_2"))
        val firstSkillItem = SItem(Material.STICK).setDisplayName(translate("hyper_engine")).setCustomModelData(4)
            .addLore(translate("hyper_engine_lore_1"))
            .addLore(translate("hyper_engine_lore_2"))
        val firstSkill = IdentityFifty.interactManager.createSInteractItem(firstSkillItem,true).setInteractEvent { _, _ ->
            p.addPotionEffect(PotionEffect(PotionEffectType.SPEED,80,1))
            p.playSound(p.location, Sound.ENTITY_WITHER_SHOOT,1f,1.5f)
            return@setInteractEvent true
        }.setInitialCoolDown(800)

        tasks.add(Bukkit.getScheduler().runTaskTimer(IdentityFifty.plugin, Runnable {
            val specPlayer = p.getTargetEntity(100)
            if (specPlayer != null){
                if (IdentityFifty.survivors.containsKey(specPlayer.uniqueId)){
                    p.walkSpeed = 0.29f
                    return@Runnable
                }
            }
            p.walkSpeed = 0.28f
        },0,2))

        p.inventory.addItem(passiveItem)
        p.inventory.addItem(firstSkill)
    }

    override fun parameters(data: HunterData): HunterData {
        data.hunterClass = this
        return data
    }


    override fun onSurvivorHelp(helper: Player, gotHelpPlayer: Player, p: Player) {
        p.addPotionEffect(PotionEffect(PotionEffectType.SPEED,100,1,true,false,true))
        p.playSound(p.location, Sound.ENTITY_WITHER_SHOOT,1f,1.5f)
    }

    override fun info(): ArrayList<ItemStack> {
        val passiveItem = SItem(Material.STICK).setDisplayName(translate("hunter_passive")).setCustomModelData(1)
            .addLore(translate("dasher_passive_lore_1"))
            .addLore(translate("dasher_passive_lore_2"))
        val firstSkillItem = SItem(Material.STICK).setDisplayName(translate("hyper_engine")).setCustomModelData(4)
            .addLore(translate("hyper_engine_lore_1"))
            .addLore(translate("hyper_engine_lore_2"))
        return arrayListOf(passiveItem,firstSkillItem)
    }
}
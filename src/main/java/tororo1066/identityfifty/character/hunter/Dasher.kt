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
import tororo1066.identityfifty.enumClass.AllowAction
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
            //加速
            p.addPotionEffect(PotionEffect(PotionEffectType.SPEED,80,1))
            p.world.playSound(p.location, Sound.ENTITY_WITHER_SHOOT,1f,1.5f)
            IdentityFifty.broadcastSpectators(translate("spec_hyper_engine_used",p.name),AllowAction.RECEIVE_HUNTERS_ACTION)
            return@setInteractEvent true
        }.setInitialCoolDown(800)

        var viewing = false

        tasks.add(Bukkit.getScheduler().runTaskTimer(IdentityFifty.plugin, Runnable {
            //サバイバーを見ていると加速
            val viewPlayer = p.getTargetEntity(100)
            if (viewPlayer != null){
                if (IdentityFifty.survivors.containsKey(viewPlayer.uniqueId)){
                    if (!viewing){ //加速の重複を防ぐ
                        p.walkSpeed += 0.01f
                        viewing = true
                    }
                    return@Runnable
                }
            }
            if (viewing){ //加速の解除
                p.walkSpeed -= 0.01f
                viewing = false
            }
        },0,2))

        p.inventory.addItem(passiveItem, firstSkill)
    }

    override fun parameters(data: HunterData): HunterData {
        data.hunterClass = this
        return data
    }


    //サバイバーの救助時に加速
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
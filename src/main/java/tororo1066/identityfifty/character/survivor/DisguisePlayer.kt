package tororo1066.identityfifty.character.survivor

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.data.SurvivorData
import tororo1066.tororopluginapi.lang.SLang.Companion.sendTranslateMsg
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sItem.SItem

class DisguisePlayer: AbstractSurvivor("disguise") {

    override fun onStart(p: Player) {
        super.onStart(p)
        val passiveItem = SItem(Material.STICK).setDisplayName(translate("passive")).setCustomModelData(8)
            .addLore(translate("disguise_passive_lore_1"))
            .addLore(translate("disguise_passive_lore_2"))

        val disguiseSkill = SItem(Material.STICK).setDisplayName(translate("disguise_skill")).setCustomModelData(10)
            .addLore(translate("disguise_skill_lore_1"))
            .addLore(translate("disguise_skill_lore_2"))
            .addLore(translate("disguise_skill_lore_3"))

        val disguiseSkillItem = IdentityFifty.interactManager.createSInteractItem(disguiseSkill,true).setInteractEvent { _, _ ->
            val target = p.getTargetEntity(4)?:return@setInteractEvent false
            if (target !is Player)return@setInteractEvent false
            if (!IdentityFifty.survivors.containsKey(target.uniqueId))return@setInteractEvent false
            val data = IdentityFifty.survivors[p.uniqueId]!!
            data.skinModifier.disguise(target)
            p.sendTranslateMsg("disguise_skill_start",target.name)
            Bukkit.getScheduler().runTaskLater(IdentityFifty.plugin, Runnable {
                if (data.skinModifier.isDisguise()){
                    data.skinModifier.unDisguise()
                    p.sendTranslateMsg("disguise_skill_end")
                }
            },1400)
            return@setInteractEvent true
        }.setInitialCoolDown(2200)

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
            damager.addPotionEffect(PotionEffect(PotionEffectType.SLOW,20,4,false,false,true))
            damager.addPotionEffect(PotionEffect(PotionEffectType.WEAKNESS,20,100,false,false,true))
            p.sendTranslateMsg("disguise_skill_end")
            return Pair(false,0)
        }
        return super.onDamage(damage, toHealth, damager, p)
    }

    override fun onHelp(helpedPlayer: Player, p: Player) {
        helpedPlayer.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY,60,1,true,false))
    }
}
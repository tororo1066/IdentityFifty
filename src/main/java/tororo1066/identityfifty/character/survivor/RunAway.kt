package tororo1066.identityfifty.character.survivor

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
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
        super.onStart(p)
        val passiveItem = SItem(Material.STICK).setDisplayName(translate("passive")).setCustomModelData(8)
            .addLore(translate("runaway_passive_lore_1"))
            .addLore(translate("runaway_passive_lore_2"))
            .addLore(translate("runaway_passive_lore_3"))
        val blindSkillItem = SItem(Material.STICK).setDisplayName(translate("camouflage")).setCustomModelData(6)
            .addLore(translate("camouflage_lore_1"))
            .addLore(translate("camouflage_lore_2"))
        val blindSkill = IdentityFifty.interactManager.createSInteractItem(blindSkillItem,true).setInteractEvent { _, _ ->
            val entities = p.location.getNearbyPlayers(12.0)
            if (entities.isEmpty()){
                p.sendTranslateMsg("camouflage_miss")
                return@setInteractEvent true
            }
            p.playSound(p.location, Sound.ENTITY_COW_DEATH,1f,1f)
            entities.forEach {
                if (!IdentityFifty.hunters.containsKey(it.uniqueId))return@forEach
                IdentityFifty.stunEffect(it,0,20)
                it.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS,140,3,false,false,true))
                it.sendTranslateMsg("camouflage_hit_hunter")
                p.sendTranslateMsg("camouflage_hit_survivor",it.name)
                it.playSound(it.location, Sound.ENTITY_COW_DEATH,1f,1f)
            }
            return@setInteractEvent true
        }.setInitialCoolDown(1200)
        p.inventory.addItem(passiveItem)
        p.inventory.addItem(blindSkill)
    }

    override fun parameters(data: SurvivorData): SurvivorData {
        data.survivorClass = this
        data.footprintsTime = 1.5
        return data
    }

    override fun onGotHelp(helper: Player, p: Player) {
        p.addPotionEffect(PotionEffect(PotionEffectType.SPEED,100,1))
    }

    override fun onDamage(damage: Int, toHealth: Int, damager: Player, p: Player): Pair<Boolean, Int> {
        p.addPotionEffect(PotionEffect(PotionEffectType.SPEED,100,1))
        return Pair(true,damage)
    }

    override fun info(): ArrayList<ItemStack> {
        val passiveItem = SItem(Material.STICK).setDisplayName(translate("passive")).setCustomModelData(8)
            .addLore(translate("runaway_passive_lore_1"))
            .addLore(translate("runaway_passive_lore_2"))
            .addLore(translate("runaway_passive_lore_3"))
        val blindSkillItem = SItem(Material.STICK).setDisplayName(translate("camouflage")).setCustomModelData(6)
            .addLore(translate("camouflage_lore_1"))
            .addLore(translate("camouflage_lore_2"))
        return arrayListOf(passiveItem,blindSkillItem)
    }

}
package tororo1066.identityfifty.talent.hunter

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Cow
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import tororo1066.identityfifty.IdentityFifty
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sItem.SItem

class SkillDelayGenerator: AbstractHunterTalent("skill_delay_generator", 5, TalentPlane::class.java) {

    override fun lore(): List<String> {
        return listOf()
    }

    override fun onStart(p: Player) {
        val delayGeneratorSkill = SItem(Material.STICK).setDisplayName(translate("skill_delay_generator")).setCustomModelData(999)
            .addLore(translate("skill_delay_generator_lore_1"))
            .addLore(translate("skill_delay_generator_lore_2"))
            .addLore(translate("skill_delay_generator_lore_3"))

        val delayGeneratorSkillItem = IdentityFifty.interactManager.createSInteractItem(delayGeneratorSkill,true).setInteractEvent { _, _ ->

            val targetEntity = p.getTargetEntity(4)?:return@setInteractEvent false
            if (!targetEntity.persistentDataContainer.has(NamespacedKey(IdentityFifty.plugin,"Generator"),
                    PersistentDataType.INTEGER))return@setInteractEvent false

            p.playSound(p.location, Sound.BLOCK_ANVIL_USE,1f,2f)
            targetEntity as Cow
            if (targetEntity.health + 500 > targetEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.baseValue){
                targetEntity.health = targetEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.baseValue
            } else {
                targetEntity.health += 500
            }
            targetEntity.customName = "§f§l羊型発電機§5(§e${targetEntity.health.toInt()}§f/§b${targetEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.baseValue.toInt()}§5)"
            return@setInteractEvent true
        }

        p.inventory.addItem(delayGeneratorSkillItem)
    }
}
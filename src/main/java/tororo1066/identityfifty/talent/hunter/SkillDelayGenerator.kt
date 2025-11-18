package tororo1066.identityfifty.talent.hunter

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.entity.Sheep
import org.bukkit.persistence.PersistentDataType
import tororo1066.identityfifty.IdentityFifty
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sItem.SItem
import tororo1066.tororopluginapi.utils.addItem

class SkillDelayGenerator: AbstractHunterTalent("skill_delay_generator", 5, EndGameSpeedUp::class.java) {

    override fun lore(): List<String> {
        return listOf("skill_delay_generator_lore_1","skill_delay_generator_lore_2")
    }

    override fun onStart(p: Player) {
        val delayGeneratorSkill = SItem(Material.STICK).setDisplayName(translate("skill_delay_generator")).setCustomModelData(20)
            .addLore(translate("skill_delay_generator_lore_1"))
            .addLore(translate("skill_delay_generator_lore_2"))

        val delayGeneratorSkillItem = IdentityFifty.createSInteractItem(delayGeneratorSkill).setInteractEvent { _, _ ->

            val targetEntity = p.getTargetEntity(4)?:return@setInteractEvent false
            if (!targetEntity.persistentDataContainer.has(NamespacedKey(IdentityFifty.plugin,"Generator"),
                    PersistentDataType.INTEGER))return@setInteractEvent false

            p.playSound(p.location, Sound.BLOCK_ANVIL_USE,0.8f,2f)
            targetEntity as Sheep
            val maxHealth = targetEntity.getAttribute(Attribute.MAX_HEALTH)!!.baseValue
            if (targetEntity.health + 300 > maxHealth){
                targetEntity.health = maxHealth
            } else {
                targetEntity.health += 300
            }
            targetEntity.customName(
                Component.text(
                translate("sheep_generator", targetEntity.health.toInt(), maxHealth.toInt())
            ))
            return@setInteractEvent true
        }.setInitialCoolDown(1800)

        p.inventory.addItem(delayGeneratorSkillItem)
    }
}
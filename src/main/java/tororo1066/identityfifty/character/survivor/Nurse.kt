package tororo1066.identityfifty.character.survivor

import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.data.SurvivorData
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sItem.SItem
import tororo1066.tororopluginapi.utils.toPlayer

class Nurse : AbstractSurvivor("nurse") {

    override fun onStart(p: Player) {
        super.onStart(p)
        val passiveItem = SItem(Material.STICK).setDisplayName(translate("passive")).setCustomModelData(8)
            .addLore(translate("nurse_passive_lore_1"))
        p.inventory.addItem(passiveItem)
        val healItem = SItem(Material.STICK).setDisplayName(translate("syringe")).setCustomModelData(2)
            .addLore(translate("syringe_lore"))
        for (i in 1..2){
            val healSkillItem = IdentityFifty.interactManager.createSInteractItem(healItem,true).setInteractEvent { e, item ->
                e.item!!.amount -= 1
                item.delete()
                val data = IdentityFifty.survivors[p.uniqueId]!!
                data.setHealth(data.getHealth() + 2)
                IdentityFifty.hunters.values.forEach {
                    val hunterP = it.uuid.toPlayer()?:return@forEach
                    it.hunterClass.onSurvivorHeal(p,p,hunterP)
                    it.talentClasses.values.forEach { clazz ->
                        clazz.onSurvivorHeal(p,p,hunterP)
                    }
                }
                p.world.playSound(p.location,Sound.ENTITY_PLAYER_LEVELUP,1f,1f)
                return@setInteractEvent true
            }
            p.inventory.addItem(healSkillItem)
        }
    }

    override fun parameters(data: SurvivorData): SurvivorData {
        data.survivorClass = this
        data.healTick = 100
        return data
    }

    override fun info(): ArrayList<ItemStack> {
        val passiveItem = SItem(Material.STICK).setDisplayName(translate("passive")).setCustomModelData(8)
            .addLore(translate("nurse_passive_lore_1"))
        val healItem = SItem(Material.STICK).setDisplayName(translate("syringe")).setCustomModelData(2)
            .addLore(translate("syringe_lore"))
        val healItem2 = SItem(Material.STICK).setDisplayName(translate("syringe")).setCustomModelData(2)
            .addLore(translate("syringe_lore")).setCustomData(IdentityFifty.plugin,"aaa", PersistentDataType.INTEGER,1)

        return arrayListOf(passiveItem,healItem,healItem2)
    }

}
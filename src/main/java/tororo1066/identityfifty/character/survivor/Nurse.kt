package tororo1066.identityfifty.character.survivor

import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.data.SurvivorData
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sItem.SItem

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

}
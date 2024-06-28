package tororo1066.identityfifty.inventory

import org.bukkit.Material
import org.bukkit.entity.Player
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.character.survivor.AbstractSurvivor
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sInventory.SInventory

class SurvivorInfoInv(val data: AbstractSurvivor): SInventory(IdentityFifty.plugin,translate(data.name),1) {

    override fun renderMenu(p: Player): Boolean {
        setOnClick {
            it.isCancelled = true
        }
        setItem(0,Material.IRON_SWORD)
        data.info().forEachIndexed { index, itemStack ->
            setItem(index+1,itemStack)
        }
        p.sendMessage(data.description())
        return true
    }
}
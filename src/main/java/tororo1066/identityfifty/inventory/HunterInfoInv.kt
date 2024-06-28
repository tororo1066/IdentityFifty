package tororo1066.identityfifty.inventory

import org.bukkit.entity.Player
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.character.hunter.AbstractHunter
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sInventory.SInventory

class HunterInfoInv(val data: AbstractHunter): SInventory(IdentityFifty.plugin,translate(data.name),1) {

    override fun renderMenu(p: Player): Boolean {
        setOnClick {
            it.isCancelled = true
        }
        data.info().forEachIndexed { index, itemStack ->
            setItem(index,itemStack)
        }
        p.sendMessage(data.description())
        return true
    }
}
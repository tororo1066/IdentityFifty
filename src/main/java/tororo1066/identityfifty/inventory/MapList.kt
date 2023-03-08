package tororo1066.identityfifty.inventory

import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import tororo1066.identityfifty.IdentityFifty
import tororo1066.tororopluginapi.defaultMenus.LargeSInventory
import tororo1066.tororopluginapi.sInventory.SInventory
import tororo1066.tororopluginapi.sInventory.SInventoryItem
import tororo1066.tororopluginapi.sItem.SItem

class MapList : LargeSInventory(IdentityFifty.plugin,"§e§lマップ一覧") {

    override fun renderMenu(p: Player): Boolean {
        setOnClick { e ->
            (e.whoClicked as Player).playSound(e.whoClicked.location,Sound.UI_BUTTON_CLICK,1f,1f)
        }
        val items = ArrayList<SInventoryItem>()
        IdentityFifty.maps.forEach {
            val item = SItem(Material.REDSTONE_BLOCK).setDisplayName(it.key).toSInventoryItem()
            item.setCanClick(false).setClickEvent { e ->
                moveChildInventory(MapConfigInv(it.key,it.value),e.whoClicked as Player)
            }
            items.add(item)
        }
        setResourceItems(items)
        return true
    }
}
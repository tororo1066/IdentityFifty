package tororo1066.identityfifty.inventory

import org.bukkit.Material
import org.bukkit.entity.Player
import tororo1066.identityfifty.IdentityFifty
import tororo1066.tororopluginapi.defaultMenus.LargeSInventory
import tororo1066.tororopluginapi.sInventory.SInventory
import tororo1066.tororopluginapi.sInventory.SInventoryItem
import tororo1066.tororopluginapi.sItem.SItem

class MapList : LargeSInventory(IdentityFifty.plugin,"§e§lマップ一覧") {

    override fun renderMenu(): Boolean {
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
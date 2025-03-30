package tororo1066.identityfifty.talent.survivor

import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import tororo1066.identityfifty.IdentityFifty
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sItem.SItem
import tororo1066.tororopluginapi.utils.addItem

class HelpHeal :AbstractSurvivorTalent("help_heal",5,WoundedGeneratorUp::class.java) {
    override fun lore(): List<String> {
        return listOf("help_heal_lore_1")
    }

    var healPrivilegeCount = 0

    override fun onStart(p: Player) {
        val helpHeal = SItem(Material.STICK).setDisplayName(translate("help_heal")).setCustomModelData(2333)
                .addLore("help_heal_lore_1")

        val helpHealItem = IdentityFifty.interactManager.createSInteractItem(helpHeal, true).setInteractEvent{ _, item ->
            p.playSound(p.location, Sound.BLOCK_BEACON_ACTIVATE,1f,1.25f)
            healPrivilegeCount += 1
            item.itemStack.amount = 0
            item.delete()
            return@setInteractEvent true
        }
        p.inventory.addItem(helpHealItem)
    }

    override fun onHelp(helpedPlayer: Player, p: Player) {
        if (healPrivilegeCount > 0){
            val data = IdentityFifty.survivors[helpedPlayer.uniqueId]!!
            data.setHealth(data.getHealth() + 2)

            healPrivilegeCount--
        }

    }


}
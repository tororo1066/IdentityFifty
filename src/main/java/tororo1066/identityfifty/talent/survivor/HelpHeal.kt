package tororo1066.identityfifty.talent.survivor

import org.bukkit.entity.Player
import tororo1066.identityfifty.IdentityFifty

class HelpHeal :AbstractSurvivorTalent("help_heal",5,WoundedGeneratorUp::class.java) {
    override fun lore(): List<String> {
        return listOf("help_heal_lore_1")
    }

    var healPrivilegeCount = 2

    override fun onHelp(helpedPlayer: Player, p: Player) {
        if (healPrivilegeCount > 0){
            val data = IdentityFifty.survivors[helpedPlayer.uniqueId]!!
            data.setHealth(data.getHealth() + 1)

            healPrivilegeCount--
        }

    }


}
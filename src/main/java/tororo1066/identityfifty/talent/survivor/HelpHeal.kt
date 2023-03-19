package tororo1066.identityfifty.talent.survivor

import org.bukkit.entity.Player
import tororo1066.identityfifty.IdentityFifty

class HelpHeal :AbstractSurvivorTalent("help_heal",4,TalentPlane::class.java) {
    override fun lore(): List<String> {
        return listOf()
    }

    var healprivilege = true

    override fun onHelp(helpedPlayer: Player, p: Player) {
        if (healprivilege){
            val data = IdentityFifty.survivors[helpedPlayer.uniqueId]!!
            data.setHealth(data.getHealth() + 1)

            healprivilege = false
        }

    }


}
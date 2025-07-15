package tororo1066.identityfifty.data

import org.bukkit.entity.Player
import tororo1066.identityfifty.character.hunter.AbstractHunter
import tororo1066.identityfifty.talent.hunter.AbstractHunterTalent

class HunterData(player: Player) : PlayerData(player) {
    lateinit var hunterClass: AbstractHunter
    val talentClasses = HashMap<Class<out AbstractHunterTalent>,AbstractHunterTalent>()
    var talentCost = TALENT_COST
    var disableSwingSlow = false

    companion object {
        const val TALENT_COST = 10
    }
}
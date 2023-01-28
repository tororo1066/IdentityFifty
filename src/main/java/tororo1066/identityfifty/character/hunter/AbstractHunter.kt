package tororo1066.identityfifty.character.hunter

import org.bukkit.Location
import org.bukkit.entity.Player
import tororo1066.identityfifty.data.HunterData

abstract class AbstractHunter(val name: String) {

    open fun onStart(p: Player) {}

    abstract fun parameters(data: HunterData): HunterData

    open fun onFinishedGenerator(dieLocation: Location, remainingGenerator: Int, p: Player) {}

    open fun onFinishedEscapeGenerator(dieLocation: Location, p: Player) {}

    open fun onAttack(attackPlayer: Player, p: Player, isFinishedGenerator: Boolean): Int {
        return if (isFinishedGenerator) 4 else 2
    }

    open fun onSurvivorHelp(helper: Player, gotHelpPlayer: Player, p: Player) {}

    open fun onDamagedWoodPlate(usedPlayer: Player, loc: Location, p: Player): Boolean {
        return true
    }

}
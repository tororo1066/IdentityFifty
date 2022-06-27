package tororo1066.identityfifty.character.survivor

import org.bukkit.Location
import org.bukkit.entity.Player
import tororo1066.identityfifty.data.SurvivorData

abstract class AbstractSurvivor(val name: String) {

    abstract fun onStart(p: Player)

    abstract fun parameters(data: SurvivorData): SurvivorData

    open fun onFinishedGenerator(dieLocation: Location, remainingGenerator: Int, p: Player) {}

    open fun onFinishedEscapeGenerator(dieLocation: Location, p: Player) {}

    open fun onDamage(toHealth: Int, damager: Player, p: Player): Boolean {
        return true
    }

    open fun onHelp(helpedPlayer: Player, p: Player) {}

    open fun onGotHelp(helper: Player, p: Player) {}

    open fun onDie(p: Player) {}

    open fun onDieOtherSurvivor(diePlayer: Player, playerNumber: Int,p: Player) {}

    open fun sheepGeneratorModify(damage: Double, remainingGenerator: Int, maxHealth: Double, nowHealth: Double, p: Player): Double {
        return damage
    }

    open fun cowGeneratorModify(damage: Double, maxHealth: Double, nowHealth: Double, p: Player): Double {
        return damage
    }
}
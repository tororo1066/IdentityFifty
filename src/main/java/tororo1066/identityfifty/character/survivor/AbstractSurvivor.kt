package tororo1066.identityfifty.character.survivor

import org.bukkit.Location
import org.bukkit.entity.Player
import tororo1066.identityfifty.data.SurvivorData

abstract class AbstractSurvivor(val name: String) {

    abstract fun onStart(p: Player)

    abstract fun parameters(data: SurvivorData): SurvivorData

    open fun onFinishedGenerator(dieLocation: Location, p: Player) {}

    open fun onFinishedEscapeGenerator(dieLocation: Location, p: Player) {}

    open fun onDamage(toHealth: Int, damager: Player, p: Player): Boolean {
        return true
    }

    open fun onHelp(helper: Player, p: Player) {}
}
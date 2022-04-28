package tororo1066.identityfifty.character.survivor

import org.bukkit.Location
import org.bukkit.entity.Player

abstract class AbstractSurvivor(name: String) {

    abstract fun onStart(p: Player)

    open fun onFinishedGenerator(dieLocation: Location, p: Player) {}
}
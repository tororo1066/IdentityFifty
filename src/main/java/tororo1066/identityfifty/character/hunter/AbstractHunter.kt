package tororo1066.identityfifty.character.hunter

import org.bukkit.Location
import org.bukkit.entity.Player

abstract class AbstractHunter(val name: String) {

    abstract fun onStart(p: Player)

    open fun onFinishedGenerator(dieLocation: Location, p: Player) {}

}
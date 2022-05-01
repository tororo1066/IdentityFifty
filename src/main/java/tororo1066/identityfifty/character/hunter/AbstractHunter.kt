package tororo1066.identityfifty.character.hunter

import org.bukkit.Location
import org.bukkit.entity.Player
import tororo1066.identityfifty.data.HunterData

abstract class AbstractHunter(val name: String) {

    abstract fun onStart(p: Player)

    abstract fun parameters(data: HunterData): HunterData

    open fun onFinishedGenerator(dieLocation: Location, p: Player) {}

    open fun onFinishedEscapeGenerator(dieLocation: Location, p: Player) {}

    open fun onAttack(attackPlayer: Player, p: Player): Int {
        return 1
    }

}
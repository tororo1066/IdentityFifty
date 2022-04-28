package tororo1066.identityfifty.character.survivor

import org.bukkit.entity.Player

abstract class AbstractSurvivor(name: String) {

    abstract fun onStart(p: Player)
}
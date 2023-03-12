package tororo1066.identityfifty.talent.hunter

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import tororo1066.identityfifty.data.HunterData
import tororo1066.identityfifty.data.PrisonData

abstract class AbstractHunterTalent(val name: String, val parent: AbstractHunterTalent? = null) {

    val tasks = ArrayList<BukkitTask>()

    open fun onStart(p: Player){}

    open fun parameters(data: HunterData): HunterData {
        return data
    }

    open fun onAttack(attackPlayer: Player, p: Player, noOne: Boolean) {}

    open fun onFinishedGenerator(dieLocation: Location, remainingGenerator: Int, p: Player) {}

    open fun onFinishedEscapeGenerator(dieLocation: Location, p: Player) {}

    open fun onSurvivorJail(survivor: Player, prison: PrisonData, p: Player) {}

    open fun onSurvivorHelp(helper: Player, gotHelpPlayer: Player, p: Player) {}

    open fun onDamagedWoodPlate(usedPlayer: Player, loc: Location, p: Player): Boolean {
        return true
    }

    open fun onEnd(p: Player) {}
}
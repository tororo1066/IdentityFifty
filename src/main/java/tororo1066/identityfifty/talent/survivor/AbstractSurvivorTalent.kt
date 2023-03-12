package tororo1066.identityfifty.talent.survivor

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import tororo1066.identityfifty.data.SurvivorData

abstract class AbstractSurvivorTalent(val name: String,var parent :AbstractSurvivorTalent? = null) {
    val tasks = ArrayList<BukkitTask>()


    open fun onStart(p: Player) {}
    open fun onHitWoodPlate(hittedPlayer: Player, loc: Location, p: Player): Pair<Int,Int> {
        return Pair(140,160)
    }

    open fun parameters(data: SurvivorData): SurvivorData {
        return data
    }
}
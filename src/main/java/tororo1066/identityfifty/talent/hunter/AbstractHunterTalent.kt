package tororo1066.identityfifty.talent.hunter

import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import tororo1066.identityfifty.data.HunterData

abstract class AbstractHunterTalent(val name: String, val parent: AbstractHunterTalent? = null) {

    val tasks = ArrayList<BukkitTask>()

    open fun onStart(p: Player){}

    open fun parameters(data: HunterData): HunterData {
        return data
    }
}
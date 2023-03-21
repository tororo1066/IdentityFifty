package tororo1066.identityfifty.talent.hunter

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import tororo1066.identityfifty.data.HunterData
import tororo1066.identityfifty.data.PrisonData
import tororo1066.tororopluginapi.otherUtils.UsefulUtility

abstract class AbstractHunterTalent(val name: String, val unlockCost: Int, val parent: Class<out AbstractHunterTalent>? = null) {

    val tasks = ArrayList<BukkitTask>()

    abstract fun lore(): List<String>

    open fun onStart(p: Player){}

    open fun parameters(data: HunterData): HunterData {
        return data
    }

    open fun onAttack(attackPlayer: Player, p: Player, noOne: Boolean) {}

    open fun onFinishedGenerator(dieLocation: Location, remainingGenerator: Int, p: Player) {}

    open fun onFinishedEscapeGenerator(dieLocation: Location, p: Player) {}

    open fun onSurvivorHeal(healPlayer: Player, healedPlayer: Player, p: Player) {}

    open fun onSurvivorJail(survivor: Player, prison: PrisonData, p: Player) {}

    open fun onSurvivorHelp(helper: Player, gotHelpPlayer: Player, p: Player) {}

    open fun onDamagedWoodPlate(usedPlayer: Player, loc: Location, p: Player): Boolean {
        return true
    }

    open fun onEnd(p: Player) {}

    companion object{
        fun getTalent(name: String): AbstractHunterTalent? {
            val classStr = name.split("_").joinToString("") { it.replaceFirstChar(Char::titlecase) }
            return UsefulUtility.sTry({
                val clazz = Class.forName("tororo1066.identityfifty.talent.hunter.${classStr}")
                clazz.getConstructor().newInstance() as AbstractHunterTalent
            },{null})
        }
    }
}
package tororo1066.identityfifty.talent.survivor

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import tororo1066.identityfifty.data.PrisonData
import tororo1066.identityfifty.data.SurvivorData
import tororo1066.tororopluginapi.otherUtils.UsefulUtility

abstract class AbstractSurvivorTalent(val name: String, val unlockCost: Int, var parent :Class<out AbstractSurvivorTalent>? = null) {

    val tasks = ArrayList<BukkitTask>()

    abstract fun lore(): List<String>

    open fun onStart(p: Player) {}

    open fun parameters(data: SurvivorData): SurvivorData {
        return data
    }

    open fun onFinishedGenerator(dieLocation: Location, remainingGenerator: Int, p: Player) {}

    open fun onFinishedEscapeGenerator(dieLocation: Location, p: Player) {}

    open fun onWoodPlate(loc: Location, p: Player) {}

    open fun onHitWoodPlate(hittedPlayer: Player, loc: Location, blindTime: Int, slowTime: Int, p: Player): Pair<Int,Int> {
        return Pair(blindTime,slowTime)
    }

    open fun onHelp(helpedPlayer: Player, p: Player) {}

    open fun onGotHelp(helper: Player, p: Player) {}

    open fun onJail(prisonData: PrisonData, p: Player) {}

    open fun onDie(p: Player) {}

    open fun onDieOtherSurvivor(diePlayer: Player, playerNumber: Int,p: Player) {}

    open fun sheepGeneratorModify(damage: Double, remainingGenerator: Int, maxHealth: Double, nowHealth: Double, p: Player): Double {
        return damage
    }

    open fun cowGeneratorModify(damage: Double, maxHealth: Double, nowHealth: Double, p: Player): Double {
        return damage
    }

    open fun onDamage(damage: Int, toHealth: Int, stun: Boolean, damager: Player, p: Player): Pair<Boolean,Int> {
        return Pair(stun,damage)
    }

    open fun onEnd(p: Player) {}

    companion object{
        fun getTalent(name: String): AbstractSurvivorTalent? {
            val classStr = name.split("_").joinToString("") { it.replaceFirstChar(Char::titlecase) }
            return UsefulUtility.sTry({
                val clazz = Class.forName("tororo1066.identityfifty.talent.survivor.${classStr}")
                clazz.getConstructor().newInstance() as AbstractSurvivorTalent
            },{null})
        }
    }
}
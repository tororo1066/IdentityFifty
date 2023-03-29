package tororo1066.identityfifty.character.hunter

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask
import tororo1066.identityfifty.data.HunterData
import tororo1066.identityfifty.data.PrisonData
import tororo1066.identityfifty.enumClass.StunState

abstract class AbstractHunter(val name: String): Cloneable {

    val tasks = ArrayList<BukkitTask>()

    open fun onStart(p: Player) {}

    abstract fun parameters(data: HunterData): HunterData

    open fun onFinishedGenerator(dieLocation: Location, remainingGenerator: Int, p: Player) {}

    open fun onFinishedEscapeGenerator(dieLocation: Location, p: Player) {}

    open fun onAttack(attackPlayer: Player, p: Player, noOne: Boolean): Int {
        return if (noOne) 4 else 2
    }

    open fun onSurvivorHeal(healPlayer: Player, healedPlayer: Player, p: Player) {}

    open fun onSurvivorJail(survivor: Player, prison: PrisonData, p: Player) {}

    open fun onSurvivorDie(survivor: Player, playerNumber: Int, p: Player) {}

    open fun onSurvivorHelp(helper: Player, gotHelpPlayer: Player, p: Player) {}

    open fun onDamagedWoodPlate(usedPlayer: Player, loc: Location, blindTime: Int, slowTime: Int, p: Player): Pair<Int,Int> {
        return Pair(blindTime, slowTime)
    }

    open fun onStun(blindTime: Int, slowTime: Int, state: StunState, p: Player): Pair<Int,Int> {
        return Pair(blindTime, slowTime)
    }

    open fun onEnd(p: Player) {}

    abstract fun info(): ArrayList<ItemStack>

    public override fun clone(): AbstractHunter {
        return super.clone() as AbstractHunter
    }

}
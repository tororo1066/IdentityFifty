package tororo1066.identityfifty.character.survivor

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask
import tororo1066.identityfifty.data.SurvivorData
import tororo1066.tororopluginapi.sItem.SItem

abstract class AbstractSurvivor(val name: String): Cloneable {

    val tasks = ArrayList<BukkitTask>()

    open fun onStart(p: Player) {
        val sword = ItemStack(Material.IRON_SWORD)
        sword.editMeta { it.isUnbreakable = true }
        p.inventory.addItem(sword)
    }

    abstract fun parameters(data: SurvivorData): SurvivorData

    open fun onFinishedGenerator(dieLocation: Location, remainingGenerator: Int, p: Player) {}

    open fun onFinishedEscapeGenerator(dieLocation: Location, p: Player) {}

    open fun onDamage(damage: Int, toHealth: Int, damager: Player, p: Player): Pair<Boolean,Int> {
        return Pair(true,damage)
    }

    open fun onHelp(helpedPlayer: Player, p: Player) {}

    open fun onGotHelp(helper: Player, p: Player) {}

    open fun onDie(p: Player) {}

    open fun onDieOtherSurvivor(diePlayer: Player, playerNumber: Int,p: Player) {}

    open fun sheepGeneratorModify(damage: Double, remainingGenerator: Int, maxHealth: Double, nowHealth: Double, p: Player): Double {
        return damage
    }

    open fun cowGeneratorModify(damage: Double, maxHealth: Double, nowHealth: Double, p: Player): Double {
        return damage
    }

    public override fun clone(): AbstractSurvivor {
        return super.clone() as AbstractSurvivor
    }
}
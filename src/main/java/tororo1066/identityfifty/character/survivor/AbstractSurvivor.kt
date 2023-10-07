package tororo1066.identityfifty.character.survivor

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.data.PrisonData
import tororo1066.identityfifty.data.SurvivorData

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

    open fun onJail(prisonData: PrisonData, p: Player) {}

    open fun onHitWoodPlate(hitPlayer: Player, loc: Location, blindTime: Int, slowTime: Int, p: Player): Pair<Int,Int> {
        return Pair(blindTime,slowTime)
    }

    open fun onTryHeal(healPlayer: Player, p: Player): Boolean {
        return true
    }

    open fun onTryGotHeal(healer: Player, p: Player): Boolean {
        return true
    }

//    open fun onHeal(heal: Int, toHealth: Int, healedPlayer: Player, p: Player) {}
//
//    open fun onGotHeal(healer: ArrayList<Player>, p: Player) {}
//
    open fun onTryGotHelp(helper: Player, p: Player): Boolean {
        return true
    }

    open fun onHelp(helpedPlayer: Player, p: Player) {}

    open fun onGotHelp(helper: Player, p: Player): ReturnAction {
        return ReturnAction.CONTINUE
    }

    open fun onDie(p: Player) {
        p.inventory.clear()
    }

    open fun onDieOtherSurvivor(diePlayer: Player, playerNumber: Int,p: Player) {}

    open fun sheepGeneratorModify(damage: Double, remainingGenerator: Int, maxHealth: Double, nowHealth: Double, p: Player): Double {
        return damage
    }

    open fun cowGeneratorModify(damage: Double, maxHealth: Double, nowHealth: Double, p: Player): Double {
        return damage
    }

    open fun onEnd(p: Player) {}

    abstract fun info(): ArrayList<ItemStack>

    public override fun clone(): AbstractSurvivor {
        return super.clone() as AbstractSurvivor
    }

    protected fun inPrison(p: Player): Boolean {
        return IdentityFifty.identityFiftyTask?.map?.prisons?.any { it.value.inPlayer.contains(p.uniqueId) } == true
    }

    enum class ReturnAction {
        CANCEL,
        CONTINUE,
        RETURN
    }
}
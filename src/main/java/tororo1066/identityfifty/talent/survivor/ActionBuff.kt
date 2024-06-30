package tororo1066.identityfifty.talent.survivor

import org.bukkit.Location
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import tororo1066.identityfifty.IdentityFifty
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import kotlin.math.floor

class ActionBuff : AbstractSurvivorTalent("action_buff", 2, RemainTimeUp::class.java) {

    override fun lore(): List<String> {
        return listOf("action_buff_lore_1", "action_buff_lore_2")
    }

    var task: BukkitRunnable? = null
    var actionPoint = 0.0

    fun addActionPoint(point: Double) {
        if (actionPoint + point > 100) {
            actionPoint = 100.0
        } else if (actionPoint + point < 0) {
            actionPoint = 0.0
        } else {
            actionPoint += point
        }
    }

    override fun onDamage(damage: Int, toHealth: Int, stun: Boolean, damager: Player, p: Player): Pair<Boolean, Int> {
        addActionPoint(-100.0)
        return super.onDamage(damage, toHealth, stun, damager, p)
    }


    override fun sheepGeneratorModify(
        damage: Double,
        remainingGenerator: Int,
        maxHealth: Double,
        nowHealth: Double,
        p: Player
    ): Double {
        addActionPoint(damage * 0.025)
        val buff = 1 + (0.03 * (actionPoint / 100))
        return damage * buff
    }

    override fun cowGeneratorModify(damage: Double, maxHealth: Double, nowHealth: Double, p: Player): Double {
        addActionPoint(damage * 0.025)
        val buff = 1 + (0.03 * (actionPoint / 100))
        return damage * buff
    }

    override fun onHelp(helpedPlayer: Player, p: Player) {
        addActionPoint(20.0)
    }

    override fun onGotHelp(helper: Player, p: Player) {
        addActionPoint(40.0)
    }

    override fun onWoodPlate(loc: Location, p: Player) {
        addActionPoint(10.0)
    }

    override fun onHitWoodPlate(
        hittedPlayer: Player,
        loc: Location,
        blindTime: Int,
        slowTime: Int,
        p: Player
    ): Pair<Int, Int> {
        addActionPoint(20.0)
        val plateBuff = 1 + (0.125 * (actionPoint / 100))
        return Pair((blindTime * plateBuff).toInt(), (slowTime * plateBuff).toInt())
    }

    override fun scoreboards(p: Player): ArrayList<Pair<Int, String>> {
        val list = arrayListOf<Pair<Int, String>>()
        list.add(-15 to translate("action_buff_scoreboard"))
        list.add(-16 to translate("action_buff_scoreboard_percent", floor(actionPoint * 10.0) / 10.0))
        return list
    }

}
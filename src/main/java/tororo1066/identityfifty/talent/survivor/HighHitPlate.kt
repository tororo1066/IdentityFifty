package tororo1066.identityfifty.talent.survivor

import org.bukkit.Location
import org.bukkit.entity.Player

class HighHitPlate : AbstractSurvivorTalent("high_hit_plate", 2, PlateSpeedUp::class.java) {
    override fun lore(): List<String> {
        return listOf("high_hit_plate_lore_1")
    }

    //var canUse = true

    //override fun onDamage(damage: Int, toHealth: Int, stun: Boolean, damager: Player, p: Player): Pair<Boolean, Int> {
        //canUse = false
        //Bukkit.getScheduler().runTaskLater(IdentityFifty.plugin, Runnable {
            //canUse = true
        //}, 200)
        //return Pair(stun, damage)
    //}

    override fun onHitWoodPlate(
        hittedPlayer: Player,
        loc: Location,
        blindTime: Int,
        slowTime: Int,
        p: Player
    ): Pair<Int, Int> {
        return Pair(blindTime, slowTime + 20)
    }
}
package tororo1066.identityfifty.talent.survivor

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import tororo1066.identityfifty.IdentityFifty

class HitPlateGamble : AbstractSurvivorTalent("hit_plate_gamble", 2, PlateSpeedUp::class.java) {
    override fun lore(): List<String> {
        return listOf(
            "hit_plate_gamble_lore_1",
            "hit_plate_gamble_lore_2"
        )
    }

    var canUse = true

    override fun onDamage(damage: Int, toHealth: Int, stun: Boolean, damager: Player, p: Player): Pair<Boolean, Int> {
        canUse = false
        Bukkit.getScheduler().runTaskLater(IdentityFifty.plugin, Runnable {
            canUse = true
        }, 200)
        return Pair(stun, damage)
    }

    override fun onHitWoodPlate(
        hittedPlayer: Player,
        loc: Location,
        blindTime: Int,
        slowTime: Int,
        p: Player
    ): Pair<Int, Int> {
        return if (canUse) {
            Pair(blindTime, slowTime + 60)
        } else {
            Pair(blindTime, slowTime - 60)
        }
    }
}
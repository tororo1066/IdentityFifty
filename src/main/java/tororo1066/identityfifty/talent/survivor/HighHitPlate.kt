package tororo1066.identityfifty.talent.survivor

import org.bukkit.Location
import org.bukkit.entity.Player

class HighHitPlate : AbstractSurvivorTalent("high_hit_plate",LowFootPrints()) {
    override fun onHitWoodPlate(hittedPlayer: Player, loc: Location, blindTime: Int, slowTime: Int, p: Player): Pair<Int, Int> {
        return Pair (blindTime,slowTime + 20)
    }

}
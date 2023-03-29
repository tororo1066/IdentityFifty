package tororo1066.identityfifty.talent.hunter

import org.bukkit.entity.Player
import tororo1066.identityfifty.enumClass.StunState

class AirSwingDown : AbstractHunterTalent("air_swing_down",1,TalentPlane::class.java) {
    override fun lore(): List<String> {
        return listOf()
    }

    override fun onStun(blindTime: Int, slowTime: Int, state: StunState, p: Player): Pair<Int, Int> {
        if (state == StunState.AIRSWING){
            return Pair(blindTime,slowTime - 2)
        }

        return super.onStun(blindTime, slowTime, state, p)
    }
}
package tororo1066.identityfifty.talent.hunter

import org.bukkit.entity.Player
import tororo1066.identityfifty.enumClass.StunState

class StunTimeDown : AbstractHunterTalent("stun_time_down",1,TalentPlane::class.java) {
    override fun lore(): List<String> {
        return listOf()
    }

    override fun onStun(blindTime: Int, slowTime: Int, state: StunState, p: Player): Pair<Int, Int> {
        return Pair(blindTime - 10, slowTime - 10)
    }
}
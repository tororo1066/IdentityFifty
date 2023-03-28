package tororo1066.identityfifty.talent.hunter

import org.bukkit.entity.Player
import tororo1066.identityfifty.IdentityFifty


class RemainTimeDown: AbstractHunterTalent("remain_time_down",2,TalentPlane::class.java) {
    override fun lore(): List<String> {
        return listOf()
    }

    override fun onStart(p: Player) {
        IdentityFifty.survivors.forEach { (_, data)->
            data.remainingTime -= 20
        }
    }
}

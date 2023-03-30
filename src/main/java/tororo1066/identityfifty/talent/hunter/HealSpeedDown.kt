package tororo1066.identityfifty.talent.hunter

import org.bukkit.entity.Player
import tororo1066.identityfifty.IdentityFifty

class HealSpeedDown: AbstractHunterTalent("heal_speed_down",1,TalentPlane::class.java) {
    override fun lore(): List<String> {
        return listOf("heal_speed_down_lore_1")
    }

    override fun onStart(p: Player) {
        IdentityFifty.survivors.forEach { (_, data) ->
            data.healTick = (data.healTick * 1.05).toInt()
        }
    }
}
package tororo1066.identityfifty.talent.hunter

import org.bukkit.entity.Player
import tororo1066.identityfifty.IdentityFifty

class HealSpeedUp :AbstractHunterTalent("heal_speed_up",1,TalentPlane::class.java) {
    override fun lore(): List<String> {
        return listOf()
    }

    override fun onStart(p: Player) {
        IdentityFifty.survivors.forEach { (_, data) ->
            data.healTick = (data.healTick * 1.05).toInt()
        }
    }
}
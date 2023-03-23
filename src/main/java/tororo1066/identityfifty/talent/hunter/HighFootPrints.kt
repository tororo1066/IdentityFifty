package tororo1066.identityfifty.talent.hunter

import org.bukkit.entity.Player
import tororo1066.identityfifty.IdentityFifty

class HighFootPrints :AbstractHunterTalent("high_foot_prints",2,TalentPlane::class.java) {
    override fun lore(): List<String> {
        return listOf()
    }

    override fun onStart(p: Player) {
        IdentityFifty.survivors.forEach { (_,data) ->
            data.footprintsTime *= 1.20
        }
    }
}
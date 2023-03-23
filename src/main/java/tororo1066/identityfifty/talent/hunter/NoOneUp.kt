package tororo1066.identityfifty.talent.hunter

import org.bukkit.entity.Player
import tororo1066.identityfifty.IdentityFifty

class NoOneUp :AbstractHunterTalent("no_one_up",5,TalentPlane::class.java) {
    override fun lore(): List<String> {
        return listOf()
    }

    override fun onStart(p: Player) {
        (IdentityFifty.identityFiftyTask?:return).count += 60
    }
}
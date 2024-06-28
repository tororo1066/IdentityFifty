package tororo1066.identityfifty.talent.hunter

import org.bukkit.entity.Player
import tororo1066.identityfifty.IdentityFifty

class NoOneUp: AbstractHunterTalent("no_one_up",2,PlateGlow::class.java) {
    override fun lore(): List<String> {
        return listOf("no_one_up_lore_1")
    }

    override fun onStart(p: Player) {
        (IdentityFifty.identityFiftyTask?:return).noOneCount += 30
    }
}
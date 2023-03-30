package tororo1066.identityfifty.talent.hunter

import org.bukkit.entity.Player
import org.inventivetalent.glow.GlowAPI
import tororo1066.identityfifty.IdentityFifty

class HelpGlow: AbstractHunterTalent("help_glow",5,TalentPlane::class.java) {
    override fun lore(): List<String> {
        return listOf("help_glow_lore_1")
    }

    override fun onSurvivorHelp(helper: Player, gotHelpPlayer: Player, p: Player) {
        val data = IdentityFifty.survivors[gotHelpPlayer.uniqueId]?:return
        data.glowManager.glow(mutableListOf(p),GlowAPI.Color.RED,200)
    }
}

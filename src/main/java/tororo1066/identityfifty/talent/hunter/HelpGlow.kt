package tororo1066.identityfifty.talent.hunter

import org.bukkit.entity.Player
import tororo1066.identityfifty.IdentityFifty
import tororo1066.nmsutils.items.GlowColor
import tororo1066.tororopluginapi.lang.SLang.Companion.sendTranslateMsg

class HelpGlow: AbstractHunterTalent("help_glow",5,StunTimeDown::class.java) {
    override fun lore(): List<String> {
        return listOf("help_glow_lore_1","help_glow_lore_2")
    }

    override fun onSurvivorHelp(helper: Player, gotHelpPlayer: Player, p: Player) {
        val data = IdentityFifty.survivors[gotHelpPlayer.uniqueId]?:return
        data.glowManager.glow(mutableListOf(p), GlowColor.RED,120)
        gotHelpPlayer.sendTranslateMsg("help_glow_inform_to_survivor")
    }
}

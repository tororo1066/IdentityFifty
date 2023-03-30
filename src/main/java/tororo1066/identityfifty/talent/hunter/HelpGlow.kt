package tororo1066.identityfifty.talent.hunter

import org.bukkit.entity.Player
import org.inventivetalent.glow.GlowAPI
import tororo1066.identityfifty.IdentityFifty
import tororo1066.tororopluginapi.lang.SLang.Companion.sendTranslateMsg

class HelpGlow: AbstractHunterTalent("help_glow",5,SurvivorJailedSpeedUp::class.java) {
    override fun lore(): List<String> {
        return listOf("help_glow_lore_1")
    }

    override fun onSurvivorHelp(helper: Player, gotHelpPlayer: Player, p: Player) {
        val data = IdentityFifty.survivors[gotHelpPlayer.uniqueId]?:return
        data.glowManager.glow(mutableListOf(p),GlowAPI.Color.RED,200)
        gotHelpPlayer.sendTranslateMsg("inform_to_survivor")
    }
}

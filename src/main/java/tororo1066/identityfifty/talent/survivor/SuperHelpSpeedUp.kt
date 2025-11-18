package tororo1066.identityfifty.talent.survivor

import org.bukkit.entity.Player
import tororo1066.identityfifty.IdentityFifty
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import java.util.UUID

class SuperHelpSpeedUp: AbstractSurvivorTalent("super_help_speed_up", 2, FinishedSheepGlow::class.java) {

    override fun lore(): List<String> {
        return listOf(
            translate("super_help_speed_up_lore_1"),
            translate("super_help_speed_up_lore_2")
        )
    }

    private var cooldown = 0
    private var uuid = UUID.randomUUID()

    override fun onStart(p: Player) {
        val data = IdentityFifty.survivors[p.uniqueId]!!
        data.helpTickModify += uuid to 0.5
    }

    override fun onHelp(helpedPlayer: Player, p: Player) {
        if (cooldown > 0) return
        val data = IdentityFifty.survivors[p.uniqueId]!!
        data.helpTickModify -= uuid
        cooldown = 150
        IdentityFifty.util.repeatDelay(amount = 150, repeatTick = 20, delayTick = 20, unit = {
            cooldown--
        }, lastAction = {
            data.helpTickModify += uuid to 0.5
        })
    }

    override fun scoreboards(p: Player): ArrayList<Pair<Int, String>> {
        val list = arrayListOf<Pair<Int, String>>()
        list.add(-17 to translate("super_help_speed_up_scoreboard"))
        if (cooldown > 0) {
            list.add(-18 to translate("super_help_speed_up_scoreboard_cooldown", cooldown))
        } else {
            list.add(-18 to translate("super_help_speed_up_scoreboard_usable"))
        }
        return list
    }
}
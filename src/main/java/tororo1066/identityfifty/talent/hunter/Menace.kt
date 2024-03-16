package tororo1066.identityfifty.talent.hunter

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import tororo1066.identityfifty.IdentityFifty
import tororo1066.tororopluginapi.lang.SLang.Companion.sendTranslateMsg
import tororo1066.tororopluginapi.lang.SLang.Companion.translate

class Menace : AbstractHunterTalent("menace",2,HighFootPrints::class.java) {
    override fun lore(): List<String> {
        return listOf("menace_lore_1","menace_lore_2")
    }
    private var menace = 0

    override fun onStart(p: Player) {
        tasks.add(Bukkit.getScheduler().runTaskTimer(IdentityFifty.plugin, Runnable {
            if (menace > 0) {
                menace--
                return@Runnable
            }
            val radiusPlayer = p.location.getNearbyPlayers(25.0).filter { IdentityFifty.identityFiftyTask?.aliveSurvivors()?.contains(it.uniqueId) == true }
            if(radiusPlayer.isNotEmpty()){
                p.sendTranslateMsg("menace_detect_radius_player")
                menace = 25
            }
        },0,20))
    }

    override fun scoreboards(p: Player): ArrayList<Pair<Int, String>> {
        val list = arrayListOf<Pair<Int,String>>()
        list.add(-10 to translate("menace_scoreboard"))
        if (menace > 0) {
            list.add(-11 to translate("menace_scoreboard_cooldown", menace))
        } else {
            list.add(-11 to translate("menace_scoreboard_usable"))
        }

        return list
    }

}
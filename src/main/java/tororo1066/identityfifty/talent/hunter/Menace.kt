package tororo1066.identityfifty.talent.hunter

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import tororo1066.identityfifty.IdentityFifty
import tororo1066.tororopluginapi.lang.SLang.Companion.sendTranslateMsg

class Menace : AbstractHunterTalent("menace",2,HighFootPrints::class.java) {
    override fun lore(): List<String> {
        return listOf("menace_lore_1","menace_lore_2")
    }
    private var menace = true

    override fun onStart(p: Player) {
        tasks.add(Bukkit.getScheduler().runTaskTimer(IdentityFifty.plugin, Runnable {
            val radiusPlayer = p.location.getNearbyPlayers(25.0).filter { IdentityFifty.identityFiftyTask?.aliveSurvivors()?.contains(it.uniqueId) == true }
            if(menace && radiusPlayer.isNotEmpty()){
                p.sendTranslateMsg("menace_detect_radius_player")
                menace = false
                Bukkit.getScheduler().runTaskLater(IdentityFifty.plugin, Runnable {
                    menace = true
                },600)
            }
        },0,5))


    }

}
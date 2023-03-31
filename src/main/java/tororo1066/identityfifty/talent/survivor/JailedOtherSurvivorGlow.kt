package tororo1066.identityfifty.talent.survivor

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import org.inventivetalent.glow.GlowAPI
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.character.survivor.AbstractSurvivor
import tororo1066.identityfifty.data.PrisonData

class JailedOtherSurvivorGlow :AbstractSurvivorTalent("jailed_other_survivor_glow",2,RemainTimeUp::class.java) {
    override fun lore(): List<String> {
        return listOf("jailed_other_survivor_glow_lore_1")
    }

    var glowtask:BukkitTask? = null

    override fun onJail(prisonData: PrisonData, p: Player) {
        glowtask = Bukkit.getScheduler().runTaskTimer(IdentityFifty.plugin, Runnable {
            val players = ArrayList<Player>()
            IdentityFifty.survivors.forEach { (uuid, _) ->
                val playerinfo = Bukkit.getPlayer(uuid)?:return@forEach
                players.add(playerinfo)
            }
            IdentityFifty.survivors.forEach { (uuid, data) ->
                Bukkit.getPlayer(uuid)?:return@forEach
                data.glowManager.glow(players,GlowAPI.Color.BLUE,100)
            }
        },0,5)
    }

    override fun onGotHelp(helper: Player, p: Player) {
        glowtask?.cancel()
        glowtask = null
    }

    override fun onEnd(p: Player) {
        glowtask?.cancel()
        glowtask = null
    }
}
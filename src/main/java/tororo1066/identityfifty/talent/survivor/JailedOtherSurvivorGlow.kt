package tororo1066.identityfifty.talent.survivor

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.data.PrisonData
import tororo1066.nmsutils.items.GlowColor
import tororo1066.tororopluginapi.utils.toPlayer

class JailedOtherSurvivorGlow : AbstractSurvivorTalent("jailed_other_survivor_glow", 5, FullSheepUp::class.java) {
    override fun lore(): List<String> {
        return listOf(
            "jailed_other_survivor_glow_lore_1"
        )
    }



    override fun onStart(p: Player) {
        tasks.add(Bukkit.getScheduler().runTaskTimer(IdentityFifty.plugin, Runnable {
            IdentityFifty.survivors.forEach { (uuid, data) ->
                if (uuid == p.uniqueId) {
                    return@forEach
                }
                Bukkit.getPlayer(uuid) ?: return@forEach
                val health = data.getHealth()


                if (health in 2..4) {
                    data.glowManager.glow(mutableListOf(p), GlowColor.YELLOW, 20)
                }
            }
        }, 0, 19))
    }


}
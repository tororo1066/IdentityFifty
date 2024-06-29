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
            "jailed_other_survivor_glow_lore_1",
            "jailed_other_survivor_glow_lore_2"
        )
    }

    var survivorGlowTask: BukkitTask? = null
    var hunterGlowTask: BukkitTask? = null

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

    override fun onJail(prisonData: PrisonData, p: Player) {
        survivorGlowTask = Bukkit.getScheduler().runTaskTimer(IdentityFifty.plugin, Runnable {
            val players = ArrayList<Player>()
            IdentityFifty.survivors.forEach { (uuid, _) ->
                val playerInfo = Bukkit.getPlayer(uuid) ?: return@forEach
                players.add(playerInfo)
            }
            IdentityFifty.survivors.forEach { (uuid, data) ->
                Bukkit.getPlayer(uuid) ?: return@forEach
                data.glowManager.glow(players, GlowColor.BLUE, 100)
            }
        }, 0, 99)

        hunterGlowTask = Bukkit.getScheduler().runTaskTimer(IdentityFifty.plugin, Runnable {
            if (IdentityFifty.identityFiftyTask?.aliveSurvivors()?.contains(p.uniqueId) == false) {
                return@Runnable
            }
            val players = p.location.getNearbyPlayers(20.0)
                .filter { it != p && IdentityFifty.hunters.containsKey(it.uniqueId) }
            players.forEach {
                val data = IdentityFifty.hunters[it.uniqueId] ?: return@forEach
                data.glowManager.glow(
                    IdentityFifty.survivors.map { map -> map.key.toPlayer() }.filterNotNull().toMutableList(),
                    GlowColor.RED,
                    9
                )
            }

        }, 5, 5)
    }

    override fun onGotHelp(helper: Player, p: Player) {
        survivorGlowTask?.cancel()
        survivorGlowTask = null
        hunterGlowTask?.cancel()
        hunterGlowTask = null
    }

    override fun onDie(p: Player) {
        survivorGlowTask?.cancel()
        survivorGlowTask = null
        hunterGlowTask?.cancel()
        hunterGlowTask = null
    }

    override fun onEnd(p: Player) {
        survivorGlowTask?.cancel()
        survivorGlowTask = null
        hunterGlowTask?.cancel()
        hunterGlowTask = null
    }
}
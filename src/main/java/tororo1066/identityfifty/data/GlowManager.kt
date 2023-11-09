package tororo1066.identityfifty.data

import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.inventivetalent.glow.GlowAPI
import org.inventivetalent.glow.GlowAPI.Color
import tororo1066.identityfifty.IdentityFifty
import tororo1066.tororopluginapi.utils.toPlayer
import java.util.*

class GlowManager(private val uuid: UUID) {

    val glowTasks = HashMap<UUID,GlowTask>()

    fun glow(visiblePlayers: MutableCollection<Player>, color: Color, duration: Int) {
        visiblePlayers.addAll(IdentityFifty.spectators.keys.mapNotNull { it.toPlayer() })
        visiblePlayers.forEach {
            glowTask(it,color,duration)
        }
    }

    private fun glowTask(visiblePlayer: Player, color: Color, duration: Int) {
        glowTasks[visiblePlayer.uniqueId]?.let {
            if (it.tick <= duration) {
                it.cancel()
            } else {
                return
            }
        }
        val task = GlowTask(uuid,visiblePlayer,color,duration)
        glowTasks[visiblePlayer.uniqueId] = task
    }

    class GlowTask(private val uuid: UUID, private val visiblePlayer: Player, private val color: Color, private val duration: Int) : BukkitRunnable() {
        var tick = duration
        init {
            runTaskTimer(IdentityFifty.plugin,0,1)
        }
        override fun run() {
            val p = uuid.toPlayer()?: run {
                cancel()
                return
            }
            if (tick <= 0) {
                GlowAPI.setGlowing(p, false, visiblePlayer)
                if (IdentityFifty.survivors.containsKey(uuid)){
                    IdentityFifty.identityFiftyTask?.survivorTeam?.addEntry(p.name)
                }

                if (IdentityFifty.hunters.containsKey(uuid)){
                    IdentityFifty.identityFiftyTask?.hunterTeam?.addEntry(p.name)
                }
                cancel()
                return
            }
            GlowAPI.setGlowing(p, false, visiblePlayer)
            GlowAPI.setGlowing(p, color, visiblePlayer)
            tick--
        }
    }

}
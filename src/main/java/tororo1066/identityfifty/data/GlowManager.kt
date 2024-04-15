package tororo1066.identityfifty.data

import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.inventivetalent.glow.GlowAPI
import tororo1066.identityfifty.IdentityFifty
import tororo1066.tororopluginapi.utils.toPlayer
import java.util.UUID

class GlowManager(private val uuid: UUID) {

    private val glowTasks = HashMap<UUID,GlowTask>()

    fun glow(visiblePlayers: MutableCollection<Player>, color: ChatColor, duration: Int): List<Int> {
        visiblePlayers.addAll(IdentityFifty.spectators.keys.mapNotNull { it.toPlayer() })
        val tasks = ArrayList<Int>()
        visiblePlayers.forEach {
            tasks.add(glowTask(it,color,duration))
        }
        return tasks
    }

    private fun glowTask(visiblePlayer: Player, color: ChatColor, duration: Int): Int {
        glowTasks[visiblePlayer.uniqueId]?.let {
            if (it.tick <= duration) {
                it.cancel()
            } else {
                return -1
            }
        }
        val task = GlowTask(uuid.toPlayer()?:return -1,visiblePlayer,color,duration)
        glowTasks[visiblePlayer.uniqueId] = task
        return task.taskId
    }

    fun cancelTask(taskId: Int) {
        val uuid = ArrayList<UUID>()
        glowTasks.values.forEach {
            if (it.taskId == taskId) {
                it.cancel()
                uuid.add(it.visiblePlayer.uniqueId)
            }
        }
        uuid.forEach {
            glowTasks.remove(it)
        }
    }

    class GlowTask(private val p: Player, val visiblePlayer: Player, color: ChatColor, duration: Int) : BukkitRunnable() {
        var tick = duration
        val glowAPIColor = GlowAPI.Color.valueOf(color.name)
        init {
//            SPlayer.getSPlayer(p).sendTeam(color, listOf(visiblePlayer))
//            SPlayer.getSPlayer(p).sendGlow(true, listOf(visiblePlayer))
            runTaskTimer(IdentityFifty.plugin,0,1)
        }
        override fun run() {
            if (p.player == null || visiblePlayer.player == null) {
                cancel()
                return
            }
            if (tick <= 0) {
                GlowAPI.setGlowing(p, glowAPIColor, "never", "never", visiblePlayer)
                if (IdentityFifty.survivors.containsKey(p.uniqueId)){
                    IdentityFifty.identityFiftyTask?.survivorTeam?.addEntry(p.name)
                }

                if (IdentityFifty.hunters.containsKey(p.uniqueId)){
                    IdentityFifty.identityFiftyTask?.hunterTeam?.addEntry(p.name)
                }
                cancel()
                return
            }

            tick--
        }

        override fun cancel() {
            GlowAPI.setGlowing(p, false, visiblePlayer)
            if (IdentityFifty.survivors.containsKey(p.uniqueId)){
                IdentityFifty.identityFiftyTask?.survivorTeam?.addEntry(p.name)
            }

            if (IdentityFifty.hunters.containsKey(p.uniqueId)){
                IdentityFifty.identityFiftyTask?.hunterTeam?.addEntry(p.name)
            }
            super.cancel()
        }
    }

}
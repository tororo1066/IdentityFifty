package tororo1066.identityfifty.data

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.inventivetalent.glow.GlowAPI
import org.inventivetalent.glow.GlowAPI.Color
import tororo1066.identityfifty.IdentityFifty
import tororo1066.tororopluginapi.utils.toPlayer
import java.util.*
import kotlin.collections.ArrayList

class GlowManager(private val uuid: UUID) {

    private val glowTasks = HashMap<UUID,GlowTask>()

    fun glow(visiblePlayers: MutableCollection<Player>, color: Color, duration: Int): List<Int> {
        visiblePlayers.addAll(IdentityFifty.spectators.keys.mapNotNull { it.toPlayer() })
        val tasks = ArrayList<Int>()
        visiblePlayers.forEach {
            tasks.add(glowTask(it,color,duration))
        }
        return tasks
    }

    private fun glowTask(visiblePlayer: Player, color: Color, duration: Int): Int {
        glowTasks[visiblePlayer.uniqueId]?.let {
            if (it.tick <= duration) {
                it.cancel()
            } else {
                return -1
            }
        }
        val task = GlowTask(uuid,visiblePlayer,color,duration)
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

    class GlowTask(private val uuid: UUID, val visiblePlayer: Player, private val color: Color, duration: Int) : BukkitRunnable() {
        var tick = duration
        init {
            runTaskTimer(IdentityFifty.plugin,0,1)
        }
        override fun run() {
            val p = Bukkit.getEntity(uuid)?: run {
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

        override fun cancel() {
            val p = Bukkit.getEntity(uuid)?:return
            GlowAPI.setGlowing(p, false, visiblePlayer)
            if (IdentityFifty.survivors.containsKey(uuid)){
                IdentityFifty.identityFiftyTask?.survivorTeam?.addEntry(p.name)
            }

            if (IdentityFifty.hunters.containsKey(uuid)){
                IdentityFifty.identityFiftyTask?.hunterTeam?.addEntry(p.name)
            }
            super.cancel()
        }
    }

}
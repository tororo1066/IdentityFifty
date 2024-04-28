package tororo1066.identityfifty.data

import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import tororo1066.identityfifty.IdentityFifty
import tororo1066.nmsutils.SEntity
import tororo1066.nmsutils.items.GlowColor
import tororo1066.tororopluginapi.utils.toPlayer
import java.util.UUID

class GlowManager(private val uuid: UUID) {

    private val glowTasks = HashMap<UUID,GlowTask>()

    fun glow(visiblePlayers: MutableCollection<Player>, color: GlowColor, duration: Int): List<Int> {
        visiblePlayers.addAll(IdentityFifty.spectators.keys.mapNotNull { it.toPlayer() })
        val tasks = ArrayList<Int>()
        visiblePlayers.forEach {
            tasks.add(glowTask(it,color,duration))
        }
        return tasks
    }

    private fun glowTask(visiblePlayer: Player, color: GlowColor, duration: Int): Int {
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

    class GlowTask(private val p: Player, val visiblePlayer: Player, color: GlowColor, duration: Int) : BukkitRunnable() {
        var tick = duration
        init {
            SEntity.getSEntity(p)!!.sendGlow(true, listOf(visiblePlayer), color)
            runTaskTimer(IdentityFifty.plugin,0,1)
        }
        override fun run() {
            if (p.player == null || visiblePlayer.player == null) {
                cancel()
                return
            }
            if (tick <= 0) {
                cancel()
                return
            }

            tick--
        }

        override fun cancel() {
            SEntity.getSEntity(p)!!.sendGlow(false, listOf(visiblePlayer))
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
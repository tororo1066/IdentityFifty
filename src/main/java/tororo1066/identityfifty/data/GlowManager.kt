package tororo1066.identityfifty.data

import org.bukkit.Bukkit
import org.bukkit.entity.Entity
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
//                it.cancel()
                it.changeColor(color)
                it.changeDuration(duration)
                return it.taskId
            } else {
                return -1
            }
        }
        val task = GlowTask(Bukkit.getEntity(uuid)?:return -1,visiblePlayer,color,duration)
        glowTasks[visiblePlayer.uniqueId] = task
        return task.taskId
    }

    fun cancelTask(taskId: Int) {
        val uuid = ArrayList<UUID>()
        uuid.addAll(glowTasks.filter { it.value.taskId == taskId }.keys)
        uuid.forEach {
            glowTasks[it]?.cancel()
        }
        uuid.forEach {
            glowTasks.remove(it)
        }
    }

    fun cancelTask(uuid: UUID) {
        glowTasks[uuid]?.cancel()
        glowTasks.remove(uuid)
    }

    inner class GlowTask(private val entity: Entity, val visiblePlayer: Player, var color: GlowColor, duration: Int) : BukkitRunnable() {
        var tick = duration
        fun changeColor(color: GlowColor) {
            if (this.color == color) return
            SEntity.getSEntity(entity).setTeam(color.name, listOf(visiblePlayer))
            this.color = color
        }

        fun changeDuration(duration: Int) {
            tick = duration
        }

        init {
            SEntity.getSEntity(entity).sendGlow(true, listOf(visiblePlayer), color)
            runTaskTimer(IdentityFifty.plugin,0,1)
        }
        override fun run() {
            if (entity.isDead || visiblePlayer.player == null) {
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
            SEntity.getSEntity(entity).sendGlow(false, listOf(visiblePlayer))
            if (IdentityFifty.survivors.containsKey(entity.uniqueId)){
                IdentityFifty.identityFiftyTask?.survivorTeam?.addEntry(entity.name)
            }

            if (IdentityFifty.hunters.containsKey(entity.uniqueId)){
                IdentityFifty.identityFiftyTask?.hunterTeam?.addEntry(entity.name)
            }
            super.cancel()
            glowTasks.remove(visiblePlayer.uniqueId)
        }
    }

}
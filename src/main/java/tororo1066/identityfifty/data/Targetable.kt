package tororo1066.identityfifty.data

import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import tororo1066.identityfifty.IdentityFiftyTask
import tororo1066.nmsutils.items.GlowColor
import java.util.UUID

open class Targetable(
    val entity: Entity
) {

    enum class Type {
        SURVIVOR,
        HUNTER,
        OTHER
    }
    private val glowManager = GlowManager(entity.uniqueId)

    var glowColor: GlowColor? = null

    var type: Type = Type.OTHER

    var asPlayer: Boolean = entity is Player

    fun glow(
        visiblePlayers: MutableCollection<Player>,
        color: GlowColor,
        duration: Int,
        override: Boolean = false
    ): List<Int> {
        val glowColor = if (override) color else this.glowColor ?: color
        return glowManager.glow(visiblePlayers, glowColor, duration)
    }

    fun convertEntityState(targetable: Targetable) {
        // walkSpeed

    }

    init {
        entities[entity.uniqueId] = this
    }

    companion object {
        val entities = HashMap<UUID, Targetable>()

        fun Location.getNearbyTargetableEntities(xRadius: Double, yRadius: Double, zRadius: Double): List<Targetable> {
            return this.world.getNearbyEntities(this, xRadius, yRadius, zRadius)
                .mapNotNull { it.getTargetable() }
        }

        fun Location.getNearbyTargetableEntities(radius: Double): List<Targetable> {
            return getNearbyTargetableEntities(radius, radius, radius)
        }

        fun Location.getNearbySurvivors(radius: Double): List<Targetable> {
            return getNearbyTargetableEntities(radius).filter { it.type == Type.SURVIVOR }
        }

        fun Location.getNearbySurvivorPlayers(radius: Double): List<Targetable> {
            return getNearbySurvivors(radius).filter { it.asPlayer }
        }

        fun Location.getNearbyAliveSurvivors(radius: Double, task: IdentityFiftyTask): List<Targetable> {
            return getNearbySurvivors(radius).filter {
                val entity = it.entity
                entity !is Player || task.aliveSurvivors().contains(entity.uniqueId)
            }
        }

        fun Location.getNearbyAliveSurvivorPlayers(radius: Double, task: IdentityFiftyTask): List<Targetable> {
            return getNearbyAliveSurvivors(radius, task).filter { it.asPlayer }
        }

        fun getSurvivors(): List<Targetable> {
            return entities.values.filter { it.type == Type.SURVIVOR }
        }

        fun getSurvivorPlayers(): List<Targetable> {
            return getSurvivors().filter { it.asPlayer }
        }

        fun getAliveSurvivors(task: IdentityFiftyTask): List<Targetable> {
            return getSurvivors().filter {
                val entity = it.entity
                entity !is Player || task.aliveSurvivors().contains(entity.uniqueId)
            }
        }

        fun getAliveSurvivorPlayers(task: IdentityFiftyTask): List<Targetable> {
            return getAliveSurvivors(task).filter { it.asPlayer }
        }

        fun Location.getNearbyHunters(radius: Double): List<Targetable> {
            return getNearbyTargetableEntities(radius).filter { it.type == Type.HUNTER }
        }

        fun Entity.getTargetable(): Targetable? {
            return entities[this.uniqueId]
        }

        fun Entity.createTargetable(): Targetable {
            return Targetable(this)
        }
    }
}
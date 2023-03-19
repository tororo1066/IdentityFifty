package tororo1066.identityfifty.talent.survivor

import org.bukkit.entity.Player
import tororo1066.identityfifty.IdentityFifty

class WoundedGeneratorUp : AbstractSurvivorTalent("wounded_generator_up",1,RemainTimeUp::class.java) {
    override fun lore(): List<String> {
        return listOf()
    }

    override fun sheepGeneratorModify(damage: Double, remainingGenerator: Int, maxHealth: Double, nowHealth: Double, p: Player): Double {
        val data = IdentityFifty.survivors[p.uniqueId]!!
        val health = data.getHealth()
        if (health in 2..4){
            return damage * 1.05
        }
        return damage

    }

}
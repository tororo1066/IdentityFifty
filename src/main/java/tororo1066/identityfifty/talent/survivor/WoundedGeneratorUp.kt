package tororo1066.identityfifty.talent.survivor

import org.bukkit.entity.Player
import tororo1066.identityfifty.IdentityFifty

class WoundedGeneratorUp : AbstractSurvivorTalent("wounded_generator_up",1,HelpSpeedUp::class.java) {
    override fun lore(): List<String> {
        return listOf("wounded_generator_up_lore_1")
    }

    override fun sheepGeneratorModify(damage: Double, remainingGenerator: Int, maxHealth: Double, nowHealth: Double, p: Player): Double {
        val data = IdentityFifty.survivors[p.uniqueId]!!
        val health = data.getHealth()
        if (health in 2..4){
            return damage * 1.03
        }
        return damage

    }

}
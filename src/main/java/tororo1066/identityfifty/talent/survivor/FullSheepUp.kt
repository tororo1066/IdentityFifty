package tororo1066.identityfifty.talent.survivor

import org.bukkit.entity.Player
import tororo1066.identityfifty.IdentityFifty

class FullSheepUp :AbstractSurvivorTalent("full_sheep_up",1,HealSpeedUp::class.java) {
    override fun lore(): List<String> {
        return listOf("full_sheep_up_lore_1")
    }

    override fun sheepGeneratorModify(damage: Double, remainingGenerator: Int, maxHealth: Double, nowHealth: Double, p: Player): Double {
        val data = IdentityFifty.survivors[p.uniqueId]!!
        val health = data.getHealth()
        if (health == 5){
            return damage * 1.03
        }
        return damage
    }
}
package tororo1066.identityfifty.talent.survivor

import org.bukkit.entity.Player
import tororo1066.identityfifty.IdentityFifty

class FullSheepUp :AbstractSurvivorTalent("full_sheep_up",1,HealSpeedUp::class.java) {
    override fun lore(): List<String> {
        return listOf()
    }

    override fun sheepGeneratorModify(damage: Double, remainingGenerator: Int, maxHealth: Double, nowHealth: Double, p: Player): Double {
        val data = IdentityFifty.survivors[p.uniqueId]!!
        val health = data.getHealth()
        if (health == 5){
            return damage * 1.05
        }
        return damage
    }
}
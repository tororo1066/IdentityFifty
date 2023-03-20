package tororo1066.identityfifty.talent.survivor

import org.bukkit.entity.Player
import tororo1066.identityfifty.IdentityFifty

class FullCowUp : AbstractSurvivorTalent("full_cow_up",1,TalentPlane::class.java) {
    override fun lore(): List<String> {
        return listOf()
    }

    override fun cowGeneratorModify(damage: Double, maxHealth: Double, nowHealth: Double, p: Player): Double {
        val data = IdentityFifty.survivors[p.uniqueId]!!
        val health = data.getHealth()
        if (health == 5){
            return damage * 1.05
        }
        return damage
    }
}
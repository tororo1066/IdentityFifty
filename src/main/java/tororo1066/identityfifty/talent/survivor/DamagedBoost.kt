package tororo1066.identityfifty.talent.survivor

import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class DamagedBoost : AbstractSurvivorTalent("damaged_boost",2,LowFootPrints::class.java) {
    override fun lore(): List<String> {
        return listOf("damaged_boost_lore_1")
    }

    override fun onDamage(damage: Int, toHealth: Int, damager: Player, p: Player): Pair<Boolean, Int> {
        p.addPotionEffect(PotionEffect(PotionEffectType.SPEED,30,1))
        return Pair(true,damage)
    }
}
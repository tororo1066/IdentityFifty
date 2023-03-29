package tororo1066.identityfifty.talent.hunter

import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class SurvivorHealedStop : AbstractHunterTalent("survivor_healed_stop",2,RemainTimeDown::class.java) {
    override fun lore(): List<String> {
        return listOf("survivor_healed_stop_lore_1")
    }

    override fun onSurvivorHeal(healPlayer: Player, healedPlayer: Player, p: Player) {
        healedPlayer.addPotionEffect(PotionEffect(PotionEffectType.SLOW,60,200))
        healedPlayer.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS,40,3))
    }
}
package tororo1066.identityfifty.talent.hunter

import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import tororo1066.identityfifty.IdentityFifty
import tororo1066.tororopluginapi.lang.SLang.Companion.sendTranslateMsg

class SurvivorHealedStop : AbstractHunterTalent("survivor_healed_stop",2,RemainTimeDown::class.java) {
    override fun lore(): List<String> {
        return listOf("survivor_healed_stop_lore_1","survivor_healed_stop_lore_2")
    }

    override fun onSurvivorHeal(healPlayer: Player, healedPlayer: Player, p: Player) {
        healedPlayer.addPotionEffect(PotionEffect(PotionEffectType.SLOW,80,200))
        healedPlayer.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS,100,3))
        val radiusPlayer = p.location.getNearbyPlayers(35.0).filter { IdentityFifty.identityFiftyTask?.aliveSurvivors()?.contains(it.uniqueId) == true }
        if (radiusPlayer.isNotEmpty()){
            p.sendTranslateMsg("survivor_healed_stop_detected")
        }
    }
}
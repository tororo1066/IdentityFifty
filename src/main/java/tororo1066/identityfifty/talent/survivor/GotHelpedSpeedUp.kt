package tororo1066.identityfifty.talent.survivor

import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class GotHelpedSpeedUp : AbstractSurvivorTalent("got_helped_speed_up",1,TalentPlane::class.java) {
    override fun lore(): List<String> {
        return listOf("got_helped_speed_up_lore_1")
    }

    override fun onGotHelp(helper: Player, p: Player) {
        p.addPotionEffect(PotionEffect(PotionEffectType.SPEED,40,1))
    }
}
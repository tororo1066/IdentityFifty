package tororo1066.identityfifty.talent.hunter

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class FinishGateBuff :AbstractHunterTalent("finish_gate_buff",2,AirSwingDown::class.java) {
    override fun lore(): List<String> {
        return listOf("finish_gate_buff_lore_1")
    }

    override fun onFinishedEscapeGenerator(dieLocation: Location, p: Player) {
        p.addPotionEffect(PotionEffect(PotionEffectType.SPEED,1200,0))
        p.addPotionEffect(PotionEffect(PotionEffectType.FIRE_RESISTANCE,1200,1))
        p.addPotionEffect(PotionEffect(PotionEffectType.NIGHT_VISION,1200,1))
        p.addPotionEffect(PotionEffect(PotionEffectType.REGENERATION,1200,1))
    }
}
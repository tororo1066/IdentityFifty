package tororo1066.identityfifty.talent.survivor

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class GateOpenBoost : AbstractSurvivorTalent("gate_open_boost", 1,WoundedGeneratorUp::class.java) {
    override fun lore(): List<String> {
        return listOf()
    }

    override fun onFinishedGenerator(dieLocation: Location, remainingGenerator: Int, p: Player) {
        if (remainingGenerator == 0){
            p.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 80, 2))
        }
    }


}
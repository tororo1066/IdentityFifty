package tororo1066.identityfifty.talent.hunter

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class GateOpenHunterBuff : AbstractHunterTalent("gate_open_hunter_buff",2,TalentPlane::class.java) {
    override fun lore(): List<String> {
        return listOf()
    }

    override fun onFinishedGenerator(dieLocation: Location, remainingGenerator: Int, p: Player) {
        if (remainingGenerator == 0){
            p.addPotionEffect(PotionEffect(PotionEffectType.SPEED,100,0))
        }
    }
}
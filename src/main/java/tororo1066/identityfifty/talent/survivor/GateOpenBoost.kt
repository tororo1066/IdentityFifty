package tororo1066.identityfifty.talent.survivor

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import tororo1066.identityfifty.IdentityFifty
import java.util.UUID

class GateOpenBoost : AbstractSurvivorTalent("gate_open_boost", 5,FullCowUp::class.java) {
    override fun lore(): List<String> {
        return listOf("gate_open_boost_lore_1")
    }

    override fun onFinishedGenerator(dieLocation: Location, remainingGenerator: Int, p: Player) {
        if (remainingGenerator == 0){
            val data = IdentityFifty.survivors[p.uniqueId]!!
            data.setHealth(data.getHealth() + 2)
            p.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 100, 2))
            val footprints = UUID.randomUUID()
            data.footprintsModify[footprints] = 0.0
            Bukkit.getScheduler().runTaskLater(IdentityFifty.plugin, Runnable {
                data.footprintsModify.remove(footprints)
            }, 100)
        }
    }


}
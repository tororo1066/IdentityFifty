package tororo1066.identityfifty.talent.survivor

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import tororo1066.identityfifty.IdentityFifty
import java.util.function.Consumer

class PlateSpeedUp : AbstractSurvivorTalent("plate_speed_up",5,WoundedCowUp::class.java,) {
    override fun lore(): List<String> {
        return listOf("plate_speed_up_lore_1","plate_speed_up_lore_2")
    }

    var boost = true

    override fun onWoodPlate(loc: Location, p: Player) {
        if(boost){
            p.addPotionEffect(PotionEffect(PotionEffectType.SPEED,100,0))
            boost = false

            Bukkit.getScheduler().runTaskLater(IdentityFifty.plugin, Runnable {
                boost = true
            },800)
        }




    }


}
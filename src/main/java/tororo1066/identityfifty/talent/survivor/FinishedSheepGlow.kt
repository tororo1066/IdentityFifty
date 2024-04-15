package tororo1066.identityfifty.talent.survivor

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.entity.Player
import tororo1066.identityfifty.IdentityFifty

class FinishedSheepGlow :AbstractSurvivorTalent("finished_sheep_glow",2,GotHelpedSpeedUp::class.java) {
    override fun lore(): List<String> {
        return listOf("finished_sheep_glow_lore_1")
    }

    override fun onFinishedGenerator(dieLocation: Location, remainingGenerator: Int, p: Player) {
        if (remainingGenerator == 0){
            IdentityFifty.hunters.forEach { (uuid, data)  ->
                Bukkit.getPlayer(uuid)?:return@forEach
                data.glowManager.glow(mutableListOf(p), ChatColor.RED,100)
            }

        }
    }
}
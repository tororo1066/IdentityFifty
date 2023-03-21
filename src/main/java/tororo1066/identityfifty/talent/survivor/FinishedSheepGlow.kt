package tororo1066.identityfifty.talent.survivor

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.inventivetalent.glow.GlowAPI
import tororo1066.identityfifty.IdentityFifty
import java.util.UUID

class FinishedSheepGlow :AbstractSurvivorTalent("finished_sheep_glow",2,FinishGateOpenBuff::class.java) {
    override fun lore(): List<String> {
        return listOf("finished_sheep_glow_lore_1")
    }

    override fun onFinishedGenerator(dieLocation: Location, remainingGenerator: Int, p: Player) {
        if (remainingGenerator == 0){
            IdentityFifty.hunters.forEach { (uuid, data)  ->
                Bukkit.getPlayer(uuid)?:return@forEach
                data.glowManager.glow(mutableListOf(p), GlowAPI.Color.RED,100)
            }

        }
    }
}
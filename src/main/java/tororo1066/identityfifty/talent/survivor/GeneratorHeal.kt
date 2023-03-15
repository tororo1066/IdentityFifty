package tororo1066.identityfifty.talent.survivor

import org.bukkit.Location
import org.bukkit.entity.Player

class GeneratorHeal : AbstractSurvivorTalent("generator_heal",1,TalentPlane::class.java) {
    override fun lore(): List<String> {
        return listOf()
    }

    override fun onFinishedGenerator(dieLocation: Location, remainingGenerator: Int, p: Player) {
        dieLocation.getNearbyPlayers(5.0).forEach {  }
    }



}
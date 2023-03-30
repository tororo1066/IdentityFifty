package tororo1066.identityfifty.talent.hunter

import org.bukkit.Location
import org.bukkit.entity.Player

class EndGameSpeedUp :AbstractHunterTalent ("end_game_speed_up",1,TalentPlane::class.java) {
    override fun lore(): List<String> {
        return listOf("end_game_speed_up_lore_1")
    }

    override fun onFinishedGenerator(dieLocation: Location, remainingGenerator: Int, p: Player) {
        val generator = remainingGenerator + 1
        val speedBuff = 0.008f / generator
        p.walkSpeed += speedBuff
    }
}
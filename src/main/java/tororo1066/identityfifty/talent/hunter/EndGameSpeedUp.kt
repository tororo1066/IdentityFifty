package tororo1066.identityfifty.talent.hunter

import org.bukkit.Location
import org.bukkit.entity.Player

class EndGameSpeedUp :AbstractHunterTalent ("end_game_speed_up",1,TalentPlane::class.java) {
    override fun lore(): List<String> {
        return listOf()
    }

    override fun onStart(p: Player) {
        p.walkSpeed += 0.0016f
    }

    override fun onFinishedGenerator(dieLocation: Location, remainingGenerator: Int, p: Player) {
        p.walkSpeed -= 0.0016f
        val generator = remainingGenerator + 1
        val speedBuff = 0.008f / generator
        p.walkSpeed += speedBuff
    }
}
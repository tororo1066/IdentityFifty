package tororo1066.identityfifty.talent.hunter

import org.bukkit.Location
import org.bukkit.entity.Player

class FirstGameSpeedUp : AbstractHunterTalent("first_game_speed_up",1,TalentPlane::class.java) {
    override fun lore(): List<String> {
        return listOf()
    }

    override fun onStart(p: Player) {
        p.walkSpeed += 0.008f
    }

    override fun onFinishedGenerator(dieLocation: Location, remainingGenerator: Int, p: Player) {
        p.walkSpeed -= 0.008f
        val speedBuff = 0.008f / (6 - remainingGenerator)
        p.walkSpeed += speedBuff
    }
}
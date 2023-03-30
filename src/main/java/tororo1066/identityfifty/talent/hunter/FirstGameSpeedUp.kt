package tororo1066.identityfifty.talent.hunter

import org.bukkit.Location
import org.bukkit.entity.Player
import tororo1066.identityfifty.IdentityFifty

class FirstGameSpeedUp : AbstractHunterTalent("first_game_speed_up",1,TalentPlane::class.java) {
    override fun lore(): List<String> {
        return listOf()
    }

    private var preventWalkSpeed = 0f

    override fun onStart(p: Player) {
        preventWalkSpeed = 0.008f
        p.walkSpeed += 0.008f
    }

    override fun onFinishedGenerator(dieLocation: Location, remainingGenerator: Int, p: Player) {
        p.walkSpeed -= preventWalkSpeed
        val speedBuff = 0.008f / (IdentityFifty.identityFiftyTask!!.map.generatorGoal - remainingGenerator)
        p.walkSpeed += speedBuff
        preventWalkSpeed = speedBuff
    }
}
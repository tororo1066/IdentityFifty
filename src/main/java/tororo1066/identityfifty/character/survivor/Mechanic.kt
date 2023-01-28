package tororo1066.identityfifty.character.survivor

import org.bukkit.entity.Player
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.data.SurvivorData

class Mechanic: AbstractSurvivor("mechanic") {

    override fun onStart(p: Player) {

    }

    override fun parameters(data: SurvivorData): SurvivorData {
        data.survivorClass = this
        return data
    }

    override fun sheepGeneratorModify(
        damage: Double,
        remainingGenerator: Int,
        maxHealth: Double,
        nowHealth: Double,
        p: Player
    ): Double {
        val players = IdentityFifty.survivors.filter { IdentityFifty.identityFiftyTask?.deadSurvivor?.contains(it.key) == false && IdentityFifty.identityFiftyTask?.escapedSurvivor?.contains(it.key) == false }.size
        val survivors = IdentityFifty.survivors.size
        val multiply = (1 + (survivors - players) * 0.5) + 0.2
        return damage * multiply
    }

    override fun cowGeneratorModify(damage: Double, maxHealth: Double, nowHealth: Double, p: Player): Double {
        val players = IdentityFifty.survivors.filter { IdentityFifty.identityFiftyTask?.deadSurvivor?.contains(it.key) == false && IdentityFifty.identityFiftyTask?.escapedSurvivor?.contains(it.key) == false }.size
        val survivors = IdentityFifty.survivors.size
        val multiply = (1 + (survivors - players) * 0.5) + 0.2
        return damage * multiply
    }

    override fun onDieOtherSurvivor(diePlayer: Player, playerNumber: Int, p: Player) {
        val multiply = 0.04f / playerNumber
        p.walkSpeed = 2f + multiply
    }
}
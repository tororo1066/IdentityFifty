package tororo1066.identityfifty.character.survivor

import org.bukkit.entity.Player
import tororo1066.identityfifty.data.SurvivorData

class Nurse : AbstractSurvivor("医師") {

    override fun onStart(p: Player) {

    }

    override fun parameters(data: SurvivorData): SurvivorData {
        data.survivorClass = this
        return data
    }
}
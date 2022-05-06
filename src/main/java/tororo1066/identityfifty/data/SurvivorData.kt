package tororo1066.identityfifty.data

import tororo1066.identityfifty.character.survivor.AbstractSurvivor
import java.util.UUID

class SurvivorData : PlayerData() {

    lateinit var survivorClass: AbstractSurvivor
    private var health = 5
    var remainingTime = 180
    var footprintsTime = 5
    var helpTick = 120
    var otherPlayerHelpDelay = 0
    var otherPlayerHelpDelayPercentage = 0
    var healProcess = 0.0
    var healingPlayers = HashMap<UUID,SurvivorData>()
    var healTick = 200
    var otherPlayerHealDelay = 100
    var otherPlayerHealDelayPercentage = 0


    fun setHealth(int: Int){
        if (health == 1){
            return
        }
        if (int > 5){
            health = 5
            return
        }
        health = int
    }

    fun setHealth(int: Int, ignoreHealth: Boolean){
        if (ignoreHealth){
            health = int
        } else {
            setHealth(int)
        }
    }

    fun getHealth(): Int {
        return health
    }
}
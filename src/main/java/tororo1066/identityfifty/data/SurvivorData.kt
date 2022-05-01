package tororo1066.identityfifty.data

import tororo1066.identityfifty.character.survivor.AbstractSurvivor

class SurvivorData : PlayerData() {

    lateinit var survivorClass: AbstractSurvivor
    private var health = 5
    var remainingTime = 180
    var footprintsTime = 5
    var helpTick = 60
    var otherPlayerHelpDelay = 0
    var otherPlayerHelpDelayPercentage = 0

    fun setHealth(int: Int){
        if (health <= 1){
            health = 1
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
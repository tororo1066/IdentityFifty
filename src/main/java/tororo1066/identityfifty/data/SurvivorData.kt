package tororo1066.identityfifty.data

import org.bukkit.boss.BossBar
import tororo1066.identityfifty.character.survivor.AbstractSurvivor
import java.util.UUID

class SurvivorData : PlayerData() {

    lateinit var survivorClass: AbstractSurvivor
    private var health = 5
    var remainingTime = 180
    var footprintsCount = 0
    var footprintsTime = 2
    var helpTick = 100
    var otherPlayerHelpDelay = 0
    var otherPlayerHelpDelayPercentage = 0.0
    var healProcess = 0.0
    lateinit var healBossBar: BossBar
    lateinit var helpBossBar: BossBar
    var healingPlayers = HashMap<UUID,SurvivorData>()
    var healTick = 200
    var otherPlayerHealDelay = 100
    var otherPlayerHealDelayPercentage = 0.0
    var heartProcess = 0.0


    fun setHealth(int: Int){
        if (health == 1){
            return
        }
        if (int < 1){
            health = 1
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
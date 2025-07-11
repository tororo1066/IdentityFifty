package tororo1066.identityfifty.data

import org.bukkit.boss.BossBar
import org.bukkit.entity.Player
import tororo1066.identityfifty.character.survivor.AbstractSurvivor
import tororo1066.identityfifty.talent.survivor.AbstractSurvivorTalent
import java.util.UUID

class SurvivorData(player: Player) : PlayerData(player) {

    lateinit var survivorClass: AbstractSurvivor
    /**
     * 体力の数値と状態
     *
     * 5: max(緑)
     *
     * 4: 1.5(橙色)
     *
     * 3: 1(赤)
     *
     * 2: 0.5(濃い赤)
     *
     * 1: 投獄状態
     *
     * 0: 死亡状態
     *
     * -1: クリア状態
     */
    private var health = 5
    var remainingTime = 180
    var footprintsCount = 0
    var footprintsTime = 2.0
    var footprintsModify = HashMap<UUID,Double>()
    var helpTick = 100
    var otherPlayerHelpDelay = 0
    var otherPlayerHelpDelayPercentage = 0.0
    var healProcess = 0.0
    lateinit var healBossBar: BossBar
    lateinit var helpBossBar: BossBar
    lateinit var hatchBossBar: BossBar
    var healingPlayers = HashMap<UUID,SurvivorData>()
    var healTick = 240
    var healTickModify = HashMap<UUID,Double>()
    var otherPlayerHealDelay = 0
    var otherPlayerHealDelayPercentage = 0.0
    var hatchTick = 200
    var heartProcess = 0.0
    var heartProcessRules = arrayListOf(Pair(25.0,0.2), Pair(20.0,0.1), Pair(15.0,0.2), Pair(10.0,0.5))
    var healSmallHealth = true
    var canHelpSelf = false
    var canHealSelf = false

    var cancelGeneratorAttack = false
    var onWindow = false

    val talentClasses = HashMap<Class<out AbstractSurvivorTalent>,AbstractSurvivorTalent>()
    var talentCost = 10

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
package tororo1066.identityfifty.talent.survivor

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import tororo1066.identityfifty.IdentityFifty
import tororo1066.tororopluginapi.lang.SLang.Companion.translate

class PlateSpeedUp : AbstractSurvivorTalent("plate_speed_up",2,HelpSpeedUp::class.java,) {
    override fun lore(): List<String> {
        return listOf("plate_speed_up_lore_1","plate_speed_up_lore_2")
    }

    private var boostCoolTime = 0
    private var windowBoostCoolTime = 0

    override fun onWoodPlate(loc: Location, p: Player) {
        if (boostCoolTime <= 0) {
            p.addPotionEffect(PotionEffect(PotionEffectType.SPEED,70,1))

            boostCoolTime = 40
            IdentityFifty.util.repeatDelay(amount = 40, repeatTick = 20, delayTick = 20, unit = {
                boostCoolTime--
            })
        }
    }

    override fun onExitWindow(p: Player) {
        if (windowBoostCoolTime <= 0) {
            p.addPotionEffect(PotionEffect(PotionEffectType.SPEED,70,1))

            windowBoostCoolTime = 40
            IdentityFifty.util.repeatDelay(amount = 40, repeatTick = 20, delayTick = 20, unit = {
                windowBoostCoolTime--
            })
        }
    }

    override fun scoreboards(p: Player): ArrayList<Pair<Int, String>> {
        val list = arrayListOf(-10 to translate("plate_speed_up_scoreboard"))
        if (boostCoolTime > 0) {
            list.add(-11 to translate("plate_speed_up_scoreboard_plate", boostCoolTime))
        } else {
            list.add(-11 to translate("plate_speed_up_scoreboard_plate_usable"))
        }

        if (windowBoostCoolTime > 0) {
            list.add(-12 to translate("plate_speed_up_scoreboard_window", windowBoostCoolTime))
        } else {
            list.add(-12 to translate("plate_speed_up_scoreboard_window_usable"))
        }

        return list
    }
}
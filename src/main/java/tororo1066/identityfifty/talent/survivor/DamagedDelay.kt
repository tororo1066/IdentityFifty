package tororo1066.identityfifty.talent.survivor

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import tororo1066.identityfifty.IdentityFifty
import tororo1066.tororopluginapi.lang.SLang.Companion.translate

class DamagedDelay : AbstractSurvivorTalent("damaged_delay", 5, FullCowUp::class.java) {

    override fun lore(): List<String> {
        return listOf("damaged_delay_lore_1", "damaged_delay_lore_2")
    }

    private var cooldown = 0
    private var willDamage = false
    private var willDamageTime = 0

    override fun onStart(p: Player) {
        tasks.add(Bukkit.getScheduler().runTaskTimer(IdentityFifty.plugin, Runnable {
            if (willDamage) {
                willDamageTime--
                if (willDamageTime <= 0) {
                    willDamage = false
                }
            }
            if (cooldown > 0) {
                cooldown--
            }
        }, 0, 20))
    }

    override fun onDamage(damage: Int, toHealth: Int, stun: Boolean, damager: Player, p: Player): Pair<Boolean, Int> {
        if (toHealth == 1 && cooldown <= 0) {
            cooldown = 240
            willDamage = true
            willDamageTime = 20
            val data = IdentityFifty.survivors[p.uniqueId]!!
            Bukkit.getScheduler().runTaskLater(IdentityFifty.plugin, Runnable {
                data.setHealth(data.getHealth() - damage)
                p.damage(0.0, damager)
            }, 400)
            return Pair(true, 0)


        }
        return Pair(true, damage)
    }

    override fun scoreboards(p: Player): ArrayList<Pair<Int, String>> {
        val list = arrayListOf<Pair<Int, String>>()
        list.add(-13 to translate("damaged_delay_scoreboard"))
        if (willDamage) {
            list.add(-14 to translate("damaged_delay_scoreboard_will_damage", willDamageTime))
        } else {
            if (cooldown > 0) {
                list.add(-14 to translate("damaged_delay_scoreboard_cooldown", cooldown))
            } else {
                list.add(-14 to translate("damaged_delay_scoreboard_usable"))
            }
        }
        return list
    }
}
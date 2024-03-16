package tororo1066.identityfifty.talent.survivor

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import tororo1066.identityfifty.IdentityFifty

class DamagedDelay : AbstractSurvivorTalent("damaged_delay",5,FullCowUp::class.java) {

    override fun lore(): List<String> {
        return listOf("damaged_delay_lore_1","damaged_delay_lore_2")
    }

    //var canfire = false

//    override fun onStart(p: Player) {
//        tasks.add(Bukkit.getScheduler().runTaskTimer(IdentityFifty.plugin, Runnable {
//            val suvdata = IdentityFifty.survivors[p.uniqueId]!!
//            val health = suvdata.getHealth()
//        },0,5))
//    }

    var canUse = true

    override fun onDamage(damage: Int, toHealth: Int, stun: Boolean, damager: Player, p: Player): Pair<Boolean, Int> {
        if (toHealth == 1 && canUse){
            canUse = false
            val data = IdentityFifty.survivors[p.uniqueId]!!
            Bukkit.getScheduler().runTaskLater(IdentityFifty.plugin, Runnable {
                data.setHealth(data.getHealth() - damage)
                p.damage(0.0, damager)
            },400)
            Bukkit.getScheduler().runTaskLater(IdentityFifty.plugin, Runnable {
                canUse = true
            },4800)
            return Pair(true,0)


        }
        return Pair(true,damage)
    }
}
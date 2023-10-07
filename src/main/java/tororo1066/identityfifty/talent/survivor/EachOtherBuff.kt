package tororo1066.identityfifty.talent.survivor

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.inventivetalent.glow.GlowAPI
import tororo1066.identityfifty.IdentityFifty

class EachOtherBuff : AbstractSurvivorTalent ("each_other_buff",5,FullSheepUp::class.java) {

    override fun lore(): List<String> {
        return listOf("each_other_buff_lore_1","each_other_buff_lore_2")
    }

    override fun onStart(p: Player) {
        tasks.add(Bukkit.getScheduler().runTaskTimer(IdentityFifty.plugin, Runnable {
            val playerData = IdentityFifty.survivors[p.uniqueId]!!
            IdentityFifty.survivors.forEach{ (uuid,data) ->
                if(uuid == p.uniqueId){
                    return@forEach
                }
                val targetPlayer = Bukkit.getPlayer(uuid)?:return@forEach
                val health = data.getHealth()


                if(health in 2..4){
                    data.glowManager.glow(mutableListOf(p), GlowAPI.Color.YELLOW,20)
                    playerData.glowManager.glow(mutableListOf(targetPlayer),GlowAPI.Color.YELLOW,20)
                }
            }
        },0,19))
    }

    override fun sheepGeneratorModify(damage: Double, remainingGenerator: Int, maxHealth: Double, nowHealth: Double, p: Player): Double {
        val playerData = IdentityFifty.survivors[p.uniqueId]!!
        val playerList = p.location.getNearbyPlayers(5.0).filter {
            if (it == p)return@filter false
            val data = IdentityFifty.survivors[it.uniqueId]?:return@filter false
            data.getHealth() == 5
        }
        if (playerList.isNotEmpty()){
            return damage * 1.15
        }
        return damage
    }

    override fun cowGeneratorModify(damage: Double, maxHealth: Double, nowHealth: Double, p: Player): Double {
        val playerData = IdentityFifty.survivors[p.uniqueId]!!
        val playerList = p.location.getNearbyPlayers(5.0).filter {
            if (it == p)return@filter false
            val data = IdentityFifty.survivors[it.uniqueId]?:return@filter false
            data.getHealth() == 5
        }
        if (playerList.isNotEmpty()){
            return damage * 1.15
        }
        return damage
    }
}
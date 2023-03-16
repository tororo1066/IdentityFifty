package tororo1066.identityfifty.talent.survivor

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player
import tororo1066.identityfifty.IdentityFifty

class GeneratorHeal : AbstractSurvivorTalent("generator_heal",1,TalentPlane::class.java) {
    override fun lore(): List<String> {
        return listOf()
    }

    var generator = true

    override fun onFinishedGenerator(dieLocation: Location, remainingGenerator: Int, p: Player) {
        if (generator) {
            dieLocation.getNearbyPlayers(5.0).filter {it == p}.forEach{
                val data = IdentityFifty.survivors[p.uniqueId]!!
                data.setHealth(data.getHealth() + 1)
                p.playSound(p.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
            }
            generator = false
            Bukkit.getScheduler().runTaskLater(IdentityFifty.plugin, Runnable {
                generator = true
            },1200)
        }
    }
}
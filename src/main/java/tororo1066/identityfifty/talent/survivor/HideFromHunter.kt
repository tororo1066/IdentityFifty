package tororo1066.identityfifty.talent.survivor

import org.bukkit.Bukkit
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import tororo1066.identityfifty.IdentityFifty
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import java.util.UUID

class HideFromHunter: AbstractSurvivorTalent("hide_from_hunter", 5, WoundedCowUp::class.java) {

    override fun lore(): List<String> {
        return listOf("hide_from_hunter_lore_1", "hide_from_hunter_lore_2")
    }

    private var cooldown = 0

    override fun onStart(p: Player) {
        tasks.add(Bukkit.getScheduler().runTaskTimer(IdentityFifty.plugin, Runnable {
            if (cooldown > 0) {
                return@Runnable
            }
            p.location.getNearbyPlayers(4.0)
                .firstOrNull { it.uniqueId in IdentityFifty.hunters} ?: return@Runnable

            p.world.spawnParticle(Particle.SMOKE, p.location, 10, 0.5, 0.5, 0.5, 0.1)
            p.world.playSound(p.location, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f)
            p.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, 60, 0))
            val data = IdentityFifty.survivors[p.uniqueId]!!
            val uuid = UUID.randomUUID()
            data.footprintsModify[uuid] = 0.0
            Bukkit.getScheduler().runTaskLater(IdentityFifty.plugin, Runnable {
                data.footprintsModify.remove(uuid)
            }, 60)
            cooldown = 180

            IdentityFifty.util.repeatDelay(amount = 180, repeatTick = 20, delayTick = 20, unit = {
                cooldown--
            })

        }, 0, 5))
    }

    override fun scoreboards(p: Player): ArrayList<Pair<Int, String>> {
        val list = arrayListOf<Pair<Int, String>>()
        list.add(-15 to translate("hide_from_hunter_scoreboard"))
        if (cooldown > 0) {
            list.add(-16 to translate("hide_from_hunter_scoreboard_cooldown", cooldown))
        } else {
            list.add(-16 to translate("hide_from_hunter_scoreboard_usable"))
        }
        return list
    }
}
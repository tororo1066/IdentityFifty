package tororo1066.identityfifty.talent.hunter

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import tororo1066.identityfifty.IdentityFifty
import tororo1066.tororopluginapi.lang.SLang.Companion.sendTranslateMsg
import java.util.*

class SurvivorHealedStop : AbstractHunterTalent("survivor_healed_stop",2,RemainTimeDown::class.java) {
    override fun lore(): List<String> {
        return listOf("survivor_healed_stop_lore_1","survivor_healed_stop_lore_2")
    }

    override fun onSurvivorHeal(healPlayer: Player, healedPlayer: Player, p: Player) {
        healedPlayer.addPotionEffect(PotionEffect(PotionEffectType.SLOW,140,1))
        val data = IdentityFifty.survivors[healedPlayer.uniqueId]!!
        val uuid = UUID.randomUUID()
        data.footprintsModify += uuid to 2.0
        Bukkit.getScheduler().runTaskLater(IdentityFifty.plugin, Runnable {
            data.footprintsModify -= uuid
        }, 200)
    }
}
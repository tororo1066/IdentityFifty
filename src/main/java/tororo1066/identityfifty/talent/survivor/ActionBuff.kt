package tororo1066.identityfifty.talent.survivor

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.data.PrisonData
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import kotlin.math.floor

class ActionBuff : AbstractSurvivorTalent("action_buff", 2, RemainTimeUp::class.java) {

    override fun lore(): List<String> {
        return listOf("action_buff_lore_1", "action_buff_lore_2")
    }

    override fun onJail(prisonData: PrisonData, p: Player) {
        IdentityFifty.hunters.forEach { (uuid, data)  ->
            Bukkit.getPlayer(uuid)?:return@forEach
            data.glowManager.glow(IdentityFifty.survivors.mapNotNull { map -> map.key.toPlayer() }.toMutableList(), GlowColor.RED,100)
        }
    }

}
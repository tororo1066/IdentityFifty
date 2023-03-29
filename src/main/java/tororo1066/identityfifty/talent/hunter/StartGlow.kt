package tororo1066.identityfifty.talent.hunter

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.inventivetalent.glow.GlowAPI
import tororo1066.identityfifty.IdentityFifty

class StartGlow : AbstractHunterTalent("start_glow",2,GateOpenHunterBuff::class.java) {
    override fun lore(): List<String> {
        return listOf()
    }

    override fun onStart(p: Player) {
        IdentityFifty.survivors.forEach { (uuid , data) ->
            Bukkit.getPlayer(uuid)?:return@forEach
            data.glowManager.glow(mutableListOf(p), GlowAPI.Color.RED,80)
        }
    }
}
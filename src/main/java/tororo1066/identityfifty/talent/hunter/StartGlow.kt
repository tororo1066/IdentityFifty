package tororo1066.identityfifty.talent.hunter

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.inventivetalent.glow.GlowAPI
import tororo1066.identityfifty.IdentityFifty
import tororo1066.tororopluginapi.utils.toPlayer

class StartGlow : AbstractHunterTalent("start_glow",2,GateOpenHunterBuff::class.java) {
    override fun lore(): List<String> {
        return listOf()
    }

    override fun onStart(p: Player) {
        val random = IdentityFifty.survivors.entries.random()
        random.key.toPlayer()?:return
        random.value.glowManager.glow(mutableListOf(p), GlowAPI.Color.RED,80)
    }
}
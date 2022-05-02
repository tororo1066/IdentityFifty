package tororo1066.identityfifty.data

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.inventivetalent.glow.GlowAPI
import tororo1066.identityfifty.IdentityFifty
import java.util.UUID
import java.util.function.Consumer

class GlowManager(val uuid: UUID) {
    var isGlowing = false
    var otherGlow = false

    fun glow(seeablePlayer: MutableCollection<Player>, color: GlowAPI.Color, duration: Int){
        if (isGlowing){
            otherGlow = true
        }

        val p = Bukkit.getPlayer(uuid)!!
        var tick = duration
        GlowAPI.setGlowing(p,color,seeablePlayer)
        Bukkit.getScheduler().runTaskTimer(IdentityFifty.plugin, Consumer {
            if (tick <= 0){
                isGlowing = false
                GlowAPI.setGlowing(p,false,seeablePlayer)
                it.cancel()
                return@Consumer
            }

            if (otherGlow){
                otherGlow = false
                it.cancel()
            }

            tick--
        },0,1)
    }
}
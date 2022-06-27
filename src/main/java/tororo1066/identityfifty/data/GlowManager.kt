package tororo1066.identityfifty.data

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.inventivetalent.glow.GlowAPI
import tororo1066.identityfifty.IdentityFifty
import java.util.UUID
import java.util.function.Consumer

class GlowManager(private val uuid: UUID) {
    var isGlowing = false
    var otherGlow = false
    var tick = 0
    fun glow(seeablePlayer: MutableCollection<Player>, color: GlowAPI.Color, duration: Int){
        if (isGlowing){
            if (duration < tick){
                return
            }
            otherGlow = true
        }

        val p = Bukkit.getPlayer(uuid)!!

        tick = duration

        GlowAPI.setGlowing(p,color,seeablePlayer)
        Bukkit.getScheduler().runTaskTimer(IdentityFifty.plugin, Consumer {

            if (otherGlow){
                otherGlow = false
                it.cancel()
            }

            if (tick <= 0){
                isGlowing = false
                GlowAPI.setGlowing(p,false,seeablePlayer)
                if (IdentityFifty.survivors.containsKey(uuid)){
                    IdentityFifty.identityFiftyTask?.survivorTeam?.addEntry(p.name)
                }

                if (IdentityFifty.hunters.containsKey(uuid)){
                    IdentityFifty.identityFiftyTask?.hunterTeam?.addEntry(p.name)
                }

                it.cancel()
                return@Consumer
            }

            if (!GlowAPI.isGlowing(p,seeablePlayer,false)){
                GlowAPI.setGlowing(p,color,seeablePlayer)
            }

            tick--
        },0,1)
    }
}
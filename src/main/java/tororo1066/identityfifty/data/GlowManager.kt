package tororo1066.identityfifty.data

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.inventivetalent.glow.GlowAPI
import tororo1066.identityfifty.IdentityFifty
import tororo1066.tororopluginapi.utils.toPlayer
import java.util.UUID
import java.util.function.Consumer

class GlowManager(private val uuid: UUID) {
    private var isGlowing = false
    private var otherGlow = false
    private var tick = 0
    private val nowSeeablePlayers = ArrayList<UUID>()

    fun glow(seeablePlayer: MutableCollection<Player>, color: GlowAPI.Color, duration: Int){
        if (isGlowing){
            if (duration < tick){
                val p = Bukkit.getPlayer(uuid)?:return
                for (player in seeablePlayer){
                    if (!GlowAPI.isGlowing(p,player)){
                        GlowAPI.setGlowing(p,color,player)
                        nowSeeablePlayers.add(player.uniqueId)
                    }
                }
                return
            }
            otherGlow = true
        }

        val p = Bukkit.getPlayer(uuid)?:return

        tick = duration

        GlowAPI.setGlowing(p,color,seeablePlayer)
        for (player in seeablePlayer){
            nowSeeablePlayers.add(player.uniqueId)
        }
        Bukkit.getScheduler().runTaskTimer(IdentityFifty.plugin, Consumer {


            if (tick <= 0 || otherGlow){
                otherGlow = false
                isGlowing = false
                GlowAPI.setGlowing(p,false, nowSeeablePlayers.mapNotNull { map -> map.toPlayer() })
                if (IdentityFifty.survivors.containsKey(uuid)){
                    IdentityFifty.identityFiftyTask?.survivorTeam?.addEntry(p.name)
                }

                if (IdentityFifty.hunters.containsKey(uuid)){
                    IdentityFifty.identityFiftyTask?.hunterTeam?.addEntry(p.name)
                }

                it.cancel()
                return@Consumer
            }

            GlowAPI.setGlowing(p,false,seeablePlayer)

            GlowAPI.setGlowing(p,color,seeablePlayer)

            tick--
        },0,1)
    }
}
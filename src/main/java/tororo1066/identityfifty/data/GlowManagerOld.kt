package tororo1066.identityfifty.data

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.inventivetalent.glow.GlowAPI
import tororo1066.identityfifty.IdentityFifty
import tororo1066.tororopluginapi.utils.toPlayer
import java.util.UUID
import java.util.function.Consumer

class GlowManagerOld(private val uuid: UUID) {
    private var isGlowing = false
    private var tick = 0
    private val nowVisiblePlayers = ArrayList<UUID>()
    private val glowingColors = HashMap<UUID,GlowAPI.Color>()

    fun glow(visiblePlayers: MutableCollection<Player>, color: GlowAPI.Color, duration: Int){
        visiblePlayers.addAll(IdentityFifty.spectators.keys.mapNotNull { it.toPlayer() })
        if (isGlowing){
            if (duration < tick){
                val p = Bukkit.getPlayer(uuid)?:return
                for (player in visiblePlayers){
                    if (!GlowAPI.isGlowing(p,player)){
                        GlowAPI.setGlowing(p,color,player)
                        nowVisiblePlayers.add(player.uniqueId)
                    }
                }
                return
            }
        }

        val p = Bukkit.getPlayer(uuid)?:return

        tick = duration

        GlowAPI.setGlowing(p,color,visiblePlayers)
        for (player in visiblePlayers){
            nowVisiblePlayers.add(player.uniqueId)
        }
        Bukkit.getScheduler().runTaskTimer(IdentityFifty.plugin, Consumer {


            if (tick <= 0){
                isGlowing = false
                GlowAPI.setGlowing(p,false, nowVisiblePlayers.mapNotNull { map -> map.toPlayer() })
                if (IdentityFifty.survivors.containsKey(uuid)){
                    IdentityFifty.identityFiftyTask?.survivorTeam?.addEntry(p.name)
                }

                if (IdentityFifty.hunters.containsKey(uuid)){
                    IdentityFifty.identityFiftyTask?.hunterTeam?.addEntry(p.name)
                }
                nowVisiblePlayers.clear()
                glowingColors.clear()

                it.cancel()
                return@Consumer
            }

            glowingColors.entries.groupBy { groupBy -> groupBy.value }.forEach { (color, list) ->
                val players = list.mapNotNull { map -> map.key.toPlayer() }
                GlowAPI.setGlowing(p, false, players)
                GlowAPI.setGlowing(p, color, players)
            }

            tick--
        },0,1)
    }
}
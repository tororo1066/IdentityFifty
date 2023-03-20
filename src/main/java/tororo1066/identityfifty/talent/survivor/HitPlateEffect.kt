package tororo1066.identityfifty.talent.survivor

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.inventivetalent.glow.GlowAPI
import tororo1066.identityfifty.IdentityFifty

class HitPlateEffect : AbstractSurvivorTalent("hit_plate_effect",2,HighHitPlate::class.java) {
    override fun lore(): List<String> {
        return listOf()
    }

    override fun onHitWoodPlate(hittedPlayer: Player, loc: Location, blindTime: Int, slowTime: Int, p: Player): Pair<Int, Int> {
        val players = ArrayList<Player>()
        IdentityFifty.survivors.forEach { (uuid, _) ->
            val playerinfo = Bukkit.getPlayer(uuid)?:return@forEach
            players.add(playerinfo)
        }
        IdentityFifty.hunters.forEach { (uuid, data)  ->
            Bukkit.getPlayer(uuid)?:return@forEach
            data.glowManager.glow(players, GlowAPI.Color.RED,100)
        }
        return Pair(blindTime + 20 , slowTime)
    }
}
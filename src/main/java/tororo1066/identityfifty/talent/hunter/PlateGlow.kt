package tororo1066.identityfifty.talent.hunter

import org.bukkit.Location
import org.bukkit.entity.Player
import org.inventivetalent.glow.GlowAPI
import tororo1066.identityfifty.IdentityFifty

class PlateGlow :AbstractHunterTalent("plate_glow",2,TalentPlane::class.java) {
    override fun lore(): List<String> {
        return listOf()
    }


    override fun onDamagedWoodPlate(usedPlayer: Player, loc: Location, blindTime: Int, slowTime: Int, p: Player): Pair<Int, Int> {
        val data = IdentityFifty.survivors[usedPlayer.uniqueId]
        if (data != null) {
            data.glowManager.glow(mutableListOf(p),GlowAPI.Color.RED,60)
        }
        return Pair(blindTime, slowTime)
    }
}
package tororo1066.identityfifty.talent.hunter

import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.entity.Player
import tororo1066.identityfifty.IdentityFifty

class PlateGlow: AbstractHunterTalent("plate_glow",2,HelpSpeedDown::class.java) {
    override fun lore(): List<String> {
        return listOf("plate_glow_lore_1")
    }


    override fun onDamagedWoodPlate(usedPlayer: Player, loc: Location, blindTime: Int, slowTime: Int, p: Player): Pair<Int, Int> {
        val data = IdentityFifty.survivors[usedPlayer.uniqueId]?:return Pair(blindTime, slowTime)
        data.glowManager.glow(mutableListOf(p), ChatColor.RED,100)
        return super.onDamagedWoodPlate(usedPlayer, loc, blindTime, slowTime, p)
    }
}
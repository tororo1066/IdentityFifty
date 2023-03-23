package tororo1066.identityfifty.talent.hunter

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.data.PrisonData

class SurvivorJailedSpeedUp :AbstractHunterTalent("survivor_jailed_speed_up",1,TalentPlane::class.java) {
    override fun lore(): List<String> {
        return listOf()
    }



    override fun onSurvivorJail(survivor: Player, prison: PrisonData, p: Player) {
        p.walkSpeed += 0.005f
    }

    override fun onSurvivorHelp(helper: Player, gotHelpPlayer: Player, p: Player) {
        p.walkSpeed -= 0.005f
    }

}
package tororo1066.identityfifty.talent.hunter

import org.bukkit.entity.Player
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.data.PrisonData


class RemainTimeDown: AbstractHunterTalent("remain_time_down",2,LowHitPlate::class.java) {
    override fun lore(): List<String> {
        return listOf("remain_time_down_lore_1")
    }

    //override fun onStart(p: Player) {
        //IdentityFifty.survivors.forEach { (_, data)->
            //data.remainingTime -= 10
        //}
    //}旧システム

    override fun onSurvivorJail(survivor: Player, prison: PrisonData, p: Player) {
        val data = IdentityFifty.survivors[survivor.uniqueId]!!
        data.remainingTime -= 7
    }
}


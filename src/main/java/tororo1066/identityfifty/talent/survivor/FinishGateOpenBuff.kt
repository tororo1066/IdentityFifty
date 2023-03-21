package tororo1066.identityfifty.talent.survivor

import org.bukkit.Location
import org.bukkit.entity.Player
import tororo1066.identityfifty.IdentityFifty

class FinishGateOpenBuff :AbstractSurvivorTalent("finish_gate_open_buff",2,HelpSpeedUp::class.java) {
    override fun lore(): List<String> {
        return listOf("finish_gate_open_buff_lore_1")
    }

    override fun onFinishedEscapeGenerator(dieLocation: Location, p: Player) {
        dieLocation.getNearbyPlayers(5.0).filter { it == p}.forEach {
            val data = IdentityFifty.survivors[p.uniqueId]!!
            data.healTick = (data.healTick * 0.5).toInt()
            data.helpTick = (data.helpTick * 0.5).toInt()
        }
    }

    }
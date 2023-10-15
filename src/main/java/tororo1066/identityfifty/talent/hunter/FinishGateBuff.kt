package tororo1066.identityfifty.talent.hunter

import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import tororo1066.identityfifty.enumClass.StunState

class FinishGateBuff :AbstractHunterTalent("finish_gate_buff",2,GateOpenHunterBuff::class.java) {

    var start = false

    override fun lore(): List<String> {
        return listOf("finish_gate_buff_lore_1")
    }

    override fun onFinishedEscapeGenerator(dieLocation: Location, p: Player) {
        p.playSound(p.location, Sound.ENTITY_WITHER_DEATH,1f,1f)
        start = true
//        p.addPotionEffect(PotionEffect(PotionEffectType.SPEED,240000,0))
//        p.addPotionEffect(PotionEffect(PotionEffectType.FIRE_RESISTANCE,240000,1))
//        p.addPotionEffect(PotionEffect(PotionEffectType.NIGHT_VISION,240000,1))
//        p.addPotionEffect(PotionEffect(PotionEffectType.REGENERATION,240000,1))
    }

    override fun onStun(blindTime: Int, slowTime: Int, state: StunState, p: Player): Pair<Int, Int> {
        return if (start && (state == StunState.AIRSWING || state == StunState.DAMAGED)){
            Pair(0,0)
        } else {
            super.onStun(blindTime, slowTime, state, p)
        }
    }
}
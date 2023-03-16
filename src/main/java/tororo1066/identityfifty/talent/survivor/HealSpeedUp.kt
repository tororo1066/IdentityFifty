package tororo1066.identityfifty.talent.survivor

import tororo1066.identityfifty.data.SurvivorData

class HealSpeedUp : AbstractSurvivorTalent("heal_speed_up",1, TalentPlane::class.java) {
    override fun lore(): List<String> {
        return listOf("heal_speed_up_lore_1")
    }

    override fun parameters(data: SurvivorData): SurvivorData {
        data.healTick = (data.healTick * 0.95).toInt()
        return data
    }
}
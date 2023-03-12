package tororo1066.identityfifty.talent.clazz

import tororo1066.identityfifty.data.SurvivorData

class HealSpeedUp : AbstractSurvivorTalent("heal_speed_up", TalentPlane()) {
    override fun parameters(data: SurvivorData): SurvivorData {
        data.healTick = (data.healTick * 0.95).toInt()
        return data
    }
}
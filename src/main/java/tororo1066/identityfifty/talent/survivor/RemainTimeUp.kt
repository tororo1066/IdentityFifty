package tororo1066.identityfifty.talent.survivor

import tororo1066.identityfifty.data.SurvivorData

class RemainTimeUp : AbstractSurvivorTalent("remain_time_up",2,HealSpeedUp::class.java) {
    override fun lore(): List<String> {
        return listOf("remain_time_up_lore_1")
    }

    override fun parameters(data: SurvivorData): SurvivorData {
        data.remainingTime = (data.remainingTime + 20)
        return data
    }

}
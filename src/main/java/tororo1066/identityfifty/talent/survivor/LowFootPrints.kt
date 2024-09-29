package tororo1066.identityfifty.talent.survivor

import tororo1066.identityfifty.data.SurvivorData

class LowFootPrints : AbstractSurvivorTalent("low_foot_prints",2,HealSpeedUp::class.java) {

    override fun lore(): List<String> {
        return listOf("low_foot_prints_lore_1")
    }
    override fun parameters(data: SurvivorData): SurvivorData {
        data.footprintsTime = (data.footprintsTime * 0.9)
        return data
    }
}
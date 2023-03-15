package tororo1066.identityfifty.talent.survivor

import tororo1066.identityfifty.data.SurvivorData

class LowFootPrints : AbstractSurvivorTalent("low_foot_prints",1,HealSpeedUp::class.java) {

    override fun lore(): List<String> {
        return listOf()
    }
    override fun parameters(data: SurvivorData): SurvivorData {
        data.footprintsTime = (data.footprintsTime * 0.8)
        return data
    }
}
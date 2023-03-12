package tororo1066.identityfifty.talent.survivor

import tororo1066.identityfifty.data.SurvivorData

class LowFootPrints : AbstractSurvivorTalent("low_foot_prints",HealSpeedUp()) {
    override fun parameters(data: SurvivorData): SurvivorData {
        data.footprintsTime = (data.footprintsTime * 0.8)
        return data
    }
}
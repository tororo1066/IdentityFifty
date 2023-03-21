package tororo1066.identityfifty.talent.survivor

import tororo1066.identityfifty.data.SurvivorData

class HatchLow : AbstractSurvivorTalent("hatch_low",1,TalentPlane::class.java) {
    override fun lore(): List<String> {
        return listOf("hatch_low_lore_1")
    }

    override fun parameters(data: SurvivorData): SurvivorData {
        data.hatchTick = (data.hatchTick * 0.9).toInt()
        return data
    }
}
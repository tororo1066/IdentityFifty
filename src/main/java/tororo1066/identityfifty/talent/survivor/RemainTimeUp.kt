package tororo1066.identityfifty.talent.survivor

import tororo1066.identityfifty.data.SurvivorData

class RemainTimeUp : AbstractSurvivorTalent("RemainTimeUp",1,HelpSpeedUp::class.java) {
    override fun lore(): List<String> {
        return listOf()
    }

    override fun parameters(data: SurvivorData): SurvivorData {
        data.remainingTime = (data.remainingTime + 20)
        return data
    }

}
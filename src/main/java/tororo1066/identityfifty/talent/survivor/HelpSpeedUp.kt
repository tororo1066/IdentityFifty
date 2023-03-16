package tororo1066.identityfifty.talent.survivor

import tororo1066.identityfifty.data.SurvivorData

class HelpSpeedUp : AbstractSurvivorTalent("help_speed_up",1,TalentPlane::class.java) {
    override fun lore(): List<String> {
        return listOf("help_speed_up_lore_1")
    }

    override fun parameters(data: SurvivorData): SurvivorData {
        data.helpTick = (data.helpTick * 0.8).toInt()
        return data
    }

}
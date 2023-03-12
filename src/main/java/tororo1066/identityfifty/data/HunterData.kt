package tororo1066.identityfifty.data

import tororo1066.identityfifty.character.hunter.AbstractHunter
import tororo1066.identityfifty.talent.hunter.AbstractHunterTalent

class HunterData : PlayerData() {
    lateinit var hunterClass: AbstractHunter
    val talentClasses = arrayListOf<AbstractHunterTalent>()
}
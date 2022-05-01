package tororo1066.identityfifty.data

import tororo1066.identityfifty.character.hunter.AbstractHunter

class HunterData : PlayerData() {
    lateinit var hunterClass: AbstractHunter
    var delayHelp = 0
    var delayHelpPercentage = 0
}
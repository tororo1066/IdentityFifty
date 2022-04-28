package tororo1066.identityfifty.data

import tororo1066.identityfifty.character.survivor.AbstractSurvivor

class SurvivorData : PlayerData() {

    lateinit var survivorClass: AbstractSurvivor
    var health = 4
}
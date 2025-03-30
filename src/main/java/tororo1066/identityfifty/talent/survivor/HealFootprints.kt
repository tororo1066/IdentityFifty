package tororo1066.identityfifty.talent.survivor

class HealFootprints: AbstractSurvivorTalent("heal_footprints", 5) {
    override fun lore(): List<String> {
        return listOf(
            "heal_footprints_lore_1"
        )
    }
    //回復された側の足跡を数秒間消す
}
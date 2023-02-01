package tororo1066.identityfifty.character.survivor

import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.data.SurvivorData
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sItem.SItem

class Mechanic: AbstractSurvivor("mechanic") {

    override fun onStart(p: Player) {
        super.onStart(p)
        val passiveItem = SItem(Material.STICK).setDisplayName(translate("passive")).setCustomModelData(8)
            .addLore(translate("mechanic_passive_lore_1"))
            .addLore(translate("mechanic_passive_lore_2"))
            .addLore(translate("mechanic_passive_lore_2"))

        val slowSkill = SItem(Material.STICK).setDisplayName(translate("slow_timer")).setCustomModelData(100)
            .addLore(translate("slow_timer_lore_1"))
            .addLore(translate("slow_timer_lore_2"))

        val slowSkillItem = IdentityFifty.interactManager.createSInteractItem(slowSkill,true).setInteractEvent { _, _ ->
            p.playSound(p.location, Sound.UI_BUTTON_CLICK, 2f, 1f)
            p.location.getNearbyPlayers(8.0).forEach {
                it.playSound(p.location, Sound.UI_BUTTON_CLICK, 2f, 1f)
                it.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 80, 1, false))
            }
            return@setInteractEvent true
        }.setInitialCoolDown(1200)

        p.inventory.addItem(passiveItem)
        p.inventory.addItem(slowSkillItem)
    }

    override fun parameters(data: SurvivorData): SurvivorData {
        data.survivorClass = this
        data.helpTick = 200
        data.healTick = 340
        return data
    }

    override fun sheepGeneratorModify(
        damage: Double,
        remainingGenerator: Int,
        maxHealth: Double,
        nowHealth: Double,
        p: Player
    ): Double {
        val players = IdentityFifty.survivors.filter { IdentityFifty.identityFiftyTask?.deadSurvivor?.contains(it.key) == false && IdentityFifty.identityFiftyTask?.escapedSurvivor?.contains(it.key) == false }.size
        val survivors = IdentityFifty.survivors.size
        val multiply = (1 + (survivors - players) * 0.5) + 0.2
        return damage * multiply
    }

    override fun cowGeneratorModify(damage: Double, maxHealth: Double, nowHealth: Double, p: Player): Double {
        val players = IdentityFifty.survivors.filter { IdentityFifty.identityFiftyTask?.deadSurvivor?.contains(it.key) == false && IdentityFifty.identityFiftyTask?.escapedSurvivor?.contains(it.key) == false }.size
        val survivors = IdentityFifty.survivors.size
        val multiply = (1 + (survivors - players) * 0.5) + 0.2
        return damage * multiply
    }

    override fun onDieOtherSurvivor(diePlayer: Player, playerNumber: Int, p: Player) {
        val multiply = 0.04f / playerNumber
        p.walkSpeed = 0.2f + multiply
    }
}
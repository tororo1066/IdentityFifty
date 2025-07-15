package tororo1066.identityfifty.character.survivor

import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.data.SurvivorData
import tororo1066.identityfifty.enumClass.AllowAction
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sItem.SItem
import tororo1066.tororopluginapi.utils.addItem

class Mechanic: AbstractSurvivor("mechanic") {

    override fun onStart(p: Player) {
        super.onStart(p)
        val passiveItem = SItem(Material.STICK).setDisplayName(translate("passive")).setCustomModelData(8)
            .addLore(translate("mechanic_passive_lore_1"))
            .addLore(translate("mechanic_passive_lore_2"))
            .addLore(translate("mechanic_passive_lore_3"))

        val slowSkill = SItem(Material.STICK).setDisplayName(translate("slow_timer")).setCustomModelData(12)
            .addLore(translate("slow_timer_lore_1"))
            .addLore(translate("slow_timer_lore_2"))

        val slowSkillItem = IdentityFifty.interactManager.createSInteractItem(slowSkill,true).setInteractEvent { e, _ ->
            val player = e.player
            IdentityFifty.util.repeatDelay(3,7) {
                player.playSound(player.location, Sound.UI_BUTTON_CLICK, 2f, 1f)
            }
            player.location.getNearbyPlayers(10.0).forEach {
                if (it == player)return@forEach
                it.playSound(player.location, Sound.UI_BUTTON_CLICK, 2f, 1f)
                it.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 60, 2, false))
            }
            IdentityFifty.broadcastSpectators(translate("spec_slow_timer_used",player.name),
                AllowAction.RECEIVE_SURVIVORS_ACTION)
            return@setInteractEvent true
        }.setInitialCoolDown(1200)

        p.inventory.addItem(passiveItem,slowSkillItem)
    }

    override fun parameters(data: SurvivorData): SurvivorData {
        data.survivorClass = this
        data.helpTick = 200
        data.healTick = 340
        return data
    }

    private fun generatorMultiply(): Double {
        val players = IdentityFifty.survivors.filter { IdentityFifty.identityFiftyTask?.aliveSurvivors()?.contains(it.key) == true }.size
        val survivors = IdentityFifty.survivors.size
        val multiplyByPlayers = (1.0 - (players.toDouble() / survivors.toDouble())) * 0.2
        val multiply = 1.2 + multiplyByPlayers
        return multiply
    }

    override fun sheepGeneratorModify(
        damage: Double,
        remainingGenerator: Int,
        maxHealth: Double,
        nowHealth: Double,
        p: Player
    ): Double {
        return damage * generatorMultiply()
    }

    override fun cowGeneratorModify(damage: Double, maxHealth: Double, nowHealth: Double, p: Player): Double {
        return damage * generatorMultiply()
    }

    override fun onDieOtherSurvivor(diePlayer: Player, playerNumber: Int, p: Player) {
        val multiply = 0.04f / playerNumber
        p.walkSpeed = 0.2f + multiply
    }

    override fun info(): ArrayList<ItemStack> {
        val passiveItem = SItem(Material.STICK).setDisplayName(translate("passive")).setCustomModelData(8)
            .addLore(translate("mechanic_passive_lore_1"))
            .addLore(translate("mechanic_passive_lore_2"))
            .addLore(translate("mechanic_passive_lore_3"))
            .build()

        val slowSkill = SItem(Material.STICK).setDisplayName(translate("slow_timer")).setCustomModelData(12)
            .addLore(translate("slow_timer_lore_1"))
            .addLore(translate("slow_timer_lore_2"))
            .build()

        return arrayListOf(passiveItem,slowSkill)
    }

}
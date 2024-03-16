package tororo1066.identityfifty.talent.survivor

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import tororo1066.identityfifty.IdentityFifty
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sItem.SItem
import tororo1066.tororopluginapi.utils.setPitchL

class DashUltraSpeed : AbstractSurvivorTalent("dash_ultra_speed", 5, WoundedCowUp::class.java) {
    override fun lore(): List<String> {
        return listOf("dash_ultra_speed_lore_1")
    }

    var noDamage = false
    override fun onStart(p: Player) {
        val dashUltraSpeed =
            SItem(Material.STICK).setDisplayName(translate("dash_ultra_speed")).setCustomModelData(3344)
                .addLore("dash_ultra_speed_lore_1")

        val dashUltraSpeedItem =
            IdentityFifty.interactManager.createSInteractItem(dashUltraSpeed, true).setInteractEvent { _, _ ->
                noDamage = true
                Bukkit.getScheduler().runTaskLater(IdentityFifty.plugin, Runnable {
                    noDamage = false
                }, 20)
                p.world.playSound(p.location, Sound.ITEM_TRIDENT_RIPTIDE_3, 1f, 1.2f)
                p.velocity = p.location.setPitchL(0f).direction.normalize().multiply(10.0).setY(-1)

                return@setInteractEvent true
            }.setInitialCoolDown(1600)
        p.inventory.addItem(dashUltraSpeedItem)
    }

    override fun onDamage(damage: Int, toHealth: Int, stun: Boolean, damager: Player, p: Player): Pair<Boolean, Int> {
        if (noDamage) {
            p.world.playSound(p.location, Sound.ITEM_TRIDENT_RETURN, 1f, 0.8f)
            return Pair(stun, 0)
        } else {
            return Pair(stun, damage)
        }
    }
}
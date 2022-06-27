package tororo1066.identityfifty.character.survivor

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.data.SurvivorData
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sItem.SItem
import java.util.function.Consumer

class Helper : AbstractSurvivor("helper") {

    private var noDamage = false

    override fun onStart(p: Player) {
        val passiveItem = SItem(Material.STICK).setDisplayName(p.translate("passive"))
            .addLore(p.translate("helper_passive_lore_1"))
        p.inventory.addItem(passiveItem)

        val protectSkill = SItem(Material.STICK).setDisplayName(p.translate("helper_protect"))
            .addLore(p.translate("helper_protect_lore_1"))
            .addLore(p.translate("helper_protect_lore_2"))
            .addLore(p.translate("helper_protect_lore_3"))

        val protectSkillItem = IdentityFifty.interactManager.createSInteractItem(protectSkill).setInteractEvent { _, _ ->
            if (p.location.getNearbyPlayers(5.0).findLast { IdentityFifty.survivors.containsKey(it.uniqueId) } == null){
                p.sendActionBar(Component.text(p.translate("helper_protect_cant_use")))
                return@setInteractEvent
            }
            noDamage = true

            p.world.playSound(p.location, Sound.ITEM_TOTEM_USE,1f,1f)
            p.world.spawnParticle(Particle.TOTEM,p.location,5)

            Bukkit.getScheduler().runTaskLater(IdentityFifty.plugin, Consumer {
                noDamage = false
            },60)

        }.setInitialCoolDown(600)

        p.inventory.addItem(protectSkillItem)

    }

    override fun parameters(data: SurvivorData): SurvivorData {
        data.survivorClass = this
        data.helpTick = 80

        return data
    }

    override fun onDamage(toHealth: Int, damager: Player, p: Player): Boolean {
        if (noDamage) return false
        return true
    }
}
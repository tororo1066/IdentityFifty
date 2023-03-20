package tororo1066.identityfifty.character.survivor

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.data.SurvivorData
import tororo1066.tororopluginapi.lang.SLang.Companion.sendTranslateMsg
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sItem.SItem
import java.util.function.Consumer

class Helper : AbstractSurvivor("helper") {

    private var noDamage = false

    override fun onStart(p: Player) {
        super.onStart(p)
        val passiveItem = SItem(Material.STICK).setDisplayName(translate("passive")).setCustomModelData(8)
            .addLore(translate("helper_passive_lore_1"))
        p.inventory.addItem(passiveItem)

        val protectSkill = SItem(Material.STICK).setDisplayName(translate("helper_protect")).setCustomModelData(7)
            .addLore(translate("helper_protect_lore_1"))
            .addLore(translate("helper_protect_lore_2"))
            .addLore(translate("helper_protect_lore_3"))

        val protectSkillItem = IdentityFifty.interactManager.createSInteractItem(protectSkill,true).setInteractEvent { _, _ ->
            if (inPrison(p))return@setInteractEvent false
            val nearPlayer = p.location.getNearbyPlayers(8.0).firstOrNull {
                IdentityFifty.identityFiftyTask?.aliveSurvivors()
                    ?.contains(it.uniqueId) == true && it.uniqueId != p.uniqueId
            }
            if (nearPlayer == null){
                p.sendTranslateMsg("helper_protect_cant_use")
                return@setInteractEvent false
            }
            noDamage = true

            p.playSound(p.location, Sound.ITEM_TOTEM_USE,1f,1f)
            p.spawnParticle(Particle.TOTEM,p.location,5)

            val nearData = IdentityFifty.survivors[nearPlayer.uniqueId]!!
            val pData = IdentityFifty.survivors[p.uniqueId]!!

            val nearPlayerHealth = nearData.getHealth()
            val pPlayerHealth = pData.getHealth()

            nearData.setHealth(pPlayerHealth)
            pData.setHealth(nearPlayerHealth)

            Bukkit.getScheduler().runTaskLater(IdentityFifty.plugin, Consumer {
                noDamage = false
            },140)

            return@setInteractEvent true

        }.setInitialCoolDown(600)

        p.inventory.addItem(protectSkillItem)

    }

    override fun parameters(data: SurvivorData): SurvivorData {
        data.survivorClass = this
        data.helpTick = 60

        return data
    }

    override fun onDamage(damage: Int, toHealth: Int, damager: Player, p: Player): Pair<Boolean, Int> {
        if (noDamage) return Pair(true,0)
        return Pair(true,damage)
    }

    override fun info(): ArrayList<ItemStack> {
        val passiveItem = SItem(Material.STICK).setDisplayName(translate("passive")).setCustomModelData(8)
            .addLore(translate("helper_passive_lore_1"))

        val protectSkill = SItem(Material.STICK).setDisplayName(translate("helper_protect")).setCustomModelData(7)
            .addLore(translate("helper_protect_lore_1"))
            .addLore(translate("helper_protect_lore_2"))
            .addLore(translate("helper_protect_lore_3"))

        return arrayListOf(passiveItem,protectSkill)
    }
}
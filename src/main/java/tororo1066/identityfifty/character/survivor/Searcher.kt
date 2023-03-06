package tororo1066.identityfifty.character.survivor

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.inventivetalent.glow.GlowAPI
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.IdentityFifty.Companion.prefixMsg
import tororo1066.identityfifty.data.SurvivorData
import tororo1066.tororopluginapi.lang.SLang.Companion.sendTranslateMsg
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sItem.SItem

class Searcher : AbstractSurvivor("searcher") {
    override fun onStart(p: Player) {
        super.onStart(p)
        val passiveItem = SItem(Material.STICK).setDisplayName(translate("passive")).setCustomModelData(8)
            .addLore(translate("searcher_passive_lore_1"))
            .addLore(translate("searcher_passive_lore_2"))

        p.inventory.addItem(passiveItem)

        val searchSkillItem = SItem(Material.STICK).setDisplayName(translate("search_lens")).setCustomModelData(5)
            .addLore(translate("search_lens_lore_1"))
            .addLore(translate("search_lens_lore_2"))

        val searchSkill = IdentityFifty.interactManager.createSInteractItem(searchSkillItem,true).setInteractEvent { e, item ->
            p.playSound(p.location, Sound.ENTITY_ARROW_HIT_PLAYER,1f,0.5f)
            p.sendTranslateMsg("search_lens_used")
            val players = ArrayList<Player>()
            IdentityFifty.survivors.forEach { (uuid, _) ->
                val player = Bukkit.getPlayer(uuid)?:return@forEach
                players.add(player)
                player.sendTranslateMsg("search_lens_used_other",p.name)
            }
            IdentityFifty.hunters.forEach { (uuid, data) ->
                Bukkit.getPlayer(uuid)?:return@forEach
                data.glowManager.glow(players, GlowAPI.Color.RED,200)
            }
            return@setInteractEvent true
        }.setInitialCoolDown(1200)

        p.inventory.addItem(searchSkill)

    }

    override fun parameters(data: SurvivorData): SurvivorData {
        data.survivorClass = this
        data.helpTick = 150
        data.otherPlayerHelpDelayPercentage = 0.25
        return data
    }

    override fun info(): ArrayList<ItemStack> {
        val passiveItem = SItem(Material.STICK).setDisplayName(translate("passive")).setCustomModelData(8)
            .addLore(translate("searcher_passive_lore_1"))
            .addLore(translate("searcher_passive_lore_2"))

        val searchSkillItem = SItem(Material.STICK).setDisplayName(translate("search_lens")).setCustomModelData(5)
            .addLore(translate("search_lens_lore_1"))
            .addLore(translate("search_lens_lore_2"))
        return arrayListOf(passiveItem,searchSkillItem)
    }
}
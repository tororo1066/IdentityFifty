package tororo1066.identityfifty.character.survivor

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.data.SurvivorData
import tororo1066.identityfifty.enumClass.AllowAction
import tororo1066.nmsutils.items.GlowColor
import tororo1066.tororopluginapi.lang.SLang.Companion.sendTranslateMsg
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sItem.SItem
import tororo1066.tororopluginapi.utils.toPlayer

class Searcher : AbstractSurvivor("searcher") {
    override fun onStart(p: Player) {
        super.onStart(p)
        val passiveItem = SItem(Material.STICK).setDisplayName(translate("passive")).setCustomModelData(8)
            .addLore(translate("searcher_passive_lore_1"))
            .addLore(translate("searcher_passive_lore_2"))
            .addLore(translate("searcher_passive_lore_3"))

        p.inventory.addItem(passiveItem)

        val searchSkillItem = SItem(Material.STICK).setDisplayName(translate("search_lens")).setCustomModelData(5)
            .addLore(translate("search_lens_lore_1"))
            .addLore(translate("search_lens_lore_2"))
            .addLore(translate("search_lens_lore_3"))

        val searchSkill = IdentityFifty.interactManager.createSInteractItem(searchSkillItem).setInteractEvent { e, _ ->
            val player = e.player
            player.sendTranslateMsg("search_lens_used")
            val players = ArrayList<Player>()
            IdentityFifty.survivors.forEach { (uuid, _) ->
                val survivor = Bukkit.getPlayer(uuid)?:return@forEach
                players.add(survivor)
                survivor.playSound(survivor.location, Sound.ENTITY_ARROW_HIT_PLAYER,1f,0.5f)
                survivor.sendTranslateMsg("search_lens_used_other",survivor.name)
            }
            IdentityFifty.hunters.forEach { (uuid, data) ->
                Bukkit.getPlayer(uuid)?:return@forEach
                data.glowManager.glow(players, GlowColor.RED,240)
            }
            IdentityFifty.broadcastSpectators(translate("spec_search_lens_used",player.name),AllowAction.RECEIVE_SURVIVORS_ACTION)
            return@setInteractEvent true
        }.setInitialCoolDown(1100)

        p.inventory.addItem(searchSkill)

        tasks.add(Bukkit.getScheduler().runTaskTimer(IdentityFifty.plugin, Runnable {
            val player = p.player?:return@Runnable
            if (IdentityFifty.identityFiftyTask?.aliveSurvivors()?.contains(player.uniqueId) == false){
                return@Runnable
            }
            val players = player.location.getNearbyPlayers(12.0)
                .filter { it != player && IdentityFifty.hunters.containsKey(it.uniqueId)}
            players.forEach {
                val data = IdentityFifty.hunters[it.uniqueId]?:return@forEach
                data.glowManager.glow(IdentityFifty.survivors.map { map -> map.key.toPlayer() }.filterNotNull().toMutableList(),GlowColor.RED,8)
            }

        },5,5))

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
            .addLore(translate("searcher_passive_lore_3"))

        val searchSkillItem = SItem(Material.STICK).setDisplayName(translate("search_lens")).setCustomModelData(5)
            .addLore(translate("search_lens_lore_1"))
            .addLore(translate("search_lens_lore_2"))
            .addLore(translate("search_lens_lore_3"))
        return arrayListOf(passiveItem,searchSkillItem)
    }
}
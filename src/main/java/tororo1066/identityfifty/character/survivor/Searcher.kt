package tororo1066.identityfifty.character.survivor

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.inventivetalent.glow.GlowAPI
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.data.SurvivorData
import tororo1066.identityfifty.enumClass.AllowAction
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

        val searchSkill = IdentityFifty.interactManager.createSInteractItem(searchSkillItem).setInteractEvent { e, item ->
            p.sendTranslateMsg("search_lens_used")
            val players = ArrayList<Player>()
            IdentityFifty.survivors.forEach { (uuid, _) ->
                val player = Bukkit.getPlayer(uuid)?:return@forEach
                players.add(player)
                player.playSound(player.location, Sound.ENTITY_ARROW_HIT_PLAYER,1f,0.5f)
                player.sendTranslateMsg("search_lens_used_other",p.name)
            }
            IdentityFifty.hunters.forEach { (uuid, data) ->
                Bukkit.getPlayer(uuid)?:return@forEach
                data.glowManager.glow(players, GlowAPI.Color.RED,240)
            }
            IdentityFifty.broadcastSpectators(translate("spec_search_lens_used",p.name),AllowAction.RECEIVE_SURVIVORS_ACTION)
            return@setInteractEvent true
        }.setInitialCoolDown(1100)

        p.inventory.addItem(searchSkill)

        tasks.add(Bukkit.getScheduler().runTaskTimer(IdentityFifty.plugin, Runnable {
            if (IdentityFifty.identityFiftyTask?.aliveSurvivors()?.contains(p.uniqueId) == false){
                return@Runnable
            }
            val players = p.location.getNearbyPlayers(15.0)
                .filter { it != p && IdentityFifty.hunters.containsKey(it.uniqueId)}
            players.forEach {
                val data = IdentityFifty.hunters[it.uniqueId]?:return@forEach
                data.glowManager.glow(IdentityFifty.survivors.map { map -> map.key.toPlayer() }.filterNotNull().toMutableList(),GlowAPI.Color.RED,8)
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
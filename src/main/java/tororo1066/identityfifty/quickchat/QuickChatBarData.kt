package tororo1066.identityfifty.quickchat

import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.event.block.Action
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.inventivetalent.glow.GlowAPI
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.enumClass.AllowAction
import tororo1066.identityfifty.quickchat.survivor.*
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sItem.SInteractItem
import tororo1066.tororopluginapi.sItem.SItem
import tororo1066.tororopluginapi.utils.toPlayer
import java.util.*

class QuickChatBarData(val uuid: UUID) {

    companion object{
        val survivorChats = arrayListOf(
            NearHunter(),
            SheepGeneratorNow(),
            CowGeneratorNow(),
            HealMe(),
            HelpMe(),
            IWillHelpYou(),
            WillFinishedGenerator()
        )

        val hunterChats = arrayListOf<AbstractQuickChat>()
    }

    fun init(){
        uuid.toPlayer()?.inventory?.setItem(8, getChatBarItem())
    }

    fun getChatBarItem(): SInteractItem {
        val displayItem = SItem(Material.BOOK).setDisplayName(translate("quick_chat"))
            .setCustomData(IdentityFifty.plugin, "close", PersistentDataType.INTEGER, 1)

        val functionItem = IdentityFifty.interactManager.createSInteractItem(displayItem, true).setInteractEvent { e, _ ->
            if (!e.action.isRightClick)return@setInteractEvent true

            val chats = arrayListOf<AbstractQuickChat>()
            var isHunter = false

            if (IdentityFifty.survivors.containsKey(e.player.uniqueId)){
                chats.addAll(survivorChats)
            } else if (IdentityFifty.hunters.containsKey(e.player.uniqueId)) {
                isHunter = true
                chats.addAll(hunterChats)
            }

            if (chats.isEmpty())return@setInteractEvent true

            val saveItems = ArrayList<ItemStack?>()

            for (i in 0..8){
                saveItems.add(e.player.inventory.getItem(i))
                e.player.inventory.setItem(i, null)
            }

            val setItems = ArrayList<SInteractItem?>()
            (0..8).forEach { _ ->
                setItems.add(null)
            }

            fun returnItems(){
                (0..8).forEach { i ->
                    e.player.inventory.setItem(i, null)
                }
                saveItems.forEachIndexed { index, itemStack ->
                    itemStack?:return@forEachIndexed
                    e.player.inventory.setItem(index, itemStack)
                }
                setItems.forEach {
                    it?.delete()
                }
            }

            for ((index, chat) in chats.withIndex()){
                val chatItem = IdentityFifty.interactManager.createSInteractItem(
                    SItem(Material.PAPER).setDisplayName(chat.message),
                    true).setInteractEvent chat@ { _, _ ->
                        if (isHunter){
                            IdentityFifty.hunters.keys.mapNotNull { it.toPlayer() }.forEach {
                                it.sendMessage("§7[§f${e.player.name}§7] -> §r${chat.message}")
                                it.playSound(it.location, Sound.ITEM_BOOK_PAGE_TURN, 1f, 2f)
                            }

                            IdentityFifty.broadcastSpectators("§7[§f${e.player.name}§7] -> §r${chat.message}",
                                AllowAction.RECEIVE_HUNTERS_CHAT)

                            val data = IdentityFifty.hunters[e.player.uniqueId]
                            data?.glowManager?.glow(IdentityFifty.hunters.mapNotNull { it.key.toPlayer() }.toMutableList(),
                                GlowAPI.Color.WHITE, 80)
                            returnItems()
                        } else {
                            IdentityFifty.survivors.keys.mapNotNull { it.toPlayer() }.forEach {
                                it.sendMessage("§7[§f${e.player.name}§7] -> §r${chat.message}")
                                it.playSound(it.location, Sound.ITEM_BOOK_PAGE_TURN, 1f, 2f)
                            }

                            IdentityFifty.broadcastSpectators("§7[§f${e.player.name}§7] -> §r${chat.message}",
                                AllowAction.RECEIVE_SURVIVORS_CHAT)

                            val data = IdentityFifty.survivors[e.player.uniqueId]
                            data?.glowManager?.glow(IdentityFifty.survivors.mapNotNull { it.key.toPlayer() }.toMutableList(),
                                GlowAPI.Color.WHITE, 80)
                            returnItems()
                        }
                        return@chat true
                }
                setItems[index] = chatItem
            }

            setItems[8] = IdentityFifty.interactManager
                .createSInteractItem(SItem(Material.BARRIER).setDisplayName(translate("close")), true)
                .setInteractEvent close@ { _, _ ->
                    returnItems()
                    return@close true
                }

            setItems.forEachIndexed { index, sInteractItem ->
                e.player.inventory.setItem(index, sInteractItem?:return@forEachIndexed)
            }

            return@setInteractEvent true
        }.setInitialCoolDown(100)

        return functionItem
    }
}
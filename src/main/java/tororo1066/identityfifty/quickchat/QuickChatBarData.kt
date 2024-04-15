package tororo1066.identityfifty.quickchat

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.enumClass.AllowAction
import tororo1066.identityfifty.quickchat.survivor.*
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sItem.SInteractItem
import tororo1066.tororopluginapi.sItem.SItem
import tororo1066.tororopluginapi.utils.toPlayer
import java.util.UUID

class QuickChatBarData(val uuid: UUID) {

    var latestChat: AbstractQuickChat? = null

    companion object {
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

    fun survivorChat(chat: AbstractQuickChat){
        latestChat = chat
        IdentityFifty.survivors.keys.mapNotNull { it.toPlayer() }.forEach {
            it.sendMessage("§7[§f${uuid.toPlayer()?.name}§7] -> §r${chat.message}")
            it.playSound(it.location, Sound.ITEM_BOOK_PAGE_TURN, 1f, 2f)
        }

        IdentityFifty.broadcastSpectators("§7[§f${uuid.toPlayer()?.name}§7] -> §r${chat.message}",
            AllowAction.RECEIVE_SURVIVORS_CHAT)

        val data = IdentityFifty.survivors[uuid]
        data?.glowManager?.glow(IdentityFifty.survivors.mapNotNull { it.key.toPlayer() }.toMutableList(),
            ChatColor.WHITE, 80)
    }

    fun hunterChat(chat: AbstractQuickChat){
        latestChat = chat
        IdentityFifty.hunters.keys.mapNotNull { it.toPlayer() }.forEach {
            it.sendMessage("§7[§f${uuid.toPlayer()?.name}§7] -> §r${chat.message}")
            it.playSound(it.location, Sound.ITEM_BOOK_PAGE_TURN, 1f, 2f)
        }

        IdentityFifty.broadcastSpectators("§7[§f${uuid.toPlayer()?.name}§7] -> §r${chat.message}",
            AllowAction.RECEIVE_HUNTERS_CHAT)

        val data = IdentityFifty.hunters[uuid]
        data?.glowManager?.glow(IdentityFifty.hunters.mapNotNull { it.key.toPlayer() }.toMutableList(),
            ChatColor.WHITE, 80)
    }

    fun getChatBarItem(): SInteractItem {
        val displayItem = SItem(Material.BOOK).setDisplayName(translate("quick_chat"))
            .addLore(translate("quick_chat_lore_1"))
            .addLore(translate("quick_chat_lore_2"))
            .setCustomData(IdentityFifty.plugin, "close", PersistentDataType.INTEGER, 1)

        val functionItem = IdentityFifty.interactManager.createSInteractItem(displayItem, true).setInteractEvent { e, _ ->
            if (e.action.isLeftClick) {
                latestChat?.let {
                    if (IdentityFifty.survivors.containsKey(e.player.uniqueId)){
                        survivorChat(it)
                    } else if (IdentityFifty.hunters.containsKey(e.player.uniqueId)) {
                        hunterChat(it)
                    }
                }
                return@setInteractEvent true
            }

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
                            hunterChat(chat)
                            returnItems()
                        } else {
                            survivorChat(chat)
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
        }.setInitialCoolDown(150)

        return functionItem
    }
}
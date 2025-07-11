package tororo1066.identityfifty.data

import org.bukkit.entity.Player
import tororo1066.identityfifty.quickchat.QuickChatBarData

abstract class PlayerData(player: Player) {
    val uuid = player.uniqueId
    val name = player.name
    val glowManager = GlowManager(uuid)
    val skinModifier = SkinModifier(uuid)
    val quickChatBarData = QuickChatBarData(uuid)


}
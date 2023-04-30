package tororo1066.identityfifty.data

import tororo1066.identityfifty.quickchat.QuickChatBarData
import java.util.UUID

abstract class PlayerData {
    lateinit var uuid: UUID
    var name = ""
    lateinit var glowManager: GlowManager
    lateinit var skinModifier: SkinModifier
    lateinit var quickChatBarData: QuickChatBarData


}
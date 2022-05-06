package tororo1066.identityfifty.data

import java.util.UUID

abstract class PlayerData {
    lateinit var uuid: UUID
    var name = ""
    lateinit var glowManager: GlowManager


}
package tororo1066.identityfifty.data

import tororo1066.identityfifty.enumClass.AllowAction
import java.util.UUID

class SpectatorData {

    lateinit var uuid: UUID
    var mcid = ""

    val actions = arrayListOf<AllowAction>()
}
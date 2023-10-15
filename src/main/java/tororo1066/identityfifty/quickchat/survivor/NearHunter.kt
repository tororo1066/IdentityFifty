package tororo1066.identityfifty.quickchat.survivor

import tororo1066.identityfifty.quickchat.AbstractQuickChat
import tororo1066.tororopluginapi.lang.SLang.Companion.translate

class NearHunter: AbstractQuickChat() {

    override val message: String
        get() = translate("near_hunter")
}
package tororo1066.identityfifty.quickchat.survivor

import tororo1066.identityfifty.quickchat.AbstractQuickChat
import tororo1066.tororopluginapi.lang.SLang.Companion.translate

class CowGeneratorNow: AbstractQuickChat() {

    override val message: String
        get() = translate("cow_generator_now")
}
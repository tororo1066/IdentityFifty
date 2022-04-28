package tororo1066.identityfifty.commands

import tororo1066.identityfifty.IdentityFifty
import tororo1066.tororopluginapi.sCommand.*
import java.util.function.Consumer

class IdentityCommand : SCommand("identity") {

    init {
        addCommand(SCommandObject().addArg(SCommandArg().addAllowString("select")).addArg(SCommandArg().addAllowType(SCommandArgType.STRING).addAlias("キャラ")).setExecutor(
            Consumer<SCommandOnlyPlayerData> {
                if (!IdentityFifty.survivors.containsKey(it.sender.uniqueId)){
                    val survivor = IdentityFifty.survivors[it.sender.uniqueId]!!
                }
            }
        ))
    }

}
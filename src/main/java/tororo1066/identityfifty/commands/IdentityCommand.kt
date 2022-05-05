package tororo1066.identityfifty.commands

import org.bukkit.Bukkit
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.IdentityFifty.Companion.prefixMsg
import tororo1066.identityfifty.IdentityFiftyTask
import tororo1066.identityfifty.data.HunterData
import tororo1066.identityfifty.data.SurvivorData
import tororo1066.tororopluginapi.sCommand.*
import java.util.function.Consumer

class IdentityCommand : SCommand("identity") {

    init {
        addCommand(SCommandObject().addArg(SCommandArg().addAllowString("survivor")).addArg(SCommandArg().addAllowString(IdentityFifty.survivorsData.keys.toTypedArray())).setExecutor(
            Consumer<SCommandOnlyPlayerData> {
                val abstractSurvivor = IdentityFifty.survivorsData[it.args[1]]!!
                val data = SurvivorData()
                data.uuid = it.sender.uniqueId
                data.name = it.sender.name
                abstractSurvivor.parameters(data)
                IdentityFifty.survivors[it.sender.uniqueId] = data

                it.sender.prefixMsg(abstractSurvivor.name + "になりました")
            }
        ))

        addCommand(SCommandObject().addArg(SCommandArg().addAllowString("asSurvivor")).addArg(SCommandArg().addAllowType(SCommandArgType.ONLINE_PLAYER)).addArg(SCommandArg().addAllowString(IdentityFifty.survivorsData.keys.toTypedArray())).setExecutor(
            Consumer<SCommandOnlyPlayerData> {
                val p = Bukkit.getPlayer(it.args[1])!!
                val abstractSurvivor = IdentityFifty.survivorsData[it.args[2]]!!
                val data = SurvivorData()
                data.uuid = p.uniqueId
                data.name = p.name
                abstractSurvivor.parameters(data)
                IdentityFifty.survivors[p.uniqueId] = data

                it.sender.prefixMsg(abstractSurvivor.name + "にしました")
            }
        ))

        addCommand(SCommandObject().addArg(SCommandArg().addAllowString("hunter")).addArg(SCommandArg().addAllowString(IdentityFifty.huntersData.keys.toTypedArray())).setExecutor(
            Consumer<SCommandOnlyPlayerData> {
                val abstractHunter = IdentityFifty.huntersData[it.args[1]]!!
                val data = HunterData()
                data.uuid = it.sender.uniqueId
                data.name = it.sender.name
                abstractHunter.parameters(data)
                IdentityFifty.hunters[it.sender.uniqueId] = data

                it.sender.prefixMsg(abstractHunter.name + "になりました")
            }
        ))

        addCommand(SCommandObject().addArg(SCommandArg().addAllowString("start")).addArg(SCommandArg().addAllowString(IdentityFifty.maps.keys.toTypedArray())).setExecutor(
            Consumer<SCommandOnlyPlayerData> {
                val map = IdentityFifty.maps[it.args[1]]!!
                IdentityFiftyTask(map).start()
            }
        ))
    }

}
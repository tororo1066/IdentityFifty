package tororo1066.identityfifty.commands

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.IdentityFifty.Companion.prefixMsg
import tororo1066.identityfifty.IdentityFiftyTask
import tororo1066.identityfifty.data.GlowManager
import tororo1066.identityfifty.data.HunterData
import tororo1066.identityfifty.data.SurvivorData
import tororo1066.identityfifty.inventory.MapList
import tororo1066.tororopluginapi.lang.SLang.Companion.sendTranslateMsg
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sCommand.*
import java.util.function.Consumer

class IdentityCommand : SCommand("identity") {

    fun createSurvivorData(p: Player): SurvivorData {
        val data = SurvivorData()
        data.uuid = p.uniqueId
        data.name = p.name
        data.glowManager = GlowManager(p.uniqueId)
        return data
    }

    fun createHunterData(p: Player): HunterData {
        val data = HunterData()
        data.uuid = p.uniqueId
        data.name = p.name
        data.glowManager = GlowManager(p.uniqueId)
        return data
    }

    init {
        addCommand(SCommandObject().addArg(SCommandArg().addAllowString("survivor")).addArg(SCommandArg().addAllowString(IdentityFifty.survivorsData.keys.toTypedArray())).setExecutor(
            Consumer<SCommandOnlyPlayerData> {
                if (IdentityFifty.hunters.containsKey(it.sender.uniqueId)){
                    IdentityFifty.hunters.remove(it.sender.uniqueId)
                }
                val abstractSurvivor = IdentityFifty.survivorsData[it.args[1]]!!
                val data = createSurvivorData(it.sender)
                abstractSurvivor.parameters(data)
                IdentityFifty.survivors[it.sender.uniqueId] = data

                it.sender.sendTranslateMsg("select_character",it.sender.translate(abstractSurvivor.name))
            }
        ))

        addCommand(SCommandObject().addArg(SCommandArg().addAllowString("as")).addArg(SCommandArg().addAllowString("survivor")).addArg(SCommandArg().addAllowType(SCommandArgType.ONLINE_PLAYER)).addArg(SCommandArg().addAllowString(IdentityFifty.survivorsData.keys.toTypedArray())).setExecutor(
            Consumer<SCommandData> {
                val p = Bukkit.getPlayer(it.args[2])!!
                if (IdentityFifty.hunters.containsKey(p.uniqueId)){
                    IdentityFifty.hunters.remove(p.uniqueId)
                }
                val abstractSurvivor = IdentityFifty.survivorsData[it.args[3]]!!
                val data = createSurvivorData(p)
                abstractSurvivor.parameters(data)
                IdentityFifty.survivors[p.uniqueId] = data

                it.sender.sendTranslateMsg("select_other_character",p.name,p.translate(abstractSurvivor.name))
            }
        ))



        addCommand(SCommandObject().addArg(SCommandArg().addAllowString("hunter")).addArg(SCommandArg().addAllowString(IdentityFifty.huntersData.keys.toTypedArray())).setExecutor(
            Consumer<SCommandOnlyPlayerData> {
                if (IdentityFifty.survivors.containsKey(it.sender.uniqueId)){
                    IdentityFifty.survivors.remove(it.sender.uniqueId)
                }
                val abstractHunter = IdentityFifty.huntersData[it.args[1]]!!
                val data = createHunterData(it.sender)
                abstractHunter.parameters(data)
                IdentityFifty.hunters[it.sender.uniqueId] = data

                it.sender.sendTranslateMsg("select_character",it.sender.translate(abstractHunter.name))
            }
        ))

        addCommand(SCommandObject().addArg(SCommandArg().addAllowString("as")).addArg(SCommandArg().addAllowString("hunter")).addArg(SCommandArg().addAllowType(SCommandArgType.ONLINE_PLAYER)).addArg(SCommandArg().addAllowString(IdentityFifty.huntersData.keys.toTypedArray())).setExecutor(
            Consumer<SCommandData> {
                val p = Bukkit.getPlayer(it.args[2])!!
                if (IdentityFifty.survivors.containsKey(p.uniqueId)){
                    IdentityFifty.survivors.remove(p.uniqueId)
                }
                val abstractHunter = IdentityFifty.huntersData[it.args[3]]!!
                val data = createHunterData(p)
                abstractHunter.parameters(data)
                IdentityFifty.hunters[p.uniqueId] = data

                it.sender.sendTranslateMsg("select_other_character",p.name,p.translate(abstractHunter.name))
            }
        ))

        addCommand(SCommandObject().addArg(SCommandArg().addAllowString("unregister")).setExecutor(
            Consumer<SCommandOnlyPlayerData> {
                IdentityFifty.hunters.remove(it.sender.uniqueId)
                IdentityFifty.survivors.remove(it.sender.uniqueId)

                it.sender.prefixMsg("登録解除しました")
            }
        ))

        addCommand(SCommandObject().addArg(SCommandArg().addAllowString("as")).addArg(SCommandArg().addAllowString("unregister")).addArg(SCommandArg().addAllowType(SCommandArgType.ONLINE_PLAYER)).setExecutor(
            Consumer<SCommandData> {
                val p = Bukkit.getPlayer(it.args[2])!!
                IdentityFifty.hunters.remove(p.uniqueId)
                IdentityFifty.survivors.remove(p.uniqueId)

                it.sender.prefixMsg("登録解除させました")
            }
        ))

        addCommand(SCommandObject().addArg(SCommandArg().addAllowString("start")).addArg(SCommandArg().addAllowString(IdentityFifty.maps.keys.toTypedArray())).setExecutor(
            Consumer<SCommandOnlyPlayerData> {
                val map = IdentityFifty.maps[it.args[1]]!!
                if (map.survivorLimit < IdentityFifty.survivors.size){
                    it.sender.prefixMsg("§cサバイバーは${map.survivorLimit}人までです！")
                    return@Consumer
                }

                if (map.hunterLimit < IdentityFifty.hunters.size){
                    it.sender.prefixMsg("§cサバイバーは${map.hunterLimit}人までです！")
                    return@Consumer
                }

                IdentityFifty.identityFiftyTask = IdentityFiftyTask(map)
                IdentityFifty.identityFiftyTask?.start()
            }
        ))

        addCommand(SCommandObject().addArg(SCommandArg().addAllowString("playerlist")).setExecutor(
            Consumer<SCommandOnlyPlayerData> {
                it.sender.sendMessage("§b${it.sender.translate("survivor")}")
                IdentityFifty.survivors.forEach { data ->
                    it.sender.sendMessage("§e${data.value.name} §d${it.sender.translate(data.value.survivorClass.name)}")
                }

                it.sender.sendMessage("§c${it.sender.translate("hunter")}")
                IdentityFifty.hunters.forEach { data ->
                    it.sender.sendMessage("§e${data.value.name} §d${it.sender.translate(data.value.hunterClass.name)}")
                }
            }
        ))

        addCommand(SCommandObject().addArg(SCommandArg().addAllowString("stop")).setExecutor(
            Consumer<SCommandOnlyPlayerData> {
                IdentityFifty.identityFiftyTask?.end()
                it.sender.prefixMsg("§c停止しました")
            }
        ))

        addCommand(SCommandObject().addArg(SCommandArg().addAllowString("edit")).setExecutor(
            Consumer<SCommandOnlyPlayerData> {
                MapList().open(it.sender)
            }
        ))


    }

}
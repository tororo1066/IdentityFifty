package tororo1066.identityfifty.commands

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.IdentityFifty.Companion.prefixMsg
import tororo1066.identityfifty.IdentityFiftyTask
import tororo1066.identityfifty.data.*
import tororo1066.identityfifty.inventory.HunterInfoInv
import tororo1066.identityfifty.inventory.MapList
import tororo1066.identityfifty.inventory.SurvivorInfoInv
import tororo1066.tororopluginapi.lang.SLang.Companion.sendTranslateMsg
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sCommand.SCommand
import tororo1066.tororopluginapi.sCommand.SCommandArg
import tororo1066.tororopluginapi.sCommand.SCommandArgType
import tororo1066.tororopluginapi.sCommand.SCommandObject

class IdentityCommand : SCommand("identity") {

    private fun createSurvivorData(p: Player): SurvivorData {
        return createPData(SurvivorData(),p)
    }

    private fun createHunterData(p: Player): HunterData {
        return createPData(HunterData(),p)
    }

    private fun <V: PlayerData>createPData(data: V, p: Player): V {
        data.uuid = p.uniqueId
        data.name = p.name
        data.glowManager = GlowManager(p.uniqueId)
        data.skinModifier = SkinModifier(p.uniqueId)
        return data
    }

    init {
        registerSLangCommand(IdentityFifty.plugin,"identity.op")

        addCommand(SCommandObject().addArg(SCommandArg().addAllowString("survivor")).addArg(SCommandArg().addAllowString(IdentityFifty.survivorsData.keys.toTypedArray()))
            .setPlayerExecutor {
                if (IdentityFifty.hunters.containsKey(it.sender.uniqueId)) {
                    IdentityFifty.hunters.remove(it.sender.uniqueId)
                }
                val abstractSurvivor = IdentityFifty.survivorsData[it.args[1]]!!.clone()
                val data = createSurvivorData(it.sender)
                abstractSurvivor.parameters(data)
                IdentityFifty.survivors[it.sender.uniqueId] = data

                it.sender.sendTranslateMsg("select_character", translate(abstractSurvivor.name))
            })

        addCommand(SCommandObject().addArg(SCommandArg().addAllowString("as")).addArg(SCommandArg().addAllowString("survivor")).addArg(SCommandArg().addAllowType(SCommandArgType.ONLINE_PLAYER)).addArg(SCommandArg().addAllowString(IdentityFifty.survivorsData.keys.toTypedArray()))
            .setNormalExecutor {
                val p = Bukkit.getPlayer(it.args[2])!!
                if (IdentityFifty.hunters.containsKey(p.uniqueId)) {
                    IdentityFifty.hunters.remove(p.uniqueId)
                }
                val abstractSurvivor = IdentityFifty.survivorsData[it.args[3]]!!.clone()
                val data = createSurvivorData(p)
                abstractSurvivor.parameters(data)
                IdentityFifty.survivors[p.uniqueId] = data

                it.sender.sendTranslateMsg("select_other_character", p.name, translate(abstractSurvivor.name))
            })



        addCommand(SCommandObject().addArg(SCommandArg().addAllowString("hunter")).addArg(SCommandArg().addAllowString(IdentityFifty.huntersData.keys.toTypedArray()))
            .setPlayerExecutor {
                if (IdentityFifty.survivors.containsKey(it.sender.uniqueId)) {
                    IdentityFifty.survivors.remove(it.sender.uniqueId)
                }
                val abstractHunter = IdentityFifty.huntersData[it.args[1]]!!.clone()
                val data = createHunterData(it.sender)
                abstractHunter.parameters(data)
                IdentityFifty.hunters[it.sender.uniqueId] = data

                it.sender.sendTranslateMsg("select_character", translate(abstractHunter.name))
            })

        addCommand(SCommandObject().addArg(SCommandArg().addAllowString("as")).addArg(SCommandArg().addAllowString("hunter")).addArg(SCommandArg().addAllowType(SCommandArgType.ONLINE_PLAYER)).addArg(SCommandArg().addAllowString(IdentityFifty.huntersData.keys.toTypedArray()))
            .setNormalExecutor {
                val p = Bukkit.getPlayer(it.args[2])!!
                if (IdentityFifty.survivors.containsKey(p.uniqueId)) {
                    IdentityFifty.survivors.remove(p.uniqueId)
                }
                val abstractHunter = IdentityFifty.huntersData[it.args[3]]!!.clone()
                val data = createHunterData(p)
                abstractHunter.parameters(data)
                IdentityFifty.hunters[p.uniqueId] = data

                it.sender.sendTranslateMsg("select_other_character", p.name, translate(abstractHunter.name))
            })

        addCommand(SCommandObject().addArg(SCommandArg().addAllowString("unregister")).setPlayerExecutor {
            IdentityFifty.hunters.remove(it.sender.uniqueId)
            IdentityFifty.survivors.remove(it.sender.uniqueId)

            it.sender.prefixMsg("登録解除しました")
        })

        addCommand(SCommandObject().addArg(SCommandArg().addAllowString("as")).addArg(SCommandArg().addAllowString("unregister")).addArg(SCommandArg().addAllowType(SCommandArgType.ONLINE_PLAYER)).setNormalExecutor {
            val p = Bukkit.getPlayer(it.args[2])!!
            IdentityFifty.hunters.remove(p.uniqueId)
            IdentityFifty.survivors.remove(p.uniqueId)

            it.sender.prefixMsg("登録解除させました")
        })

        addCommand(SCommandObject().addArg(SCommandArg().addAllowString("start")).addArg(SCommandArg().addAllowString(IdentityFifty.maps.keys.toTypedArray()))
            .setPlayerExecutor {
                val map = IdentityFifty.maps[it.args[1]]!!
                if (map.survivorLimit < IdentityFifty.survivors.size) {
                    it.sender.prefixMsg("§cサバイバーは${map.survivorLimit}人までです！")
                    return@setPlayerExecutor
                }

                if (map.hunterLimit < IdentityFifty.hunters.size) {
                    it.sender.prefixMsg("§cサバイバーは${map.hunterLimit}人までです！")
                    return@setPlayerExecutor
                }

                IdentityFifty.identityFiftyTask = IdentityFiftyTask(map)
                IdentityFifty.identityFiftyTask?.start()
            })

        addCommand(SCommandObject().addArg(SCommandArg().addAllowString("playerlist")).setPlayerExecutor {
            it.sender.sendMessage("§b${translate("survivor")}")
            IdentityFifty.survivors.forEach { data ->
                it.sender.sendMessage("§e${data.value.name} §d???")
            }

            it.sender.sendMessage("§c${translate("hunter")}")
            IdentityFifty.hunters.forEach { data ->
                it.sender.sendMessage("§e${data.value.name} §d???")
            }
        })

        addCommand(SCommandObject().addArg(SCommandArg().addAllowString("playerlist")).addArg(SCommandArg("all")).setPlayerExecutor {
            it.sender.sendMessage("§b${translate("survivor")}")
            IdentityFifty.survivors.forEach { data ->
                it.sender.sendMessage("§e${data.value.name} §d${translate(data.value.survivorClass.name)}")
            }

            it.sender.sendMessage("§c${translate("hunter")}")
            IdentityFifty.hunters.forEach { data ->
                it.sender.sendMessage("§e${data.value.name} §d${translate(data.value.hunterClass.name)}")
            }
        })

        addCommand(SCommandObject().addArg(SCommandArg().addAllowString("stop")).setPlayerExecutor {
            IdentityFifty.identityFiftyTask?.end()
            it.sender.sendTranslateMsg("stopped_game")
        })

        addCommand(SCommandObject().addArg(SCommandArg().addAllowString("edit")).setPlayerExecutor {
            MapList().open(it.sender)
        })

        addCommand(SCommandObject().addArg(SCommandArg("info")).addArg(SCommandArg("survivor")).addArg(SCommandArg(IdentityFifty.survivorsData.keys)).setPlayerExecutor {
            SurvivorInfoInv(IdentityFifty.survivorsData[it.args[2]]!!).open(it.sender)
        })

        addCommand(SCommandObject().addArg(SCommandArg("info")).addArg(SCommandArg("hunter")).addArg(SCommandArg(IdentityFifty.huntersData.keys)).setPlayerExecutor {
            HunterInfoInv(IdentityFifty.huntersData[it.args[2]]!!).open(it.sender)
        })

        registerDebugCommand("identity.op")

    }

}
package tororo1066.identityfifty.commands

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.IdentityFifty.Companion.prefixMsg
import tororo1066.identityfifty.IdentityFiftyTask
import tororo1066.identityfifty.data.*
import tororo1066.identityfifty.discord.DiscordClient
import tororo1066.identityfifty.inventory.*
import tororo1066.identityfifty.quickchat.QuickChatBarData
import tororo1066.identityfifty.talent.hunter.AbstractHunterTalent
import tororo1066.identityfifty.talent.survivor.AbstractSurvivorTalent
import tororo1066.nmsutils.SPlayer
import tororo1066.tororopluginapi.lang.SLang.Companion.sendTranslateMsg
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sCommand.SCommand
import tororo1066.tororopluginapi.sCommand.SCommandArg
import tororo1066.tororopluginapi.sCommand.SCommandArgType
import tororo1066.tororopluginapi.sCommand.SCommandObject
import tororo1066.tororopluginapi.utils.toPlayer
import java.util.UUID

class IdentityCommand : SCommand("identity","","identity.user") {

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
        data.quickChatBarData = QuickChatBarData(p.uniqueId)
        return data
    }

    private fun saveSurvivorTalents(data: SurvivorData): Pair<List<AbstractSurvivorTalent>,Int> {
        val talents = mutableListOf<AbstractSurvivorTalent>()
        var cost = 0
        data.talentClasses.forEach {
            talents.add(it.value)
            cost += it.value.unlockCost
        }
        return Pair(talents,cost)
    }

    private fun saveHunterTalents(data: HunterData): Pair<List<AbstractHunterTalent>,Int> {
        val talents = mutableListOf<AbstractHunterTalent>()
        var cost = 0
        data.talentClasses.forEach {
            talents.add(it.value)
            cost += it.value.unlockCost
        }
        return Pair(talents,cost)
    }

    init {
        registerSLangCommand(IdentityFifty.plugin,"identity.op")

        addCommand(SCommandObject().addArg(SCommandArg().addAllowString("survivor")).addArg(SCommandArg().addAllowString(IdentityFifty.survivorsData.keys.toTypedArray()))
            .setPlayerExecutor {
                if (DiscordClient.enable){
                    if (!DiscordClient.survivors.containsKey(it.sender.uniqueId)){
                        it.sender.sendMessage("§cあなたは選択できません")
                        return@setPlayerExecutor
                    }
                }
                val abstractSurvivor = IdentityFifty.survivorsData[it.args[1]]!!.clone()
                if (IdentityFifty.survivors.filter { filter -> filter.value.survivorClass == abstractSurvivor.javaClass }.isNotEmpty()){
                    it.sender.sendMessage("§c既に選択されています")
                    return@setPlayerExecutor
                }
                IdentityFifty.hunters.remove(it.sender.uniqueId)
                IdentityFifty.spectators.remove(it.sender.uniqueId)
                val saveTalents = IdentityFifty.survivors[it.sender.uniqueId]?.let { it1 -> saveSurvivorTalents(it1) }
                val data = createSurvivorData(it.sender)
                abstractSurvivor.parameters(data)
                saveTalents?.first?.forEach { clazz ->
                    data.talentClasses[clazz.javaClass] = clazz
                }
                saveTalents?.second?.let { it1 -> data.talentCost -= it1 }
                IdentityFifty.survivors[it.sender.uniqueId] = data

                it.sender.sendTranslateMsg("select_character", translate(abstractSurvivor.name))
            })

        addCommand(SCommandObject().addNeedPermission("identity.op")
            .addArg(SCommandArg().addAllowString("as")).addArg(SCommandArg().addAllowString("survivor"))
            .addArg(SCommandArg().addAllowType(SCommandArgType.ONLINE_PLAYER)).addArg(SCommandArg().addAllowString(IdentityFifty.survivorsData.keys.toTypedArray()))
            .setNormalExecutor {
                val p = Bukkit.getPlayer(it.args[2])!!
                IdentityFifty.hunters.remove(p.uniqueId)
                IdentityFifty.spectators.remove(p.uniqueId)
                val saveTalents = IdentityFifty.survivors[p.uniqueId]?.let { it1 -> saveSurvivorTalents(it1) }
                val abstractSurvivor = IdentityFifty.survivorsData[it.args[3]]!!.clone()
                val data = createSurvivorData(p)
                abstractSurvivor.parameters(data)
                saveTalents?.first?.forEach { clazz ->
                    data.talentClasses[clazz.javaClass] = clazz
                }
                saveTalents?.second?.let { it1 -> data.talentCost -= it1 }
                IdentityFifty.survivors[p.uniqueId] = data

                it.sender.sendTranslateMsg("select_other_character", p.name, translate(abstractSurvivor.name))
            })



        addCommand(SCommandObject().addArg(SCommandArg().addAllowString("hunter")).addArg(SCommandArg().addAllowString(IdentityFifty.huntersData.keys.toTypedArray()))
            .setPlayerExecutor {
                if (DiscordClient.enable){
                    if (!DiscordClient.hunters.containsKey(it.sender.uniqueId)){
                        it.sender.sendMessage("§cあなたは選択できません")
                        return@setPlayerExecutor
                    }
                }

                IdentityFifty.survivors.remove(it.sender.uniqueId)
                IdentityFifty.spectators.remove(it.sender.uniqueId)

                val saveTalents = IdentityFifty.hunters[it.sender.uniqueId]?.let { it1 -> saveHunterTalents(it1) }
                val abstractHunter = IdentityFifty.huntersData[it.args[1]]!!.clone()
                val data = createHunterData(it.sender)
                abstractHunter.parameters(data)
                saveTalents?.first?.forEach { clazz ->
                    data.talentClasses[clazz.javaClass] = clazz
                }
                saveTalents?.second?.let { it1 -> data.talentCost -= it1 }
                IdentityFifty.hunters[it.sender.uniqueId] = data

                it.sender.sendTranslateMsg("select_character", translate(abstractHunter.name))
            })

        addCommand(SCommandObject().addNeedPermission("identity.op")
            .addArg(SCommandArg().addAllowString("as")).addArg(SCommandArg().addAllowString("hunter"))
            .addArg(SCommandArg().addAllowType(SCommandArgType.ONLINE_PLAYER)).addArg(SCommandArg().addAllowString(IdentityFifty.huntersData.keys.toTypedArray()))
            .setNormalExecutor {
                val p = Bukkit.getPlayer(it.args[2])!!

                IdentityFifty.survivors.remove(p.uniqueId)
                IdentityFifty.spectators.remove(p.uniqueId)

                val saveTalents = IdentityFifty.hunters[p.uniqueId]?.let { it1 -> saveHunterTalents(it1) }
                val abstractHunter = IdentityFifty.huntersData[it.args[3]]!!.clone()
                val data = createHunterData(p)
                abstractHunter.parameters(data)
                saveTalents?.first?.forEach { clazz ->
                    data.talentClasses[clazz.javaClass] = clazz
                }
                saveTalents?.second?.let { it1 -> data.talentCost -= it1 }
                IdentityFifty.hunters[p.uniqueId] = data

                it.sender.sendTranslateMsg("select_other_character", p.name, translate(abstractHunter.name))
            })

        addCommand(SCommandObject().addArg(SCommandArg().addAllowString("spectator"))
            .setPlayerExecutor {
                if (DiscordClient.enable){
                    if (!DiscordClient.spectators.containsKey(it.sender.uniqueId)){
                        it.sender.sendMessage("§cあなたは選択できません")
                        return@setPlayerExecutor
                    }
                }

                IdentityFifty.survivors.remove(it.sender.uniqueId)
                IdentityFifty.hunters.remove(it.sender.uniqueId)

                val data = SpectatorData().apply {
                    uuid = it.sender.uniqueId
                    mcid = it.sender.name
                    actions.addAll(IdentityFifty.allowSpectatorActions)
                }

                IdentityFifty.spectators[data.uuid] = data

                it.sender.sendTranslateMsg("select_spectator")
            })

        addCommand(SCommandObject().addNeedPermission("identity.op")
            .addArg(SCommandArg().addAllowString("as")).addArg(SCommandArg("spectator"))
            .addArg(SCommandArg().addAllowType(SCommandArgType.ONLINE_PLAYER))
            .setNormalExecutor {

                val p = Bukkit.getPlayer(it.args[2])!!

                IdentityFifty.survivors.remove(p.uniqueId)
                IdentityFifty.hunters.remove(p.uniqueId)

                val data = SpectatorData().apply {
                    uuid = p.uniqueId
                    mcid = p.name
                    actions.addAll(IdentityFifty.allowSpectatorActions)
                }

                IdentityFifty.spectators[data.uuid] = data

                it.sender.sendTranslateMsg("select_other_spectator", p.name)
            })

        addCommand(SCommandObject().addArg(SCommandArg().addAllowString("unregister")).setPlayerExecutor {
            if (DiscordClient.enable){
                it.sender.sendMessage("§c登録解除はできません")
                return@setPlayerExecutor
            }
            IdentityFifty.hunters.remove(it.sender.uniqueId)
            IdentityFifty.survivors.remove(it.sender.uniqueId)
            IdentityFifty.spectators.remove(it.sender.uniqueId)

            it.sender.sendTranslateMsg("unregistered")
        })

        addCommand(SCommandObject().addNeedPermission("identity.op")
            .addArg(SCommandArg().addAllowString("as")).addArg(SCommandArg().addAllowString("unregister"))
            .addArg(SCommandArg().addAllowType(SCommandArgType.ONLINE_PLAYER)).setNormalExecutor {
                val p = Bukkit.getPlayer(it.args[2])!!
                IdentityFifty.hunters.remove(p.uniqueId)
                IdentityFifty.survivors.remove(p.uniqueId)
                IdentityFifty.spectators.remove(p.uniqueId)

                it.sender.sendTranslateMsg("unregistered_other", p.name)
        })

        addCommand(SCommandObject().addNeedPermission("identity.op")
            .addArg(SCommandArg().addAllowString("start")).addArg(SCommandArg().addAllowString(IdentityFifty.maps.keys.toTypedArray()))
            .setPlayerExecutor {
                val map = IdentityFifty.maps[it.args[1]]!!.clone()
                if (map.survivorLimit < IdentityFifty.survivors.size) {
                    it.sender.prefixMsg("§cサバイバーは${map.survivorLimit}人までです！")
                    return@setPlayerExecutor
                }

                if (map.hunterLimit < IdentityFifty.hunters.size) {
                    it.sender.prefixMsg("§cハンターは${map.hunterLimit}人までです！")
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

        addCommand(SCommandObject().addNeedPermission("identity.op")
            .addArg(SCommandArg().addAllowString("playerlist"))
            .addArg(SCommandArg(SCommandArgType.ONLINE_PLAYER).addAlias(translate("player_name")))
            .setPlayerExecutor {
                val p = Bukkit.getPlayer(it.args[1])!!
                if (IdentityFifty.survivors.containsKey(p.uniqueId)) {
                    it.sender.sendMessage("§e${p.name} §d${translate(IdentityFifty.survivors[p.uniqueId]!!.survivorClass.name)}")
                } else if (IdentityFifty.hunters.containsKey(p.uniqueId)) {
                    it.sender.sendMessage("§e${p.name} §d${translate(IdentityFifty.hunters[p.uniqueId]!!.hunterClass.name)}")
                } else {
                    it.sender.sendMessage("§c${p.name}はゲームに参加していません")
                }
            })

        addCommand(SCommandObject().addNeedPermission("identity.op")
            .addArg(SCommandArg().addAllowString("playerlist")).addArg(SCommandArg("all")).setPlayerExecutor {
            it.sender.sendMessage("§b${translate("survivor")}")
            IdentityFifty.survivors.forEach { data ->
                it.sender.sendMessage("§e${data.value.name} §d${translate(data.value.survivorClass.name)}")
            }

            it.sender.sendMessage("§c${translate("hunter")}")
            IdentityFifty.hunters.forEach { data ->
                it.sender.sendMessage("§e${data.value.name} §d${translate(data.value.hunterClass.name)}")
            }
        })

        addCommand(SCommandObject().addNeedPermission("identity.op")
            .addArg(SCommandArg().addAllowString("stop")).setPlayerExecutor {
            IdentityFifty.identityFiftyTask?.end()
            it.sender.sendTranslateMsg("stopped_game")
        })

        addCommand(SCommandObject().addNeedPermission("identity.op")
            .addArg(SCommandArg().addAllowString("edit")).setPlayerExecutor {
            MapList().open(it.sender)
        })

        addCommand(SCommandObject().addArg(SCommandArg("info")).addArg(SCommandArg("survivor")).addArg(SCommandArg(IdentityFifty.survivorsData.keys)).setPlayerExecutor {
            SurvivorInfoInv(IdentityFifty.survivorsData[it.args[2]]!!).open(it.sender)
        })

        addCommand(SCommandObject().addArg(SCommandArg("info")).addArg(SCommandArg("hunter")).addArg(SCommandArg(IdentityFifty.huntersData.keys)).setPlayerExecutor {
            HunterInfoInv(IdentityFifty.huntersData[it.args[2]]!!).open(it.sender)
        })

        addCommand(SCommandObject().addArg(SCommandArg("talent")).setPlayerExecutor {
            if (IdentityFifty.survivors.containsKey(it.sender.uniqueId)){
                val data = IdentityFifty.survivors[it.sender.uniqueId]!!
                SurvivorTalentInv(data).Center().open(it.sender)
            } else if (IdentityFifty.hunters.containsKey(it.sender.uniqueId)){
                val data = IdentityFifty.hunters[it.sender.uniqueId]!!
                HunterTalentInv(data).Center().open(it.sender)
            } else {
                it.sender.sendTranslateMsg("not_registered")
            }
        })

        //debug

        addCommand(setDebug()
            .addArg(SCommandArg("remainingTime"))
            .setNormalExecutor {
                val data = it.sender.survivorDataCheck(it.args[2])?:return@setNormalExecutor
                it.sender.sendTranslateMsg("now_remaining_time", data.name, data.remainingTime.toString())
            })

        addCommand(setDebug()
            .addArg(SCommandArg("remainingTime"))
            .addArg(SCommandArg(SCommandArgType.INT).addAlias("int"))
            .setNormalExecutor {
                val data = it.sender.survivorDataCheck(it.args[2])?:return@setNormalExecutor

                data.remainingTime = it.args[4].toPlusInt(it.sender)?:return@setNormalExecutor
                it.sender.sendTranslateMsg("set_remaining_time", data.name, data.remainingTime.toString())
            })

        addCommand(setDebug()
            .addArg(SCommandArg("health"))
            .setNormalExecutor {
                val data = it.sender.survivorDataCheck(it.args[2])?:return@setNormalExecutor
                it.sender.sendTranslateMsg("now_health", data.name, data.getHealth().toString())
            })

        addCommand(setDebug()
            .addArg(SCommandArg("health"))
            .addArg(SCommandArg(SCommandArgType.INT).addAlias("int"))
            .setNormalExecutor {
                if (it.args[4].toInt() !in -1..5){
                    it.sender.sendTranslateMsg("health_range_error")
                    return@setNormalExecutor
                }
                val data = it.sender.survivorDataCheck(it.args[2])?:return@setNormalExecutor

                data.setHealth(it.args[4].toInt(),true)
                it.sender.sendTranslateMsg("set_health", data.name, data.getHealth().toString())
            })

        addCommand(setDebug()
            .addArg(SCommandArg("helpTick"))
            .setNormalExecutor {
                val data = it.sender.survivorDataCheck(it.args[2])?:return@setNormalExecutor
                it.sender.sendTranslateMsg("now_help_tick", data.name, data.helpTick.toString())
            })

        addCommand(setDebug()
            .addArg(SCommandArg("helpTick"))
            .addArg(SCommandArg(SCommandArgType.INT).addAlias("int"))
            .setNormalExecutor {
                val data = it.sender.survivorDataCheck(it.args[2])?:return@setNormalExecutor

                data.helpTick = it.args[4].toPlusInt(it.sender)?:return@setNormalExecutor
                it.sender.sendTranslateMsg("set_help_tick", data.name, data.helpTick.toString())
            })

        addCommand(setDebug()
            .addArg(SCommandArg("healTick"))
            .setNormalExecutor {
                val data = it.sender.survivorDataCheck(it.args[2])?:return@setNormalExecutor
                it.sender.sendTranslateMsg("now_heal_tick", data.name, data.healTick.toString())
            })

        addCommand(setDebug()
            .addArg(SCommandArg("healTick"))
            .addArg(SCommandArg(SCommandArgType.INT).addAlias("int"))
            .setNormalExecutor {
                val data = it.sender.survivorDataCheck(it.args[2])?:return@setNormalExecutor

                data.healTick = it.args[4].toPlusInt(it.sender)?:return@setNormalExecutor
                it.sender.sendTranslateMsg("set_heal_tick", data.name, data.healTick.toString())
            })

        addCommand(setDebug()
            .addArg(SCommandArg("healProcess"))
            .setNormalExecutor {
                val data = it.sender.survivorDataCheck(it.args[2])?:return@setNormalExecutor
                it.sender.sendTranslateMsg("now_heal_process", data.name, data.healProcess.toString())
            })

        addCommand(setDebug()
            .addArg(SCommandArg("healProcess"))
            .addArg(SCommandArg(SCommandArgType.DOUBLE).addAlias("double"))
            .setNormalExecutor {
                if (it.args[4].toDouble() !in 0.0..1.0){
                    it.sender.sendTranslateMsg("heal_process_range_error")
                    return@setNormalExecutor
                }
                val data = it.sender.survivorDataCheck(it.args[2])?:return@setNormalExecutor

                data.healProcess = it.args[4].toDouble()
                it.sender.sendTranslateMsg("set_heal_process", data.name, data.healProcess.toString())
            })

        addCommand(setDebug()
            .addArg(SCommandArg("otherPlayerHealDelay"))
            .setNormalExecutor {
                val data = it.sender.survivorDataCheck(it.args[2])?:return@setNormalExecutor
                it.sender.sendTranslateMsg("now_other_player_heal_delay", data.name, data.otherPlayerHealDelay.toString())
            })

        addCommand(setDebug()
            .addArg(SCommandArg("otherPlayerHealDelay"))
            .addArg(SCommandArg(SCommandArgType.INT).addAlias("int"))
            .setNormalExecutor {
                val data = it.sender.survivorDataCheck(it.args[2])?:return@setNormalExecutor

                data.otherPlayerHealDelay = it.args[4].toPlusInt(it.sender)?:return@setNormalExecutor
                it.sender.sendTranslateMsg("set_other_player_heal_delay", data.name, data.otherPlayerHealDelay.toString())
            })

        addCommand(setDebug()
            .addArg(SCommandArg("otherPlayerHealDelayPercentage"))
            .setNormalExecutor {
                val data = it.sender.survivorDataCheck(it.args[2])?:return@setNormalExecutor
                it.sender.sendTranslateMsg("now_other_player_heal_delay_percentage", data.name, data.otherPlayerHealDelayPercentage.toString())
            })

        addCommand(setDebug()
            .addArg(SCommandArg("otherPlayerHealDelayPercentage"))
            .addArg(SCommandArg(SCommandArgType.DOUBLE).addAlias("double"))
            .setNormalExecutor {
                if (it.args[4].toDouble() !in 0.0..1.0){
                    it.sender.sendTranslateMsg("other_player_heal_delay_percentage_range_error")
                    return@setNormalExecutor
                }
                val data = it.sender.survivorDataCheck(it.args[2])?:return@setNormalExecutor

                data.otherPlayerHealDelayPercentage = it.args[4].toDouble()
                it.sender.sendTranslateMsg("set_other_player_heal_delay_percentage", data.name, data.otherPlayerHealDelayPercentage.toString())
            })

        addCommand(setDebug()
            .addArg(SCommandArg("otherPlayerHelpDelay"))
            .setNormalExecutor {
                val data = it.sender.survivorDataCheck(it.args[2])?:return@setNormalExecutor
                it.sender.sendTranslateMsg("now_other_player_help_delay", data.name, data.otherPlayerHelpDelay.toString())
            })

        addCommand(setDebug()
            .addArg(SCommandArg("otherPlayerHelpDelay"))
            .addArg(SCommandArg(SCommandArgType.INT).addAlias("int"))
            .setNormalExecutor {
                val data = it.sender.survivorDataCheck(it.args[2])?:return@setNormalExecutor

                data.otherPlayerHelpDelay = it.args[4].toPlusInt(it.sender)?:return@setNormalExecutor
                it.sender.sendTranslateMsg("set_other_player_help_delay", data.name, data.otherPlayerHelpDelay.toString())
            })

        addCommand(setDebug()
            .addArg(SCommandArg("otherPlayerHelpDelayPercentage"))
            .setNormalExecutor {
                val data = it.sender.survivorDataCheck(it.args[2])?:return@setNormalExecutor
                it.sender.sendTranslateMsg("now_other_player_help_delay_percentage", data.name, data.otherPlayerHelpDelayPercentage.toString())
            })

        addCommand(setDebug()
            .addArg(SCommandArg("otherPlayerHelpDelayPercentage"))
            .addArg(SCommandArg(SCommandArgType.DOUBLE).addAlias("double"))
            .setNormalExecutor {
                if (it.args[4].toDouble() !in 0.0..1.0){
                    it.sender.sendTranslateMsg("other_player_help_delay_percentage_range_error")
                    return@setNormalExecutor
                }
                val data = it.sender.survivorDataCheck(it.args[2])?:return@setNormalExecutor

                data.otherPlayerHelpDelayPercentage = it.args[4].toDouble()
                it.sender.sendTranslateMsg("set_other_player_help_delay_percentage", data.name, data.otherPlayerHelpDelayPercentage.toString())
            })

        addCommand(setDebug()
            .addArg(SCommandArg("hatchTick"))
            .setNormalExecutor {
                val data = it.sender.survivorDataCheck(it.args[2])?:return@setNormalExecutor
                it.sender.sendTranslateMsg("now_hatch_tick", data.name, data.hatchTick.toString())
            })

        addCommand(setDebug()
            .addArg(SCommandArg("hatchTick"))
            .addArg(SCommandArg(SCommandArgType.INT).addAlias("int"))
            .setNormalExecutor {
                val data = it.sender.survivorDataCheck(it.args[2])?:return@setNormalExecutor

                data.hatchTick = it.args[4].toPlusInt(it.sender)?:return@setNormalExecutor
                it.sender.sendTranslateMsg("set_hatch_tick", data.name, data.hatchTick.toString())
            })

        addCommand(setDebug()
            .addArg(SCommandArg("footprintsTime"))
            .setNormalExecutor {
                val data = it.sender.survivorDataCheck(it.args[2])?:return@setNormalExecutor
                it.sender.sendTranslateMsg("now_footprints_time", data.name, data.footprintsTime.toString())
            })

        addCommand(setDebug()
            .addArg(SCommandArg("footprintsTime"))
            .addArg(SCommandArg(SCommandArgType.DOUBLE).addAlias("double"))
            .setNormalExecutor {
                val data = it.sender.survivorDataCheck(it.args[2])?:return@setNormalExecutor

                data.footprintsTime = it.args[4].toPlusDouble(it.sender)?:return@setNormalExecutor
                it.sender.sendTranslateMsg("set_footprints_time", data.name, data.footprintsTime.toString())
            })

        addCommand(setDebug()
            .addArg(SCommandArg("survivorTalentCost"))
            .setNormalExecutor {
                val data = it.sender.survivorDataCheck(it.args[2])?:return@setNormalExecutor
                it.sender.sendTranslateMsg("now_talent_cost", data.name, data.talentCost.toString())
            })

        addCommand(setDebug()
            .addArg(SCommandArg("survivorTalentCost"))
            .addArg(SCommandArg(SCommandArgType.INT).addAlias("int"))
            .setNormalExecutor {
                val data = it.sender.survivorDataCheck(it.args[2])?:return@setNormalExecutor

                data.talentCost = it.args[4].toPlusInt(it.sender)?:return@setNormalExecutor
                it.sender.sendTranslateMsg("set_talent_cost", data.name, data.talentCost.toString())
            })

        addCommand(setDebug()
            .addArg(SCommandArg("hunterTalentCost"))
            .setNormalExecutor {
                val data = it.sender.hunterDataCheck(it.args[2])?:return@setNormalExecutor
                it.sender.sendTranslateMsg("now_talent_cost", data.name, data.talentCost.toString())
            })

        addCommand(setDebug()
            .addArg(SCommandArg("hunterTalentCost"))
            .addArg(SCommandArg(SCommandArgType.INT).addAlias("int"))
            .setNormalExecutor {
                val data = it.sender.hunterDataCheck(it.args[2])?:return@setNormalExecutor

                data.talentCost = it.args[4].toPlusInt(it.sender)?:return@setNormalExecutor
                it.sender.sendTranslateMsg("set_talent_cost", data.name, data.talentCost.toString())
            })

        registerDebugCommand("identity.op")

        addCommand(SCommandObject()
            .addArg(SCommandArg("test"))
            .setPlayerExecutor {
                it.sender.sendMessage(it.sender.location.block.boundingBox.centerY.toString())
                it.sender.sendMessage(it.sender.location.block.boundingBox.maxY.toString())
                it.sender.sendMessage(it.sender.location.block.boundingBox.minY.toString())
            })

    }

    private fun setDebug(): SCommandObject = debug().addArg(SCommandArg("set")).addArg(SCommandArg(SCommandArgType.ONLINE_PLAYER).addAlias(
        translate("player_name")))
    private fun debug(): SCommandObject = command().addNeedPermission("identity.op").addArg(SCommandArg("debug"))

    private fun CommandSender.survivorDataCheck(uuid: UUID?): SurvivorData? {
        val data = IdentityFifty.survivors[uuid]
        if (data == null){
            sendTranslateMsg("survivor_data_not_found")
            return null
        }
        return data
    }

    private fun CommandSender.survivorDataCheck(name: String?): SurvivorData? {
        return survivorDataCheck(name?.toPlayer()?.uniqueId)
    }

    private fun CommandSender.hunterDataCheck(uuid: UUID?): HunterData? {
        val data = IdentityFifty.hunters[uuid]
        if (data == null){
            sendTranslateMsg("hunter_data_not_found")
            return null
        }
        return data
    }

    private fun CommandSender.hunterDataCheck(name: String?): HunterData? {
        return hunterDataCheck(name?.toPlayer()?.uniqueId)
    }

    private fun String.toPlusInt(commandSender: CommandSender): Int? {
        val int = this.toIntOrNull()
        if (int == null || int < 0){
            commandSender.sendTranslateMsg("plus_int_error")
            return null
        }
        return int
    }

    private fun String.toPlusDouble(commandSender: CommandSender): Double? {
        val double = this.toDoubleOrNull()
        if (double == null || double < 0){
            commandSender.sendTranslateMsg("plus_double_error")
            return null
        }
        return double
    }

}
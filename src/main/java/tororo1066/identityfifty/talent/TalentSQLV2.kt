package tororo1066.identityfifty.talent

import com.mongodb.client.model.Updates
import org.bukkit.entity.Player
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.talent.hunter.AbstractHunterTalent
import tororo1066.identityfifty.talent.survivor.AbstractSurvivorTalent
import tororo1066.tororopluginapi.database.SDBCondition
import tororo1066.tororopluginapi.database.SDBVariable
import tororo1066.tororopluginapi.database.SDatabase
import java.util.UUID
import java.util.concurrent.CompletableFuture

class TalentSQLV2 {

    val sDatabase = SDatabase.newInstance(IdentityFifty.plugin)

    init {
        sDatabase.asyncCreateTable(
            "talent", mapOf(
                "id" to SDBVariable(SDBVariable.Int, autoIncrement = true),
                "uuid" to SDBVariable(SDBVariable.VarChar, length = 36),
                "name" to SDBVariable(SDBVariable.VarChar, length = 16),
                "preset_name" to SDBVariable(SDBVariable.VarChar, length = 50),
                "type" to SDBVariable(SDBVariable.VarChar, length = 16),
                "talent_list" to SDBVariable(SDBVariable.Text),
            )
        ).exceptionally {
            null
        }
    }

    private fun getTalents(searchUUID: UUID, isSurvivor: Boolean): CompletableFuture<List<Pair<String, List<Any>>>> {
        val talents = arrayListOf<Pair<String, List<Any>>>()
        if (sDatabase.isMongo) {
            return sDatabase.asyncSelect(
                "talent", SDBCondition().equal("uuid", searchUUID.toString())
                    .and(SDBCondition().equal("type", if (isSurvivor) "survivor" else "hunter"))
            ).thenApplyAsync { rs ->
                rs.forEach {
                    val talentMap = it.getNullableDeepResult("talents") ?: return@forEach
                    talentMap.result.forEach { (presetName, _) ->
                        val talentsList = arrayListOf<Any>()
                        talentMap.getList<String>(presetName).forEach { talentName ->
                            val talent: Any? = if (isSurvivor) {
                                AbstractSurvivorTalent.getTalent(talentName)
                            } else {
                                AbstractHunterTalent.getTalent(talentName)
                            }
                            if (talent != null) {
                                talentsList.add(talent)
                            }
                        }
                        talents.add(Pair(presetName, talentsList))
                    }
                }
                talents
            }
        } else {
            return sDatabase.asyncSelect(
                "talent", SDBCondition().equal("uuid", searchUUID.toString())
                    .and(SDBCondition().equal("type", if (isSurvivor) "survivor" else "hunter"))
            ).thenApplyAsync { rs ->
                rs.forEach {
                    val talentList = it.getString("talent_list").split(",")
                    val talentDataList = arrayListOf<Any>()
                    talentList.forEach { talentName ->
                        val talent: Any? = if (isSurvivor) {
                            AbstractSurvivorTalent.getTalent(talentName)
                        } else {
                            AbstractHunterTalent.getTalent(talentName)
                        }
                        if (talent != null) {
                            talentDataList.add(talent)
                        }
                    }
                    talents.add(Pair(it.getString("preset_name"), talentDataList))
                }
                talents
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun getSurvivorTalents(searchUUID: UUID): CompletableFuture<List<Pair<String, List<AbstractSurvivorTalent>>>> =
        getTalents(searchUUID, true) as CompletableFuture<List<Pair<String, List<AbstractSurvivorTalent>>>>

    @Suppress("UNCHECKED_CAST")
    fun getHunterTalents(searchUUID: UUID): CompletableFuture<List<Pair<String, List<AbstractHunterTalent>>>> =
        getTalents(searchUUID, false) as CompletableFuture<List<Pair<String, List<AbstractHunterTalent>>>>

    fun insertTalents(
        p: Player,
        presetName: String,
        talentList: List<Any>,
        isSurvivor: Boolean
    ): CompletableFuture<Boolean> {
        val talentNameList = talentList.map { it.javaClass.simpleName }
        return if (sDatabase.isMongo) {
            sDatabase.asyncSelect("talent", SDBCondition().equal("uuid", p.uniqueId.toString())
                .and(SDBCondition().equal("type", if (isSurvivor) "survivor" else "hunter"))
            ).thenApplyAsync { rs ->
                val talents = rs.firstOrNull()?.getNullableDeepResult("talents")
                if (talents == null) {
                    return@thenApplyAsync sDatabase.insert(
                        "talent", mapOf(
                            "uuid" to p.uniqueId.toString(),
                            "name" to p.name,
                            "type" to if (isSurvivor) "survivor" else "hunter",
                            "talents" to mapOf(presetName to talentNameList)
                        )
                    )
                } else {
                    return@thenApplyAsync sDatabase.update(
                        "talent", Updates.push("talents.${presetName}", talentNameList), SDBCondition().equal("uuid", p.uniqueId.toString())
                            .and(SDBCondition().equal("type", if (isSurvivor) "survivor" else "hunter"))
                    )
                }
            }
        } else {
            sDatabase.asyncInsert(
                "talent", mapOf(
                    "uuid" to p.uniqueId.toString(),
                    "name" to p.name,
                    "preset_name" to presetName,
                    "type" to if (isSurvivor) "survivor" else "hunter",
                    "talent_list" to talentNameList.joinToString(",")
                )
            )
        }
    }

    fun dupeTalentName(searchUUID: UUID, presetName: String, isSurvivor: Boolean): CompletableFuture<Boolean> {
        val clazzType = if (isSurvivor) "survivor" else "hunter"
        return if (sDatabase.isMongo) {
            sDatabase.asyncSelect("talent", SDBCondition().equal("uuid", searchUUID.toString())
                .and(SDBCondition().equal("type", clazzType))
            ).thenApplyAsync { rs ->
                rs.firstOrNull()?.getNullableDeepResult("talents")?.result?.containsKey(presetName) ?: false
            }
        } else {
            sDatabase.asyncSelect("talent", SDBCondition().equal("uuid", searchUUID.toString())
                .and(SDBCondition().equal("type", clazzType))
                .and(SDBCondition().equal("preset_name", presetName))
            ).thenApplyAsync { rs ->
                rs.isNotEmpty()
            }
        }
    }

    fun removeTalent(searchUUID: UUID, presetName: String, isSurvivor: Boolean): CompletableFuture<Boolean> {
        val clazzType = if (isSurvivor) "survivor" else "hunter"
        return if (sDatabase.isMongo) {
            sDatabase.asyncSelect("talent", SDBCondition().equal("uuid", searchUUID)
                .and(SDBCondition().equal("type", clazzType))
            ).thenApplyAsync { rs ->
                val talents = rs.firstOrNull()?.getNullableDeepResult("talents")
                if (talents == null) {
                    return@thenApplyAsync false
                } else {
                    return@thenApplyAsync sDatabase.update(
                        "talent", Updates.unset("talents.$presetName"), SDBCondition().equal("uuid", searchUUID)
                            .and(SDBCondition().equal("type", clazzType))
                    )
                }
            }
        } else {
            sDatabase.asyncDelete("talent", SDBCondition().equal("uuid", searchUUID)
                .and(SDBCondition().equal("type", clazzType))
                .and(SDBCondition().equal("preset_name", presetName))
            )
        }
    }
}
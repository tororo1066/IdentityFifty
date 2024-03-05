package tororo1066.identityfifty

import tororo1066.identityfifty.data.HunterData
import tororo1066.identityfifty.data.SurvivorData
import tororo1066.tororopluginapi.database.SDBVariable
import tororo1066.tororopluginapi.database.SDatabase
import tororo1066.tororopluginapi.database.SDatabase.Companion.toSQLVariable
import java.util.Date

class IdentityFiftyCharacterLogSQL {

    private val sDatabase = SDatabase.newInstance(IdentityFifty.plugin)

    init {
        sDatabase.createTable("identity_character_log", mapOf(
            "id" to SDBVariable(SDBVariable.Int, autoIncrement = true),
            "game_id" to SDBVariable(SDBVariable.Int),
            "uuid" to SDBVariable(SDBVariable.VarChar, length = 36),
            "name" to SDBVariable(SDBVariable.VarChar, length = 16, nullable = true),
            "type" to SDBVariable(SDBVariable.VarChar, length = 10),
            "character" to SDBVariable(SDBVariable.VarChar, length = 100),
            "talent" to SDBVariable(SDBVariable.Text),
            "date" to SDBVariable(SDBVariable.DateTime),
            "result" to SDBVariable(SDBVariable.VarChar, length = 4),
        ))
    }

    fun insertAll(survivorWin: Boolean?){ //nullでドロー
        val date = Date()

        if (sDatabase.isMongo) {
            val survivors = IdentityFifty.survivors.values.associate<SurvivorData, String, Any> {
                it.uuid.toString() to mapOf(
                    "uuid" to it.uuid.toString(),
                    "name" to it.name,
                    "character" to it.survivorClass.name,
                    "talent" to it.talentClasses.values.map { talent -> talent.javaClass.simpleName }
                )
            }

            val hunters = IdentityFifty.hunters.values.associate<HunterData, String, Any> {
                it.uuid.toString() to mapOf(
                    "uuid" to it.uuid.toString(),
                    "name" to it.name,
                    "character" to it.hunterClass.name,
                    "talent" to it.talentClasses.values.map { talent -> talent.javaClass.simpleName }
                )
            }

            sDatabase.backGroundInsert("identity_character_log", mapOf(
                "survivors" to survivors,
                "hunters" to hunters,
                "result" to if (survivorWin == null) "draw" else if (survivorWin) "survivor" else "hunter",
                "date" to date,
            ))
        } else {
            val gameId = (sDatabase.query("SELECT MAX(game_id) FROM identity_character_log").firstOrNull()?.getInt("MAX(game_id)")?:0)+1

            IdentityFifty.survivors.values.forEach {
                sDatabase.insert("identity_character_log", mapOf(
                    "game_id" to gameId + 1,
                    "uuid" to it.uuid.toString(),
                    "name" to it.name,
                    "type" to "survivor",
                    "character" to it.survivorClass.name,
                    "talent" to it.talentClasses.values.joinToString(",") { talent -> talent.javaClass.simpleName },
                    "date" to date.toSQLVariable(SDBVariable.DateTime),
                    "result" to if (survivorWin == null) "draw" else if (survivorWin) "win" else "lose"
                ))
            }

            IdentityFifty.hunters.values.forEach {
                sDatabase.insert("identity_character_log", mapOf(
                    "game_id" to gameId + 1,
                    "uuid" to it.uuid.toString(),
                    "name" to it.name,
                    "type" to "hunter",
                    "character" to it.hunterClass.name,
                    "talent" to it.talentClasses.values.joinToString(",") { talent -> talent.javaClass.simpleName },
                    "date" to date.toSQLVariable(SDBVariable.DateTime),
                    "result" to if (survivorWin == null) "draw" else if (survivorWin) "lose" else "win"
                ))
            }
        }
    }
}
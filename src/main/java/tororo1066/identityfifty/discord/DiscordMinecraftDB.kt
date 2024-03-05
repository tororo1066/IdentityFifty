package tororo1066.identityfifty.discord

import tororo1066.tororopluginapi.SJavaPlugin
import tororo1066.tororopluginapi.database.SDBCondition
import tororo1066.tororopluginapi.database.SDBResultSet
import tororo1066.tororopluginapi.database.SDBVariable
import tororo1066.tororopluginapi.database.SDatabase
import java.util.UUID

class DiscordMinecraftDB {
    private var database = SDatabase.newInstance(SJavaPlugin.plugin)

    init {
        database.createTable("discord_minecraft", mapOf(
            "id" to SDBVariable(SDBVariable.Int, autoIncrement = true),
            "discordId" to SDBVariable(SDBVariable.BigInt),
            "uuid" to SDBVariable(SDBVariable.VarChar, 36),
            "mcid" to SDBVariable(SDBVariable.VarChar, 16)
        ))
    }

    fun getFromDiscordId(id: Long): SDBResultSet? {
        val result = database.select("discord_minecraft", SDBCondition().equal("discordId", id))
        if (result.isNotEmpty()) return result.first()
        return null
    }

    fun getFromMcid(mcid: String): SDBResultSet? {
        val result = database.select("discord_minecraft", SDBCondition().equal("mcid", mcid))
        if (result.isNotEmpty()) return result.first()
        return null
    }

    fun getFromUUID(uuid: UUID): SDBResultSet? {
        val result = database.select("discord_minecraft", SDBCondition().equal("uuid", uuid.toString()))
        if (result.isNotEmpty()) return result.first()
        return null
    }

    fun insertData(discordId: Long, uuid: UUID, mcid: String): Boolean {
        return database.insert("discord_minecraft", mapOf(
            "discordId" to discordId,
            "uuid" to uuid.toString(),
            "mcid" to mcid
        ))
    }
}
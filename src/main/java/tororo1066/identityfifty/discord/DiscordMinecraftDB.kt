package tororo1066.identityfifty.discord

import com.mongodb.client.model.Updates
import org.bukkit.Bukkit
import tororo1066.tororopluginapi.SJavaPlugin
import tororo1066.tororopluginapi.database.SDBCondition
import tororo1066.tororopluginapi.database.SDBResultSet
import tororo1066.tororopluginapi.database.SDBVariable
import tororo1066.tororopluginapi.database.SDatabase
import java.util.UUID
import java.util.concurrent.CompletableFuture

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

    fun getFromDiscordId_(id: Long): CompletableFuture<SDBResultSet?> {
        return database.asyncSelect(
            "discord_minecraft", SDBCondition().equal("discordId", id)
        ).thenApply { result ->
            if (result.isNotEmpty()) return@thenApply result.first()
            return@thenApply null
        }
    }

    fun getFromMcid(mcid: String): SDBResultSet? {
        val result = database.select("discord_minecraft", SDBCondition().equal("mcid", mcid))
        if (result.isNotEmpty()) return result.first()
        return null
    }

    fun getFromMcid_(mcid: String): CompletableFuture<SDBResultSet?> {
        return database.asyncSelect(
            "discord_minecraft", SDBCondition().equal("mcid", mcid)
        ).thenApply { result ->
            if (result.isNotEmpty()) return@thenApply result.first()
            return@thenApply null
        }
    }

    fun getFromUUID(uuid: UUID): SDBResultSet? {
        val result = database.select("discord_minecraft", SDBCondition().equal("uuid", uuid.toString()))
        if (result.isNotEmpty()) return result.first()
        return null
    }

    fun getFromUUID_(uuid: UUID): CompletableFuture<SDBResultSet?> {
        return database.asyncSelect(
            "discord_minecraft", SDBCondition().equal("uuid", uuid.toString())
        ).thenApply { result ->
            if (result.isNotEmpty()) return@thenApply result.first()
            return@thenApply null
        }
    }

    fun insertData(discordId: Long, uuid: UUID, mcid: String): Boolean {
        return database.insert("discord_minecraft", mapOf(
            "discordId" to discordId,
            "uuid" to uuid.toString(),
            "mcid" to mcid
        ))
    }

    fun insertData_(discordId: Long, uuid: UUID, mcid: String): CompletableFuture<Boolean> {
        return database.asyncInsert("discord_minecraft", mapOf(
            "discordId" to discordId,
            "uuid" to uuid.toString(),
            "mcid" to mcid
        ))
    }

    fun updateName(uuid: UUID): CompletableFuture<Boolean> {
        val player = Bukkit.getOfflinePlayer(uuid)
        return if (database.isMongo) {
            database.asyncUpdate(
                "discord_minecraft",
                Updates.push("mcid", player.name),
                SDBCondition().equal("uuid", uuid.toString())
            )
        } else {
            database.asyncUpdate(
                "discord_minecraft",
                mapOf("mcid" to player.name),
                SDBCondition().equal("uuid", uuid.toString())
            )
        }
    }
}
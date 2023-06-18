package tororo1066.identityfifty.discord

import org.bukkit.Bukkit
import tororo1066.identityfifty.IdentityFifty
import tororo1066.tororopluginapi.mysql.SMySQL
import tororo1066.tororopluginapi.mysql.SMySQLResultSet
import tororo1066.tororopluginapi.mysql.ultimate.USQLTable
import tororo1066.tororopluginapi.mysql.ultimate.USQLVariable
import java.util.UUID

class DiscordMinecraftSQL: USQLTable("discord_minecraft", SMySQL(IdentityFifty.plugin)) {

    companion object {
        val id = USQLVariable(USQLVariable.INT, autoIncrement = true)
        val discordId = USQLVariable(USQLVariable.BIGINT)
        val uuid = USQLVariable(USQLVariable.VARCHAR,36)
        val mcid = USQLVariable(USQLVariable.VARCHAR,16)
    }

    fun getFromDiscordId(id: Long): SMySQLResultSet? {

        val result = select(discordId.equal(id))
        if (result.isNotEmpty())return result.first()
        return null
    }

    fun getFromMcid(mcid: String): SMySQLResultSet? {

        val result = select(Companion.mcid.equal(mcid))
        if (result.isNotEmpty())return result.first()
        return null
    }

    fun getFromUUID(uuid: UUID): SMySQLResultSet? {

        val result = select(Companion.uuid.equal(uuid))
        if (result.isNotEmpty())return result.first()
        return null
    }

    fun insertData(discordId: Long, uuid: UUID, mcid: String): Boolean {
        return insert(discordId,uuid,mcid)
    }
}
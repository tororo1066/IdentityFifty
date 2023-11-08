package tororo1066.identityfifty.discord

import tororo1066.identityfifty.IdentityFifty
import tororo1066.tororopluginapi.database.SDBVariable
import tororo1066.tororopluginapi.mysql.SMySQL
import tororo1066.tororopluginapi.mysql.SMySQLResultSet
import tororo1066.tororopluginapi.mysql.ultimate.USQLTable
import java.util.*

class DiscordMinecraftSQL: USQLTable("discord_minecraft", SMySQL(IdentityFifty.plugin)) {

    companion object {
        val id = SDBVariable(SDBVariable.Int, autoIncrement = true)
        val discordId = SDBVariable(SDBVariable.BigInt)
        val uuid = SDBVariable(SDBVariable.VarChar,36)
        val mcid = SDBVariable(SDBVariable.VarChar,16)
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
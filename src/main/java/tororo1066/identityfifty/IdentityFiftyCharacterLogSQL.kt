package tororo1066.identityfifty

import tororo1066.tororopluginapi.database.SDBVariable
import tororo1066.tororopluginapi.mysql.SMySQL
import tororo1066.tororopluginapi.mysql.ultimate.USQLTable

class IdentityFiftyCharacterLogSQL: USQLTable("identity_character_log", SMySQL(IdentityFifty.plugin)) {

    companion object {
        val id = SDBVariable(SDBVariable.Int, autoIncrement = true)
        val uuid = SDBVariable(SDBVariable.VarChar, length = 36)
        val mcid = SDBVariable(SDBVariable.VarChar, length = 16, nullable = true)
        val type = SDBVariable(SDBVariable.VarChar, length = 10)
        val value = SDBVariable(SDBVariable.VarChar, length = 100)
        val date = SDBVariable(SDBVariable.DateTime)
    }

    fun insertAll(){
        IdentityFifty.survivors.values.forEach {
            callBackInsert(it.uuid, it.name, "survivor", it.survivorClass.name, "now()")
        }

        IdentityFifty.hunters.values.forEach {
            callBackInsert(it.uuid, it.name, "hunters", it.hunterClass.name, "now()")
        }
    }
}
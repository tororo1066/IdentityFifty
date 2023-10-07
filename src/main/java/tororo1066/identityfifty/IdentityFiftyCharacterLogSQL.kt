package tororo1066.identityfifty

import tororo1066.tororopluginapi.mysql.SMySQL
import tororo1066.tororopluginapi.mysql.ultimate.USQLTable
import tororo1066.tororopluginapi.mysql.ultimate.USQLVariable

class IdentityFiftyCharacterLogSQL: USQLTable("identity_character_log", SMySQL(IdentityFifty.plugin)) {

    companion object {
        val id = USQLVariable(USQLVariable.Int, autoIncrement = true)
        val uuid = USQLVariable(USQLVariable.VarChar, length = 36)
        val mcid = USQLVariable(USQLVariable.VarChar, length = 16, nullable = true)
        val type = USQLVariable(USQLVariable.VarChar, length = 10)
        val value = USQLVariable(USQLVariable.VarChar, length = 100)
        val date = USQLVariable(USQLVariable.DateTime)
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
package tororo1066.identityfifty.talent

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.talent.hunter.AbstractHunterTalent
import tororo1066.identityfifty.talent.survivor.AbstractSurvivorTalent
import tororo1066.tororopluginapi.database.SDBVariable
import tororo1066.tororopluginapi.mysql.SMySQL
import tororo1066.tororopluginapi.mysql.ultimate.USQLTable
import java.util.UUID

class TalentSQL: USQLTable("talent", SMySQL(IdentityFifty.plugin)){

    companion object{
        val id = SDBVariable(SDBVariable.Int,autoIncrement = true)
        val uuid = SDBVariable(SDBVariable.VarChar,36,false,SDBVariable.Index.KEY)
        val name = SDBVariable(SDBVariable.VarChar,16)
        val preset_name = SDBVariable(SDBVariable.VarChar,50)
        val type = SDBVariable(SDBVariable.VarChar,16)
        val talent_list = SDBVariable(SDBVariable.Text)
    }

    init {
        debug = false
    }

    fun getSurvivorTalents(searchUUID: UUID): List<Pair<String,List<AbstractSurvivorTalent>>>{
        val talents = arrayListOf<Pair<String,List<AbstractSurvivorTalent>>>()
        select(uuid.equal(searchUUID).and().equal(type,"survivor")).forEach {
            val talentList = talent_list.type.getVal(it).split(",")
            val talentDataList = arrayListOf<AbstractSurvivorTalent>()
            talentList.forEach { talentName ->
                val talent = AbstractSurvivorTalent.getTalent(talentName)
                if (talent != null){
                    talentDataList.add(talent)
                }
            }
            talents.add(Pair(preset_name.type.getVal(it),talentDataList))
        }
        return talents
    }

    fun getHunterTalents(searchUUID: UUID): List<Pair<String,List<AbstractHunterTalent>>> {
        val talents = arrayListOf<Pair<String,List<AbstractHunterTalent>>>()
        select(uuid.equal(searchUUID).and().equal(type,"hunter")).forEach {
            val talentList = talent_list.type.getVal(it).split(",")
            val talentDataList = arrayListOf<AbstractHunterTalent>()
            talentList.forEach { talentName ->
                val talent = AbstractHunterTalent.getTalent(talentName)
                if(talent != null){
                    talentDataList.add(talent)
                }
            }
            talents.add(Pair(preset_name.type.getVal(it),talentDataList))
        }
        return talents
    }

    fun insertSurvivorTalent(p: Player, presetName: String, talentList: List<AbstractSurvivorTalent>): Boolean {
        val talentNameList = arrayListOf<String>()
        talentList.forEach { talentNameList.add(it.name) }
        return insert(p.uniqueId,p.name,presetName,"survivor",talentNameList.joinToString(","))
    }

    fun insertHunterTalent(p: Player, presetName: String, talentList: List<AbstractHunterTalent>): Boolean {
        val talentNameList = arrayListOf<String>()
        talentList.forEach { talentNameList.add(it.name) }
        return insert(p.uniqueId,p.name,presetName,"hunter",talentNameList.joinToString(","))
    }

    fun dumpTalentName(searchUUID: UUID, clazzType: String, presetName: String): Boolean {
        select(uuid.equal(searchUUID).and().equal(type,clazzType).and().equal(preset_name,presetName)).forEach { _ ->
            return true
        }

        return false
    }

    fun removeTalent(searchUUID: UUID, clazzType: String, presetName: String): Boolean {
        return delete(uuid.equal(searchUUID).and().equal(type,clazzType).and().equal(preset_name,presetName))
    }

}
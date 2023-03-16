package tororo1066.identityfifty.talent

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.talent.hunter.AbstractHunterTalent
import tororo1066.identityfifty.talent.survivor.AbstractSurvivorTalent
import tororo1066.tororopluginapi.mysql.SMySQL
import tororo1066.tororopluginapi.mysql.ultimate.USQLTable
import tororo1066.tororopluginapi.mysql.ultimate.USQLVariable
import java.util.UUID

class TalentSQL: USQLTable("talent", SMySQL(IdentityFifty.plugin)){

    companion object{
        val id = USQLVariable(USQLVariable.INT,autoIncrement = true)
        val uuid = USQLVariable(USQLVariable.VARCHAR,36,false,USQLVariable.Index.KEY)
        val name = USQLVariable(USQLVariable.VARCHAR,16)
        val preset_name = USQLVariable(USQLVariable.VARCHAR,50)
        val type = USQLVariable(USQLVariable.VARCHAR,16)
        val talent_list = USQLVariable(USQLVariable.TEXT)
    }

    init {
        debug = true
        println(preset_name.name)
        println(preset_name.type.name)
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
            println(preset_name.type.getVal(it))
            println(type.type.getVal(it))
            println(preset_name.name)
            println(preset_name.type.name)
            println(it.getString("type"))
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

    fun insertSurvivorTalent(p: Player, presetName: String, talentList: List<AbstractSurvivorTalent>){
        val talentNameList = arrayListOf<String>()
        talentList.forEach { talentNameList.add(it.name) }
        insert(p.uniqueId,p.name,presetName,"survivor",talentNameList.joinToString(","))
    }

    fun insertHunterTalent(p: Player, presetName: String, talentList: List<AbstractHunterTalent>){
        val talentNameList = arrayListOf<String>()
        talentList.forEach { talentNameList.add(it.name) }
        insert(p.uniqueId,p.name,presetName,"hunter",talentNameList.joinToString(","))
    }

    fun dumpTalentName(searchUUID: UUID, clazzType: String, presetName: String): Boolean {
        select(uuid.equal(searchUUID).and().equal(type,clazzType).and().equal(preset_name,presetName)).forEach { _ ->
            return true
        }

        return false
    }

    fun removeTalent(searchUUID: UUID, clazzType: String, presetName: String){
        delete(uuid.equal(searchUUID).and().equal(type,clazzType).and().equal(preset_name,presetName))
    }

}
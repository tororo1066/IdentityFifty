package tororo1066.identityfifty

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.inventivetalent.glow.GlowAPI
import tororo1066.identityfifty.character.hunter.AbstractHunter
import tororo1066.identityfifty.character.hunter.Dasher
import tororo1066.identityfifty.character.survivor.AbstractSurvivor
import tororo1066.identityfifty.character.survivor.Nurse
import tororo1066.identityfifty.data.HunterData
import tororo1066.identityfifty.data.SurvivorData
import tororo1066.tororopluginapi.SJavaPlugin
import tororo1066.tororopluginapi.otherUtils.UsefulUtility
import tororo1066.tororopluginapi.sEvent.SEvent
import tororo1066.tororopluginapi.sItem.SInteractItemManager
import tororo1066.tororopluginapi.sItem.SItem
import java.util.UUID

class IdentityFifty : SJavaPlugin() {

    companion object {
        val survivorsData = HashMap<String,AbstractSurvivor>()
        val huntersData = HashMap<String,AbstractHunter>()
        val survivors = HashMap<UUID,SurvivorData>()
        val hunters = HashMap<UUID,HunterData>()
        lateinit var interactManager: SInteractItemManager
        lateinit var plugin: SJavaPlugin

        fun stunEffect(p: Player){
            p.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS,60,0,true,false,false))
            p.addPotionEffect(PotionEffect(PotionEffectType.SLOW,100,200,true,false,false))
            p.addPotionEffect(PotionEffect(PotionEffectType.JUMP,100,200,true,false,false))
        }
    }

    private fun register(string: String, abstractSurvivor: AbstractSurvivor){
        survivorsData[string] = abstractSurvivor
    }

    private fun register(string: String, abstractHunter: AbstractHunter){
        huntersData[string] = abstractHunter
    }

    private fun registerAll(){
        register("nurse",Nurse())
        register("dasher",Dasher())

    }

    override fun onEnable() {
        plugin = this
        interactManager = SInteractItemManager(this)
        SEvent(this).register(PlayerJoinEvent::class.java){
            it.player.inventory.setItem(0,IdentityFifty.interactManager.createSInteractItem(SItem(Material.DIAMOND).setDisplayName("test")).setInteractEvent { e ->
                GlowAPI.setGlowing(Bukkit.getPlayer("tororo_1066")!!,GlowAPI.Color.BLUE,e.player)
                Bukkit.getScheduler().runTaskLater(this, Runnable {
                    GlowAPI.setGlowing(Bukkit.getPlayer("tororo_1066")!!,false,e.player)
                },100)
            }.setInitialCoolDown(100))
        }
    }


}
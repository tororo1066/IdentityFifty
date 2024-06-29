package tororo1066.identityfifty.talent.hunter

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffectType
import tororo1066.identityfifty.IdentityFifty
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sItem.SItem

class StunClear : AbstractHunterTalent("stun_clear",5,SurvivorJailedSpeedUp::class.java) {
    override fun lore(): List<String> {
        return listOf(
            "stun_clear_lore_1",
            "stun_clear_lore_2",
            "stun_clear_lore_3"
        )
    }

    var noImp = false
    override fun onAttack(attackPlayer: Player, p: Player, noOne: Boolean) {
        noImp = true
        Bukkit.getScheduler().runTaskLater(IdentityFifty.plugin, Runnable {
            noImp = false
        },140)
    }

    override fun onStart(p: Player) {
        val stunClear = SItem(Material.STICK).setDisplayName(translate("stun_clear")).setCustomModelData(1111)
                .addLore(translate("stun_clear_lore_1"))
                .addLore(translate("stun_clear_lore_2"))
                .addLore(translate("stun_clear_lore_3"))


        val stunClearItem = IdentityFifty.interactManager.createSInteractItem(stunClear,noDump = true).setInteractEvent{ _, _ ->

            if(!noImp){
                p.world.playSound(p.location, Sound.BLOCK_BEACON_POWER_SELECT,1f,0.85f)
                p.removePotionEffect(PotionEffectType.BLINDNESS)
                p.removePotionEffect(PotionEffectType.SLOW)
                p.removePotionEffect(PotionEffectType.JUMP)
                p.removePotionEffect(PotionEffectType.WEAKNESS)
                p.removePotionEffect(PotionEffectType.SLOW_DIGGING)
                return@setInteractEvent true
            }

            return@setInteractEvent false
        }.setInitialCoolDown(2800)
        p.inventory.addItem(stunClearItem)
    }


}

//5ポイントかつSurvivorJailedSpeedUpに接続
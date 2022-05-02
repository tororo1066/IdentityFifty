package tororo1066.identityfifty.character.hunter

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.data.HunterData
import tororo1066.tororopluginapi.sItem.SInteractItem
import tororo1066.tororopluginapi.sItem.SItem
import java.util.function.Consumer

class Dasher : AbstractHunter("ダッシャー") {

    override fun onStart(p: Player) {
        p.addPotionEffect(PotionEffect(PotionEffectType.SPEED,2000000,0,false,false))
        val firstSkillItem = SItem(Material.DIAMOND_SHOVEL).setDisplayName("§c§l燃え上がる§4§l怒り").addLore("§f自身の移動速度を大幅に上げる").addLore("§c§lクールタイム§f：§e§l40§b§l秒")
        val firstSkill = IdentityFifty.interactManager.createSInteractItem(firstSkillItem,true).setInteractEvent {
            p.addPotionEffect(PotionEffect(PotionEffectType.SPEED,60,1))
            p.playSound(p.location, Sound.ENTITY_WITHER_SHOOT,1f,1.5f)
        }.setInitialCoolDown(800)

        p.inventory.setItem(0,firstSkill)



    }

    override fun parameters(data: HunterData): HunterData {
        data.hunterClass = this
        return data
    }

    override fun onFinishedGenerator(dieLocation: Location, p: Player) {

    }

    override fun onAttack(attackPlayer: Player, p: Player): Int {
        return 2
    }
}
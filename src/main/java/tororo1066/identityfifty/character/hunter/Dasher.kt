package tororo1066.identityfifty.character.hunter

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import tororo1066.identityfifty.IdentityFifty
import tororo1066.tororopluginapi.sItem.SInteractItem
import tororo1066.tororopluginapi.sItem.SItem
import java.util.function.Consumer

class Dasher : AbstractHunter("dasher") {

    override fun onStart(p: Player) {
        p.addPotionEffect(PotionEffect(PotionEffectType.SPEED,2000000,0,false,false))
        val firstSkillItem = SItem(Material.DIAMOND_SHOVEL).setDisplayName("§c§l燃え上がる§4§l怒り").addLore("")
        val firstSkill = IdentityFifty.interactManager.createSInteractItem(firstSkillItem,true).setInteractEvent { e ->
            p.addPotionEffect(PotionEffect(PotionEffectType.SPEED,60,1))
            p.playSound(p.location, Sound.ENTITY_WITHER_SHOOT,1f,1.5f)
        }.setInitialCoolDown(8000)



    }

    override fun onFinishedGenerator(dieLocation: Location, p: Player) {

    }
}
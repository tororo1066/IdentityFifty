package tororo1066.identityfifty.character.hunter

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.entity.Sheep
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.inventivetalent.glow.GlowAPI
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.data.HunterData
import tororo1066.tororopluginapi.lang.SLang.Companion.sendTranslateMsg
import kotlin.random.Random
import kotlin.random.nextInt
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sItem.SItem
import tororo1066.tororopluginapi.utils.toPlayer
import java.util.function.Consumer

class Gambler: AbstractHunter("gambler") {

    override fun onStart(p: Player) {
        val passiveItem = SItem(Material.STICK).setDisplayName(translate("hunter_passive")).setCustomModelData(1)
            .addLore(translate("gambler_passive_lore_1"))
        val firstSkillItem = SItem(Material.STICK).setDisplayName(translate("gamble_dice"))
            .addLore(translate("gamble_dice_lore_1"))
            .addLore(translate("gamble_dice_lore_2"))
        val firstSkill = IdentityFifty.interactManager.createSInteractItem(firstSkillItem,true).setInteractEvent { _, _ ->
            var count = 3
            Bukkit.getScheduler().runTaskTimer(IdentityFifty.plugin, { task ->
                if (count <= 0){
                    p.playSound(p.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2f, 1f)
                    when(Random.nextInt(1..100)){
                        in 1..15->{
                            p.sendTranslateMsg("gamble_dice_action_1")
                            val players = IdentityFifty.hunters.map { map -> map.key.toPlayer() }.filterNotNull().toMutableList()
                            IdentityFifty.survivors.values.forEach {
                                it.glowManager.glow(players, GlowAPI.Color.RED, 200)
                            }
                        }
                        in 16..30->{
                            p.sendTranslateMsg("gamble_dice_action_2")
                            val players = IdentityFifty.survivors.map { map -> map.key.toPlayer() }.filterNotNull().toMutableList()
                            IdentityFifty.hunters.values.forEach {
                                it.glowManager.glow(players, GlowAPI.Color.RED, 200)
                            }
                            players.forEach {
                                it.sendTranslateMsg("gamble_dice_action_2_survivors")
                            }
                        }
                        in 31..45->{
                            p.sendTranslateMsg("gamble_dice_action_3")
                            p.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 300, 0, false, false))
                        }
                        in 46..60->{
                            p.sendTranslateMsg("gamble_dice_action_4")
                            p.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 300, 0, false, false))
                        }
                        in 61..75->{
                            p.sendTranslateMsg("gamble_dice_action_5")
                            p.world.getEntitiesByClass(Sheep::class.java).filter { it.persistentDataContainer.has(
                                NamespacedKey(IdentityFifty.plugin, "Generator"), PersistentDataType.INTEGER) }.forEach {
                                    if (it.health + 300 > it.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.baseValue){
                                        it.health = it.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.baseValue
                                    } else {
                                        it.health += 300
                                    }
                                it.customName = "§f§l羊型発電機§5(§e${it.health.toInt()}§f/§b${it.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.baseValue.toInt()}§5)"
                            }
                        }
                        in 76..100->{
                            p.sendTranslateMsg("gamble_dice_action_6")
                            p.world.getEntitiesByClass(Sheep::class.java).filter { it.persistentDataContainer.has(
                                NamespacedKey(IdentityFifty.plugin, "Generator"), PersistentDataType.INTEGER) }.forEach {
                                if (it.health - 300 < 1){
                                    it.health = 1.0
                                } else {
                                    it.health -= 300
                                }
                                it.customName = "§f§l羊型発電機§5(§e${it.health.toInt()}§f/§b${it.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.baseValue.toInt()}§5)"
                            }
                        }
                    }
                    task.cancel()
                } else {
                    p.playSound(p.location, Sound.UI_BUTTON_CLICK, 2f, 1f)
                    count--
                }
            },0,20)
            return@setInteractEvent true
        }.setInitialCoolDown(1200)

        p.inventory.setItem(0,passiveItem)
        p.inventory.setItem(1,firstSkill)
    }

    override fun parameters(data: HunterData): HunterData {
        data.hunterClass = this
        return data
    }

    override fun onAttack(attackPlayer: Player, p: Player, isFinishedGenerator: Boolean): Int {
        if (isFinishedGenerator){
            return 4
        } else {
            when(Random.nextInt(1..100)){
                in 1..30 ->{
                    return 1
                }
                in 31..70->{
                    return 2
                }
                in 71..90->{
                    return 3
                }
                in 91..100->{
                    return 4
                }
            }
        }
        return 0
    }
}
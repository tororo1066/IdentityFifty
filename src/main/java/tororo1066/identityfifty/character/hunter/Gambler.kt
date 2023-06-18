package tororo1066.identityfifty.character.hunter

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Cow
import org.bukkit.entity.Player
import org.bukkit.entity.Sheep
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.inventivetalent.glow.GlowAPI
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.data.HunterData
import tororo1066.tororopluginapi.lang.SLang.Companion.sendTranslateMsg
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sItem.SItem
import tororo1066.tororopluginapi.utils.toPlayer
import kotlin.random.Random
import kotlin.random.nextInt

class Gambler: AbstractHunter("gambler") {

    private fun diceTask(p: Player){
        IdentityFifty.util.repeatDelay(3,20,{p.playSound(p.location, Sound.UI_BUTTON_CLICK, 2f, 1f)},{
            p.playSound(p.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2f, 1f)
            when(Random.nextInt(1..100)){
                in 1..20->{
                    p.sendTranslateMsg("gamble_dice_action_1")
                    val players = IdentityFifty.hunters.map { map -> map.key.toPlayer() }.filterNotNull().toMutableList()
                    IdentityFifty.survivors.values.forEach {
                        it.glowManager.glow(players, GlowAPI.Color.RED, 300)
                    }
                }
                in 21..35->{
                    p.sendTranslateMsg("gamble_dice_action_2")
                    val players = IdentityFifty.survivors.map { map -> map.key.toPlayer() }.filterNotNull().toMutableList()
                    IdentityFifty.hunters.values.forEach {
                        it.glowManager.glow(players, GlowAPI.Color.RED, 200)
                    }
                    players.forEach {
                        it.sendTranslateMsg("gamble_dice_action_2_survivors")
                    }
                }
                in 36..50->{
                    p.sendTranslateMsg("gamble_dice_action_3")
                    p.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 300, 1, false, false))
                }
                in 51..65->{
                    p.sendTranslateMsg("gamble_dice_action_4")
                    p.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 300, 0, false, false))
                }
                in 66..84->{
                    p.sendTranslateMsg("gamble_dice_action_5")
                    p.world.getEntitiesByClass(Sheep::class.java).filter { it.persistentDataContainer.has(
                        NamespacedKey(IdentityFifty.plugin, "Generator"), PersistentDataType.INTEGER) }.forEach {
                        if (it.health + 200 > it.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.baseValue){
                            it.health = it.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.baseValue
                        } else {
                            it.health += 200
                        }
                        it.customName = "§f§l羊型発電機§5(§e${it.health.toInt()}§f/§b${it.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.baseValue.toInt()}§5)"
                    }

                    p.world.getEntitiesByClass(Cow::class.java).filter { it.persistentDataContainer.has(
                        NamespacedKey(IdentityFifty.plugin, "EscapeGenerator"), PersistentDataType.INTEGER) }.forEach {
                        if (it.health + 100 > it.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.baseValue){
                            it.health = it.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.baseValue
                        } else {
                            it.health += 100
                        }
                        it.customName = "§f§l牛型発電機§5(§e${it.health.toInt()}§f/§b${it.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.baseValue.toInt()}§5)"
                    }
                }
                in 85..100->{
                    p.sendTranslateMsg("gamble_dice_action_6")
                    p.world.getEntitiesByClass(Sheep::class.java).filter { it.persistentDataContainer.has(
                        NamespacedKey(IdentityFifty.plugin, "Generator"), PersistentDataType.INTEGER) }.forEach {
                        if (it.health - 100 < 1){
                            it.health = 1.0
                        } else {
                            it.health -= 100
                        }
                        it.customName = "§f§l羊型発電機§5(§e${it.health.toInt()}§f/§b${it.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.baseValue.toInt()}§5)"
                    }
                }
            }
        })
    }

    override fun onStart(p: Player) {
        val passiveItem = SItem(Material.STICK).setDisplayName(translate("hunter_passive")).setCustomModelData(1)
            .addLore(translate("gambler_passive_lore_1"))
            .addLore(translate("gambler_passive_lore_2"))
        val firstSkillItem = SItem(Material.STICK).setDisplayName(translate("gamble_dice")).setCustomModelData(11)
            .addLore(translate("gamble_dice_lore_1"))
            .addLore(translate("gamble_dice_lore_2"))
        val firstSkill = IdentityFifty.interactManager.createSInteractItem(firstSkillItem,true).setInteractEvent { _, _ ->
            diceTask(p)
            return@setInteractEvent true
        }.setInitialCoolDown(600)

        p.inventory.addItem(passiveItem)
        p.inventory.addItem(firstSkill)
    }

    override fun parameters(data: HunterData): HunterData {
        data.hunterClass = this
        return data
    }

    override fun onAttack(attackPlayer: Player, p: Player, noOne: Boolean): Int {
        if (Random.nextInt(1..100) <= 30){
            diceTask(p)
        }
        if (noOne){
            return 4
        } else {
            when(Random.nextInt(1..100)){
                in 1..30 ->{
                    return 1
                }
                in 31..70->{
                    return 2
                }
                in 71..85->{
                    return 3
                }
                in 86..100->{
                    return 4
                }
            }
        }
        return 0
    }

    override fun info(): ArrayList<ItemStack> {
        val passiveItem = SItem(Material.STICK).setDisplayName(translate("hunter_passive")).setCustomModelData(1)
            .addLore(translate("gambler_passive_lore_1"))
            .addLore(translate("gambler_passive_lore_2"))
        val firstSkillItem = SItem(Material.STICK).setDisplayName(translate("gamble_dice")).setCustomModelData(11)
            .addLore(translate("gamble_dice_lore_1"))
            .addLore(translate("gamble_dice_lore_2"))
        return arrayListOf(passiveItem,firstSkillItem)
    }
}
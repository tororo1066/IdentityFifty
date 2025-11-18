package tororo1066.identityfifty.character.hunter

import net.kyori.adventure.text.Component
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
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.data.HunterData
import tororo1066.identityfifty.enumClass.AllowAction
import tororo1066.nmsutils.items.GlowColor
import tororo1066.tororopluginapi.lang.SLang.Companion.sendTranslateMsg
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sItem.SItem
import tororo1066.tororopluginapi.utils.addItem
import tororo1066.tororopluginapi.utils.toPlayer
import kotlin.random.Random
import kotlin.random.nextInt

class Gambler: AbstractHunter("gambler") {

    private fun diceTask(p: Player){
        IdentityFifty.util.repeatDelay(3,20,{p.world.playSound(p.location, Sound.UI_BUTTON_CLICK, 2f, 1f)},{
            p.world.playSound(p.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2f, 1f)
            when(Random.nextInt(1..100)){
                in 1..20->{
                    p.sendTranslateMsg("gamble_dice_action_1")
                    val players = IdentityFifty.hunters.map { map -> map.key.toPlayer() }.filterNotNull().toMutableList()
                    IdentityFifty.survivors.values.forEach {
                        it.glowManager.glow(players, GlowColor.RED, 300)
                    }
                    IdentityFifty.broadcastSpectators(translate("spec_gamble_dice_action_1",p.name),
                        AllowAction.RECEIVE_HUNTERS_ACTION)
                }
                in 21..35->{
                    p.sendTranslateMsg("gamble_dice_action_2")
                    val players = IdentityFifty.survivors.map { map -> map.key.toPlayer() }.filterNotNull().toMutableList()
                    IdentityFifty.hunters.values.forEach {
                        it.glowManager.glow(players, GlowColor.RED, 200)
                    }
                    players.forEach {
                        it.sendTranslateMsg("gamble_dice_action_2_survivors")
                    }
                    IdentityFifty.broadcastSpectators(translate("spec_gamble_dice_action_2",p.name),
                        AllowAction.RECEIVE_HUNTERS_ACTION)
                }
                in 36..50->{
                    p.sendTranslateMsg("gamble_dice_action_3")
                    p.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 300, 1, false, false))
                    IdentityFifty.broadcastSpectators(translate("spec_gamble_dice_action_3",p.name),
                        AllowAction.RECEIVE_HUNTERS_ACTION)
                }
                in 51..65->{
                    p.sendTranslateMsg("gamble_dice_action_4")
                    p.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 300, 0, false, false))
                    IdentityFifty.broadcastSpectators(translate("spec_gamble_dice_action_4",p.name),
                        AllowAction.RECEIVE_HUNTERS_ACTION)
                }
                in 66..84->{
                    p.sendTranslateMsg("gamble_dice_action_5")
                    p.world.getEntitiesByClass(Sheep::class.java).filter { it.persistentDataContainer.has(
                        NamespacedKey(IdentityFifty.plugin, "Generator"), PersistentDataType.INTEGER) }.forEach {
                        val maxHealth = it.getAttribute(Attribute.MAX_HEALTH)?.baseValue ?: return@forEach
                        if (it.health + 175 > maxHealth){
                            it.health = maxHealth
                        } else {
                            it.health += 175
                        }
                        it.customName(Component.text(
                            translate("sheep_generator",
                                it.health.toInt(),
                                maxHealth.toInt()
                            )
                        ))
                    }

                    p.world.getEntitiesByClass(Cow::class.java).filter { it.persistentDataContainer.has(
                        NamespacedKey(IdentityFifty.plugin, "EscapeGenerator"), PersistentDataType.INTEGER) }.forEach {
                        val maxHealth = it.getAttribute(Attribute.MAX_HEALTH)?.baseValue ?: return@forEach
                        if (it.health + 100 > maxHealth){
                            it.health = maxHealth
                        } else {
                            it.health += 100
                        }
                        it.customName(Component.text(
                            translate("cow_generator",
                                it.health.toInt(),
                                maxHealth.toInt()
                            )
                        ))
                    }

                    IdentityFifty.broadcastSpectators(translate("spec_gamble_dice_action_5",p.name),
                        AllowAction.RECEIVE_HUNTERS_ACTION)
                }
                in 85..100->{
                    p.sendTranslateMsg("gamble_dice_action_6")
                    p.world.getEntitiesByClass(Sheep::class.java).filter { it.persistentDataContainer.has(
                        NamespacedKey(IdentityFifty.plugin, "Generator"), PersistentDataType.INTEGER) }.forEach {
                        val maxHealth = it.getAttribute(Attribute.MAX_HEALTH)?.baseValue ?: return@forEach
                        if (it.health - 150 < 1){
                            it.health = 1.0
                        } else {
                            it.health -= 150
                        }
                        it.customName(Component.text(
                            translate("sheep_generator",
                                it.health.toInt(),
                                maxHealth.toInt()
                            )
                        ))
                    }

                    IdentityFifty.broadcastSpectators(translate("spec_gamble_dice_action_6",p.name),
                        AllowAction.RECEIVE_HUNTERS_ACTION)
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
            .addLore(translate("gamble_dice_lore_3"))
            .addLore(translate("gamble_dice_lore_4"))
            .addLore(translate("gamble_dice_lore_5"))
            .addLore(translate("gamble_dice_lore_6"))
            .addLore(translate("gamble_dice_lore_7"))
            .addLore(translate("gamble_dice_lore_8"))
        val firstSkill = IdentityFifty.createSInteractItem(firstSkillItem).setInteractEvent { e, _ ->
            if (isStunned(e.player)) return@setInteractEvent false
            diceTask(e.player)
            return@setInteractEvent true
        }.setInitialCoolDown(600)

        p.inventory.addItem(passiveItem, firstSkill)
    }

    override fun parameters(data: HunterData): HunterData {
        data.hunterClass = this
        return data
    }

    override fun onAttack(attackPlayer: Player, p: Player, noOne: Boolean): Int {
        if (Random.nextInt(1..100) <= 35){
            diceTask(p)
        }
        if (noOne){
            return 4
        } else {
            when(Random.nextInt(1..100)){
                in 1..20 -> {
                    return 1
                }
                in 21..60 -> {
                    return 2
                }
                in 61..85 -> {
                    return 3
                }
                in 86..100 -> {
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
            .build()
        val firstSkillItem = SItem(Material.STICK).setDisplayName(translate("gamble_dice")).setCustomModelData(11)
            .addLore(translate("gamble_dice_lore_1"))
            .addLore(translate("gamble_dice_lore_2"))
            .addLore(translate("gamble_dice_lore_3"))
            .addLore(translate("gamble_dice_lore_4"))
            .addLore(translate("gamble_dice_lore_5"))
            .addLore(translate("gamble_dice_lore_6"))
            .addLore(translate("gamble_dice_lore_7"))
            .addLore(translate("gamble_dice_lore_8"))
            .build()
        return arrayListOf(passiveItem,firstSkillItem)
    }

}
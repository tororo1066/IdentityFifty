package tororo1066.identityfifty.character.hunter

import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.potion.PotionEffectType
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.data.HunterData
import tororo1066.identityfifty.enumClass.AllowAction
import tororo1066.identityfifty.enumClass.StunState
import tororo1066.nmsutils.items.GlowColor
import tororo1066.tororopluginapi.lang.SLang.Companion.sendTranslateMsg
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sEvent.SEvent
import tororo1066.tororopluginapi.sItem.SItem
import tororo1066.tororopluginapi.utils.toPlayer

class Swapper: AbstractHunter("swapper") {

    private var white = false
    private val sEvent = SEvent()

    override fun onStart(p: Player) {
        fun generateCap(white: Boolean): ItemStack {
            val cap = SItem(Material.LEATHER_HELMET)
            cap.editMeta(LeatherArmorMeta::class.java) {
                it.setColor(if (white) Color.WHITE else Color.BLACK)
            }
            return cap
        }

        val passiveItem = SItem(Material.STICK).setDisplayName(translate("hunter_passive")).setCustomModelData(1)
            .addLore(translate("swapper_passive_lore_1"))
            .addLore(translate("swapper_passive_lore_2"))

        val changeColorSkill = SItem(Material.STICK).setDisplayName(translate("change_color")).setCustomModelData(28)
            .addLore(translate("change_color_lore_1"))
            .addLore(translate("change_color_lore_2"))
            .addLore(translate("change_color_lore_3"))
            .addLore(translate("change_color_lore_4"))
            .addLore(translate("change_color_lore_5"))
            .addLore(translate("change_color_lore_6"))

        val changeColorSkillItem = IdentityFifty.interactManager.createSInteractItem(changeColorSkill).setInteractEvent { e, _ ->
            val player = e.player
            white = !white

            if (white) {
                player.world.playSound(player.location, Sound.BLOCK_BELL_USE, 0.7f, 2f)
                player.location.getNearbyPlayers(30.0)
                    .sortedBy { player.location.distance(it.location) }
                    .firstOrNull { IdentityFifty.identityFiftyTask?.aliveSurvivors()?.contains(it.uniqueId) == true }?.let {
                        val data = IdentityFifty.survivors[it.uniqueId]!!
                        data.glowManager.glow(mutableSetOf(player), GlowColor.LIGHT_PURPLE, 200)
                        val hunterData = IdentityFifty.hunters[player.uniqueId]!!
                        hunterData.glowManager.glow(mutableSetOf(it), GlowColor.LIGHT_PURPLE, 200)
                        player.sendTranslateMsg("change_color_glowing")
                        it.sendTranslateMsg("change_color_glowing_survivor")
                    }
                player.walkSpeed -= 0.02f
                player.sendTranslateMsg("change_color_white")
                IdentityFifty.broadcastSpectators(translate("spec_change_color_white", player.name),
                    AllowAction.RECEIVE_HUNTERS_ACTION)
            } else {
                player.world.playSound(player.location, Sound.ENTITY_IRON_GOLEM_DEATH, 0.7f, 1.5f)
                val players = ArrayList<Player>()
                val data = IdentityFifty.hunters[player.uniqueId]!!
                IdentityFifty.survivors.values.forEach {
                    val survivor = it.uuid.toPlayer() ?: return@forEach
                    if (IdentityFifty.identityFiftyTask?.aliveSurvivors()?.contains(survivor.uniqueId) == false)return@forEach
                    if (inPrison(survivor))return@forEach
                    players.add(survivor)
                    it.glowManager.glow(mutableSetOf(player), GlowColor.RED, 100)
                    survivor.sendTranslateMsg("change_color_glowing_survivor")
                }
                data.glowManager.glow(players, GlowColor.RED, 100)
                IdentityFifty.stunEffect(player, 100, 100, StunState.OTHER)
                player.walkSpeed += 0.02f
                player.sendTranslateMsg("change_color_black")
                IdentityFifty.broadcastSpectators(translate("spec_change_color_black", player.name),
                    AllowAction.RECEIVE_HUNTERS_ACTION)
            }
            player.inventory.helmet = generateCap(white)
            return@setInteractEvent true
        }.setInitialCoolDown(400)

        sEvent.register(PlayerInteractEvent::class.java) { e ->
            if (e.player.uniqueId != p.uniqueId || e.hand != EquipmentSlot.HAND || !e.action.isLeftClick)return@register
            val player = e.player
            val entity = player.getTargetEntity(if (white) 5 else 3)
            if (entity == null) {
                if (!white) {
                    val defaultTarget = player.getTargetEntity(4)
                    if (defaultTarget is LivingEntity) {
                        val data = IdentityFifty.hunters[player.uniqueId]!!
                        if (data.disableSwingSlow){
                            return@register
                        }
                        e.isCancelled = true
                        player.playSound(player.location,Sound.ENTITY_PLAYER_ATTACK_SWEEP,1f,1f)
                        IdentityFifty.stunEffect(player, 0, 20, StunState.AIRSWING)
                        return@register
                    }
                }
                return@register
            }
            if (entity !is LivingEntity)return@register
            if (entity is Player) e.isCancelled = true
            if (player.getPotionEffect(PotionEffectType.SLOW_DIGGING)?.amplifier == 200) {
                return@register
            }
            entity.damage(1.0, player)
        }

        sEvent.register(InventoryClickEvent::class.java) { e ->
            if (e.slotType == InventoryType.SlotType.ARMOR) e.isCancelled = true
        }

        p.inventory.helmet = generateCap(white)
        p.inventory.addItem(passiveItem, changeColorSkillItem)
    }

    override fun parameters(data: HunterData): HunterData {
        data.hunterClass = this
        return data
    }

    override fun onStun(blindTime: Int, slowTime: Int, state: StunState, p: Player): Pair<Int, Int> {
        when (state) {
            StunState.AIRSWING -> {
                return if (white) {
                    blindTime to slowTime + 10
                } else {
                    blindTime to slowTime - 10
                }
            }
            StunState.DAMAGED -> {
                return if (white) {
                    blindTime + 20 to slowTime + 30
                } else {
                    blindTime - 30 to slowTime - 30
                }
            }
            StunState.WOODPLATE -> {
                return if (white) {
                    blindTime + 10 to slowTime + 10
                } else {
                    blindTime - 30 to slowTime - 30
                }
            }
            else -> {
            }
        }
        return super.onStun(blindTime, slowTime, state, p)
    }

    override fun info(): ArrayList<ItemStack> {
        val passiveItem = SItem(Material.STICK).setDisplayName(translate("hunter_passive")).setCustomModelData(1)
            .addLore(translate("swapper_passive_lore_1"))
            .addLore(translate("swapper_passive_lore_2"))

        val changeColorSkill = SItem(Material.STICK).setDisplayName(translate("change_color")).setCustomModelData(28)
            .addLore(translate("change_color_lore_1"))
            .addLore(translate("change_color_lore_2"))
            .addLore(translate("change_color_lore_3"))
            .addLore(translate("change_color_lore_4"))
            .addLore(translate("change_color_lore_5"))
            .addLore(translate("change_color_lore_6"))

        return arrayListOf(passiveItem,changeColorSkill)
    }

    override fun onEnd(p: Player) {
        sEvent.unregisterAll()
    }
}
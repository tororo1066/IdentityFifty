package tororo1066.identityfifty.character.hunter

import org.bukkit.ChatColor
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

        val changeColorSkillItem = IdentityFifty.interactManager.createSInteractItem(changeColorSkill).setInteractEvent { _, _ ->
            white = !white

            if (white) {
                p.world.playSound(p.location, Sound.BLOCK_BELL_USE, 0.7f, 2f)
                p.location.getNearbyPlayers(30.0)
                    .sortedBy { p.location.distance(it.location) }
                    .firstOrNull { IdentityFifty.survivors.containsKey(it.uniqueId) }?.let {
                        val data = IdentityFifty.survivors[it.uniqueId]!!
                        data.glowManager.glow(mutableSetOf(p), ChatColor.LIGHT_PURPLE, 200)
                        val hunterData = IdentityFifty.hunters[p.uniqueId]!!
                        hunterData.glowManager.glow(mutableSetOf(it), ChatColor.LIGHT_PURPLE, 200)
                        p.sendTranslateMsg("change_color_glowing")
                        it.sendTranslateMsg("change_color_glowing_survivor")
                    }
                p.walkSpeed -= 0.02f
                p.sendTranslateMsg("change_color_white")
                IdentityFifty.broadcastSpectators(translate("spec_change_color_white", p.name),
                    AllowAction.RECEIVE_HUNTERS_ACTION)
            } else {
                p.world.playSound(p.location, Sound.ENTITY_IRON_GOLEM_DEATH, 0.7f, 1.5f)
                val players = ArrayList<Player>()
                val data = IdentityFifty.hunters[p.uniqueId]!!
                IdentityFifty.survivors.values.forEach {
                    val player = it.uuid.toPlayer() ?: return@forEach
                    if (inPrison(player))return@forEach
                    players.add(player)
                    it.glowManager.glow(mutableSetOf(p), ChatColor.RED, 100)
                    player.sendTranslateMsg("change_color_glowing_survivor")
                }
                data.glowManager.glow(players, ChatColor.RED, 100)
                IdentityFifty.stunEffect(p, 100, 100, StunState.OTHER)
                p.walkSpeed += 0.02f
                p.sendTranslateMsg("change_color_black")
                IdentityFifty.broadcastSpectators(translate("spec_change_color_black", p.name),
                    AllowAction.RECEIVE_HUNTERS_ACTION)
            }
            p.inventory.helmet = generateCap(white)
            return@setInteractEvent true
        }.setInitialCoolDown(400)

        sEvent.register(PlayerInteractEvent::class.java) { e ->
            if (e.player.uniqueId != p.uniqueId || e.hand != EquipmentSlot.HAND || !e.action.isLeftClick)return@register
            val entity = e.player.getTargetEntity(if (white) 5 else 3)
            if (entity == null) {
                if (!white) {
                    val defaultTarget = e.player.getTargetEntity(4)
                    if (defaultTarget is LivingEntity) {
                        val data = IdentityFifty.hunters[p.uniqueId]!!
                        if (data.disableSwingSlow){
                            return@register
                        }
                        e.isCancelled = true
                        e.player.playSound(e.player.location,Sound.ENTITY_PLAYER_ATTACK_SWEEP,1f,1f)
                        IdentityFifty.stunEffect(e.player, 0, 20, StunState.AIRSWING)
                        return@register
                    }
                }
                return@register
            }
            if (entity !is LivingEntity)return@register
            if (entity is Player) e.isCancelled = true
            if (p.getPotionEffect(PotionEffectType.SLOW_DIGGING)?.amplifier == 200) {
                return@register
            }
            entity.damage(1.0, e.player)
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
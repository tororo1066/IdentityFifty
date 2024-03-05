package tororo1066.identityfifty.character.survivor

import me.libraryaddict.disguise.disguisetypes.PlayerDisguise
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Chicken
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.inventivetalent.glow.GlowAPI
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.data.SurvivorData
import tororo1066.identityfifty.enumClass.AllowAction
import tororo1066.identityfifty.enumClass.StunState
import tororo1066.tororopluginapi.lang.SLang.Companion.sendTranslateMsg
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sEvent.SEvent
import tororo1066.tororopluginapi.sItem.SItem
import tororo1066.tororopluginapi.utils.setPitchL
import tororo1066.tororopluginapi.utils.toPlayer

class Controller: AbstractSurvivor("controller") {

    private val sEvent = SEvent(IdentityFifty.plugin)
    private var latestEntity: PlayerDisguise? = null
    private var movingNow = false


    override fun onStart(p: Player) {
        super.onStart(p)
        val passiveItem = SItem(Material.STICK).setDisplayName(translate("passive")).setCustomModelData(8)
            .addLore(translate("controller_passive_lore_1"))
            .addLore(translate("controller_passive_lore_2"))
            .addLore(translate("controller_passive_lore_3"))

        val controllerSkill = SItem(Material.STICK).setDisplayName(translate("control_doll"))
            .addLore(translate("control_doll_lore_1"))
            .addLore(translate("control_doll_lore_2"))
            .addLore(translate("control_doll_lore_3"))
            .addLore(translate("control_doll_lore_4"))
            .setCustomModelData(26)

        val controllerSkillItem = IdentityFifty.interactManager.createSInteractItem(controllerSkill,true).setInteractEvent { _, item ->
            fun saveDisguise(loc: Location){
                p.world.spawn(loc, Chicken::class.java) { chicken ->
                    chicken.setAI(false)
                    chicken.isSilent = true
                    val saveDisguise = PlayerDisguise(p)
                        .setEntity(chicken)
                        .setNameVisible(false)
                    saveDisguise.isSelfDisguiseVisible = false
                    saveDisguise.startDisguise()
                    Bukkit.getScheduler().runTaskLater(IdentityFifty.plugin, Runnable {
                        saveDisguise.watcher.armor = arrayOf(
                            ItemStack(Material.AIR),
                            ItemStack(Material.AIR),
                            ItemStack(Material.AIR),
                            SItem(Material.LEATHER_HELMET).setEnchantment(Enchantment.BINDING_CURSE,1)
                        )
                    }, 1)
                    latestEntity = saveDisguise
                }
            }

            fun teleport(p: Player, loc: Location, movingNow: Boolean){
                p.isInvisible = true
                p.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 23, 100, false, false, false))
                Bukkit.getScheduler().runTaskLater(IdentityFifty.plugin, Runnable {
                    p.teleport(loc)
                    this.movingNow = movingNow
                    Bukkit.getScheduler().runTaskLater(IdentityFifty.plugin, Runnable {
                        p.isInvisible = false
                    }, 10)
                }, 3)
            }

            if (latestEntity == null && inPrison(p)){
                p.sendTranslateMsg("controller_doll_in_prison")
                return@setInteractEvent false
            }

            if (movingNow){
                IdentityFifty.broadcastSpectators(translate("spec_control_doll_end",p.name), AllowAction.RECEIVE_SURVIVORS_ACTION)
                val loc = p.location
                p.inventory.setItem(EquipmentSlot.HEAD, null)
                if (latestEntity != null){
                    latestEntity!!.stopDisguise()
                    latestEntity!!.entity.remove()
                    teleport(p, latestEntity!!.entity.location, false)
                } else {
                    movingNow = false
                }
                saveDisguise(loc)
                p.playSound(p.location, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f)
                return@setInteractEvent true
            }
            IdentityFifty.broadcastSpectators(translate("spec_control_doll_used",p.name), AllowAction.RECEIVE_SURVIVORS_ACTION)

            sEvent.unregisterAll()
            p.world.spawn(p.location, Chicken::class.java) {
                it.setAI(false)
                it.isSilent = true
                val disguise = PlayerDisguise(p)
                    .setEntity(it)
                    .setNameVisible(false)
                disguise.isSelfDisguiseVisible = false
                disguise.startDisguise()

                if (latestEntity != null){
                    val loc = latestEntity!!.entity.location
                    latestEntity!!.stopDisguise()
                    latestEntity!!.entity.remove()
                    teleport(p, loc, true)
                } else {
                    teleport(p, p.location.setPitchL(0f), true)
                }

                Bukkit.getScheduler().runTaskLater(IdentityFifty.plugin, Runnable {
                    p.inventory.setItem(EquipmentSlot.HEAD, SItem(Material.LEATHER_HELMET)
                        .setEnchantment(Enchantment.BINDING_CURSE,1))
                }, 23)

                latestEntity = disguise

                p.world.playSound(p.location, Sound.ENTITY_SKELETON_DEATH, 1f, 0.5f)

                sEvent.register(EntityDamageEvent::class.java) { e ->
                    if (e is EntityDamageByEntityEvent)return@register
                    if (e.entity == latestEntity?.entity){
                        e.isCancelled = true
                    }
                }


                sEvent.register(EntityDamageByEntityEvent::class.java, EventPriority.HIGHEST) { e ->
                    if (e.isCancelled)return@register
                    if (e.entity == latestEntity?.entity && movingNow){ //操作中で尚且つ、操作中の人が攻撃を受けた場合
                        if (!IdentityFifty.hunters.containsKey(e.damager.uniqueId)){
                            e.isCancelled = true
                            return@register
                        }
                        val loc = p.location
                        p.inventory.setItem(EquipmentSlot.HEAD, null)
                        latestEntity!!.stopDisguise()
                        latestEntity!!.entity.remove()
                        teleport(p, latestEntity!!.entity.location, false)
                        saveDisguise(loc)
                        Bukkit.getScheduler().runTaskLater(IdentityFifty.plugin, Runnable {
                            p.damage(1.0, e.damager)
                        }, 5)
                    } else if (e.entity == p && movingNow){ //操作中で尚且つ、人形が攻撃を受けた場合
                        if (!IdentityFifty.hunters.containsKey(e.damager.uniqueId)){
                            e.isCancelled = true
                            return@register
                        }
                        IdentityFifty.broadcastSpectators(translate("spec_control_doll_broken",p.name), AllowAction.RECEIVE_SURVIVORS_ACTION)
                        p.inventory.setItem(EquipmentSlot.HEAD, null)
                        disguise.stopDisguise()
                        it.remove()
                        teleport(p, it.location, false)
                        latestEntity = null
                        sEvent.unregisterAll()
                        IdentityFifty.survivors[p.uniqueId]!!.glowManager.glow(
                            IdentityFifty.hunters.mapNotNull { map -> map.key.toPlayer() }.toMutableList(),
                            GlowAPI.Color.DARK_PURPLE, 200
                        )
                        p.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 200, 2, false, false, true))
                        item.setInteractCoolDown(2400)
                    } else if (e.entity == latestEntity?.entity) { //人形が攻撃を受けた場合
                        if (!IdentityFifty.hunters.containsKey(e.damager.uniqueId)){
                            e.isCancelled = true
                            return@register
                        }
                        IdentityFifty.broadcastSpectators(translate("spec_control_doll_broken",p.name), AllowAction.RECEIVE_SURVIVORS_ACTION)
                        e.damage = 0.0
                        latestEntity!!.stopDisguise()
                        e.entity.remove()
                        latestEntity = null
                        IdentityFifty.survivors[p.uniqueId]!!.glowManager.glow(
                            IdentityFifty.hunters.mapNotNull { map -> map.key.toPlayer() }.toMutableList(),
                            GlowAPI.Color.DARK_PURPLE, 200
                        )
                        p.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 200, 2, false, false, true))
                        item.setInteractCoolDown(2400)
                        IdentityFifty.stunEffect(e.damager as Player, 20, 40, StunState.DAMAGED)
                    }

                }
            }

            return@setInteractEvent true
        }.setInitialCoolDown(100)

        p.inventory.addItem(passiveItem,controllerSkillItem)
    }

    override fun onDamage(damage: Int, toHealth: Int, damager: Player, p: Player): Pair<Boolean, Int> {
        if (movingNow){
            IdentityFifty.stunEffect(damager, 20, 40, StunState.DAMAGED)
            return Pair(false,0)
        }

        return super.onDamage(damage, toHealth, damager, p)
    }

    override fun parameters(data: SurvivorData): SurvivorData {
        data.canHelpSelf = true
        data.survivorClass = this
        return data
    }

    override fun onTryGotHeal(healer: Player, p: Player): Boolean {
        if (movingNow) return false
        return super.onTryGotHeal(healer, p)
    }

    override fun onTryGotHelp(helper: Player, p: Player): Boolean {
        if (helper == p && !movingNow){
            return false
        }

        return super.onTryGotHelp(helper, p)
    }

    override fun onGotHelp(helper: Player, p: Player): ReturnAction {
        val data = IdentityFifty.survivors[p.uniqueId]?:return ReturnAction.RETURN
        if (movingNow){
            return ReturnAction.CANCEL
        }

        if (helper.uniqueId == p.uniqueId){
            data.setHealth(2, true)
            return ReturnAction.CONTINUE
        }

        return super.onGotHelp(helper, p)
    }

    override fun onGoal(p: Player): ReturnAction {
        if (movingNow){
            return ReturnAction.CANCEL
        }
        return super.onGoal(p)
    }

    override fun onDie(p: Player) {
        super.onDie(p)
        if (latestEntity != null){
            latestEntity!!.stopDisguise()
            latestEntity!!.entity.remove()
        }
        sEvent.unregisterAll()
    }

    override fun onEnd(p: Player) {
        movingNow = false
        if (latestEntity != null){
            latestEntity!!.stopDisguise()
            latestEntity!!.entity.remove()
        }
        latestEntity = null
        sEvent.unregisterAll()
    }

    override fun onHitWoodPlate(
        hitPlayer: Player,
        loc: Location,
        blindTime: Int,
        slowTime: Int,
        p: Player
    ): Pair<Int, Int> {
        if (!movingNow){
            return Pair(blindTime-40,slowTime-40)
        }

        return super.onHitWoodPlate(hitPlayer, loc, blindTime, slowTime, p)
    }

    override fun cowGeneratorModify(damage: Double, maxHealth: Double, nowHealth: Double, p: Player): Double {
        if (movingNow){
            return damage * 0.8
        }
        return super.cowGeneratorModify(damage, maxHealth, nowHealth, p)
    }

    override fun sheepGeneratorModify(
        damage: Double,
        remainingGenerator: Int,
        maxHealth: Double,
        nowHealth: Double,
        p: Player
    ): Double {
        if (movingNow){
            return damage * 0.8
        }
        return super.sheepGeneratorModify(damage, remainingGenerator, maxHealth, nowHealth, p)
    }


    override fun info(): ArrayList<ItemStack> {
        val passiveItem = SItem(Material.STICK).setDisplayName(translate("passive")).setCustomModelData(8)
            .addLore(translate("controller_passive_lore_1"))
            .addLore(translate("controller_passive_lore_2"))
            .addLore(translate("controller_passive_lore_3"))

        val controllerSkill = SItem(Material.STICK).setDisplayName(translate("control_doll"))
            .addLore(translate("control_doll_lore_1"))
            .addLore(translate("control_doll_lore_2"))
            .addLore(translate("control_doll_lore_3"))
            .addLore(translate("control_doll_lore_4"))
            .setCustomModelData(26)

        return arrayListOf(passiveItem,controllerSkill)
    }
}
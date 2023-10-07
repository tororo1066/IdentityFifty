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
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.data.SurvivorData
import tororo1066.identityfifty.enumClass.AllowAction
import tororo1066.identityfifty.enumClass.StunState
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sEvent.SEvent
import tororo1066.tororopluginapi.sItem.SItem
import tororo1066.tororopluginapi.utils.setPitchL

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
            .addLore(translate("controller_passive_lore_4"))

        val controllerSkill = SItem(Material.STICK).setDisplayName(translate("control_doll"))
            .addLore(translate("control_doll_lore_1"))
            .addLore(translate("control_doll_lore_2"))
            .addLore(translate("control_doll_lore_3"))
            .setCustomModelData(27)

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

            if (movingNow){
                IdentityFifty.broadcastSpectators(translate("spec_controller_end",p.name), AllowAction.RECEIVE_SURVIVORS_ACTION)
                val loc = p.location
                p.inventory.setItem(EquipmentSlot.HEAD, null)
                if (latestEntity != null){
                    latestEntity!!.stopDisguise()
                    latestEntity!!.entity.remove()
                    p.teleport(latestEntity!!.entity.location)
                }
                saveDisguise(loc)
                movingNow = false
                p.playSound(p.location, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f)
                return@setInteractEvent true
            }
            IdentityFifty.broadcastSpectators(translate("spec_controller_used",p.name), AllowAction.RECEIVE_SURVIVORS_ACTION)

            sEvent.unregisterAll()
            movingNow = true
            p.world.spawn(p.location, Chicken::class.java) {
                it.setAI(false)
                it.isSilent = true
                val disguise = PlayerDisguise(p)
                    .setEntity(it)
                    .setNameVisible(false)
                disguise.isSelfDisguiseVisible = false
                disguise.startDisguise()

                p.inventory.setItem(EquipmentSlot.HEAD, SItem(Material.LEATHER_HELMET)
                    .setEnchantment(Enchantment.BINDING_CURSE,1))

                if (latestEntity != null){
                    val loc = latestEntity!!.entity.location
                    latestEntity!!.stopDisguise()
                    latestEntity!!.entity.remove()
                    p.teleport(loc)
                } else {
                    p.teleport(p.location.setPitchL(0f))
                }

                latestEntity = disguise

                p.playSound(p.location, Sound.ENTITY_SKELETON_DEATH, 1f, 0.5f)

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
                        p.teleport(latestEntity!!.entity.location)
                        saveDisguise(loc)
                        movingNow = false
                        p.damage(1.0, e.damager)
                    } else if (e.entity == p && movingNow){ //操作中で尚且つ、人形が攻撃を受けた場合
                        if (!IdentityFifty.hunters.containsKey(e.damager.uniqueId)){
                            e.isCancelled = true
                            return@register
                        }
                        IdentityFifty.broadcastSpectators(translate("spec_controller_broken",p.name), AllowAction.RECEIVE_SURVIVORS_ACTION)
                        p.inventory.setItem(EquipmentSlot.HEAD, null)
                        disguise.stopDisguise()
                        it.remove()
                        p.teleport(it.location)
                        latestEntity = null

                        sEvent.unregisterAll()
                        movingNow = false
                        item.setInteractCoolDown(2400)
                    } else if (e.entity == latestEntity?.entity) { //人形が攻撃を受けた場合
                        if (!IdentityFifty.hunters.containsKey(e.damager.uniqueId)){
                            e.isCancelled = true
                            return@register
                        }
                        IdentityFifty.broadcastSpectators(translate("spec_controller_broken",p.name), AllowAction.RECEIVE_SURVIVORS_ACTION)
                        e.damage = 0.0
                        latestEntity!!.stopDisguise()
                        e.entity.remove()
                        latestEntity = null
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
            return ReturnAction.RETURN
        }

        if (helper == p){
            data.setHealth(2)
            return ReturnAction.CONTINUE
        }

        return super.onGotHelp(helper, p)
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
            .addLore(translate("controller_passive_lore_4"))

        val controllerSkill = SItem(Material.STICK).setDisplayName(translate("control_doll"))
            .addLore(translate("control_doll_lore_1"))
            .addLore(translate("control_doll_lore_2"))
            .addLore(translate("control_doll_lore_3"))
            .setCustomModelData(27)

        return arrayListOf(passiveItem,controllerSkill)
    }
}
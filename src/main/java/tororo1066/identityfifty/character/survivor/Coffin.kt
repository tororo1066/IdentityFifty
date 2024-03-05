package tororo1066.identityfifty.character.survivor

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask
import org.inventivetalent.glow.GlowAPI
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.data.PrisonData
import tororo1066.identityfifty.data.SurvivorData
import tororo1066.identityfifty.enumClass.AllowAction
import tororo1066.tororopluginapi.lang.SLang.Companion.sendTranslateMsg
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sEvent.SEvent
import tororo1066.tororopluginapi.sItem.SItem
import tororo1066.tororopluginapi.utils.toPlayer
import java.util.UUID

class Coffin: AbstractSurvivor("coffin") {

    private var coffin: Location? = null
    private var coffinUUID: UUID? = null
    private var coffinTask: BukkitTask? = null
    private val sEvent = SEvent(IdentityFifty.plugin)

    override fun onStart(p: Player) {
        super.onStart(p)
        val passiveItem = SItem(Material.STICK).setDisplayName(translate("passive")).setCustomModelData(8)
            .addLore(translate("coffin_passive_lore_1"))
            .addLore(translate("coffin_passive_lore_2"))

        val coffinSkill = SItem(Material.STICK).setDisplayName(translate("coffin_skill")).setCustomModelData(17)
            .addLore(translate("coffin_skill_lore_1"))
            .addLore(translate("coffin_skill_lore_2"))
            .addLore(translate("coffin_skill_lore_3"))
            .addLore(translate("coffin_skill_lore_4"))
            .addLore(translate("coffin_skill_lore_5"))

        val coffinSkillItem = IdentityFifty.interactManager.createSInteractItem(coffinSkill,true).setInteractEvent { _, item ->
            if (inPrison(p)){
                return@setInteractEvent false
            }

            if (coffin != null){
                coffinUUID?.let { Bukkit.getEntity(it)?.remove() }
                coffin = null
                coffinUUID = null
                coffinTask?.cancel()
                coffinTask = null
                sEvent.unregisterAll()
            }

            IdentityFifty.broadcastSpectators(translate("spec_coffin_placed",p.name),
                AllowAction.RECEIVE_SURVIVORS_ACTION)

            p.world.spawn(p.location,ArmorStand::class.java) {
                p.playSound(p.location, Sound.ENTITY_EVOKER_FANGS_ATTACK, 1f, 2f)
                coffin = it.location
                coffinUUID = it.uniqueId
                it.setDisabledSlots(EquipmentSlot.CHEST,EquipmentSlot.FEET,EquipmentSlot.HEAD,EquipmentSlot.LEGS)
                it.isInvisible = true
                it.isInvulnerable = true
                GlowAPI.setGlowing(it, GlowAPI.Color.DARK_RED, Bukkit.getOnlinePlayers())
                Bukkit.getScheduler().runTaskLater(IdentityFifty.plugin, Runnable {
                    it.isInvulnerable = false
                }, 60)
                it.setItem(EquipmentSlot.HEAD, SItem(Material.STICK).setCustomModelData(16))
                coffinTask = Bukkit.getScheduler().runTaskTimer(IdentityFifty.plugin, Runnable {
                    GlowAPI.setGlowing(it,false,Bukkit.getOnlinePlayers())
                    GlowAPI.setGlowing(it, GlowAPI.Color.WHITE, Bukkit.getOnlinePlayers())
                }, 60, 5)

                sEvent.register(EntityDamageByEntityEvent::class.java){ e ->
                    if (e.entity.uniqueId == it.uniqueId){
                        if (!IdentityFifty.hunters.containsKey(e.damager.uniqueId)){
                            e.isCancelled = true
                            return@register
                        }
                        e.entity.remove()
                        coffin = null
                        coffinUUID = null
                        coffinTask?.cancel()
                        coffinTask = null
                        sEvent.unregisterAll()
                        item.setInteractCoolDown(1600)
                        p.sendTranslateMsg("coffin_broken")
                        IdentityFifty.broadcastSpectators(translate("spec_coffin_broken",p.name),
                            AllowAction.RECEIVE_SURVIVORS_ACTION)
                    }
                }

                sEvent.register(EntityDeathEvent::class.java) { e ->
                    if (e.entity.uniqueId == it.uniqueId){
                        e.drops.clear()
                    }
                }

            }
            return@setInteractEvent true
        }.setDropEvent { e, item ->
            e.isCancelled = true
            if (coffin == null){
                return@setDropEvent
            }
            if (!inPrison(p)){
                return@setDropEvent
            }

            val prisonData = IdentityFifty.identityFiftyTask!!.map.prisons.entries.find { it.value.inPlayer.contains(p.uniqueId) }!!
            val playerData = IdentityFifty.survivors[p.uniqueId]!!
            prisonData.value.inPlayer.remove(p.uniqueId)
            val onGotHelp = playerData.survivorClass.onGotHelp(p,p)
            if (onGotHelp == ReturnAction.CANCEL){
                p.teleport(prisonData.value.spawnLoc)
                return@setDropEvent
            }
            if (onGotHelp == ReturnAction.RETURN){
                return@setDropEvent
            }
            playerData.setHealth(3, true)
            playerData.survivorClass.onHelp(p,p)
            playerData.talentClasses.values.forEach {
                it.onHelp(p,p)
                it.onGotHelp(p,p)
            }
            IdentityFifty.hunters.values.forEach {
                val hunterP = it.uuid.toPlayer()?:return@forEach
                it.hunterClass.onSurvivorHelp(p,p,hunterP)
                it.talentClasses.values.forEach { clazz ->
                    clazz.onSurvivorHelp(p,p,hunterP)
                }
            }
            p.teleport(coffin!!)
            p.world.playSound(p.location, Sound.BLOCK_ENDER_CHEST_OPEN, 2f, 0.5f)

            coffinUUID?.let { Bukkit.getEntity(it)?.remove() }
            coffin = null
            coffinUUID = null
            coffinTask?.cancel()
            coffinTask = null
            sEvent.unregisterAll()

            IdentityFifty.hunters.forEach {
                it.key.toPlayer()?.sendTranslateMsg("coffin_helped")
            }

            IdentityFifty.broadcastSpectators(translate("spec_coffin_helped",p.name),
                AllowAction.RECEIVE_SURVIVORS_ACTION)

            item.setInteractCoolDown(1600)

        }.setInitialCoolDown(1600)

        p.inventory.addItem(passiveItem,coffinSkillItem)
    }

    override fun parameters(data: SurvivorData): SurvivorData {
        data.survivorClass = this
        data.healTick = 180
        return data
    }

    override fun onJail(prisonData: PrisonData, p: Player) {
        val data = IdentityFifty.survivors[p.uniqueId]!!
        data.remainingTime -= 20
    }

    override fun onEnd(p: Player) {
        coffinUUID?.let { Bukkit.getEntity(it)?.remove() }
        coffin = null
        coffinUUID = null
        coffinTask?.cancel()
        coffinTask = null
        sEvent.unregisterAll()
    }

    override fun info(): ArrayList<ItemStack> {
        val passiveItem = SItem(Material.STICK).setDisplayName(translate("passive")).setCustomModelData(8)
            .addLore(translate("coffin_passive_lore_1"))
            .addLore(translate("coffin_passive_lore_2"))

        val coffinSkill = SItem(Material.STICK).setDisplayName(translate("coffin_skill")).setCustomModelData(17)
            .addLore(translate("coffin_skill_lore_1"))
            .addLore(translate("coffin_skill_lore_2"))
            .addLore(translate("coffin_skill_lore_3"))
            .addLore(translate("coffin_skill_lore_4"))
            .addLore(translate("coffin_skill_lore_5"))
        return arrayListOf(passiveItem, coffinSkill)
    }
}
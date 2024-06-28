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
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.data.GlowManager
import tororo1066.identityfifty.data.PrisonData
import tororo1066.identityfifty.data.SurvivorData
import tororo1066.identityfifty.enumClass.AllowAction
import tororo1066.nmsutils.items.GlowColor
import tororo1066.tororopluginapi.lang.SLang.Companion.sendTranslateMsg
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sEvent.SEvent
import tororo1066.tororopluginapi.sItem.SItem
import tororo1066.tororopluginapi.utils.toPlayer
import java.util.UUID

class Coffin: AbstractSurvivor("coffin") {

    private var coffin: Location? = null
    private var coffinUUID: UUID? = null
    private var coffinGlowManager: GlowManager? = null
    private var coffinGlowTask: Int = -1
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

        val coffinSkillItem = IdentityFifty.interactManager.createSInteractItem(coffinSkill,true).setInteractEvent { e, item ->
            val player = e.player
            if (inPrison(player)){
                return@setInteractEvent false
            }

            if (coffin != null){
                coffinUUID?.let { Bukkit.getEntity(it)?.remove() }
                coffin = null
                coffinUUID = null
                sEvent.unregisterAll()
            }

            IdentityFifty.broadcastSpectators(translate("spec_coffin_placed",player.name),
                AllowAction.RECEIVE_SURVIVORS_ACTION)

            player.world.spawn(player.location,ArmorStand::class.java) {
                player.world.playSound(player.location, Sound.ENTITY_EVOKER_FANGS_ATTACK, 1f, 2f)
                coffin = it.location
                coffinUUID = it.uniqueId
                it.setDisabledSlots(EquipmentSlot.CHEST,EquipmentSlot.FEET,EquipmentSlot.HEAD,EquipmentSlot.LEGS)
                it.isInvisible = true
                coffinGlowManager = GlowManager(it.uniqueId)
                coffinGlowManager!!.glow(Bukkit.getOnlinePlayers().toMutableList(),GlowColor.DARK_RED,60)
                Bukkit.getScheduler().runTaskLater(IdentityFifty.plugin, Runnable {
                    coffinGlowTask = coffinGlowManager!!.glow(Bukkit.getOnlinePlayers().toMutableList(),GlowColor.WHITE,Int.MAX_VALUE).first()
                }, 60)
                it.setItem(EquipmentSlot.HEAD, SItem(Material.STICK).setCustomModelData(16))

                sEvent.register(EntityDamageByEntityEvent::class.java){ e ->
                    if (e.entity.uniqueId == it.uniqueId){
                        if (!IdentityFifty.hunters.containsKey(e.damager.uniqueId)){
                            e.isCancelled = true
                            return@register
                        }

                        e.entity.remove()
                        coffin = null
                        coffinUUID = null
                        coffinGlowManager?.cancelTask(coffinGlowTask)
                        coffinGlowManager = null
                        sEvent.unregisterAll()
                        item.setInteractCoolDown(2000)
                        player.sendTranslateMsg("coffin_broken")
                        IdentityFifty.broadcastSpectators(translate("spec_coffin_broken",player.name),
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
            val player = e.player
            e.isCancelled = true
            if (coffin == null){
                return@setDropEvent
            }
            if (!inPrison(player)){
                return@setDropEvent
            }

            val prisonData = IdentityFifty.identityFiftyTask!!.map.prisons.entries.find { it.value.inPlayer.contains(player.uniqueId) }!!
            val playerData = IdentityFifty.survivors[player.uniqueId]!!
            prisonData.value.inPlayer.remove(player.uniqueId)
            val onGotHelp = playerData.survivorClass.onGotHelp(player,player)
            if (onGotHelp == ReturnAction.CANCEL){
                player.teleport(prisonData.value.spawnLoc)
                return@setDropEvent
            }
            if (onGotHelp == ReturnAction.RETURN){
                return@setDropEvent
            }
            playerData.setHealth(3, true)
            playerData.survivorClass.onHelp(player,player)
            playerData.talentClasses.values.forEach {
                it.onHelp(player,player)
                it.onGotHelp(player,player)
            }
            IdentityFifty.hunters.values.forEach {
                val hunterP = it.uuid.toPlayer()?:return@forEach
                it.hunterClass.onSurvivorHelp(player,player,hunterP)
                it.talentClasses.values.forEach { clazz ->
                    clazz.onSurvivorHelp(player,player,hunterP)
                }
            }
            player.teleport(coffin!!)
            player.world.playSound(player.location, Sound.BLOCK_ENDER_CHEST_OPEN, 2f, 0.5f)

            coffinUUID?.let { Bukkit.getEntity(it)?.remove() }
            coffin = null
            coffinUUID = null
            coffinGlowManager?.cancelTask(coffinGlowTask)
            coffinGlowManager = null
            sEvent.unregisterAll()

            IdentityFifty.hunters.forEach {
                it.key.toPlayer()?.sendTranslateMsg("coffin_helped")
            }

            IdentityFifty.broadcastSpectators(translate("spec_coffin_helped",player.name),
                AllowAction.RECEIVE_SURVIVORS_ACTION)

            item.setInteractCoolDown(2000)

        }.setInitialCoolDown(2000)

        p.inventory.addItem(passiveItem,coffinSkillItem)
    }

    override fun parameters(data: SurvivorData): SurvivorData {
        data.survivorClass = this
        data.healTick = 180
        return data
    }

    override fun onJail(prisonData: PrisonData, p: Player) {
        val data = IdentityFifty.survivors[p.uniqueId]!!
        data.remainingTime -= 15
    }

    override fun onEnd(p: Player) {
        coffinUUID?.let { Bukkit.getEntity(it)?.remove() }
        coffin = null
        coffinUUID = null
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

    override fun description(): String {
        return translate("coffin_description")
    }
}
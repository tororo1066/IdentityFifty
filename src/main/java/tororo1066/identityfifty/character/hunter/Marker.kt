package tororo1066.identityfifty.character.hunter

import org.bukkit.*
import org.bukkit.Particle.DustTransition
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.Arrow
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.data.HunterData
import tororo1066.identityfifty.data.PrisonData
import tororo1066.identityfifty.enumClass.AllowAction
import tororo1066.nmsutils.items.GlowColor
import tororo1066.tororopluginapi.lang.SLang.Companion.sendTranslateMsg
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sEvent.SEvent
import tororo1066.tororopluginapi.sItem.SItem
import tororo1066.tororopluginapi.utils.addItem
import tororo1066.tororopluginapi.utils.toPlayer
import java.util.UUID
import kotlin.math.min

class Marker: AbstractHunter("marker") {

    private var arrowTask: BukkitTask? = null
    private val marks = HashMap<UUID,Pair<Int,BukkitTask>>()
    private val sEvent = SEvent(IdentityFifty.plugin)
    private var bossbar: BossBar? = null

    override fun onStart(p: Player) {

        val passiveItem = SItem(Material.STICK).setDisplayName(translate("hunter_passive")).setCustomModelData(1)
            .addLore(translate("marker_passive_lore_1"))
            .addLore(translate("marker_passive_lore_2"))
            .addLore(translate("marker_passive_lore_3"))
            .addLore(translate("marker_passive_lore_4"))

        val crossBowSkill = SItem(Material.STICK).setDisplayName(translate("mark_crossbow")).setCustomModelData(15)
            .addLore(translate("mark_crossbow_lore_1"))
            .addLore(translate("mark_crossbow_lore_2"))



        val crossBowSkillItem = IdentityFifty.interactManager.createSInteractItem(crossBowSkill,true).setInteractEvent { e, _ ->
            val player = e.player
            if (isStunned(player)) return@setInteractEvent false
            val arrow = player.launchProjectile(Arrow::class.java)
            player.world.playSound(player.location, Sound.ENTITY_ARROW_SHOOT,1f,1f)
            arrow.persistentDataContainer.set(NamespacedKey(IdentityFifty.plugin,"marker"),
                PersistentDataType.INTEGER,1)

            IdentityFifty.broadcastSpectators(translate("spec_mark_crossbow_used",player.name),
                AllowAction.RECEIVE_HUNTERS_ACTION)

            //着弾するまでパーティクルを出す
            arrowTask = object : BukkitRunnable() {
                override fun run() {
                    if (arrow.isDead || arrow.isOnGround){
                        IdentityFifty.util.runTask { arrow.remove() }
                        cancel()
                        return
                    }
                    arrow.world.spawnParticle(Particle.DUST_COLOR_TRANSITION,arrow.location,1,DustTransition(Color.RED,Color.RED,1f))
                }
            }.runTaskTimer(IdentityFifty.plugin,0,1)
            return@setInteractEvent true
        }.setInitialCoolDown(400)

        p.inventory.addItem(passiveItem, crossBowSkillItem)

        sEvent.register(EntityDamageByEntityEvent::class.java){ e ->
            if (!IdentityFifty.survivors.containsKey(e.entity.uniqueId))return@register
            if (e.damager !is Arrow)return@register
            if (!e.damager.persistentDataContainer.has(NamespacedKey(IdentityFifty.plugin,"marker"),PersistentDataType.INTEGER))return@register
            if (e.entity !is Player)return@register
            val player = (e.damager as Arrow).shooter as Player
            val data = IdentityFifty.survivors[e.entity.uniqueId]?:return@register
            update(e.entity.uniqueId, (marks[e.entity.uniqueId]?.first?:0) + 5)
            data.glowManager.glow(mutableListOf(player),GlowColor.RED,150)
            e.damage = 0.0
            IdentityFifty.broadcastSpectators(translate("spec_mark_crossbow_hit",player.name,e.entity.name),
                AllowAction.RECEIVE_HUNTERS_ACTION)
        }

        tasks.add(Bukkit.getScheduler().runTaskTimer(IdentityFifty.plugin, Runnable {
            //視線にいるサバイバーの印の数を表示させる
            val player = p.player?:return@Runnable
            val target = player.getTargetEntity(100)
            if (target !is Player || !marks.containsKey(target.uniqueId)){
                bossbar?.removeAll()
                bossbar = null
                return@Runnable
            }
            val mark = marks[target.uniqueId]!!.first
            bossbar?.removeAll()
            bossbar = Bukkit.createBossBar(translate("marker_bossbar",mark.toString()), BarColor.GREEN, BarStyle.SOLID)
            bossbar!!.progress = (min(mark, 10) / 10.0)
            bossbar!!.addPlayer(player)
        },0,5))
    }

    override fun parameters(data: HunterData): HunterData {
        data.hunterClass = this
        return data
    }

    override fun onEnd(p: Player) {
        arrowTask?.cancel()
        arrowTask = null
        bossbar?.removeAll()
        marks.forEach {
            it.value.second.cancel()
        }
        marks.clear()
        sEvent.unregisterAll()
    }

    override fun onAttack(attackPlayer: Player, p: Player, noOne: Boolean): Int {
        if (noOne) return 4
        val mark = marks[attackPlayer.uniqueId]?:return 2
        return 2 + mark.first / 5
    }

    override fun onFinishedAttack(attackPlayer: Player, result: Int, p: Player) {
        if (result == 0)return

        remove(attackPlayer.uniqueId)
        update(attackPlayer.uniqueId,5)
    }

    override fun onSurvivorHeal(healPlayer: Player, healedPlayer: Player, p: Player) {
        val data = IdentityFifty.survivors[healedPlayer.uniqueId]?:return
        data.glowManager.glow(mutableSetOf(p),GlowColor.RED,200)
        p.sendTranslateMsg("marker_healed_survivor")
        p.playSound(p.location,Sound.BLOCK_ENCHANTMENT_TABLE_USE,1f,1f)
        healedPlayer.sendTranslateMsg("marker_heal_view")
        healPlayer.sendTranslateMsg("marker_heal_view")
        healedPlayer.world.playSound(healedPlayer.location,Sound.BLOCK_ENCHANTMENT_TABLE_USE,1f,1f)
    }


    override fun onSurvivorJail(survivor: Player, prison: PrisonData, p: Player) {
        val data = IdentityFifty.survivors[survivor.uniqueId]?:return
        data.glowManager.cancelTask(p.uniqueId)
        Bukkit.getScheduler().runTaskLater(IdentityFifty.plugin, Runnable {
            remove(survivor.uniqueId)
            p.sendTranslateMsg("mark_remove")
            IdentityFifty.broadcastSpectators(
                translate("spec_mark_remove",p.name),
                AllowAction.RECEIVE_HUNTERS_ACTION
            )
        }, 10)
    }

    override fun onSurvivorDie(survivor: Player, playerNumber: Int, p: Player) {
        remove(survivor.uniqueId)
    }

    private fun update(uuid: UUID, i: Int){
        uuid.toPlayer()?.sendTranslateMsg("mark_update", (marks[uuid]?.first?:0).toString(), i.toString())
        IdentityFifty.broadcastSpectators(
            translate(
                "spec_mark_update",
                uuid.toPlayer()?.name?:uuid.toString(), (marks[uuid]?.first?:0).toString(), i.toString()
            ),
            AllowAction.RECEIVE_HUNTERS_ACTION
        )
        marks[uuid]?.second?.cancel()
        marks[uuid] = Pair(i, task(uuid))
    }

    private fun remove(uuid: UUID){
        marks[uuid]?.second?.cancel()
        marks.remove(uuid)
    }

    private fun task(uuid: UUID): BukkitTask {
        return Bukkit.getScheduler().runTaskLater(IdentityFifty.plugin, Runnable {
            marks.remove(uuid)
            uuid.toPlayer()?.sendTranslateMsg("mark_remove")
            IdentityFifty.broadcastSpectators(
                translate("spec_mark_remove",uuid.toPlayer()?.name?:"Unknown"),
                AllowAction.RECEIVE_HUNTERS_ACTION
            )
        }, 1400)
    }

    override fun info(): ArrayList<ItemStack> {
        val passiveItem = SItem(Material.STICK).setDisplayName(translate("hunter_passive")).setCustomModelData(1)
            .addLore(translate("marker_passive_lore_1"))
            .addLore(translate("marker_passive_lore_2"))
            .addLore(translate("marker_passive_lore_3"))
            .addLore(translate("marker_passive_lore_4"))
            .build()

        val crossBowSkill = SItem(Material.STICK).setDisplayName(translate("mark_crossbow")).setCustomModelData(15)
            .addLore(translate("mark_crossbow_lore_1"))
            .addLore(translate("mark_crossbow_lore_2"))
            .build()

        return arrayListOf(passiveItem,crossBowSkill)
    }

}
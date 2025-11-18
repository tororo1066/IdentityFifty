package tororo1066.identityfifty.character.hunter

import org.bukkit.*
import org.bukkit.entity.Interaction
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.data.HunterData
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
import kotlin.random.Random

class TraceCollector: AbstractHunter("trace_collector") {

    private val chaseSkillCooldown = 1000

    private val passiveItem = SItem(Material.STICK).setDisplayName(translate("hunter_passive")).setCustomModelData(1)
        .addLore(translate("trace_collector_passive_lore_1"))
        .addLore(translate("trace_collector_passive_lore_2"))
        .addLore(translate("trace_collector_passive_lore_3"))
        .addLore(translate("trace_collector_passive_lore_4"))
        .addLore(translate("trace_collector_passive_lore_5"))
        .addLore(translate("trace_collector_passive_lore_6"))
        .addLore(translate("trace_collector_passive_lore_7"))

    private val chaseSkillItem = SItem(Material.STICK).setDisplayName(translate("chase")).setCustomModelData(30)
        .addLore(translate("chase_lore_1"))
        .addLore(translate("chase_lore_2"))
        .addLore(translate("chase_lore_3"))
        .addLore(translate("chase_lore_4"))
        .addLore(translate("chase_lore_5"))
        .addLore(translate("chase_lore_6", chaseSkillCooldown / 20))

    private val collectedTraces = HashMap<UUID, Int>()
    private val traces = HashMap<Location, Trace>()
    private val sEvent = SEvent(IdentityFifty.plugin)

    private data class Trace(
        val uuid: UUID,
        val entityUUID: UUID,
        var duration: Int
    )

    override fun onStart(p: Player) {
        super.onStart(p)

        IdentityFifty.survivors.forEach { (uuid, _) ->
            collectedTraces[uuid] = 0
        }

        val chaseSkill = IdentityFifty.createSInteractItem(chaseSkillItem).setInteractEvent { e, _ ->
            val player = e.player
            if (isStunned(player)) return@setInteractEvent false
            val task = IdentityFifty.identityFiftyTask ?: return@setInteractEvent false
            collectedTraces.entries.removeIf { !task.aliveSurvivors().contains(it.key) }
            val maxTrace = collectedTraces.values.maxOrNull() ?: return@setInteractEvent false
            if (maxTrace <= 0) {
                player.sendTranslateMsg("chase_no_trace")
                return@setInteractEvent false
            }
            val targets = collectedTraces.filter { it.value == maxTrace }.keys
            if (targets.isEmpty()) return@setInteractEvent false
            targets.forEach { uuid ->
                collectedTraces[uuid] = 0
            }

            player.world.playSound(player.location, Sound.BLOCK_BEACON_POWER_SELECT, 1f, 2f)

            when (maxTrace) {
                1 -> {
                    val duration = min(targets.size * 60, 300)
                    val durationSeconds = duration / 20
                    p.addPotionEffect(
                        PotionEffect(PotionEffectType.SPEED, duration, 1)
                    )
                    p.sendTranslateMsg("chase_1", durationSeconds)
                    IdentityFifty.broadcastSpectators(
                        translate("spec_chase_1", p.name, durationSeconds),
                        AllowAction.RECEIVE_HUNTERS_ACTION
                    )
                }
                2 -> {
                    val random = targets.randomOrNull() ?: return@setInteractEvent false
                    val survivor = random.toPlayer() ?: return@setInteractEvent false
                    val survivorData = IdentityFifty.survivors[random] ?: return@setInteractEvent false
                    val duration = min(targets.size * 60, 300)
                    val durationSeconds = duration / 20
                    survivorData.glowManager.glow(
                        IdentityFifty.hunters.mapNotNull { it.key.toPlayer() }.toMutableList(),
                        GlowColor.RED,
                        duration
                    )
                    survivor.sendTranslateMsg("trace_collected_glow_self")
                    p.sendTranslateMsg("chase_2", durationSeconds)
                    IdentityFifty.broadcastSpectators(
                        translate("spec_chase_2", p.name, survivor.name, durationSeconds),
                        AllowAction.RECEIVE_HUNTERS_ACTION
                    )
                }
                3 -> {
                    val duration = min(targets.size * 100, 400)
                    val durationSeconds = duration / 20
                    targets.forEach { uuid ->
                        val survivor = uuid.toPlayer() ?: return@forEach
                        val survivorData = IdentityFifty.survivors[uuid] ?: return@forEach
                        survivorData.glowManager.glow(
                            IdentityFifty.hunters.mapNotNull { it.key.toPlayer() }.toMutableList(),
                            GlowColor.RED,
                            duration
                        )
                        survivor.sendTranslateMsg("trace_collected_glow_self")
                    }
                    p.sendTranslateMsg("chase_3", durationSeconds)
                    IdentityFifty.broadcastSpectators(
                        translate("spec_chase_3", p.name, targets.joinToString(", ") { it.toPlayer()?.name ?: "Unknown" }, durationSeconds),
                        AllowAction.RECEIVE_HUNTERS_ACTION
                    )
                }
            }

            return@setInteractEvent true
        }.setInitialCoolDown(chaseSkillCooldown)

        tasks.add(Bukkit.getScheduler().runTaskTimer(IdentityFifty.plugin, Runnable {
            val task = IdentityFifty.identityFiftyTask ?: return@Runnable
            val player = p.player ?: return@Runnable

            task.aliveSurvivors().forEach { uuid ->
                val survivor = Bukkit.getPlayer(uuid) ?: return@forEach
                if (inPrison(survivor)) return@forEach

                val entity = survivor.world.spawn(survivor.location, Interaction::class.java) {
                    it.interactionWidth = 1.5f
                    it.interactionHeight = 1f
                    it.isVisibleByDefault = false
                    player.showEntity(IdentityFifty.plugin, it)
                }
                traces[entity.location]?.let { trace ->
                    Bukkit.getEntity(trace.entityUUID)?.remove()
                }
                traces[entity.location] = Trace(
                    uuid = uuid,
                    entityUUID = entity.uniqueId,
                    duration = 60
                )
            }
        }, Random.nextLong(200, 600),600))

        tasks.add(Bukkit.getScheduler().runTaskTimer(IdentityFifty.plugin, Runnable {
            val player = p.player ?: return@Runnable
            val task = IdentityFifty.identityFiftyTask ?: return@Runnable
            val willRemove = ArrayList<Location>()
            traces.forEach { (location, trace) ->
                val survivorData = IdentityFifty.survivors[trace.uuid] ?: return@forEach
                if (!task.aliveSurvivors().contains(trace.uuid)) {
                    willRemove.add(location)
                    Bukkit.getEntity(trace.entityUUID)?.remove()
                    return@forEach
                }

                if (location.world != player.world) return@forEach
                if (location.distanceSquared(player.location) < 15 * 15) {

                    val health = survivorData.getHealth()

                    val color = when(health) {
                        5 -> Color.LIME
                        4 -> Color.YELLOW
                        3 -> Color.RED
                        2 -> Color.MAROON
                        else -> Color.BLACK
                    }
                    repeat(30) {
                        Particle.ENTITY_EFFECT.builder()
                            .location(
                                location.clone().add(
                                    Random.nextDouble(-0.5, 0.5),
                                    Random.nextDouble(-0.5, 0.5),
                                    Random.nextDouble(-0.5, 0.5)
                                )
                            )
                            .count(0)
                            .extra(1.0)
                            .color(color)
                            .receivers(player)
                            .spawn()
                    }
                }
                trace.duration -= 1
                if (trace.duration <= 0) {
                    willRemove.add(location)
                    Bukkit.getEntity(trace.entityUUID)?.remove()
                }
            }

            willRemove.forEach { traces.remove(it) }
        }, 0, 20))

        sEvent.register<PlayerInteractEntityEvent> { e ->
            if (e.player.uniqueId != p.uniqueId) return@register
            if (isStunned(e.player)) return@register
            val task = IdentityFifty.identityFiftyTask ?: return@register
            val entity = e.rightClicked
            val trace = traces.entries.find { it.value.entityUUID == entity.uniqueId } ?: return@register
            val traceData = trace.value
            val survivorData = IdentityFifty.survivors[traceData.uuid] ?: return@register
            if (!task.aliveSurvivors().contains(traceData.uuid)) return@register
            val survivor = traceData.uuid.toPlayer() ?: return@register
            val collected = collectedTraces.getOrDefault(traceData.uuid, 0) + 1
            if (collected > 3) {
                e.player.sendTranslateMsg("trace_collected_max")
                return@register
            }
            collectedTraces[traceData.uuid] = collected
            traces.remove(trace.key)
            entity.remove()

            e.player.sendTranslateMsg("trace_collected", survivor.name, collected)
            IdentityFifty.broadcastSpectators(
                translate("spec_trace_collected", e.player.name, survivor.name, collected),
                AllowAction.RECEIVE_HUNTERS_ACTION
            )
            e.player.world.playSound(e.player.location, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1f, 0.8f)

            when(collected) {
                2 -> {
                    survivorData.glowManager.glow(
                        IdentityFifty.hunters.mapNotNull { it.key.toPlayer() }.toMutableList(),
                        GlowColor.RED,
                        200
                    )

                    survivor.sendTranslateMsg("trace_collected_glow_self")
                }
                3 -> {
                    survivorData.glowManager.glow(
                        IdentityFifty.hunters.mapNotNull { it.key.toPlayer() }.toMutableList(),
                        GlowColor.RED,
                        200
                    )

                    e.player.addPotionEffect(
                        PotionEffect(PotionEffectType.SPEED, 200, 0)
                    )

                    survivor.sendTranslateMsg("trace_collected_glow_self")
                }
            }
        }

        p.inventory.addItem(passiveItem, chaseSkill)
    }

    override fun parameters(data: HunterData): HunterData {
        data.hunterClass = this
        return data
    }

    override fun onEnd(p: Player) {
        collectedTraces.clear()
        traces.values.forEach { trace ->
            Bukkit.getEntity(trace.entityUUID)?.remove()
        }
        traces.clear()
        sEvent.unregisterAll()
    }

    override fun scoreboards(p: Player): ArrayList<Pair<Int, String>> {
        val list = ArrayList<Pair<Int, String>>()

        list.add(Pair(-1, translate("trace_collector_scoreboard")))

        IdentityFifty.survivors.forEach { (uuid, _) ->
            val count = collectedTraces.getOrDefault(uuid, 0)
            val survivor = uuid.toPlayer() ?: return@forEach
            list.add(Pair(-1 - list.size, "  ${translate("trace_collector_scoreboard_count", survivor.name, count)}"))
        }

        return list
    }

    override fun info(): ArrayList<ItemStack> {
        return arrayListOf(
            passiveItem.build(),
            chaseSkillItem.build()
        )
    }


}
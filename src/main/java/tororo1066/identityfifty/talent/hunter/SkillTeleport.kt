package tororo1066.identityfifty.talent.hunter

import org.bukkit.*
import org.bukkit.Particle.DustTransition
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.*
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.data.GlowManager
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sItem.SItem
import java.util.UUID

class SkillTeleport: AbstractHunterTalent("skill_teleport",5,FirstGameSpeedUp::class.java) {

    override fun lore(): List<String> {
        return listOf("skill_teleport_lore_1","skill_teleport_lore_2","skill_teleport_lore_3", "skill_teleport_lore_4")
    }

    companion object {
        val generatorGlowManager = HashMap<UUID, GlowManager>()
        val ids = HashMap<UUID, MutableList<Int>>()
    }

    private var task: BukkitRunnable? = null

    override fun onStart(p: Player) {
        val teleportSkill = SItem(Material.STICK).setDisplayName(translate("skill_teleport")).setCustomModelData(21)
            .addLore(translate("skill_teleport_lore_1"))
            .addLore(translate("skill_teleport_lore_2"))
            .addLore(translate("skill_teleport_lore_3"))
            .addLore(translate("skill_teleport_lore_4"))

        var glowing = false

        fun cancelGlowing() {
            glowing = false
            generatorGlowManager.forEach { (uuid, manager) ->
                ids[uuid]?.forEach { manager.cancelTask(it) }
            }
            if (p.getPotionEffect(PotionEffectType.BLINDNESS)?.amplifier == 1) {
                p.removePotionEffect(PotionEffectType.BLINDNESS)
            }
            task?.cancel()
        }

        val teleportSkillItem = IdentityFifty.interactManager.createSInteractItem(teleportSkill, true).setInteractEvent { e, _ ->
            if (e.action.isLeftClick && glowing) {
                val ray = p.location
                for (i in 0 until  400) {
                    ray.add(ray.direction.multiply(1.5))
                    val entities = ray.getNearbyEntities(1.5, 1.5, 1.5)
                        .filter {
                            it.persistentDataContainer.has(NamespacedKey(IdentityFifty.plugin, "Generator"), PersistentDataType.INTEGER) ||
                                    it.persistentDataContainer.has(NamespacedKey(IdentityFifty.plugin, "EscapeGenerator"), PersistentDataType.INTEGER) ||
                                    it.persistentDataContainer.has(NamespacedKey(IdentityFifty.plugin, "PrisonLoc"), PersistentDataType.INTEGER_ARRAY)
                        }
                        .sortedBy { it.location.distance(p.location) }

                    if (entities.isNotEmpty()) {
                        cancelGlowing()
                        p.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 60, 5))
                        val entity = entities[0]
                        p.world.spawnParticle(Particle.DUST_COLOR_TRANSITION, entity.location, 100, 0.5, 3.0, 0.5, DustTransition(Color.RED, Color.RED, 1f))
                        p.world.playSound(entity.location, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.2f, 1f)
                        p.playSound(p.location, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.2f, 1f)

                        Bukkit.getScheduler().runTaskLater(IdentityFifty.plugin, Runnable {
                            Bukkit.getScheduler().runTask(IdentityFifty.plugin, Runnable {
                                p.teleport(entity.location)
                                p.world.playSound(entity.location, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f)
                            })
                        }, 60)
                        return@setInteractEvent true
                    }

                }

            }

            if (e.action.isRightClick) {
                if (glowing) {
                    p.playSound(p.location, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 2f, 1f)
                    cancelGlowing()
                } else {
                    p.playSound(p.location, Sound.ENTITY_MOOSHROOM_CONVERT, 2f, 1f)
                    glowing = true
                    p.world.getEntitiesByClasses(Sheep::class.java, Cow::class.java, ArmorStand::class.java).filter {
                        it.persistentDataContainer.has(NamespacedKey(IdentityFifty.plugin, "Generator"), PersistentDataType.INTEGER) ||
                                it.persistentDataContainer.has(NamespacedKey(IdentityFifty.plugin, "EscapeGenerator"), PersistentDataType.INTEGER) ||
                                it.persistentDataContainer.has(NamespacedKey(IdentityFifty.plugin, "PrisonLoc"), PersistentDataType.INTEGER_ARRAY)
                    }.forEach {
                        val glowManager = generatorGlowManager.getOrPut(it.uniqueId) { GlowManager(it.uniqueId) }

                        val id = ids.getOrPut(it.uniqueId) { mutableListOf() }

                        val glowColor = when(it.type) {
                            EntityType.SHEEP -> ChatColor.YELLOW
                            EntityType.COW -> ChatColor.BLUE
                            else -> ChatColor.RED
                        }
                        id.addAll(glowManager.glow(mutableListOf(p), glowColor, 100000))
                    }

                    task = IdentityFifty.speedModifier(p, 0.0, 999999, AttributeModifier.Operation.ADD_SCALAR)
                    p.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 100000, 1))
                }
            }

            return@setInteractEvent false
        }.setInitialCoolDown(2000).setInteractCoolDown(1200)

        p.inventory.addItem(teleportSkillItem)
    }

    override fun onEnd(p: Player) {
        generatorGlowManager.forEach { (uuid, manager) ->
            ids[uuid]?.forEach { manager.cancelTask(it) }
        }
        generatorGlowManager.clear()
        ids.clear()
    }
}
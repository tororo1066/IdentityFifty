package tororo1066.identityfifty.talent.hunter

import org.bukkit.*
import org.bukkit.entity.Cow
import org.bukkit.entity.Player
import org.bukkit.entity.Sheep
import org.bukkit.persistence.PersistentDataType
import tororo1066.identityfifty.IdentityFifty
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sItem.SItem

class SkillTeleport: AbstractHunterTalent("skill_teleport",5,TalentPlane::class.java) {

    override fun lore(): List<String> {
        return listOf("skill_teleport_lore_1","skill_teleport_lore_2")
    }

    override fun onStart(p: Player) {
        val teleportSkill = SItem(Material.STICK).setDisplayName(translate("skill_teleport")).setCustomModelData(999)
            .addLore(translate("skill_teleport_lore_1"))
            .addLore(translate("skill_teleport_lore_2"))

        val teleportSkillItem = IdentityFifty.interactManager.createSInteractItem(teleportSkill,true).setInteractEvent { _, _ ->
            val task = IdentityFifty.identityFiftyTask?:return@setInteractEvent false
            val distances = ArrayList<Pair<Double,Location>>()
            if (task.remainingGenerator == 0){
                p.world.getEntitiesByClass(Cow::class.java).filter {
                    it.persistentDataContainer.has(NamespacedKey(IdentityFifty.plugin,"EscapeGenerator"), PersistentDataType.INTEGER)
                }.forEach {
                    distances.add(Pair(it.location.distance(p.location),it.location))
                }
            } else {
                p.world.getEntitiesByClass(Sheep::class.java).filter {
                    it.persistentDataContainer.has(NamespacedKey(IdentityFifty.plugin,"Generator"), PersistentDataType.INTEGER)
                }.forEach {
                    distances.add(Pair(it.location.distance(p.location),it.location))
                }
            }

            if (distances.isEmpty()) return@setInteractEvent false
            val max = distances.maxByOrNull { it.first }?:return@setInteractEvent false
            p.world.spawnParticle(Particle.REDSTONE,max.second,100,0.5,3.0,0.5)
            p.world.playSound(max.second, Sound.ENTITY_ENDER_DRAGON_GROWL,1.2f,1f)
            p.playSound(p.location,Sound.ENTITY_ENDER_DRAGON_GROWL,1.2f,1f)

            Bukkit.getScheduler().runTaskLater(IdentityFifty.plugin, Runnable {
                Bukkit.getScheduler().runTask(IdentityFifty.plugin, Runnable {
                    p.teleport(max.second)
                    p.world.playSound(max.second, Sound.ENTITY_ENDERMAN_TELEPORT,1f,1f)
                })
            },50)

            return@setInteractEvent true
        }.setInitialCoolDown(1600)

        p.inventory.addItem(teleportSkillItem)
    }
}
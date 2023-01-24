package tororo1066.identityfifty.character.hunter

import de.slikey.effectlib.effect.CylinderEffect
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.inventivetalent.glow.GlowAPI
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.data.HunterData
import tororo1066.tororopluginapi.lang.SLang.Companion.sendTranslateMsg
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sItem.SItem

class AreaMan: AbstractHunter("areaman") {

    override fun onStart(p: Player) {

        val passiveItem = SItem(Material.STICK).setDisplayName(translate("hunter_passive")).setCustomModelData(1)
            .addLore(translate("areaman_passive_lore_1"))
            .addLore(translate("areaman_passive_lore_2"))

        val areaSkill = SItem(Material.STICK).setDisplayName(translate("area_skill")).setCustomModelData(9)
            .addLore(translate("area_skill_lore_1"))
            .addLore(translate("area_skill_lore_2"))

        val areaSkillItem = IdentityFifty.interactManager.createSInteractItem(areaSkill,true).setInitialCoolDown(1200).setInteractEvent { e, item ->
            val cylinder = CylinderEffect(IdentityFifty.effectManager)
            val loc = p.location
            cylinder.location = loc
            cylinder.height = 45f
            cylinder.radius = 20f
            cylinder.particle = Particle.FLAME
            cylinder.iterations = 30
            cylinder.period = 20
            cylinder.particles = 5000


            cylinder.start()

            val players = ArrayList<Player>()
            IdentityFifty.hunters.forEach second@ { (uuid, _) ->
                players.add(Bukkit.getPlayer(uuid)?:return@second)
            }

            Bukkit.getScheduler().runTaskAsynchronously(IdentityFifty.plugin, Runnable {
                for (i in 1..300){
                    Bukkit.getScheduler().runTask(IdentityFifty.plugin, Runnable {
                        loc.getNearbyPlayers(20.0,45.0).forEach {
                            val data = IdentityFifty.survivors[it.uniqueId]?:return@forEach
                            data.glowManager.glow(players,GlowAPI.Color.RED,2)
                            p.playSound(p.location,Sound.ENTITY_ARROW_HIT_PLAYER,0.2f,2f)
                        }
                    })

                    Thread.sleep(100)
                }
            })

            return@setInteractEvent true
        }

        p.inventory.addItem(passiveItem)
        p.inventory.addItem(areaSkillItem)
    }

    override fun parameters(data: HunterData): HunterData {
        data.hunterClass = this
        return data
    }

    override fun onFinishedGenerator(dieLocation: Location, remainingGenerator: Int, p: Player) {
        val players = IdentityFifty.hunters.mapNotNull { Bukkit.getPlayer(it.key) }.toMutableList()

        dieLocation.getNearbyPlayers(3.0).forEach {
            val data = IdentityFifty.survivors[it.uniqueId]?:return@forEach
            data.glowManager.glow(players,GlowAPI.Color.PURPLE,80)
        }

        if (remainingGenerator == 0){
            val survivor = IdentityFifty.survivors.filter { IdentityFifty.identityFiftyTask?.deadSurvivor?.contains(it.key) == false && it.value.getHealth() != 0 }.entries.random()
            val player = Bukkit.getPlayer(survivor.key)!!
            player.addPotionEffect(PotionEffect(PotionEffectType.SLOW,120,4))
            survivor.value.glowManager.glow(players,GlowAPI.Color.DARK_RED,300)
            player.sendTranslateMsg("area_spec_message",p.name)
        }
    }
}
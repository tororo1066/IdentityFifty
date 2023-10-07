package tororo1066.identityfifty.character.hunter

import de.slikey.effectlib.EffectType
import de.slikey.effectlib.effect.ConeEffect
import de.slikey.effectlib.effect.CylinderEffect
import de.slikey.effectlib.effect.SphereEffect
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.inventivetalent.glow.GlowAPI
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.data.HunterData
import tororo1066.identityfifty.data.PrisonData
import tororo1066.identityfifty.enumClass.AllowAction
import tororo1066.tororopluginapi.lang.SLang.Companion.sendTranslateMsg
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sItem.SItem

class AreaMan: AbstractHunter("areaman") {

    override fun onStart(p: Player) {

        val passiveItem = SItem(Material.STICK).setDisplayName(translate("hunter_passive")).setCustomModelData(1)
            .addLore(translate("areaman_passive_lore_1"))
            .addLore(translate("areaman_passive_lore_2"))
            .addLore(translate("areaman_passive_lore_3"))

        val areaSkill = SItem(Material.STICK).setDisplayName(translate("area_skill")).setCustomModelData(9)
            .addLore(translate("area_skill_lore_1"))
            .addLore(translate("area_skill_lore_2"))
            .addLore(translate("area_skill_lore_3"))
            .addLore(translate("area_skill_lore_4"))

        val areaSkillItem = IdentityFifty.interactManager.createSInteractItem(areaSkill,true).setInitialCoolDown(600).setInteractEvent { e, item ->

            val length = 15
            val width = 6

            val loc = p.location

            val players = ArrayList<Player>()
            IdentityFifty.hunters.forEach second@ { (uuid, _) ->
                players.add(Bukkit.getPlayer(uuid)?:return@second)
            }

            val glowedPlayers = ArrayList<Player>()
            for (i in 1..length){
                loc.add(loc.direction.multiply(1)).getNearbyPlayers(width.toDouble()).filter { !glowedPlayers.contains(it) }.forEach {
                    val data = IdentityFifty.survivors[it.uniqueId]?:return@forEach
                    data.glowManager.glow(players,GlowAPI.Color.RED,100)
                    p.playSound(p.location,Sound.ENTITY_ARROW_HIT_PLAYER,1f,2f)
                    it.playSound(it.location,Sound.ENTITY_ARROW_HIT_PLAYER,1f,2f)
                    glowedPlayers.add(it)
                }

                val cylinder = SphereEffect(IdentityFifty.effectManager)
                cylinder.location = loc
                cylinder.radius = width.toDouble()
                cylinder.particle = Particle.FLAME
                cylinder.particles = 200
                cylinder.type = EffectType.INSTANT
                cylinder.updateLocations = false
                cylinder.updateDirections = false

                cylinder.start()
            }

            if (glowedPlayers.isNotEmpty()){
                p.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 40+glowedPlayers.size*40, glowedPlayers.size-1, true, false))
            }

            IdentityFifty.broadcastSpectators(translate("spec_area_skill_used",p.name), AllowAction.RECEIVE_HUNTERS_ACTION)

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
            data.glowManager.glow(players,GlowAPI.Color.PURPLE,140)
        }

        if (remainingGenerator == 0){
            val survivor = IdentityFifty.survivors.filter { IdentityFifty.identityFiftyTask?.deadSurvivor?.contains(it.key) == false && it.value.getHealth() != 0 }.entries.random()
            val player = Bukkit.getPlayer(survivor.key)!!
            player.addPotionEffect(PotionEffect(PotionEffectType.SLOW,120,4))
            survivor.value.glowManager.glow(players,GlowAPI.Color.DARK_RED,300)
            player.sendTranslateMsg("area_spec_message",p.name)
        }
    }

    override fun onSurvivorJail(survivor: Player, prison: PrisonData, p: Player) {
        val cylinder = CylinderEffect(IdentityFifty.effectManager)
        val loc = prison.spawnLoc
        cylinder.location = loc
        cylinder.height = 45f
        cylinder.radius = 20f
        cylinder.particle = Particle.FLAME
        cylinder.iterations = 30
        cylinder.period = 20
        cylinder.particles = 2000
        cylinder.angularVelocityX = 0.0
        cylinder.angularVelocityY = 0.0
        cylinder.angularVelocityZ = 0.0


        cylinder.start()

        val players = ArrayList<Player>()
        IdentityFifty.hunters.forEach second@ { (uuid, _) ->
            players.add(Bukkit.getPlayer(uuid)?:return@second)
        }

        Bukkit.getScheduler().runTaskAsynchronously(IdentityFifty.plugin, Runnable {
            for (i in 1..20){
                Bukkit.getScheduler().runTask(IdentityFifty.plugin, Runnable {
                    loc.getNearbyPlayers(20.0,7.0).forEach {
                        val data = IdentityFifty.survivors[it.uniqueId]?:return@forEach
                        data.glowManager.glow(players,GlowAPI.Color.RED,21)
                        p.playSound(p.location,Sound.ENTITY_ARROW_HIT_PLAYER,0.2f,2f)
                    }
                })

                Thread.sleep(1000)
            }
        })
    }

    override fun info(): ArrayList<ItemStack> {
        val passiveItem = SItem(Material.STICK).setDisplayName(translate("hunter_passive")).setCustomModelData(1)
            .addLore(translate("areaman_passive_lore_1"))
            .addLore(translate("areaman_passive_lore_2"))
            .addLore(translate("areaman_passive_lore_3"))

        val areaSkill = SItem(Material.STICK).setDisplayName(translate("area_skill")).setCustomModelData(9)
            .addLore(translate("area_skill_lore_1"))
            .addLore(translate("area_skill_lore_2"))
            .addLore(translate("area_skill_lore_3"))
            .addLore(translate("area_skill_lore_4"))
        return arrayListOf(passiveItem,areaSkill)
    }
}
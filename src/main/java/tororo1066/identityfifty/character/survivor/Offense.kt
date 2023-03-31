package tororo1066.identityfifty.character.survivor

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.data.SurvivorData
import tororo1066.identityfifty.enumClass.StunState
import tororo1066.tororopluginapi.SDebug
import tororo1066.tororopluginapi.lang.SLang.Companion.sendTranslateMsg
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sItem.SItem
import tororo1066.tororopluginapi.utils.*

class Offense : AbstractSurvivor("offense") {

    override fun onStart(p: Player) {
        super.onStart(p)
        val passiveItem = SItem(Material.STICK).setDisplayName(translate("passive")).setCustomModelData(8)
            .addLore(translate("offense_passive_lore_1"))
            .addLore(translate("offense_passive_lore_2"))

        val tackleSkillItem = SItem(Material.STICK).setDisplayName(translate("rugby_ball")).setCustomModelData(14)
            .addLore(translate("rugby_ball_lore_1"))
            .addLore(translate("rugby_ball_lore_2"))
            .addLore(translate("rugby_ball_lore_3"))
            .addLore(translate("rugby_ball_lore_4"))


        val tackleItem = IdentityFifty.interactManager.createSInteractItem(tackleSkillItem,true).setInteractEvent { e, item ->
            var actionTime = 0
            var lock: Boolean
            Bukkit.getScheduler().runTaskAsynchronously(IdentityFifty.plugin, Runnable {
                while (true){
                    if (actionTime >= 60){
                        break
                    }
                    var players: List<Player> = listOf()
                    lock = true
                    IdentityFifty.util.runTask {
                        players = p.location.getNearbyPlayers(1.5).filter { IdentityFifty.hunters.containsKey(it.uniqueId) }
                        lock = false
                    }
                    while (lock){
                        Thread.sleep(1)
                    }
                    if (players.isNotEmpty()){
                        players.forEach {
                            it.playSound(it.location, Sound.BLOCK_ANVIL_PLACE, 1f, 1f)
                            it.sendTranslateMsg("rugby_ball_hit_hunter")
                            p.playSound(p.location, Sound.BLOCK_ANVIL_PLACE, 1f, 1f)
                            p.sendTranslateMsg("rugby_ball_hit",it.name)
                            IdentityFifty.stunEffect(it, (actionTime*5-20), (actionTime*5), StunState.OTHER)
                        }
                        break
                    }

                    val block1 = p.world.rayTraceBlocks(p.location,p.location.setPitchL(0f).direction,1.5)?.hitBlock
                    val block2 = p.world.rayTraceBlocks(p.location.add(0.0,1.0,0.0),p.location.setPitchL(0f).direction,1.5)?.hitBlock

                    if (block1 != null){
                        if (!block1.isPassable)break
                    }
                    if (block2 != null){
                        if (!block2.isPassable)break
                    }

                    lock = true
                    IdentityFifty.util.runTask {
                        val loc = p.location.setPitchL(0f)
                        val vec = loc.add(loc.direction.normalize().multiply(1.5))
                        p.teleport(getRayLoc(Location(p.world,vec.x, loc.y, vec.z, loc.yaw, loc.pitch)))
                        p.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 1f)
                        lock = false
                    }
                    while (lock){
                        Thread.sleep(1)
                    }
                    actionTime++
                }

                IdentityFifty.util.runTask {
                    p.addPotionEffect(PotionEffect(PotionEffectType.SLOW, actionTime, 10))
                }
                item.setInteractCoolDown(actionTime*12 + 100)
            })
            return@setInteractEvent true
        }.setInitialCoolDown(100000)

        p.inventory.addItem(passiveItem,tackleItem)
    }

    override fun parameters(data: SurvivorData): SurvivorData {
        data.otherPlayerHealDelayPercentage = 0.5
        data.survivorClass = this
        return data
    }

    override fun onHitWoodPlate(
        hittedPlayer: Player,
        loc: Location,
        blindTime: Int,
        slowTime: Int,
        p: Player
    ): Pair<Int, Int> {
        return Pair(blindTime+20,slowTime+20)
    }

    override fun info(): ArrayList<ItemStack> {
        val passiveItem = SItem(Material.STICK).setDisplayName(translate("passive")).setCustomModelData(8)
            .addLore(translate("offense_passive_lore_1"))
            .addLore(translate("offense_passive_lore_2"))

        val tackleSkillItem = SItem(Material.STICK).setDisplayName(translate("rugby_ball")).setCustomModelData(14)
            .addLore(translate("rugby_ball_lore_1"))
            .addLore(translate("rugby_ball_lore_2"))
            .addLore(translate("rugby_ball_lore_3"))
            .addLore(translate("rugby_ball_lore_4"))
        return arrayListOf(passiveItem,tackleSkillItem)
    }

    private fun getRayLoc(location: Location): Location {
        var loc = location
        while (true){
            if (loc.y <= 0){
                return loc
            }
            loc = loc.add(0.0,-1.0,0.0)
            if (!loc.block.type.isAir){
                return loc.add(0.0,1.0,0.0)
            }
        }
    }
}
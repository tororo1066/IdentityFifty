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
import tororo1066.identityfifty.enumClass.AllowAction
import tororo1066.identityfifty.enumClass.StunState
import tororo1066.tororopluginapi.lang.SLang.Companion.sendTranslateMsg
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sItem.SItem
import tororo1066.tororopluginapi.utils.setPitchL
import java.util.function.Consumer

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
            fun end(){
                IdentityFifty.util.runTask {
                    p.addPotionEffect(PotionEffect(PotionEffectType.SLOW, (actionTime.toDouble()*1.5).toInt(), 10))
                }
                item.setInteractCoolDown(actionTime*18 + 100)
            }

            IdentityFifty.broadcastSpectators(translate("spec_rugby_ball_used",p.name),
                AllowAction.RECEIVE_SURVIVORS_ACTION)
            Bukkit.getScheduler().runTaskTimer(IdentityFifty.plugin, Consumer {
                if (actionTime >= 35){
                    it.cancel()
                    end()
                    return@Consumer
                }
                var stop = false
                val block1 = p.world.rayTraceBlocks(p.location,p.location.setPitchL(0f).direction,1.5)?.hitBlock
                val block2 = p.world.rayTraceBlocks(p.location.add(0.0,1.0,0.0),p.location.setPitchL(0f).direction,1.5)?.hitBlock

                if (block1 != null && !block1.isPassable){
                    val maxDiff = block1.boundingBox.maxY-block1.boundingBox.minY
                    if (maxDiff > 0.5){
                        it.cancel()
                        stop = true
                    }
                }
                if (block2 != null){
                    if (!block2.isPassable){
                        it.cancel()
                        stop = true
                    }
                }

                p.world.playSound(p.location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 1f)
                p.velocity = p.location.setPitchL(0f).direction.normalize().multiply(1.2).setY(-1)
                val players = p.location.getNearbyPlayers(1.0).filter { IdentityFifty.hunters.containsKey(it.uniqueId) }
                if (players.isNotEmpty()){
                    players.forEach { player ->
                        player.playSound(player.location, Sound.BLOCK_ANVIL_PLACE, 1f, 1f)
                        player.sendTranslateMsg("rugby_ball_hit_hunter")
                        p.playSound(p.location, Sound.BLOCK_ANVIL_PLACE, 1f, 1f)
                        p.sendTranslateMsg("rugby_ball_hit",player.name)
                        IdentityFifty.broadcastSpectators(translate("spec_rugby_ball_hit",p.name,player.name),
                            AllowAction.RECEIVE_SURVIVORS_ACTION)
                        IdentityFifty.stunEffect(player, (actionTime*6-20), (actionTime*6), StunState.OTHER)
                    }
                    it.cancel()
                    end()
                    return@Consumer
                }

                if (stop){
                    it.cancel()
                    end()
                    return@Consumer
                }

                actionTime++
            },0,3)
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
        hitPlayer: Player,
        loc: Location,
        blindTime: Int,
        slowTime: Int,
        p: Player
    ): Pair<Int, Int> {
        return Pair(blindTime+40,slowTime+40)
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
}
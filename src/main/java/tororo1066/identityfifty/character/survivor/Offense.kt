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
                            IdentityFifty.util.runTask { _ ->
                                IdentityFifty.stunEffect(it, (actionTime*5-20), (actionTime*5))
                            }
                        }
                        break
                    }

//                    涙の結晶(不必要)
//                    val multiply0 = loc.clone()
//                    val multiply1 = loc.clone().add(loc.clone().direction.normalize().multiply(1)).setYL(loc.y)
//                    val multiply15 = loc.clone().add(loc.clone().direction.normalize().multiply(1.5)).setYL(loc.y)
//                    if (!multiply0.block.isPassable || !multiply1.block.isPassable || !multiply15.block.isPassable){
//                        SDebug.broadcastDebug(1,"すり抜け防止プロトコル 通常")
//                        SDebug.broadcastDebug(1,"原因ブロック(0) ${multiply0.block.type}")
//                        SDebug.broadcastDebug(1,"原因ブロック(1) ${multiply1.block.type}")
//                        SDebug.broadcastDebug(1,"原因ブロック(1.5) ${multiply15.block.type}")
//                        break
//                    }
//                    val normalize0 = multiply0.clone().direction.normalize()
//
//                    SDebug.broadcastDebug(1,"すり抜け防止プロトコル debug")
//                    SDebug.broadcastDebug(1,"原因ブロック(45) ${loc.clone().add(multiply0.clone().direction.rotateAroundY(45.0).multiply(1)).setYL(loc.y).block.type}")
//                    SDebug.broadcastDebug(1,"原因ブロック(-45) ${loc.clone().add(multiply0.clone().direction.rotateAroundY(-45.0).multiply(1)).setYL(loc.y).block.type}")
//                    SDebug.broadcastDebug(1,"デバッグLoc ${loc.toLocString(LocType.COMMA)}")
//                    SDebug.broadcastDebug(1,"原因Loc(45) ${loc.clone().add(normalize0.clone().rotateAroundY(45.0).multiply(1)).setYL(loc.y).toLocString(LocType.COMMA)}")
//                    SDebug.broadcastDebug(1,"原因Loc(-45) ${loc.clone().add(normalize0.clone().rotateAroundY(-45.0).multiply(1)).setYL(loc.y).toLocString(LocType.COMMA)}")
//
//                    val normalize1 = multiply1.clone().direction.normalize()
//                    val normalize15 = multiply15.clone().direction.normalize()
//                    if (!loc.clone().add(normalize0.clone().rotateAroundY(45.0).multiply(1)).setYL(loc.y).block.isPassable ||
//                        !loc.clone().add(normalize0.clone().rotateAroundY(-45.0).multiply(1)).setYL(loc.y).block.isPassable){
//                        SDebug.broadcastDebug(1,"すり抜け防止プロトコル normalize0")
//                        SDebug.broadcastDebug(1,"原因ブロック(45) ${loc.clone().add(multiply0.clone().direction.rotateAroundY(45.0).multiply(1)).setYL(loc.y).block.type}")
//                        SDebug.broadcastDebug(1,"原因ブロック(-45) ${loc.clone().add(multiply0.clone().direction.rotateAroundY(-45.0).multiply(1)).setYL(loc.y).block.type}")
//                        SDebug.broadcastDebug(1,"デバッグLoc ${loc.toLocString(LocType.COMMA)}")
//                        SDebug.broadcastDebug(1,"原因Loc(45) ${loc.clone().add(normalize0.clone().rotateAroundY(45.0).multiply(1)).setYL(loc.y).toLocString(LocType.COMMA)}")
//                        SDebug.broadcastDebug(1,"原因Loc(-45) ${loc.clone().add(normalize0.clone().rotateAroundY(-45.0).multiply(1)).setYL(loc.y).toLocString(LocType.COMMA)}")
//                        break
//                    } else if (!loc.clone().add(normalize1.clone().rotateAroundY(45.0).multiply(1)).setYL(loc.y).block.isPassable ||
//                        !loc.clone().add(normalize1.clone().rotateAroundY(-45.0).multiply(1)).setYL(loc.y).block.isPassable){
//                        SDebug.broadcastDebug(1,"すり抜け防止プロトコル normalize1")
//                        SDebug.broadcastDebug(1,"原因ブロック(45) ${loc.clone().add(multiply1.clone().direction.rotateAroundY(45.0).multiply(1)).setYL(loc.y).block.type}")
//                        SDebug.broadcastDebug(1,"原因ブロック(-45) ${loc.clone().add(multiply1.clone().direction.rotateAroundY(-45.0).multiply(1)).setYL(loc.y).block.type}")
//                        SDebug.broadcastDebug(1,"デバッグLoc ${loc.toLocString(LocType.COMMA)}")
//                        SDebug.broadcastDebug(1,"原因Loc(45) ${loc.clone().add(normalize1.clone().rotateAroundY(45.0).multiply(1)).setYL(loc.y).toLocString(LocType.COMMA)}")
//                        SDebug.broadcastDebug(1,"原因Loc(-45) ${loc.clone().add(normalize1.clone().rotateAroundY(-45.0).multiply(1)).setYL(loc.y).toLocString(LocType.COMMA)}")
//                        break
//                    } else if (!loc.clone().add(normalize15.clone().rotateAroundY(45.0).multiply(1)).setYL(loc.y).block.isPassable ||
//                        !loc.clone().add(normalize15.clone().rotateAroundY(-45.0).multiply(1)).setYL(loc.y).block.isPassable){
//                        lock = true
//                        IdentityFifty.util.runTask {
//                            val vec = p.location.add(p.location.direction.normalize().multiply(0.5))
//                            p.teleport(getRayLoc(Location(p.world,vec.x, p.location.y, vec.z, p.location.yaw, p.location.pitch)))
//                            p.playSound(p.location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 1f)
//                            lock = false
//                        }
//                        SDebug.broadcastDebug(1,"すり抜け防止プロトコル normalize15")
//                        SDebug.broadcastDebug(1,"原因ブロック(45) ${loc.clone().add(normalize15.clone().rotateAroundY(45.0).multiply(1)).setYL(loc.y).block.type}")
//                        SDebug.broadcastDebug(1,"原因ブロック(-45) ${loc.clone().add(normalize15.clone().rotateAroundY(-45.0).multiply(1)).setYL(loc.y).block.type}")
//                        SDebug.broadcastDebug(1,"デバッグLoc ${loc.toLocString(LocType.COMMA)}")
//                        SDebug.broadcastDebug(1,"原因Loc(45) ${loc.clone().add(normalize15.clone().rotateAroundY(45.0).multiply(1)).setYL(loc.y).toLocString(LocType.COMMA)}")
//                        SDebug.broadcastDebug(1,"原因Loc(-45) ${loc.clone().add(normalize15.clone().rotateAroundY(-45.0).multiply(1)).setYL(loc.y).toLocString(LocType.COMMA)}")
//                        break
//                    }

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
                item.setInteractCoolDown(actionTime*10 + 100)
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

    override fun onHitWoodPlate(hittedPlayer: Player, loc: Location, p: Player): Pair<Int, Int> {
        return Pair(180,200)
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
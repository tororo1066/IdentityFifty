package tororo1066.identityfifty.character.hunter

import org.bukkit.*
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import org.inventivetalent.glow.GlowAPI
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.data.HunterData
import tororo1066.identityfifty.enumClass.AllowAction
import tororo1066.tororopluginapi.lang.SLang.Companion.sendTranslateMsg
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sItem.SItem
import java.util.*
import kotlin.collections.ArrayList

class Fader: AbstractHunter("fader"){
    private val traps = LinkedHashMap<UUID,TrapData>()

    override fun onStart(p: Player) {
        traps.clear()
        val passiveItem = SItem(Material.STICK).setDisplayName(translate("hunter_passive")).setCustomModelData(1)
            .addLore(translate("fader_passive_lore_1"))
            .addLore(translate("fader_passive_lore_2"))
            .addLore(translate("fader_passive_lore_3"))

        tasks.add(Bukkit.getScheduler().runTaskTimer(IdentityFifty.plugin, Runnable {
            //半径20ブロック以内にサバイバーがいないと透明化+盲目+加速
            val radiusPlayer = p.location.getNearbyPlayers(20.0).filter { IdentityFifty.identityFiftyTask?.aliveSurvivors()?.contains(it.uniqueId) == true }
            if (radiusPlayer.isEmpty()){
                p.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY,200,0,true,false,true))
                p.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS,200,0,true,false,true))
                p.addPotionEffect(PotionEffect(PotionEffectType.SPEED,200,2,true,false,true))
            } else {
                //透明解除
                if (p.getPotionEffect(PotionEffectType.INVISIBILITY) != null){
                    p.world.playSound(p.location,Sound.ENTITY_GHAST_HURT,1f,2f)
                    p.world.spawnParticle(Particle.SMOKE_LARGE,p.location,100,0.0,1.0,0.0)
                }
                p.removePotionEffect(PotionEffectType.INVISIBILITY)
                val blindness = p.getPotionEffect(PotionEffectType.BLINDNESS)
                if (blindness != null && blindness.amplifier != 3){ //盲目の強さ3は解除しない(逃亡者のスキルなど)
                    p.isSprinting = false
                    p.removePotionEffect(PotionEffectType.BLINDNESS)
                }
                p.removePotionEffect(PotionEffectType.SPEED)
            }
        },0,5))

        tasks.add(Bukkit.getScheduler().runTaskTimer(IdentityFifty.plugin, Runnable {
            if (p.inventory.filter { it != null && it.itemMeta?.persistentDataContainer?.has(NamespacedKey(IdentityFifty.plugin,"glow_trap"),
                    PersistentDataType.INTEGER) == true }.size < 2){ //トラップを二個以上持っていない時だけ与える
                val trap = SItem(Material.STICK).setDisplayName(translate("glow_trap"))
                    .setCustomModelData(13)
                    .addLore(translate("glow_trap_lore_1"))
                    .addLore(translate("glow_trap_lore_2"))
                    .addLore(translate("glow_trap_lore_3"))
                    .addLore(translate("glow_trap_lore_4"))
                    .addLore(translate("glow_trap_lore_5"))
                    .setCustomData(IdentityFifty.plugin,"glow_trap", PersistentDataType.INTEGER, 1)
                val trapSkillItem = IdentityFifty.interactManager.createSInteractItem(trap, true).setInteractEvent { e, item ->
                    e.item!!.amount -= 1
                    item.delete()
                    if (traps.size >= 2){ //設置済のトラップが2個以上ある場合は一番古いものを削除
                        val preventTrap = traps.entries.first()
                        Bukkit.getEntity(preventTrap.key)?.remove()
                        preventTrap.value.task.cancel()
                        traps.remove(preventTrap.key)
                    }
                    val pLoc = p.location
                    p.playSound(pLoc, Sound.ENTITY_HORSE_ARMOR, 1f, 1f)
                    pLoc.world.spawn(pLoc, ArmorStand::class.java) {
                        it.persistentDataContainer.set(
                            NamespacedKey(IdentityFifty.plugin,"glow_trap"),
                            PersistentDataType.INTEGER,1
                        )
                        //ハンターのチームに追加して位置がわかるように
                        IdentityFifty.identityFiftyTask?.hunterTeam?.addEntry(it.uniqueId.toString())
                        it.isInvisible = true
                        it.isInvulnerable = true
                        it.isMarker = true
                        val task = object : BukkitRunnable(){

                            override fun run() {
                                val players = it.location.getNearbyPlayers(2.0).filter {
                                    fil-> IdentityFifty.identityFiftyTask?.aliveSurvivors()?.contains(fil.uniqueId) == true
                                        && !fil.isSneaking
                                } //半径以内でなおかつしゃがんでいないサバイバー
                                if (players.isEmpty())return
                                players.forEach { surP ->
                                    val data = IdentityFifty.survivors[surP.uniqueId]?:return@forEach
                                    p.playSound(p.location, Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE,1f,1f)
                                    p.sendTranslateMsg("glow_trap_hit")
                                    surP.playSound(p.location, Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE,1f,1f)
                                    surP.sendTranslateMsg("glow_trap_hit_survivor")
                                    data.glowManager.glow(mutableListOf(p),GlowAPI.Color.DARK_RED,240)
                                    IdentityFifty.broadcastSpectators(translate("spec_glow_trap_hit",surP.name),
                                        AllowAction.RECEIVE_HUNTERS_ACTION)
                                }
                                Bukkit.getEntity(it.uniqueId)?.remove()
                                traps.remove(it.uniqueId)
                                cancel()
                            }
                        }.runTaskTimer(IdentityFifty.plugin,40,5) //置いてすぐには反応しないように

                        traps[it.uniqueId] = TrapData(it.uniqueId,task)
                    }
                    IdentityFifty.broadcastSpectators(translate("spec_glow_trap_used",p.name),
                        AllowAction.RECEIVE_HUNTERS_ACTION)
                    return@setInteractEvent true
                }

                p.inventory.addItem(trapSkillItem)
            }
        },0,800))

        p.inventory.addItem(passiveItem)

        IdentityFifty.survivors.values.forEach {
//            保留
//            val cloneRules = it.heartProcessRules.toMutableList()
//            cloneRules.forEachIndexed { index, pair ->
//                it.heartProcessRules[index] = Pair(pair.first-2,pair.second*0.8)
//
//            }
            //心音の反応力を下げる
            it.heartProcessRules = arrayListOf(Pair(23.0,0.1), Pair(17.0,0.2), Pair(12.0,0.3), Pair(8.0,0.4))
        }
    }

    override fun onEnd(p: Player) {
        traps.values.forEach {
            it.task.cancel()
            Bukkit.getEntity(it.uuid)?.remove()
        }
        traps.clear()
    }


    override fun parameters(data: HunterData): HunterData {
        data.hunterClass = this
        return data
    }

    class TrapData(var uuid: UUID, var task: BukkitTask)

    override fun info(): ArrayList<ItemStack> {
        val passiveItem = SItem(Material.STICK).setDisplayName(translate("hunter_passive")).setCustomModelData(1)
            .addLore(translate("fader_passive_lore_1"))
            .addLore(translate("fader_passive_lore_2"))
            .addLore(translate("fader_passive_lore_3"))
        val trap = SItem(Material.STICK).setDisplayName(translate("glow_trap"))
            .setCustomModelData(13)
            .addLore(translate("glow_trap_lore_1"))
            .addLore(translate("glow_trap_lore_2"))
            .addLore(translate("glow_trap_lore_3"))
            .addLore(translate("glow_trap_lore_4"))
            .addLore(translate("glow_trap_lore_5"))
        return arrayListOf(passiveItem,trap)
    }
}
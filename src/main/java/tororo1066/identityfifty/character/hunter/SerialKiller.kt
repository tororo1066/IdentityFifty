package tororo1066.identityfifty.character.hunter

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.data.HunterData
import tororo1066.identityfifty.enumClass.AllowAction
import tororo1066.nmsutils.items.GlowColor
import tororo1066.tororopluginapi.lang.SLang.Companion.sendTranslateMsg
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sItem.SItem
import tororo1066.tororopluginapi.utils.addItem
import tororo1066.tororopluginapi.utils.toPlayer
import java.util.UUID
import java.util.function.Consumer
import kotlin.math.floor

class SerialKiller: AbstractHunter("serialkiller") {

    private var previousDiff = 0
    private var damageFlag = false

    override fun onStart(p: Player) {

        val passiveItem = SItem(Material.STICK).setDisplayName(translate("hunter_passive")).setCustomModelData(1)
            .addLore(translate("serialkiller_passive_lore_1"))
            .addLore(translate("serialkiller_passive_lore_2"))

        val killFindSkill = SItem(Material.STICK).setDisplayName(translate("kill_find")).setCustomModelData(27)
            .addLore(translate("kill_find_lore_1"))
            .addLore(translate("kill_find_lore_2"))
            .addLore(translate("kill_find_lore_3"))

        val killFindSkillItem = IdentityFifty.createSInteractItem(killFindSkill).setInteractEvent { e, _ ->
            val player = e.player
            if (isStunned(player)) return@setInteractEvent false
            player.world.playSound(player.location, Sound.ENTITY_WITHER_AMBIENT, 1f, 1f)
            player.world.playSound(player.location, Sound.ENTITY_BLAZE_SHOOT, 1f, 1f)
            IdentityFifty.broadcastSpectators(translate("spec_kill_find_used",player.name),
                AllowAction.RECEIVE_HUNTERS_ACTION)
            val uuid = UUID.randomUUID()
            IdentityFifty.survivors.values.forEach {
                it.footprintsModify += uuid to 10.0
            }

            Bukkit.getScheduler().runTaskLater(IdentityFifty.plugin, Runnable {
                IdentityFifty.survivors.values.forEach {
                    it.footprintsModify -= uuid
                }
            }, 200)

            damageFlag = false
            val bossBar = Bukkit.createBossBar(translate("kill_find_remaining_time","30"),BarColor.RED,BarStyle.SOLID)
            bossBar.progress = 1.0
            bossBar.addPlayer(player)
            var timer = 600
            Bukkit.getScheduler().runTaskTimer(IdentityFifty.plugin, Consumer {
                if (IdentityFifty.identityFiftyTask == null){
                    it.cancel()
                    return@Consumer
                }
                if (timer <= 0){
                    if (!damageFlag){
                        val data = IdentityFifty.hunters[player.uniqueId]!!
                        data.glowManager.glow(IdentityFifty.survivors.mapNotNull { map -> map.key.toPlayer() }.toMutableList(), GlowColor.RED, 200)
                        player.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS,200,1))
                        player.sendTranslateMsg("kill_find_failed")
                        IdentityFifty.survivors.keys.forEach { uuid ->
                            uuid.toPlayer()?.sendTranslateMsg("kill_find_failed_survivor")
                        }
                        IdentityFifty.broadcastSpectators(translate("spec_kill_find_failed",player.name),
                            AllowAction.RECEIVE_HUNTERS_ACTION)
                        player.world.playSound(player.location, Sound.BLOCK_BEACON_DEACTIVATE, 1f, 1f)
                    }

                    bossBar.removePlayer(player)

                    it.cancel()
                }

                timer--
                val progress = 1.0 - (timer.toDouble() / 600.0)
                if (progress <= 1.0){
                    bossBar.progress = progress
                }
                bossBar.setTitle(translate("kill_find_remaining_time", (floor(timer / 20.0 * 10.0) / 10.0).toString()))
            }, 0, 1)
            return@setInteractEvent true
        }.setInitialCoolDown(1100)

        tasks.add(Bukkit.getScheduler().runTaskTimer(IdentityFifty.plugin, Runnable {
            val player = p.player?:return@Runnable
            val survivors = (IdentityFifty.identityFiftyTask?.aliveSurvivors()?:return@Runnable)
            val sumHealth = survivors.sumOf {
                val health = IdentityFifty.survivors[it]!!.getHealth()
                if (health == 0) 3 else health
            }
            val healthDiff = survivors.size * 5 - sumHealth

            player.walkSpeed -= previousDiff * 0.005f
            player.walkSpeed += healthDiff * 0.005f

            previousDiff = healthDiff

        },5,5))

        p.inventory.addItem(passiveItem, killFindSkillItem)
    }

    override fun onSurvivorHeal(healPlayer: Player, healedPlayer: Player, p: Player) {
        p.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS,100,1))
    }

    override fun onSurvivorHelp(helper: Player, gotHelpPlayer: Player, p: Player) {
        p.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS,100,1))
    }

    override fun onFinishedAttack(attackPlayer: Player, result: Int, p: Player) {
        if (result > 0){
            damageFlag = true
        }
    }

    override fun parameters(data: HunterData): HunterData {
        data.hunterClass = this
        return data
    }

    override fun info(): ArrayList<ItemStack> {
        val passiveItem = SItem(Material.STICK).setDisplayName(translate("hunter_passive")).setCustomModelData(1)
            .addLore(translate("serialkiller_passive_lore_1"))
            .addLore(translate("serialkiller_passive_lore_2"))
            .build()

        val killFindSkill = SItem(Material.STICK).setDisplayName(translate("kill_find")).setCustomModelData(27)
            .addLore(translate("kill_find_lore_1"))
            .addLore(translate("kill_find_lore_2"))
            .addLore(translate("kill_find_lore_3"))
            .build()

        return arrayListOf(passiveItem,killFindSkill)
    }

}
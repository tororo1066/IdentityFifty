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
import org.inventivetalent.glow.GlowAPI
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.data.HunterData
import tororo1066.identityfifty.enumClass.AllowAction
import tororo1066.tororopluginapi.lang.SLang.Companion.sendTranslateMsg
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sItem.SItem
import tororo1066.tororopluginapi.utils.toPlayer
import java.util.UUID
import java.util.function.Consumer
import kotlin.math.floor

class SerialKiller: AbstractHunter("serialkiller") {

    private var preventDiff = 0
    private var damageFlag = false

    override fun onStart(p: Player) {

        val passiveItem = SItem(Material.STICK).setDisplayName(translate("hunter_passive")).setCustomModelData(1)
            .addLore(translate("serialkiller_passive_lore_1"))
            .addLore(translate("serialkiller_passive_lore_2"))

        val killFindSkill = SItem(Material.STICK).setDisplayName(translate("kill_find")).setCustomModelData(27)
            .addLore(translate("kill_find_lore_1"))
            .addLore(translate("kill_find_lore_2"))
            .addLore(translate("kill_find_lore_3"))

        val killFindSkillItem = IdentityFifty.interactManager.createSInteractItem(killFindSkill).setInteractEvent { _, _ ->
            p.playSound(p.location, Sound.ENTITY_WITHER_AMBIENT, 1f, 1f)
            p.playSound(p.location, Sound.ENTITY_BLAZE_SHOOT, 1f, 1f)
            IdentityFifty.broadcastSpectators(translate("spec_kill_find_used",p.name),
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
            bossBar.addPlayer(p)
            var timer = 600
            Bukkit.getScheduler().runTaskTimer(IdentityFifty.plugin, Consumer {
                if (IdentityFifty.identityFiftyTask == null){
                    it.cancel()
                    return@Consumer
                }
                if (timer <= 0){
                    if (!damageFlag){
                        val data = IdentityFifty.hunters[p.uniqueId]!!
                        data.glowManager.glow(IdentityFifty.survivors.mapNotNull { map -> map.key.toPlayer() }.toMutableList(), GlowAPI.Color.RED, 200)
                        p.addPotionEffect(PotionEffect(PotionEffectType.SLOW,200,1))
                        p.sendTranslateMsg("kill_find_failed")
                        IdentityFifty.broadcastSpectators(translate("spec_kill_find_failed",p.name),
                            AllowAction.RECEIVE_HUNTERS_ACTION)
                        p.playSound(p.location, Sound.BLOCK_BEACON_DEACTIVATE, 1f, 1f)
                    }

                    bossBar.removePlayer(p)

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
            val survivors = (IdentityFifty.identityFiftyTask?.aliveSurvivors()?:return@Runnable)
            val sumHealth = survivors.sumOf {
                IdentityFifty.survivors[it]!!.getHealth()
            }
            val healthDiff = survivors.size * 5 - sumHealth

            p.walkSpeed -= preventDiff * 0.005f
            p.walkSpeed += healthDiff * 0.005f

            preventDiff = healthDiff

        },5,5))

        p.inventory.addItem(passiveItem, killFindSkillItem)
    }

    override fun onSurvivorHeal(healPlayer: Player, healedPlayer: Player, p: Player) {
        val data = IdentityFifty.hunters[p.uniqueId]!!
        data.glowManager.glow(IdentityFifty.survivors.mapNotNull { it.key.toPlayer() }.toMutableList(), GlowAPI.Color.RED, 100)
    }

    override fun onSurvivorHelp(helper: Player, gotHelpPlayer: Player, p: Player) {
        val data = IdentityFifty.hunters[p.uniqueId]!!
        data.glowManager.glow(IdentityFifty.survivors.mapNotNull { it.key.toPlayer() }.toMutableList(), GlowAPI.Color.RED, 100)
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

        val killFindSkill = SItem(Material.STICK).setDisplayName(translate("kill_find")).setCustomModelData(999)
            .addLore(translate("kill_find_lore_1"))
            .addLore(translate("kill_find_lore_2"))
            .addLore(translate("kill_find_lore_3"))

        return arrayListOf(passiveItem,killFindSkill)
    }


}
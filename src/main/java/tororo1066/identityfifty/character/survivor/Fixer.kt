package tororo1066.identityfifty.character.survivor

import org.bukkit.*
import org.bukkit.attribute.AttributeModifier
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Stairs
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitRunnable
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.IdentityFiftyTask
import tororo1066.identityfifty.data.SurvivorData
import tororo1066.identityfifty.enumClass.AllowAction
import tororo1066.tororopluginapi.lang.SLang.Companion.sendTranslateMsg
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sItem.SItem
import tororo1066.tororopluginapi.utils.addItem
import java.util.function.Consumer

class Fixer: AbstractSurvivor("fixer") {

    private val fixPlateCoolDown = 2400
    private val fixTime = 80
    private val fixLengthMultiplier = 20

    private val passiveItem = SItem(Material.STICK).setDisplayName(translate("passive")).setCustomModelData(8)
        .addLore(translate("fixer_passive_lore_1"))
        .addLore(translate("fixer_passive_lore_2"))

    private val fixPlateItem = SItem(Material.STICK).setDisplayName(translate("fix_plate")).setCustomModelData(29)
        .addLore(translate("fix_plate_lore_1"))
        .addLore(translate("fix_plate_lore_2"))
        .addLore(translate("fix_plate_lore_3"))
        .addLore(translate("fix_plate_lore_4", fixPlateCoolDown / 20))

    private val slowTasks = ArrayList<BukkitRunnable>()

    override fun onStart(p: Player) {
        super.onStart(p)

        val fixPlateSkill = IdentityFifty.interactManager.createSInteractItem(fixPlateItem, true).setInteractEvent { e, item ->
            val player = e.player
            val task = IdentityFifty.identityFiftyTask?:return@setInteractEvent false
            val plate = player.location.getNearbyEntitiesByType(ArmorStand::class.java, 3.0)
                .filter {
                    it.persistentDataContainer.has(
                        NamespacedKey(IdentityFifty.plugin, "UsedPlate"),
                        PersistentDataType.INTEGER_ARRAY
                    )
                }.minByOrNull { it.location.distance(player.location) }

            if (plate == null) {
                player.sendTranslateMsg("fix_plate_no_plate")
                return@setInteractEvent false
            }
            val plateLoc = plate.persistentDataContainer.get(
                NamespacedKey(IdentityFifty.plugin, "UsedPlate"),
                PersistentDataType.INTEGER_ARRAY
            ) ?: return@setInteractEvent false
            val location = Location(
                task.map.world,
                plateLoc[0].toDouble(),
                plateLoc[1].toDouble(),
                plateLoc[2].toDouble()
            )
            val plateData = task.map.woodPlates[location]?:return@setInteractEvent false
            val length = plateData.length
            var action = (fixTime + (length * fixLengthMultiplier))
            val actionInit = action
            val bossBar = Bukkit.createBossBar(translate("fix_plate_bossbar"), BarColor.BLUE, BarStyle.SOLID)
            bossBar.progress = 0.0
            bossBar.addPlayer(player)
            val slow = IdentityFifty.speedModifier(player, -0.7, 999999, AttributeModifier.Operation.MULTIPLY_SCALAR_1)
            IdentityFifty.broadcastSpectators(
                translate("spec_fix_plate_used", player.name),
                AllowAction.RECEIVE_SURVIVORS_ACTION
            )
            Bukkit.getScheduler().runTaskTimer(IdentityFifty.plugin, Consumer {

                if (player.location.distance(plate.location) > 3.0) {
                    bossBar.removeAll()
                    slow.cancel()
                    item.setInteractCoolDown(0)
                    IdentityFifty.broadcastSpectators(
                        translate("spec_fix_plate_cancel", player.name),
                        AllowAction.RECEIVE_SURVIVORS_ACTION
                    )
                    it.cancel()
                    return@Consumer
                }

                if (action <= 0) {
                    var block = plateData.loc.block
                    (0 until length).forEach { _ ->
                        block.type = Material.AIR
                        block = block.getRelative(plateData.face)
                    }

                    var upBlock = plateData.loc.block
                    (0 until plateData.length).forEach { _ ->
                        upBlock.type = Material.OAK_STAIRS
                        val blockData = upBlock.blockData as Stairs
                        blockData.shape = Stairs.Shape.STRAIGHT
                        blockData.half = Bisected.Half.TOP
                        blockData.facing = IdentityFiftyTask.changeBlockFace(plateData.face)
                        upBlock.blockData = blockData

                        upBlock = upBlock.getRelative(BlockFace.UP)
                    }

                    plate.persistentDataContainer.set(
                        NamespacedKey(IdentityFifty.plugin, "PlateLoc"),
                        PersistentDataType.INTEGER_ARRAY,
                        plateLoc
                    )
                    plate.persistentDataContainer.remove(NamespacedKey(IdentityFifty.plugin, "UsedPlate"))
                    player.world.playSound(player.location, Sound.BLOCK_ANVIL_USE, 2f, 1f)
                    bossBar.removeAll()
                    slow.cancel()
                    if (slowTasks.size < 5) {
                        slowTasks.add(IdentityFifty.speedModifier(player, -0.005, 9999999, AttributeModifier.Operation.ADD_NUMBER))
                    }
                    IdentityFifty.broadcastSpectators(
                        translate("spec_fix_plate_success", player.name),
                        AllowAction.RECEIVE_SURVIVORS_ACTION
                    )
                    it.cancel()
                    return@Consumer
                }
                action--
                bossBar.progress = 1.0 - action / actionInit.toDouble()
            }, 0, 1)

            return@setInteractEvent true
        }.setInitialCoolDown(fixPlateCoolDown)

        p.inventory.addItem(passiveItem, fixPlateSkill)
    }

    override fun parameters(data: SurvivorData): SurvivorData {
        data.survivorClass = this
        data.healTick = 300
        return data
    }

    override fun onHitWoodPlate(
        hitPlayer: Player,
        loc: Location,
        blindTime: Int,
        slowTime: Int,
        p: Player
    ): Pair<Int, Int> {
        return blindTime + 40 to slowTime
    }

    override fun onEnd(p: Player) {
        slowTasks.forEach { it.cancel() }
        slowTasks.clear()
    }

    override fun info(): ArrayList<ItemStack> {
        return arrayListOf(passiveItem.build(), fixPlateItem.build())
    }

}
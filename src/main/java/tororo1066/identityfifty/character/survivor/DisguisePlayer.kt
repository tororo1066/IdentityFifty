package tororo1066.identityfifty.character.survivor

import org.bukkit.Bukkit
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
import java.util.UUID

class DisguisePlayer: AbstractSurvivor("disguise") {

    override fun onStart(p: Player) {
        super.onStart(p)
        val passiveItem = SItem(Material.STICK).setDisplayName(translate("passive")).setCustomModelData(8)
            .addLore(translate("disguise_passive_lore_1"))
            .addLore(translate("disguise_passive_lore_2"))

        val disguiseSkill = SItem(Material.STICK).setDisplayName(translate("disguise_skill")).setCustomModelData(10)
            .addLore(translate("disguise_skill_lore_1"))
            .addLore(translate("disguise_skill_lore_2"))
            .addLore(translate("disguise_skill_lore_3"))

        val disguiseSkillItem = IdentityFifty.interactManager.createSInteractItem(disguiseSkill,true).setInteractEvent { _, _ ->
            val target = p.getTargetEntity(4)?:return@setInteractEvent false
            if (target !is Player)return@setInteractEvent false
            if (!IdentityFifty.survivors.containsKey(target.uniqueId))return@setInteractEvent false
            val data = IdentityFifty.survivors[p.uniqueId]!!
            data.skinModifier.disguise(target)
            p.sendTranslateMsg("disguise_skill_start",target.name)
            IdentityFifty.broadcastSpectators(translate("spec_disguise_skill_used",p.name,target.name),
                AllowAction.RECEIVE_SURVIVORS_ACTION)
            p.playSound(p.location, Sound.ENTITY_ENDER_DRAGON_HURT, 1f, 1f)
            Bukkit.getScheduler().runTaskLater(IdentityFifty.plugin, Runnable {
                if (data.skinModifier.isDisguise()){
                    data.skinModifier.unDisguise()
                    p.sendTranslateMsg("disguise_skill_end")
                    IdentityFifty.broadcastSpectators(translate("spec_disguise_skill_end",p.name),
                        AllowAction.RECEIVE_SURVIVORS_ACTION)
                    p.playSound(p.location, Sound.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, 1f, 1f)
                }
            },700)
            return@setInteractEvent true
        }.setInitialCoolDown(1000)

        p.inventory.addItem(passiveItem)
        p.inventory.addItem(disguiseSkillItem)

    }

    override fun parameters(data: SurvivorData): SurvivorData {
        data.survivorClass = this
        return data
    }

    override fun onDamage(damage: Int, toHealth: Int, damager: Player, p: Player): Pair<Boolean, Int> {
        val data = IdentityFifty.survivors[p.uniqueId]!!
        if (data.skinModifier.isDisguise()){
            data.skinModifier.unDisguise()
            IdentityFifty.stunEffect(damager,20,60,StunState.OTHER)
            p.sendTranslateMsg("disguise_skill_end")
            IdentityFifty.broadcastSpectators(translate("spec_disguise_skill_end",p.name),
                AllowAction.RECEIVE_SURVIVORS_ACTION)
            return Pair(false,0)
        }
        return super.onDamage(damage, toHealth, damager, p)
    }

    override fun onHelp(helpedPlayer: Player, p: Player) {
        val helpedPlayerData = IdentityFifty.survivors[helpedPlayer.uniqueId]!!
        val uuid = UUID.randomUUID()
        helpedPlayerData.footprintsModify += uuid to 0.0
        helpedPlayer.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY,100,1,true,false))
        Bukkit.getScheduler().runTaskLater(IdentityFifty.plugin, Runnable {
            helpedPlayerData.footprintsModify -= uuid
        }, 100)
    }

    override fun info(): ArrayList<ItemStack> {
        val passiveItem = SItem(Material.STICK).setDisplayName(translate("passive")).setCustomModelData(8)
            .addLore(translate("disguise_passive_lore_1"))
            .addLore(translate("disguise_passive_lore_2"))

        val disguiseSkill = SItem(Material.STICK).setDisplayName(translate("disguise_skill")).setCustomModelData(10)
            .addLore(translate("disguise_skill_lore_1"))
            .addLore(translate("disguise_skill_lore_2"))
            .addLore(translate("disguise_skill_lore_3"))

        return arrayListOf(passiveItem,disguiseSkill)
    }
}
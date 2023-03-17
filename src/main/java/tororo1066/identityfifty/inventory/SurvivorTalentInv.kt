package tororo1066.identityfifty.inventory

import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.data.SurvivorData
import tororo1066.identityfifty.talent.survivor.AbstractSurvivorTalent
import tororo1066.identityfifty.talent.survivor.HealSpeedUp
import tororo1066.identityfifty.talent.survivor.TalentPlane
import tororo1066.tororopluginapi.SInput
import tororo1066.tororopluginapi.defaultMenus.LargeSInventory
import tororo1066.tororopluginapi.lang.SLang.Companion.sendTranslateMsg
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sInventory.SInventory
import tororo1066.tororopluginapi.sInventory.SInventoryItem
import tororo1066.tororopluginapi.sItem.SItem

class SurvivorTalentInv(val data: SurvivorData) {

    abstract inner class AbstractSurvivorTalentInv: SInventory(IdentityFifty.plugin, translate("survivor_talent"),6) {
        fun glass() = SInventoryItem(Material.ORANGE_STAINED_GLASS_PANE).setDisplayName(" ").setCanClick(false)
        fun roadGlass() = SInventoryItem(Material.BLACK_STAINED_GLASS_PANE).setDisplayName(" ").setCanClick(false)
        private fun costHead(p: Player) = createInputItem(SItem(Material.PLAYER_HEAD).setDisplayName(translate("cost_head",p.name))
            .addLore(translate("cost_head_point_lore",data.talentCost.toString()))
            .addLore(translate("cost_head_preset_lore"))
            .addLore(translate("cost_head_create_preset_lore"))
            .setSkullOwner(p.uniqueId),String::class.java,"/<保存名>", ClickType.SHIFT_LEFT) { str, _ ->
            if (str.length > 50){
                p.sendTranslateMsg("too_long_name")
                return@createInputItem
            }

            if (IdentityFifty.talentSQL.dumpTalentName(p.uniqueId,"survivor",str)){
                p.sendTranslateMsg("dump_name")
                return@createInputItem
            }

            IdentityFifty.talentSQL.insertSurvivorTalent(p,str,data.talentClasses.values.toList())
            p.sendTranslateMsg("saved_talent")
        }.setClickEvent {
            if (it.click != ClickType.LEFT)return@setClickEvent
            val inv = object : LargeSInventory(IdentityFifty.plugin,translate("talent_presets")) {
                override fun renderMenu(p: Player): Boolean {
                    val presets = IdentityFifty.talentSQL.getSurvivorTalents(p.uniqueId)
                    val items = arrayListOf<SInventoryItem>()
                    presets.forEach { pair ->
                        items.add(SInventoryItem(Material.DIAMOND_BLOCK)
                            .setDisplayName(pair.first)
                            .addLore(translate("talent_remove")).setCanClick(false).setClickEvent second@ { e ->
                                if (e.click == ClickType.LEFT){
                                    val costs = pair.second.sumOf { sum -> sum.unlockCost }
                                    val defaultCosts = SurvivorData().talentCost
                                    if (defaultCosts < costs){
                                        p.sendTranslateMsg("not_enough_talent_point")
                                        return@second
                                    }

                                    data.talentClasses.clear()
                                    data.talentCost = defaultCosts - costs
                                    pair.second.forEach { talent ->
                                        data.talentClasses[talent.javaClass] = talent
                                    }
                                    p.closeInventory()
                                }

                                if (e.click == ClickType.SHIFT_LEFT){
                                    IdentityFifty.talentSQL.removeTalent(p.uniqueId,"survivor",pair.first)
                                    allRenderMenu(p)
                                }
                            })
                    }
                    setResourceItems(items)
                    return true
                }
            }

            this.moveChildInventory(inv,p)
        }

        fun talentItem(talent: AbstractSurvivorTalent): SInventoryItem {
            return SInventoryItem(if (data.talentClasses.containsKey(talent.javaClass)) Material.LIME_STAINED_GLASS_PANE else Material.RED_STAINED_GLASS_PANE)
                .setDisplayName(translate(talent.name))
                .setLore(talent.lore().map { translate(it) })
                .addLore(" ", if (data.talentClasses.containsKey(talent.javaClass)) translate("unlocked") else translate("locked"),
                    translate("need_point",talent.unlockCost.toString()))
                .setCanClick(false)
                .setClickEvent {
                    if (data.talentClasses.containsKey(talent.javaClass)){
                        removeTalent(talent)
                    }else{
                        if (talent.parent != null && !data.talentClasses.containsKey(talent.parent!!)){
                            it.whoClicked.sendTranslateMsg("locked_parent_talent")
                            return@setClickEvent
                        }
                        if (data.talentCost < talent.unlockCost){
                            it.whoClicked.sendTranslateMsg("not_enough_talent_point")
                            return@setClickEvent
                        }
                        data.talentCost -= talent.unlockCost
                        data.talentClasses[talent.javaClass] = talent
                        (it.whoClicked as Player).playSound(it.whoClicked.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP,1f,1f)
                    }

                    allRenderMenu(it.whoClicked as Player)
                }.uiSound()
        }

        private fun removeTalent(talent: AbstractSurvivorTalent){
            val children = data.talentClasses.filter { it.value.parent == talent.javaClass }
            if (children.isNotEmpty()){
                children.forEach {
                    removeTalent(it.value)
                }
            }

            data.talentClasses.remove(talent.javaClass)
            data.talentCost += talent.unlockCost
        }

        override fun renderMenu(p: Player): Boolean {
            fillItem(SInventoryItem(Material.GLASS_PANE).setDisplayName(" ").setCanClick(false))
            setItem(49,costHead(p))
            return true
        }
    }


    inner class Center: AbstractSurvivorTalentInv() {
        override fun renderMenu(p: Player): Boolean {
            super.renderMenu(p)

            setItems(45..46,BannerItems.left().setClickEvent {
                Left().open(p)
            })
            setItems(52..53,BannerItems.right().setClickEvent {
                Right().open(p)
            })
            setItem(48,BannerItems.up().setClickEvent {
                Up().open(p)
            })
            setItem(50,BannerItems.down().setClickEvent {
                Down().open(p)
            })
            setItems(listOf(47,51), glass())

            setItems(listOf(13,19,21,23,25,31),roadGlass())

            setItem(22,talentItem(TalentPlane()))
            setItem(20,talentItem(HealSpeedUp()))

            return true
        }
    }

    inner class Left: AbstractSurvivorTalentInv() {
        override fun renderMenu(p: Player): Boolean {
            super.renderMenu(p)

            setItems(52..53,BannerItems.right().setClickEvent {
                Center().open(p)
            })
            setItems(listOf(45,46,47,48,50,51), glass())
            setItems(listOf(24,26),roadGlass())

            return true
        }
    }

    inner class Right: AbstractSurvivorTalentInv() {
        override fun renderMenu(p: Player): Boolean {
            super.renderMenu(p)

            setItems(45..46,BannerItems.left().setClickEvent {
                Center().open(p)
            })
            setItems(listOf(47,48,50,51,52,53), glass())
            setItems(listOf(18,20),roadGlass())

            return true
        }
    }

    inner class Up: AbstractSurvivorTalentInv() {
        override fun renderMenu(p: Player): Boolean {
            super.renderMenu(p)

            setItem(48,BannerItems.up().setClickEvent {
                Up2().open(p)
            })
            setItem(50,BannerItems.down().setClickEvent {
                Center().open(p)
            })
            setItems(listOf(45,46,47,51,52,53), glass())
            setItems(listOf(4,22,40),roadGlass())

            return true
        }
    }

    inner class Up2: AbstractSurvivorTalentInv() {
        override fun renderMenu(p: Player): Boolean {
            super.renderMenu(p)

            setItem(50,BannerItems.down().setClickEvent {
                Up().open(p)
            })
            setItems(listOf(45,46,47,48,51,52,53), glass())

            return true
        }
    }

    inner class Down: AbstractSurvivorTalentInv() {
        override fun renderMenu(p: Player): Boolean {
            super.renderMenu(p)

            setItem(48,BannerItems.up().setClickEvent {
                Center().open(p)
            })
            setItem(50,BannerItems.down().setClickEvent {
                Down2().open(p)
            })
            setItems(listOf(45,46,47,51,52,53), glass())
            setItems(listOf(4,22,40),roadGlass())

            return true
        }
    }

    inner class Down2: AbstractSurvivorTalentInv() {
        override fun renderMenu(p: Player): Boolean {
            super.renderMenu(p)

            setItem(48,BannerItems.up().setClickEvent {
                Down().open(p)
            })
            setItems(listOf(45,46,47,50,51,52,53), glass())

            return true
        }
    }
}
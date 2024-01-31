package tororo1066.identityfifty.inventory

import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import tororo1066.identityfifty.IdentityFifty
import tororo1066.identityfifty.data.HunterData
import tororo1066.identityfifty.talent.hunter.*
import tororo1066.tororopluginapi.defaultMenus.LargeSInventory
import tororo1066.tororopluginapi.lang.SLang.Companion.sendTranslateMsg
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sInventory.SInventory
import tororo1066.tororopluginapi.sInventory.SInventoryItem
import tororo1066.tororopluginapi.sItem.SItem

class HunterTalentInv(val data: HunterData) {

    abstract inner class AbstractHunterTalentInv: SInventory(IdentityFifty.plugin,translate("hunter_talent"),6) {
        fun glass() = SInventoryItem(Material.ORANGE_STAINED_GLASS_PANE).setDisplayName(" ").setCanClick(false)
        fun roadGlass() = SInventoryItem(Material.BLACK_STAINED_GLASS_PANE).setDisplayName(" ").setCanClick(false)
        private fun costHead(p: Player) = createInputItem(SItem(Material.PLAYER_HEAD).setDisplayName(translate("cost_head",p.name))
            .addLore(translate("cost_head_point_lore",data.talentCost.toString()))
            .addLore(translate("cost_head_preset_lore"))
            .addLore(translate("cost_head_create_preset_lore"))
            .setSkullOwner(p.uniqueId),String::class.java,"/<保存名>", clickType = listOf(ClickType.SHIFT_LEFT)) { str, _ ->
            if (str.length > 50){
                p.sendTranslateMsg("too_long_name")
                return@createInputItem
            }

            if (IdentityFifty.talentSQL.dumpTalentName(p.uniqueId,"hunter",str)){
                p.sendTranslateMsg("dump_name")
                return@createInputItem
            }

            IdentityFifty.talentSQL.insertHunterTalent(p,str,data.talentClasses.values.toList())
            p.sendTranslateMsg("saved_talent")
        }.setClickEvent {
            if (it.click != ClickType.LEFT)return@setClickEvent
            val inv = object : LargeSInventory(IdentityFifty.plugin,translate("talent_presets")) {
                override fun renderMenu(p: Player): Boolean {
                    val presets = IdentityFifty.talentSQL.getHunterTalents(p.uniqueId)
                    val items = arrayListOf<SInventoryItem>()
                    presets.forEach { pair ->
                        items.add(SInventoryItem(Material.REDSTONE_BLOCK)
                            .setDisplayName(pair.first)
                            .addLore(translate("talent_remove")).setCanClick(false).setClickEvent second@ { e ->
                                if (e.click == ClickType.LEFT){
                                    val costs = pair.second.sumOf { sum -> sum.unlockCost }
                                    val defaultCosts = HunterData().talentCost
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
                                    IdentityFifty.talentSQL.removeTalent(p.uniqueId,"hunter",pair.first)
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

        fun talentItem(talent: AbstractHunterTalent): SInventoryItem {
            return SInventoryItem(if (data.talentClasses.containsKey(talent.javaClass)) Material.LIME_STAINED_GLASS_PANE else Material.RED_STAINED_GLASS_PANE)
                .setDisplayName(translate(talent.name))
                .addLore(talent.lore().map { translate(it) })
                .addLore(" ", if (data.talentClasses.containsKey(talent.javaClass)) translate("unlocked") else translate("locked"),
                    translate("need_point",talent.unlockCost.toString()))
                .setCanClick(false)
                .setClickEvent {
                    if (data.talentClasses.containsKey(talent.javaClass)){
                        removeTalent(talent)
                    }else{
                        if (talent.parent != null && !data.talentClasses.containsKey(talent.parent)){
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

        private fun removeTalent(talent: AbstractHunterTalent){
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


    inner class Center: AbstractHunterTalentInv() {
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

            setItems(listOf(5,7,8,11,13,19,21,23,25,31,33,36,37,39),roadGlass())

            setItem(4,talentItem(HelpSpeedDown()))
            setItem(6,talentItem(PlateGlow()))
            setItem(22,talentItem(TalentPlane()))
            setItem(18,talentItem(EndGameSpeedUp()))
            setItem(2,talentItem(RemainTimeDown()))
            setItem(20,talentItem(LowHitPlate()))
            setItem(24,talentItem(AirSwingDown()))
            setItem(42,talentItem(GateOpenHunterBuff()))
            setItem(26,talentItem(FirstGameSpeedUp()))
            setItem(40,talentItem(HealSpeedDown()))
            setItem(38,talentItem(HighFootPrints()))

            return true
        }
    }

    inner class Left: AbstractHunterTalentInv() {
        override fun renderMenu(p: Player): Boolean {
            super.renderMenu(p)

            setItems(52..53,BannerItems.right().setClickEvent {
                Center().open(p)
            })
            setItems(listOf(45,46,47,48,50,51), glass())
            setItems(listOf(26),roadGlass())
            setItem(25,talentItem(SkillDelayGenerator()))
            setItem(44,talentItem(Menace()))
            return true
        }
    }

    inner class Right: AbstractHunterTalentInv() {
        override fun renderMenu(p: Player): Boolean {
            super.renderMenu(p)

            setItems(45..46,BannerItems.left().setClickEvent {
                Center().open(p)
            })
            setItems(listOf(47,48,50,51,52,53), glass())
            setItems(listOf(18),roadGlass())
            setItem(19,talentItem(SkillTeleport()))
            setItem(0,talentItem(StartGlow()))
            return true
        }
    }

    inner class Up: AbstractHunterTalentInv() {
        override fun renderMenu(p: Player): Boolean {
            super.renderMenu(p)

            setItem(50,BannerItems.down().setClickEvent {
                Center().open(p)
            })
            setItems(listOf(45,46,47,48,51,52,53), glass())
            setItems(listOf(22,29,38,40),roadGlass())
            setItem(20,talentItem(SurvivorHealedStop()))
            setItem(31,talentItem(SurvivorJailedSpeedUp()))
            setItem(13,talentItem(HelpGlow()))
            return true
        }
    }

    inner class Down: AbstractHunterTalentInv() {
        override fun renderMenu(p: Player): Boolean {
            super.renderMenu(p)

            setItem(48,BannerItems.up().setClickEvent {
                Center().open(p)
            })
            setItems(listOf(45,46,47,50,51,52,53), glass())
            setItems(listOf(4,6,15,22),roadGlass())
            setItem(24,talentItem(FinishGateBuff()))
            setItem(13,talentItem(StunTimeDown()))
            setItem(31,talentItem(NoOneUp()))
            return true
        }
    }
}
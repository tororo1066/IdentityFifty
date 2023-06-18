package tororo1066.identityfifty.inventory

import org.bukkit.Material
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sInventory.SInventoryItem

object BannerItems {

    fun left() = SInventoryItem(Material.STICK)
        .setDisplayName(translate("left"))
        .setCustomModelData(22)
        .setCanClick(false)
        .uiSound()

    fun right() = SInventoryItem(Material.STICK)
        .setDisplayName(translate("right"))
        .setCustomModelData(23)
        .setCanClick(false)
        .uiSound()

    fun up() = SInventoryItem(Material.STICK)
        .setDisplayName(translate("up"))
        .setCustomModelData(24)
        .setCanClick(false)
        .uiSound()

    fun down() = SInventoryItem(Material.STICK)
        .setDisplayName(translate("down"))
        .setCustomModelData(25)
        .setCanClick(false)
        .uiSound()

//    fun left() = SItem(Material.BLACK_BANNER)
//        .addPattern(Pattern(DyeColor.WHITE, PatternType.HALF_VERTICAL_MIRROR))
//        .addPattern(Pattern(DyeColor.BLACK, PatternType.STRIPE_MIDDLE))
//        .addPattern(Pattern(DyeColor.WHITE, PatternType.STRIPE_TOP))
//        .addPattern(Pattern(DyeColor.WHITE, PatternType.STRIPE_BOTTOM))
//        .addPattern(Pattern(DyeColor.WHITE, PatternType.CURLY_BORDER))
//        .setDisplayName(translate("left"))
//        .apply { editMeta { it.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS) } }.toSInventoryItem()
//        .setCanClick(false)
//        .uiSound()
//
//    fun right() = SItem(Material.BLACK_BANNER)
//        .addPattern(Pattern(DyeColor.WHITE, PatternType.HALF_VERTICAL))
//        .addPattern(Pattern(DyeColor.BLACK, PatternType.STRIPE_MIDDLE))
//        .addPattern(Pattern(DyeColor.WHITE, PatternType.STRIPE_TOP))
//        .addPattern(Pattern(DyeColor.WHITE, PatternType.STRIPE_BOTTOM))
//        .addPattern(Pattern(DyeColor.WHITE, PatternType.CURLY_BORDER))
//        .setDisplayName(translate("right"))
//        .apply { editMeta { it.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS) } }.toSInventoryItem()
//        .setCanClick(false)
//        .uiSound()
//
//    fun up() = SItem(Material.WHITE_BANNER)
//        .addPattern(Pattern(DyeColor.BLACK, PatternType.STRIPE_TOP))
//        .addPattern(Pattern(DyeColor.WHITE, PatternType.CURLY_BORDER))
//        .addPattern(Pattern(DyeColor.BLACK, PatternType.STRIPE_CENTER))
//        .addPattern(Pattern(DyeColor.WHITE, PatternType.CURLY_BORDER))
//        .addPattern(Pattern(DyeColor.WHITE, PatternType.STRIPE_BOTTOM))
//        .setDisplayName(translate("up"))
//        .apply { editMeta { it.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS) } }.toSInventoryItem()
//        .setCanClick(false)
//        .uiSound()
//
//    fun down() = SItem(Material.WHITE_BANNER)
//        .addPattern(Pattern(DyeColor.BLACK, PatternType.STRIPE_BOTTOM))
//        .addPattern(Pattern(DyeColor.WHITE, PatternType.CURLY_BORDER))
//        .addPattern(Pattern(DyeColor.BLACK, PatternType.STRIPE_CENTER))
//        .addPattern(Pattern(DyeColor.WHITE, PatternType.CURLY_BORDER))
//        .addPattern(Pattern(DyeColor.WHITE, PatternType.STRIPE_TOP))
//        .setDisplayName(translate("down"))
//        .apply { editMeta { it.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS) } }.toSInventoryItem()
//        .setCanClick(false)
//        .uiSound()

}
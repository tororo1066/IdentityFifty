package tororo1066.identityfifty.inventory

import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.block.banner.Pattern
import org.bukkit.block.banner.PatternType
import org.bukkit.inventory.ItemFlag
import tororo1066.identityfifty.IdentityFifty
import tororo1066.tororopluginapi.defaultMenus.NormalInventory
import tororo1066.tororopluginapi.frombukkit.SBukkit
import tororo1066.tororopluginapi.lang.SLang.Companion.translate
import tororo1066.tororopluginapi.sInventory.SInventoryItem
import tororo1066.tororopluginapi.sItem.SItem

object BannerItems {

    fun left() = SItem(Material.BLACK_BANNER)
        .addPattern(Pattern(DyeColor.WHITE, PatternType.HALF_VERTICAL_MIRROR))
        .addPattern(Pattern(DyeColor.BLACK, PatternType.STRIPE_MIDDLE))
        .addPattern(Pattern(DyeColor.WHITE, PatternType.STRIPE_TOP))
        .addPattern(Pattern(DyeColor.WHITE, PatternType.STRIPE_BOTTOM))
        .addPattern(Pattern(DyeColor.WHITE, PatternType.CURLY_BORDER))
        .setDisplayName(translate("left"))
        .apply { editMeta { it.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS) } }.toSInventoryItem()
        .setCanClick(false)
        .uiSound()

    fun right() = SItem(Material.BLACK_BANNER)
        .addPattern(Pattern(DyeColor.WHITE, PatternType.HALF_VERTICAL))
        .addPattern(Pattern(DyeColor.BLACK, PatternType.STRIPE_MIDDLE))
        .addPattern(Pattern(DyeColor.WHITE, PatternType.STRIPE_TOP))
        .addPattern(Pattern(DyeColor.WHITE, PatternType.STRIPE_BOTTOM))
        .addPattern(Pattern(DyeColor.WHITE, PatternType.CURLY_BORDER))
        .setDisplayName(translate("right"))
        .apply { editMeta { it.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS) } }.toSInventoryItem()
        .setCanClick(false)
        .uiSound()

    fun up() = SItem(Material.WHITE_BANNER)
        .addPattern(Pattern(DyeColor.BLACK, PatternType.STRIPE_TOP))
        .addPattern(Pattern(DyeColor.WHITE, PatternType.CURLY_BORDER))
        .addPattern(Pattern(DyeColor.BLACK, PatternType.STRIPE_CENTER))
        .addPattern(Pattern(DyeColor.WHITE, PatternType.CURLY_BORDER))
        .addPattern(Pattern(DyeColor.WHITE, PatternType.STRIPE_BOTTOM))
        .setDisplayName(translate("up"))
        .apply { editMeta { it.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS) } }.toSInventoryItem()
        .setCanClick(false)
        .uiSound()

    fun down() = SItem(Material.WHITE_BANNER)
        .addPattern(Pattern(DyeColor.BLACK, PatternType.STRIPE_BOTTOM))
        .addPattern(Pattern(DyeColor.WHITE, PatternType.CURLY_BORDER))
        .addPattern(Pattern(DyeColor.BLACK, PatternType.STRIPE_CENTER))
        .addPattern(Pattern(DyeColor.WHITE, PatternType.CURLY_BORDER))
        .addPattern(Pattern(DyeColor.WHITE, PatternType.STRIPE_TOP))
        .setDisplayName(translate("down"))
        .apply { editMeta { it.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS) } }.toSInventoryItem()
        .setCanClick(false)
        .uiSound()

}
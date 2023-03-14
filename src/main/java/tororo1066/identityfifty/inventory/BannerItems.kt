package tororo1066.identityfifty.inventory

import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.block.banner.Pattern
import org.bukkit.block.banner.PatternType
import tororo1066.identityfifty.IdentityFifty
import tororo1066.tororopluginapi.defaultMenus.NormalInventory
import tororo1066.tororopluginapi.frombukkit.SBukkit
import tororo1066.tororopluginapi.sItem.SItem

object BannerItems {

    val left = SItem(Material.BLACK_BANNER)
        .addPattern(Pattern(DyeColor.WHITE, PatternType.STRIPE_LEFT))
        .addPattern(Pattern(DyeColor.WHITE, PatternType.STRIPE_CENTER))
        .addPattern(Pattern(DyeColor.BLACK, PatternType.STRIPE_TOP))
        .addPattern(Pattern(DyeColor.BLACK, PatternType.STRIPE_BOTTOM))
        .addPattern(Pattern(DyeColor.WHITE, PatternType.BORDER))

    val right = SItem(Material.BLACK_BANNER)
        .addPattern(Pattern(DyeColor.WHITE, PatternType.STRIPE_LEFT))
        .addPattern(Pattern(DyeColor.WHITE, PatternType.STRIPE_CENTER))
        .addPattern(Pattern(DyeColor.BLACK, PatternType.STRIPE_TOP))
        .addPattern(Pattern(DyeColor.BLACK, PatternType.STRIPE_BOTTOM))
        .addPattern(Pattern(DyeColor.WHITE, PatternType.BORDER))

    val up = SItem(Material.WHITE_BANNER)
        .addPattern(Pattern(DyeColor.BLACK, PatternType.STRIPE_TOP))
        .addPattern(Pattern(DyeColor.WHITE, PatternType.BORDER))
        .addPattern(Pattern(DyeColor.BLACK, PatternType.STRIPE_CENTER))
        .addPattern(Pattern(DyeColor.WHITE, PatternType.BORDER))
        .addPattern(Pattern(DyeColor.WHITE, PatternType.STRIPE_MIDDLE))


    fun test(): NormalInventory {
        return SBukkit.createSInventory(IdentityFifty.plugin,"6",6).apply {
            setItem(0,left)
            setItem(1,right)
            setItem(2,up)
        }
    }
}
package app.kodex.patches.vlc

import app.kodex.patches.shared.Constants.COMPATIBILITY_VLC
import app.morphe.patcher.patch.resourcePatch

/**
 * Every dark surface VLC can draw, repainted to true black (#000000).
 *
 * VLC's "black" theme is Material dark grey. The greys live in `res/values/colors.xml` across
 * several token layers — VLC's own greys, Material Components dark, the Material dark
 * background/floating surface, and the Material 3 neutral palette that `m3_sys_color_dark_*`
 * references. All of them are flattened here by hex value, which covers the window background,
 * cards, dialogs, bottom sheets, popups, the mini-player, the toolbar/header surface, and the
 * edge-to-edge navigation bar.
 *
 * The `@android:color/system_*_dark` dynamic (Material You) tokens in values-v31/v34 are unused by
 * VLC's Material 2 theme, so they are left alone.
 */
private val DARK_HEX = listOf(
    // VLC surfaces (6-digit)
    "#131313", "#212121", "#2a2a2a", "#323232", "#424242", "#121212", "#011422",
    // Material dark background / floating surface (8-digit)
    "#ff303030", "#ff424242", "#ff212121",
    // Material 3 neutral palette, dark half (8-digit)
    "#ff0f0d13", "#ff141218", "#ff1d1b20", "#ff211f26", "#ff2b2930",
    "#ff322f35", "#ff36343b", "#ff3b383e", "#ff48464c", "#ff605d64",
    "#ff1d1a22", "#ff322f37", "#ff49454f", "#ff605d66",
)

@Suppress("unused")
val amoledBlackThemePatch = resourcePatch(
    name = "AMOLED black theme",
    description = "Repaints every dark surface — background, cards, dialogs, popups, header and the navigation bar — to true black.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_VLC)

    execute {
        val file = get("res/values/colors.xml")
        var text = file.readText()

        for (hex in DARK_HEX) {
            text = text.replace(hex, "#000000")
        }
        // Semi-transparent audio-player grey -> transparent black (keeps its alpha).
        text = text.replace("#ee2a2a2a", "#ee000000")

        file.writeText(text)
    }
}

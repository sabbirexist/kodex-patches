package app.kodex.patches.vlc

import app.kodex.patches.shared.Constants.COMPATIBILITY_VLC
import app.morphe.patcher.patch.resourcePatch

/**
 * Every dark surface VLC can draw, repainted to true black (#000000).
 *
 * Two layers are handled:
 *
 *  1. `res/values/colors.xml` — the grey surfaces (VLC greys, Material Components dark, the
 *     Material dark background/floating surface, and the Material 3 neutral palette) are flattened
 *     to #000000 by hex value.
 *
 *  2. `res/values/styles.xml` — the black theme's toolbar surface is forced black and the Material
 *     elevation overlay is disabled. The overlay otherwise blends white onto `colorSurface` in dark
 *     mode, which is why the header still looked grey (#1e1e1e) even after every colour was black.
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

private val BLACK_THEME_TAG =
    "<style name=\"Theme.VLC.Apearance.Black\" parent=\"@style/Theme.MaterialComponents.NoActionBar\">"

@Suppress("unused")
val amoledBlackThemePatch = resourcePatch(
    name = "AMOLED black theme",
    description = "Repaints every dark surface — background, cards, dialogs, popups, header and the navigation bar — to true black.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_VLC)

    execute {
        // Flatten the grey colour resources.
        val colors = get("res/values/colors.xml")
        var text = colors.readText()
        for (hex in DARK_HEX) {
            text = text.replace(hex, "#000000")
        }
        // Semi-transparent audio-player grey -> transparent black (keeps its alpha).
        text = text.replace("#ee2a2a2a", "#ee000000")
        colors.writeText(text)

        // Force the black theme's surface to true black and disable the elevation overlay.
        val styles = get("res/values/styles.xml")
        val override = BLACK_THEME_TAG + "\n" +
            "        <item name=\"elevationOverlayEnabled\">false</item>"
        styles.writeText(styles.readText().replace(BLACK_THEME_TAG, override))
    }
}

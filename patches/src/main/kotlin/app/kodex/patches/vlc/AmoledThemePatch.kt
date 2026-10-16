package app.kodex.patches.vlc

import app.kodex.patches.shared.Constants.COMPATIBILITY_VLC
import app.morphe.patcher.patch.resourcePatch

/**
 * VLC's "black" theme is dark grey, not AMOLED.
 *
 * In the decompiled APK the black theme resolves through `Theme.VLC.Apearance.Black`
 * (`values/styles.xml` + `values-night/styles.xml`), whose surfaces come from two
 * color resources in `res/values/colors.xml`:
 *
 *   <color name="dark_background">#131313</color>  -> android:windowBackground of the black theme
 *   <color name="grey900">#212121</color>          -> window / surface backgrounds
 *
 * Repainting both to #000000 turns the black theme into true AMOLED.
 */
@Suppress("unused")
val amoledBlackThemePatch = resourcePatch(
    name = "AMOLED black theme",
    description = "Uses a true black (#000000) background for the black theme instead of dark grey.",
    default = true
) {
    compatibleWith(COMPATIBILITY_VLC)

    execute {
        val colorsFile = get("res/values/colors.xml")
        val colors = colorsFile.readText()
            // Black-theme window background (#131313) -> true black.
            .replace("#131313", "#000000")
            // Main dark surface / window background (#212121) -> true black.
            // Safe: the transparent variants (#bb212121, #80212121, ...) do not contain "#212121".
            .replace("#212121", "#000000")

        colorsFile.writeText(colors)
    }
}

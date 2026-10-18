package app.kodex.patches.eyecon

import app.kodex.patches.shared.Constants.COMPATIBILITY_EYECON
import app.morphe.patcher.patch.resourcePatch

/**
 * Eyecon's dark themes use a blue-tinted palette (#19232f, #060f1b, #07111e, ...) rather than
 * grey. This flattens every dark *surface* — the app background, cards, bars, avatar frames and
 * the dark-green variant — to true black (#000000), while leaving text and accent colours alone.
 */
private val DARK_HEX = listOf(
    // Main dark backgrounds (Eyecon's blue-tinted palette)
    "#19232f", "#060f1b", "#07111e", "#000104",
    // Dark-green theme background
    "#062224",
    // Surfaces / bars / avatar frames / active-text highlight
    "#343e53", "#22323c",
)

@Suppress("unused")
val amoledBlackThemePatch = resourcePatch(
    name = "AMOLED black theme",
    description = "Repaints the dark theme's background, cards, bars and dialogs to true black for AMOLED screens.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_EYECON)

    execute {
        for (file in listOf("res/values/colors.xml", "res/values/styles.xml")) {
            val resource = get(file)
            var text = resource.readText()
            for (hex in DARK_HEX) {
                text = text.replace(hex, "#000000")
            }
            resource.writeText(text)
        }
    }
}

package app.kodex.patches.tempmail

import app.kodex.patches.shared.Constants.COMPATIBILITY_TEMPMAIL
import app.morphe.patcher.patch.resourcePatch

/**
 * Temp Mail's dark mode uses the stock AppCompat + Material grey surfaces. This flattens every dark
 * surface — the window background, cards, dialogs and the Material 3 neutral palette — to true
 * black (#000000) for AMOLED screens, while leaving text and accent colours alone.
 */
private val DARK_HEX = listOf(
    // Material 2 / design dark background + surface
    "#121212",
    // AppCompat material dark greys
    "#ff303030", "#ff424242", "#ff212121",
    // Material 3 dark neutral palette (surfaces)
    "#ff0f0d13", "#ff141218", "#ff1d1b20", "#ff211f26",
    "#ff2b2930", "#ff322f35", "#ff36343b", "#ff3b383e",
)

@Suppress("unused")
val amoledBlackThemePatch = resourcePatch(
    name = "AMOLED black theme",
    description = "Makes the dark mode background, cards and dialogs true black for AMOLED screens.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_TEMPMAIL)

    execute {
        val colors = get("res/values/colors.xml")
        var text = colors.readText()
        for (hex in DARK_HEX) {
            text = text.replace(hex, "#000000")
        }
        colors.writeText(text)
    }
}

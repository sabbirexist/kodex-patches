package app.kodex.patches.ridmik

import app.kodex.patches.shared.Constants.COMPATIBILITY_RIDMIK
import app.morphe.patcher.patch.resourcePatch

/**
 * Restyles the keyboard to Gboard's two default palettes:
 *
 *  - Light keys -> Gboard grey (#f1f3f4), pressed #d8dade, label near-black #1f1f1f
 *  - Dark keys  -> Gboard dark grey (#2d2e30)
 *  - Key corner radius bumped to 8dp (Gboard's rounded look) where a shape already exists.
 *
 * This is the achievable "looks like Gboard" part. The draggable toolbar / Material You engine are
 * Gboard features, not resources, so they are out of scope for a theme patch.
 */
@Suppress("unused")
val gboardStyleThemePatch = resourcePatch(
    name = "Gboard-style theme",
    description = "Restyles the keyboard to Gboard's white and dark palettes with rounded 8dp keys.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_RIDMIK)

    execute {
        // Key colours -> Gboard palette.
        val colors = get("res/values/colors.xml")
        var text = colors.readText()
        text = text.replace("<color name=\"blue\">#1976d2</color>", "<color name=\"blue\">#f1f3f4</color>")
        text = text.replace("<color name=\"blueDark\">#0d47a1</color>", "<color name=\"blueDark\">#d8dade</color>")
        text = text.replace("<color name=\"theme_midnight_black_primary\">#111320</color>", "<color name=\"theme_midnight_black_primary\">#2d2e30</color>")
        text = text.replace("<color name=\"theme_rounded_black_primary\">#00223a</color>", "<color name=\"theme_rounded_black_primary\">#2d2e30</color>")
        text = text.replace("<color name=\"key_text_color_lxx_light\">#37474f</color>", "<color name=\"key_text_color_lxx_light\">#1f1f1f</color>")
        colors.writeText(text)

        // Corner radius -> Gboard's rounded 8dp.
        for (file in listOf(
            "res/drawable/key_background_custom_generic.xml",
            "res/drawable/functional_key_background_custom_generic.xml",
        )) {
            val drawable = get(file)
            drawable.writeText(drawable.readText().replace("android:radius=\"4.0dp\"", "android:radius=\"8.0dp\""))
        }
    }
}

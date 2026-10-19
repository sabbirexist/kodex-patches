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
        // LXX theme (the default): light grey -> Gboard light, dark blue-grey -> Gboard dark.
        text = text.replace("<color name=\"key_background_lxx_light\">#eceff1</color>", "<color name=\"key_background_lxx_light\">#f1f3f4</color>")
        text = text.replace("<color name=\"key_background_lxx_dark\">#263238</color>", "<color name=\"key_background_lxx_dark\">#2d2e30</color>")
        text = text.replace("<color name=\"key_background_pressed_lxx_light\">#2637474f</color>", "<color name=\"key_background_pressed_lxx_light\">#d8dade</color>")
        // Blue theme -> Gboard.
        text = text.replace("<color name=\"blue\">#1976d2</color>", "<color name=\"blue\">#f1f3f4</color>")
        text = text.replace("<color name=\"blueDark\">#0d47a1</color>", "<color name=\"blueDark\">#d8dade</color>")
        // Green brand theme -> Gboard (light) / Gboard dark.
        text = text.replace("<color name=\"background_color_in_ridmik_green_theme\">#18807a</color>", "<color name=\"background_color_in_ridmik_green_theme\">#f1f3f4</color>")
        text = text.replace("<color name=\"background_dark_color_in_green_theme\">#20a17f</color>", "<color name=\"background_dark_color_in_green_theme\">#2d2e30</color>")
        text = text.replace("<color name=\"background_light_color_in_green_theme\">#4fba8a</color>", "<color name=\"background_light_color_in_green_theme\">#f1f3f4</color>")
        // Dark themes -> Gboard dark grey.
        text = text.replace("<color name=\"theme_midnight_black_primary\">#111320</color>", "<color name=\"theme_midnight_black_primary\">#2d2e30</color>")
        text = text.replace("<color name=\"theme_rounded_black_primary\">#00223a</color>", "<color name=\"theme_rounded_black_primary\">#2d2e30</color>")
        text = text.replace("<color name=\"key_background_custom_generic\">#434343</color>", "<color name=\"key_background_custom_generic\">#2d2e30</color>")
        // Key label -> near-black (Gboard).
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

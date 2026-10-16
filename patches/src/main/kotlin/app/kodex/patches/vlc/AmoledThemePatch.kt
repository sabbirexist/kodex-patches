package app.kodex.patches.vlc

import app.kodex.patches.shared.Constants.COMPATIBILITY_VLC
import app.morphe.patcher.patch.resourcePatch
import org.w3c.dom.Element

/**
 * Every dark surface VLC draws, repainted to true black (#000000).
 *
 * VLC's "black" theme is Material dark grey. The obvious colours (dark_background, grey900) sit in
 * `res/values/colors.xml`, but dialogs, bottom sheets, popups, the mini-player and the edge-to-edge
 * navigation bar resolve through the Material 3 dark surface tokens
 * (`m3_sys_color_dark_surface*` -> `m3_ref_palette_neutral*`) and the Material Components dark
 * surface (`design_dark_default_color_surface`). All of them are flattened here, so every surface —
 * background, cards, dialogs, popups and the navbar — is pure black.
 */
private val AMOLED_COLORS = setOf(
    // VLC surfaces
    "dark_background", "grey900", "grey875", "grey850", "grey800", "mini_player_dark",
    "onboarding_grey",
    // Material Components dark
    "design_dark_default_color_surface", "design_dark_default_color_background",
    "design_snackbar_background_color",
    // Material dark background / floating surface (dialogs, windows)
    "material_grey_850", "material_grey_800", "material_grey_900",
    // Material 3 dark surfaces (dialogs, sheets, popups, navbar)
    "m3_sys_color_dark_background",
    "m3_sys_color_dark_surface",
    "m3_sys_color_dark_surface_bright",
    "m3_sys_color_dark_surface_dim",
    "m3_sys_color_dark_surface_variant",
    "m3_sys_color_dark_surface_container",
    "m3_sys_color_dark_surface_container_low",
    "m3_sys_color_dark_surface_container_lowest",
    "m3_sys_color_dark_surface_container_high",
    "m3_sys_color_dark_surface_container_highest",
)

@Suppress("unused")
val amoledBlackThemePatch = resourcePatch(
    name = "AMOLED black theme",
    description = "Repaints every dark surface — background, cards, dialogs, popups and the navigation bar — to true black.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_VLC)

    execute {
        document("res/values/colors.xml").use { doc ->
            val colors = doc.getElementsByTagName("color")
            for (i in 0 until colors.length) {
                val el = colors.item(i) as? Element ?: continue
                if (el.getAttribute("name") in AMOLED_COLORS) {
                    el.textContent = "#000000"
                }
            }
        }
    }
}

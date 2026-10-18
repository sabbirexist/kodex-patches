package app.kodex.patches.eyecon

import app.kodex.patches.shared.Constants.COMPATIBILITY_EYECON
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

/**
 * Removes the premium tab from the home bottom bar. `MainActivity.S0()` is the method that decides
 * whether to show the premium `EIB_premium` button; the button is `gone` in the layout by default,
 * so a no-op keeps it hidden while the keypad/spam buttons stay at their normal layout defaults.
 */
@Suppress("unused")
val removePremiumTabPatch = bytecodePatch(
    name = "Remove premium tab",
    description = "Hides the premium tab from the home bottom bar.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_EYECON)

    execute {
        PremiumTabSetupFingerprint.method.addInstructions(0, "return-void")
    }
}

package app.kodex.patches.touchretouch

import app.kodex.patches.shared.Constants.COMPATIBILITY_TOUCHRETOUCH
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

/**
 * Removes the "rate us" popup.
 *
 * `r1/w1.k()` is the single method that builds and shows the custom rate-us dialog. Returning early
 * stops it being shown. Off by default since it is a minor cosmetic change.
 */
@Suppress("unused")
val removeRateUsNagPatch = bytecodePatch(
    name = "Remove rate-us popup",
    description = "Stops the app from showing the 'rate us' prompt.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_TOUCHRETOUCH)

    execute {
        RateUsNagFingerprint.method.addInstructions(0, "return-void")
    }
}

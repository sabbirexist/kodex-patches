package app.kodex.patches.heartrate

import app.kodex.patches.shared.Constants.COMPATIBILITY_HEARTRATE
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

/**
 * Blocks tracking & analytics.
 *
 * `od/a.c()` is the single sink every Firebase Analytics event funnels through, so returning early
 * drops all Firebase event logging. `Adjust.initSdk()` is the Adjust attribution SDK init; returning
 * early means Adjust never starts, so no install-attribution/device data is sent.
 */
@Suppress("unused")
val blockTrackersPatch = bytecodePatch(
    name = "Block tracking & analytics",
    description = "Stops the app from sending data to Firebase and Adjust.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_HEARTRATE)

    execute {
        FirebaseSinkFingerprint.method.addInstructions(0, "return-void")
        AdjustInitFingerprint.method.addInstructions(0, "return-void")
    }
}

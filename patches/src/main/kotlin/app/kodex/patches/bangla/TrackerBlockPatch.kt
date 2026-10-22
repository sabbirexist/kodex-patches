package app.kodex.patches.bangla

import app.kodex.patches.shared.Constants.COMPATIBILITY_BANGLA
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

/**
 * Stubs the Facebook SDK init so AppEvents/tracking never starts. This blocks the main tracker;
 * other SDKs (Firebase Crashlytics, Google Analytics) are left intact.
 */
@Suppress("unused")
val blockTrackersPatch = bytecodePatch(
    name = "Block tracking & analytics",
    description = "Stops the app from sending your data to Facebook.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_BANGLA)

    execute {
        FacebookInitFingerprint.method.addInstructions(0, "return-void")
    }
}

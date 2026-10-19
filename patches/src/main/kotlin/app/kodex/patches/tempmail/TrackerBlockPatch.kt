package app.kodex.patches.tempmail

import app.kodex.patches.shared.Constants.COMPATIBILITY_TEMPMAIL
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

/**
 * Stubs Temp Mail's analytics wrapper so no events reach Firebase Analytics or Crashlytics.
 * `AnalyticsUtils.n()` is the single Firebase logEvent every event funnels through; `f()`/`g()` are
 * the Crashlytics crash reporters.
 */
@Suppress("unused")
val blockTrackersPatch = bytecodePatch(
    name = "Block tracking & analytics",
    description = "Stops the app from sending your data to tracking companies.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_TEMPMAIL)

    execute {
        AnalyticsLogFingerprint.method.addInstructions(0, "return-void")
        CrashReportFingerprint.method.addInstructions(0, "return-void")
        CrashReport2Fingerprint.method.addInstructions(0, "return-void")
    }
}

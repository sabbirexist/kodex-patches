package app.kodex.patches.eyecon

import app.kodex.patches.shared.Constants.COMPATIBILITY_EYECON
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

/**
 * Stubs Eyecon's own analytics wrappers so no events reach the tracking SDKs. `AmplitudeHelper.g()`
 * is the single consent/queue gate for every Amplitude event including Session Replay (screen
 * recording); `SingularHelper.i()` starts the Singular attribution queue; `CrashReportImp.b/f` are
 * the Crashlytics crash reporters.
 */
@Suppress("unused")
val blockTrackersPatch = bytecodePatch(
    name = "Block tracking & analytics",
    description = "Disables Amplitude (including Session Replay screen recording), Singular " +
        "attribution and Crashlytics crash reporting.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_EYECON)

    execute {
        AmplitudeGateFingerprint.method.addInstructions(0, "return-void")
        SingularGateFingerprint.method.addInstructions(0, "return-void")
        CrashReportGateFingerprint.method.addInstructions(0, "return-void")
        FatalCrashReportFingerprint.method.addInstructions(0, "return-void")
    }
}

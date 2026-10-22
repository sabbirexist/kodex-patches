package app.kodex.patches.touchretouch

import app.kodex.patches.shared.Constants.COMPATIBILITY_TOUCHRETOUCH
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

/**
 * Blocks tracking & analytics.
 *
 * `l1/c.b()` is the single dispatcher every analytics event funnels through (the event-builder
 * helpers in `a2/c` all end up here), so returning early disables the entire pipeline — Firebase
 * included. `TRApplication.f()` returns the AppsFlyer API key; returning an empty string makes
 * AppsFlyer initialize with no key so it doesn't track.
 */
@Suppress("unused")
val blockTrackersPatch = bytecodePatch(
    name = "Block tracking & analytics",
    description = "Stops the app from sending analytics to Firebase and AppsFlyer.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_TOUCHRETOUCH)

    execute {
        AnalyticsDispatcherFingerprint.method.addInstructions(0, "return-void")
        AppsFlyerKeyFingerprint.method.addInstructions(
            0,
            """
                const-string v0, ""
                return-object v0
            """,
        )
    }
}

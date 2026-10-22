package app.kodex.patches.heartrate

import app.kodex.patches.shared.Constants.COMPATIBILITY_HEARTRATE
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

/**
 * Removes ads.
 *
 * The app's only in-app purchase is a single "remove ads" SKU. All ads are Google AdMob, loaded
 * through the base loaders under `Tb/` — the interstitial (`Tb/g.j()`) is the full-screen ad shown
 * after measurements. Returning early stops the interstitial from loading. (Banner/rewarded ads use
 * separate loader methods and are not covered by this patch.)
 */
@Suppress("unused")
val removeAdsPatch = bytecodePatch(
    name = "Remove ads",
    description = "Removes the full-screen interstitial ads.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_HEARTRATE)

    execute {
        InterstitialLoadFingerprint.method.addInstructions(0, "return-void")
    }
}

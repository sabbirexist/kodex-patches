package app.kodex.patches.inshare

import app.kodex.patches.shared.Constants.COMPATIBILITY_INSHARE
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val removeAdsPatch = bytecodePatch(
    name = "Remove ads",
    description = "Unlocks the paid Remove Ads feature: disables every ad and hides the Remove Ads purchase icon.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_INSHARE)

    execute {
        // Disable AdMob ad loading (banner, interstitial, native) via the premium gate.
        PremiumFlagFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """
        )

        // Hide the "Remove Ads" purchase icon and the "greatApp" cross-promo button.
        RemoveAdsUiFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """
        )
    }
}

package app.kodex.patches.inshare

import app.kodex.patches.shared.Constants.COMPATIBILITY_INSHARE
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val removeAdsPatch = bytecodePatch(
    name = "Remove ads",
    description = "Unlocks the paid Remove Ads feature so no ads are shown.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_INSHARE)

    execute {
        PremiumFlagFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """
        )
    }
}

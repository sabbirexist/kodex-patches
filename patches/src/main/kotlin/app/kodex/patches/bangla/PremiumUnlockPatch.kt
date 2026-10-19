package app.kodex.patches.bangla

import app.kodex.patches.shared.Constants.COMPATIBILITY_BANGLA
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

/**
 * Forces the central premium check (`billing/e.a()`) to true. This removes the promoted items (ads)
 * and unlocks the premium UI. Server-side features (AI writing assistant, server-validated perks)
 * still require a real subscription.
 */
@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock premium",
    description = "Removes the ads and unlocks the premium screens.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_BANGLA)

    execute {
        PremiumGateFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )
    }
}

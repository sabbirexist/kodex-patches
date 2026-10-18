package app.kodex.patches.eyecon

import app.kodex.patches.shared.Constants.COMPATIBILITY_EYECON
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

/**
 * Eyecon's premium state is decided entirely client-side (SharedPreferences + a cached Play Billing
 * flag). Forcing the three leaf checks to true unlocks everything premium gates: ad removal,
 * themes, premium caller ID/after-call screen, call recording, SMS extras, reverse lookup, ID Plus.
 */
@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock premium",
    description = "Unlocks every premium feature client-side: removes ads, unlocks themes, premium " +
        "caller ID and after-call screen, call recording, SMS extras, reverse lookup and ID Plus.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_EYECON)

    execute {
        // FreePremium.c() -> true (covers the "free/viral premium" timestamp check).
        FreePremiumGateFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )

        // PaidPremium.a(Boolean) -> Boolean.TRUE (covers the cached Play Billing check).
        PaidPremiumGateFingerprint.method.addInstructions(
            0,
            """
                sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                return-object v0
            """,
        )

        // FeaturesHelper.e(int) -> true (covers the a-la-carte "did purchase <feature>" gates).
        FeaturePurchaseGateFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )
    }
}

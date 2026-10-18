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

        // FreePremiumUserActivity.K0() -> show "Lifetime" instead of "Valid until Jan 1, 1970",
        // and hide the "Watch a video", "Invite a friend" and "More Buying Options" rows
        // (which would play ads or open the store).
        FreePremiumValidUntilFingerprint.method.addInstructions(
            0,
            """
                iget-object v0, p0, Lcom/eyecon/global/Billing/Premium/FreePremiumUserActivity;->X:Lcom/eyecon/global/databinding/ActivityFreePremiumUserV2Binding;
                iget-object v0, v0, Lcom/eyecon/global/databinding/ActivityFreePremiumUserV2Binding;->n:Lcom/eyecon/global/Others/Views/CustomTextView;
                const-string v1, "Lifetime"
                invoke-virtual {v0, v1}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V
                const v0, 0x7f0a01b6
                invoke-virtual {p0, v0}, Landroid/app/Activity;->findViewById(I)Landroid/view/View;
                move-result-object v0
                const/16 v1, 0x8
                invoke-virtual {v0, v1}, Landroid/view/View;->setVisibility(I)V
                const v0, 0x7f0a015c
                invoke-virtual {p0, v0}, Landroid/app/Activity;->findViewById(I)Landroid/view/View;
                move-result-object v0
                invoke-virtual {v0, v1}, Landroid/view/View;->setVisibility(I)V
                const v0, 0x7f0a012b
                invoke-virtual {p0, v0}, Landroid/app/Activity;->findViewById(I)Landroid/view/View;
                move-result-object v0
                invoke-virtual {v0, v1}, Landroid/view/View;->setVisibility(I)V
                return-void
            """,
        )
    }
}

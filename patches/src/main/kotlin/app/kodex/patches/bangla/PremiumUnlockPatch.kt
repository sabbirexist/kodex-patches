package app.kodex.patches.bangla

import app.kodex.patches.shared.Constants.COMPATIBILITY_BANGLA
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation

/**
 * Unlocks premium end-to-end.
 *
 * The app has two separate premium surfaces:
 *  1. `billing/e.a()` / `billing/e.b()` — the static "is premium" / "has subscription" booleans.
 *  2. `SubscriptionState.b()` — the subscription the premium tab reads directly from the StateFlow.
 *
 * Patching only (1) left the premium tab showing "Buy Premium", because it renders the subscribed UI
 * based on (2) returning a non-null `Subscription`. So we force all three seams: `e.a()`, `e.b()`,
 * and `SubscriptionState.b()` (which now returns a synthetically-constructed, server-validated,
 * non-expiring subscription).
 *
 * Server-side features (AI writing generation, server-validated perks) still require a real
 * subscription — this unlocks the client-side UI and removes the promoted items (ads).
 */
@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock premium",
    description = "Removes the ads and unlocks the premium screens.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_BANGLA)

    execute {
        // 1. Force the central "is premium" gate.
        PremiumGateFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )

        // 2. Force the "has subscription" boolean (read directly by settings / writing assistant).
        HasSubscriptionFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )

        // 3. Make the premium tab render the subscribed UI by returning a valid Subscription.
        val method = SubscriptionStateBFingerprint.method
        val impl = method.implementation!!
        // The method only declares 4 locals; the Subscription constructor needs 8 args + `this`.
        val registerCount = MutableMethodImplementation::class.java.getDeclaredField("registerCount")
        registerCount.isAccessible = true
        registerCount.setInt(impl, 10)
        // Drop the original body so its stale register references don't trip the verifier.
        val originalCount = impl.instructions.size
        if (originalCount > 0) method.removeInstructions(0, originalCount)
        method.addInstructions(
            0,
            """
                new-instance v0, Lcom/deshkeyboard/billing/Subscription;
                const-string v1, "ai_writing_yearly"
                const-string v2, "kodex-premium"
                const/4 v3, 0x0
                const/4 v4, 0x1
                sget-object v5, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                const/4 v6, 0x1
                const-wide v7, 0x7fffffffffffffffL
                invoke-static {v7, v8}, Ljava/lang/Long;->valueOf(J)Ljava/lang/Long;
                move-result-object v7
                sget-object v8, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
                invoke-direct/range {v0 .. v8}, Lcom/deshkeyboard/billing/Subscription;-><init>(Ljava/lang/String;Ljava/lang/String;ZZLjava/lang/Boolean;ZLjava/lang/Long;Ljava/lang/Boolean;)V
                return-object v0
            """,
        )
    }
}

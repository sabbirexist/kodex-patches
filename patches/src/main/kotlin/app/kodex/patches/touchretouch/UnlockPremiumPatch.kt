package app.kodex.patches.touchretouch

import app.kodex.patches.shared.Constants.COMPATIBILITY_TOUCHRETOUCH
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

/**
 * Unlocks premium (RetouchPlus).
 *
 * The premium state is the `b2/e` enum (RetouchFree / RetouchCore / RetouchPlus) held in
 * `TRApplication$b` and read through the single accessor `g()`. Forcing it to always return
 * RetouchPlus makes every premium check pass, so the premium tools are unlocked (and, where the
 * watermark/ad gating keys off the same state, those are disabled too).
 *
 * NOTE: this is opt-in (default off) because forcing the premium state while RevenueCat still
 * reports "no entitlement" crashes the app in the editor — the native engine keys its entitlement
 * off the RevenueCat App User ID, not `g()`, so the two states diverge and the app crashes on the
 * main screen. The crash could not be fixed without a device to debug.
 */
@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock premium",
    description = "Unlocks all premium tools and features. (May crash — premium is server-validated.)",
    default = false,
) {
    compatibleWith(COMPATIBILITY_TOUCHRETOUCH)

    execute {
        PremiumStateFingerprint.method.addInstructions(
            0,
            """
                sget-object v0, Lb2/e;->i:Lb2/e;
                return-object v0
            """,
        )
    }
}

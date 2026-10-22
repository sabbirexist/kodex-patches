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
 * The subscription itself is server-validated by RevenueCat, so this only flips the client-side
 * state — the same state the app itself uses for gating.
 */
@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock premium",
    description = "Unlocks all premium tools and features.",
    default = true,
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

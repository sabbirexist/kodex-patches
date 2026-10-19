package app.kodex.patches.tempmail

import app.kodex.patches.shared.Constants.COMPATIBILITY_TEMPMAIL
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

/**
 * Temp Mail's premium state is the subscriber id (`sid`) stored in SharedPreferences. `AppUtils.y()`
 * returns true when that id is empty (not premium) and gates every ad slot and the premium UI.
 * Forcing it to return false unlocks ad-free browsing and the premium-looking UI. The server-side
 * perks (longer storage, multiple mailboxes, custom domains) still need a real `sid`.
 */
@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock premium",
    description = "Removes all ads and hides the \"Upgrade to Premium\" prompts.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_TEMPMAIL)

    execute {
        // AppUtils.y(Context) -> false (always premium).
        PremiumGateFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """,
        )
    }
}

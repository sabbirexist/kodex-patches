package app.kodex.patches.bangla

import app.morphe.patcher.Fingerprint

/**
 * Bangla Keyboard keeps package/class names readable, so these match on exact class + method.
 *
 * `billing/e.a()` is the central "is premium" check: it returns true when the premium StateFlow
 * flag is set OR `billing/e.b()` finds an active subscription. Forcing it true unlocks the premium
 * UI and removes the promoted items (ads) without needing a real subscription.
 */

/** `billing/e.a()` — the central "is premium" gate. */
object PremiumGateFingerprint : Fingerprint(
    definingClass = "Lcom/deshkeyboard/billing/e;",
    name = "a",
    returnType = "Z",
    parameters = listOf(),
)

/** `com.facebook.e.i(Context)` — the Facebook SDK init (AppEvents + tracking). */
object FacebookInitFingerprint : Fingerprint(
    definingClass = "Lcom/facebook/e;",
    name = "i",
    returnType = "V",
    parameters = listOf("Landroid/content/Context;"),
)

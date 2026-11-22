package app.kodex.patches.bangla

import app.morphe.patcher.Fingerprint

/**
 * Bangla Keyboard keeps package/class names readable, so these match on exact class + method.
 *
 * There are two distinct premium surfaces:
 *
 *  1. `billing/e.a()` / `billing/e.b()` — the static "is premium" / "has subscription" booleans.
 *     These gate the settings, the writing assistant and the premium-features activity.
 *  2. `billing/e.d` (a `StateFlow<SubscriptionState>`) — the *actual* subscription state, observed
 *     directly by the premium tab / subscription fragment. It renders "Buy Premium" when
 *     `SubscriptionState.b()` returns null, and the subscribed UI otherwise.
 *
 * Forcing (1) is not enough — the premium tab reads (2) directly, so `SubscriptionState.b()` has to
 * return a non-null `Subscription` as well.
 */

/** `billing/e.a()` — the central "is premium" gate. */
object PremiumGateFingerprint : Fingerprint(
    definingClass = "Lcom/deshkeyboard/billing/e;",
    name = "a",
    returnType = "Z",
    parameters = listOf(),
)

/** `billing/e.b()` — the "has subscription" boolean (used directly by settings / writing assistant). */
object HasSubscriptionFingerprint : Fingerprint(
    definingClass = "Lcom/deshkeyboard/billing/e;",
    name = "b",
    returnType = "Z",
    parameters = listOf(),
)

/**
 * `SubscriptionState.b()` — derives the current `Subscription` from the state's list. The premium tab
 * shows the subscribed UI only when this returns non-null, so we force it to build a valid
 * subscription.
 */
object SubscriptionStateBFingerprint : Fingerprint(
    definingClass = "Lcom/deshkeyboard/billing/SubscriptionState;",
    name = "b",
    returnType = "Lcom/deshkeyboard/billing/Subscription;",
    parameters = listOf(),
)

/** `com.facebook.e.i(Context)` — the Facebook SDK init (AppEvents + tracking). */
object FacebookInitFingerprint : Fingerprint(
    definingClass = "Lcom/facebook/e;",
    name = "i",
    returnType = "V",
    parameters = listOf("Landroid/content/Context;"),
)

/**
 * `Lyh/b.b(Object)` — the `PremiumfeaturesActivity` listener that switches between the "Buy
 * Premium" and the "subscribed" sections based on the premium flag. Forcing the incoming boolean to
 * true makes the premium screen always render the subscribed UI.
 */
object PremiumScreenHandlerFingerprint : Fingerprint(
    definingClass = "Lyh/b;",
    name = "b",
    returnType = "V",
    parameters = listOf("Ljava/lang/Object;"),
)

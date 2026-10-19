package app.kodex.patches.touchretouch

import app.morphe.patcher.Fingerprint

/**
 * TouchRetouch (com.advasoft.touchretouch.plus) seams.
 *
 * The codebase is heavily R8-obfuscated, so the obfuscated class names below (`b2/e`, `l1/c`, `r1/w1`)
 * are pinned to 5.3.2 exactly and must be re-derived on any update. The premium state (`b2/e`) is read
 * through the single accessor `TRApplication$b.g()`; analytics funnel through `l1/c.b()`.
 */

/**
 * `TRApplication$b.g()` — the single accessor for the premium state (`b2/e`: RetouchFree /
 * RetouchCore / RetouchPlus). Forcing it to RetouchPlus makes every premium check pass.
 */
object PremiumStateFingerprint : Fingerprint(
    definingClass = "Lcom/advasoft/touchretouch/TRApplication\$b;",
    name = "g",
    returnType = "Lb2/e;",
    parameters = listOf(),
)

/**
 * `l1/c.b(Context, l1/a)` — the global analytics dispatcher; every event (Firebase, etc.) flows
 * through this static method. Returning early disables the whole analytics pipeline.
 */
object AnalyticsDispatcherFingerprint : Fingerprint(
    definingClass = "Ll1/c;",
    name = "b",
    returnType = "V",
    parameters = listOf("Landroid/content/Context;", "Ll1/a;"),
)

/**
 * `TRApplication.f()` — decrypts and returns the AppsFlyer API key. Returning an empty string means
 * AppsFlyer initializes with no key and does not track.
 */
object AppsFlyerKeyFingerprint : Fingerprint(
    definingClass = "Lcom/advasoft/touchretouch/TRApplication;",
    name = "f",
    returnType = "Ljava/lang/String;",
    parameters = listOf(),
)

/** `r1/w1.k(Lv1/p1;)` — shows the custom "rate us" popup. Early-return removes the nag. */
object RateUsNagFingerprint : Fingerprint(
    definingClass = "Lr1/w1;",
    name = "k",
    returnType = "V",
    parameters = listOf("Lv1/p1;"),
)

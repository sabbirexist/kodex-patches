package app.kodex.patches.eyecon

import app.morphe.patcher.Fingerprint

/**
 * Eyecon keeps its class and method names readable (only field names are R8-obfuscated), so these
 * fingerprints match on exact class + method + descriptor and are expected to be stable.
 *
 * Premium gate chain: `Premium.b()` = `FreePremium.c() ? true : PaidPremium.a()` — the two leaf
 * checks plus the a-la-carte `FeaturesHelper.e()` are the only seams that determine "premium".
 */

/** `FreePremium.c()` — "free/viral premium" = a stored timestamp in the future. */
object FreePremiumGateFingerprint : Fingerprint(
    definingClass = "Lcom/eyecon/global/Billing/Premium/FreePremium;",
    name = "c",
    returnType = "Z",
    parameters = listOf(),
)

/** `PaidPremium.a(Boolean)` — paid premium cached from Play Billing (valid 30 days). */
object PaidPremiumGateFingerprint : Fingerprint(
    definingClass = "Lcom/eyecon/global/Billing/Premium/PaidPremium;",
    name = "a",
    returnType = "Ljava/lang/Boolean;",
    parameters = listOf("Ljava/lang/Boolean;"),
)

/** `FeaturesHelper.e(int)` — per-feature "did purchase" gate (server-synced flag). */
object FeaturePurchaseGateFingerprint : Fingerprint(
    definingClass = "Lcom/eyecon/global/Billing/Features/FeaturesHelper;",
    name = "e",
    returnType = "Z",
    parameters = listOf("I"),
)

/**
 * `AmplitudeHelper.g(boolean, Runnable)` — the consent gate that queues every Amplitude event,
 * including `startSessionReplay`. Forcing it to a no-op disables Amplitude AND the screen
 * recorder in one seam.
 */
object AmplitudeGateFingerprint : Fingerprint(
    definingClass = "Lcom/eyecon/global/Analytics/Amplitude/AmplitudeHelper;",
    name = "g",
    returnType = "V",
    parameters = listOf("Z", "Ljava/lang/Runnable;"),
)

/** `SingularHelper.i(String)` — starts the Singular event queue. */
object SingularGateFingerprint : Fingerprint(
    definingClass = "Lcom/eyecon/global/Analytics/SingularHelper;",
    name = "i",
    returnType = "V",
    parameters = listOf("Ljava/lang/String;"),
)

/** `CrashReportImp.b(String, Throwable)` — the Crashlytics crash reporter (`c`/`d` delegate here). */
object CrashReportGateFingerprint : Fingerprint(
    definingClass = "Lcom/eyecon/global/Analytics/Crashes/CrashReportImp;",
    name = "b",
    returnType = "V",
    parameters = listOf("Ljava/lang/String;", "Ljava/lang/Throwable;"),
)

/** `CrashReportImp.f(Throwable)` — the fatal/uncaught-exception Crashlytics reporter. */
object FatalCrashReportFingerprint : Fingerprint(
    definingClass = "Lcom/eyecon/global/Analytics/Crashes/CrashReportImp;",
    name = "f",
    returnType = "V",
    parameters = listOf("Ljava/lang/Throwable;"),
)

/**
 * `Server.a(String, boolean)` — the actual `addcontacts.jsp` POST. `Server.c()` builds the contact
 * string then calls this, so forcing a no-op blocks every contact upload to Eyecon's server.
 */
object ContactUploadFingerprint : Fingerprint(
    definingClass = "Lcom/eyecon/global/Network/Server;",
    name = "a",
    returnType = "V",
    parameters = listOf("Ljava/lang/String;", "Z"),
)

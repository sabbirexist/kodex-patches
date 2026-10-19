package app.kodex.patches.tempmail

import app.morphe.patcher.Fingerprint

/**
 * Temp Mail keeps class names readable but method names are single-letter (R8), so these match on
 * exact class + method + descriptor.
 *
 * Premium gate: `AppUtils.y()` returns `TextUtils.isEmpty(sid)` — TRUE when NOT premium. It is the
 * single gate used by AdUtils/AdManager/MailboxRepository/DomainsRepository/etc., so forcing it to
 * return false makes the whole app treat the user as premium (ad-free + premium UI).
 */

/** `AppUtils.y(Context)` — "is NOT premium" (empty subscriber id). Force false = premium. */
object PremiumGateFingerprint : Fingerprint(
    definingClass = "Lcom/tempmail/utils/AppUtils;",
    name = "y",
    returnType = "Z",
    parameters = listOf("Landroid/content/Context;"),
)

/** `AnalyticsUtils.n(...)` — the central Firebase Analytics logEvent (every event funnels here). */
object AnalyticsLogFingerprint : Fingerprint(
    definingClass = "Lcom/tempmail/utils/AnalyticsUtils;",
    name = "n",
    returnType = "V",
    parameters = listOf(
        "Landroid/content/Context;",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
    ),
)

/** `AnalyticsUtils.f(...)` — Crashlytics recordException (API error path). */
object CrashReportFingerprint : Fingerprint(
    definingClass = "Lcom/tempmail/utils/AnalyticsUtils;",
    name = "f",
    returnType = "V",
    parameters = listOf(
        "Landroid/content/Context;",
        "Ljava/lang/String;",
        "Lretrofit2/Response;",
        "Lcom/tempmail/data/api/models/answers/ApiError;",
        "Ljava/lang/String;",
    ),
)

/** `AnalyticsUtils.g(...)` — Crashlytics recordException (throwable path). */
object CrashReport2Fingerprint : Fingerprint(
    definingClass = "Lcom/tempmail/utils/AnalyticsUtils;",
    name = "g",
    returnType = "V",
    parameters = listOf(
        "Landroid/content/Context;",
        "Ljava/lang/String;",
        "Ljava/lang/Throwable;",
        "Ljava/lang/String;",
        "Ljava/lang/Integer;",
    ),
)

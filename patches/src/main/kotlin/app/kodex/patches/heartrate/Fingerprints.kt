package app.kodex.patches.heartrate

import app.morphe.patcher.Fingerprint

/**
 * Heart Rate Monitor - Pulse App (heartratemonitor.heartrate.pulse.pulseapp) seams.
 *
 * The Android host is heavily R8-obfuscated, so the obfuscated class names below (`Tb/g`, `od/a`)
 * are pinned to 1.7.4 exactly and must be re-derived on any update. The ad loading goes through the
 * AdMob base loaders (`Tb/g`); analytics goes through `od/a` (Firebase) and the Adjust SDK.
 */

/**
 * `Tb/g.j(Activity)` — the AdMob interstitial load (every full-screen ad funnels through this).
 * Early-return disables interstitial ads.
 */
object InterstitialLoadFingerprint : Fingerprint(
    definingClass = "LTb/g;",
    name = "j",
    returnType = "V",
    parameters = listOf("Landroid/app/Activity;"),
)

/** `od/a.c(...)` — the Firebase Analytics sink (calls `FirebaseAnalytics.logEvent`). */
object FirebaseSinkFingerprint : Fingerprint(
    definingClass = "Lod/a;",
    name = "c",
    returnType = "V",
    parameters = listOf("Landroid/content/Context;", "Ljava/lang/String;", "Landroid/os/Bundle;", "Z"),
)

/** `com.adjust.sdk.Adjust.initSdk(AdjustConfig)` — the Adjust attribution SDK init. */
object AdjustInitFingerprint : Fingerprint(
    definingClass = "Lcom/adjust/sdk/Adjust;",
    name = "initSdk",
    returnType = "V",
    parameters = listOf("Lcom/adjust/sdk/AdjustConfig;"),
)

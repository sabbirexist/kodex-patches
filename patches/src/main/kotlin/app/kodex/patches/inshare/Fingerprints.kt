package app.kodex.patches.inshare

import app.morphe.patcher.Fingerprint

/**
 * InShare is R8-obfuscated, so this matches the on-disk names.
 *
 * `t2` is the premium/ad gate singleton. `t2.c()` is the central "is premium" check: every ad slot
 * (banner, interstitial, native) calls `t2.a.d()` or `t2.a.c()`, and `d()` funnels into `c()` when
 * the cached flag is null. Forcing `c()` to return true therefore disables every ad, which is
 * exactly what the paid "Remove Ads" unlock does.
 */
object PremiumFlagFingerprint : Fingerprint(
    definingClass = "Lt2;",
    name = "c",
    returnType = "Z",
    parameters = listOf(),
)

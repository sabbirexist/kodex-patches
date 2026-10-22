package app.kodex.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    /**
     * VLC for Android.
     *
     * 3.7.2 is the latest confirmed target; 3.7.1 is kept for older installs. The `null` target
     * carries patches forward experimentally; VLC is unobfuscated and its key seams (VLCOptions,
     * WhatsNewManager, FeatureFlagManager) are stable across releases.
     */
    val COMPATIBILITY_VLC = Compatibility(
        name = "VLC",
        packageName = "org.videolan.vlc",
        apkFileType = ApkFileType.APK,
        appIconColor = 0xFF6600, // VLC orange
        targets = listOf(
            AppTarget(
                version = null,
                isExperimental = true
            ),
            AppTarget(
                version = "3.7.1"
            ),
            AppTarget(
                version = "3.7.2"
            )
        )
    )

    /**
     * File Sharing - InShare (InShot Inc.).
     *
     * 2.1.3 is the confirmed target. InShare is R8-obfuscated, so the patch seams (`t2`, the
     * premium/ad gate) are pinned to this exact version and must be re-derived on updates.
     */
    val COMPATIBILITY_INSHARE = Compatibility(
        name = "InShare",
        packageName = "sharefiles.sharemusic.shareapps.filetransfer",
        apkFileType = ApkFileType.APK,
        appIconColor = 0x1E88E5, // InShare blue
        targets = listOf(
            AppTarget(
                version = "2.1.3"
            )
        )
    )

    /**
     * Eyecon Caller ID & Dialer (Eyecon Global Ltd).
     *
     * 4.2026.09.16.1233 is the confirmed target. Eyecon keeps its class/package names readable
     * (only fields are R8-obfuscated), so the premium/ad/analytics seams are matched by exact
     * class + method name and are expected to survive minor version bumps; still pin to this
     * version and re-derive on major updates.
     */
    val COMPATIBILITY_EYECON = Compatibility(
        name = "Eyecon",
        packageName = "com.eyecon.global",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0x00A884, // Eyecon teal-green
        targets = listOf(
            AppTarget(
                version = "4.2026.09.16.1233"
            )
        )
    )

    /**
     * Desh Bangla Keyboard — Bengali keyboard IME.
     *
     * 17.6.8 is the confirmed target. Readable package names, no anti-tamper, so the premium
     * (billing/e) and analytics seams are matched by exact class + method name.
     */
    val COMPATIBILITY_BANGLA = Compatibility(
        name = "Desh Bangla Keyboard",
        packageName = "com.bangla.keyboard.for.android",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0x00897B, // Bangla Keyboard teal-green
        targets = listOf(
            AppTarget(
                version = "17.6.8"
            )
        )
    )

    /**
     * TouchRetouch (Retouch) — ADVA Soft photo editor for removing objects.
     *
     * 5.3.2 is the confirmed target. No PairIP/anti-tamper. The premium state is the `b2/e` enum
     * (RetouchFree / RetouchCore / RetouchPlus) read through `TRApplication$b.g()`, backed by a
     * RevenueCat subscription plus a legacy "installed from Play Store" one-time check. The analytics
     * sink is `a2/d.c()` (Firebase). Obfuscated seams are pinned to this exact version.
     */
    val COMPATIBILITY_TOUCHRETOUCH = Compatibility(
        name = "TouchRetouch",
        packageName = "com.advasoft.touchretouch.plus",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0xFF6D00, // TouchRetouch orange
        targets = listOf(
            AppTarget(
                version = "5.3.2"
            )
        )
    )

    /**
     * Heart Rate Monitor - Pulse App — Flutter health tracker (heart rate, blood pressure, blood
     * sugar, AI doctor).
     *
     * 1.7.4 is the confirmed target. No PairIP/anti-tamper. The app's only in-app purchase is a
     * single "remove ads" SKU; ads are Google AdMob (mediation adapters for Pangle/Facebook/etc.),
     * and tracking is Adjust + Firebase. Obfuscated seams (`Tb/g`, `od/a`) are pinned to this
     * version.
     */
    val COMPATIBILITY_HEARTRATE = Compatibility(
        name = "Heart Rate Monitor",
        packageName = "heartratemonitor.heartrate.pulse.pulseapp",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0xE53935, // heart-rate red
        targets = listOf(
            AppTarget(
                version = "1.7.4"
            )
        )
    )
}

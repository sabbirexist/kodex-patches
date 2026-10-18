package app.kodex.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    /**
     * VLC for Android.
     *
     * 3.7.1 is the confirmed target: patches are verified against the official 3.7.1 APK.
     *
     * The `null` target carries patches forward experimentally; VLC is unobfuscated and its key
     * seams (VLCOptions, WhatsNewManager, FeatureFlagManager, OnboardingActivityKt) are stable
     * across releases, so newer versions are expected to work too.
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
}

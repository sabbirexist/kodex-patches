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
}

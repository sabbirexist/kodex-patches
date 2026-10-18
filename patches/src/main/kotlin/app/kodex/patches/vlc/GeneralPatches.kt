package app.kodex.patches.vlc

import app.kodex.patches.shared.Constants.COMPATIBILITY_VLC
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

/**
 * Simple behaviour patches: each overrides a single method's entry point with an early return, so
 * the original body becomes dead code. Register numbers are safe because v0 is always a local in
 * these methods and the injected return runs before any of the original instructions.
 */
@Suppress("unused")
val skipWhatsNewPatch = bytecodePatch(
    name = "Skip \"What's New\" dialog",
    description = "Suppresses the What's New changelog dialog shown after app updates.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_VLC)
    execute { WhatsNewLaunchFingerprint.method.addInstructions(0, "return-void") }
}

@Suppress("unused")
val unlockFeatureFlagsPatch = bytecodePatch(
    name = "Unlock experimental features",
    description = "Enables every experimental feature flag regardless of its stored value.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_VLC)
    execute {
        FeatureFlagEnabledFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """
        )
    }
}

@Suppress("unused")
val forcePassthroughPatch = bytecodePatch(
    name = "Force audio passthrough",
    description = "Enables bitstream audio passthrough (digital output) to an external DAC or receiver.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_VLC)
    execute {
        AudioDigitalOutputFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """
        )
    }
}

@Suppress("unused")
val forceAaudioPatch = bytecodePatch(
    name = "Force AAudio output",
    description = "Locks the audio output to AAudio (low latency), overriding the device preference.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_VLC)
    execute {
        AoutFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return-object v0
            """
        )
    }
}

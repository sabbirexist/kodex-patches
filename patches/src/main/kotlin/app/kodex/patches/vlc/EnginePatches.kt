package app.kodex.patches.vlc

import app.kodex.patches.shared.Constants.COMPATIBILITY_VLC
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

/**
 * Builds the smali that appends a set of libVLC options to the `ArrayList<String>` held in `arr`.
 * The string constant reuses the next local register (dead at the return point), so no register
 * remapping is needed.
 */
private fun buildAddOptions(arr: Int, options: List<String>): String {
    val str = arr + 1
    return options.joinToString("\n") { opt ->
        "const-string v$str, \"$opt\"\n" +
            "invoke-virtual {v$arr, v$str}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z"
    }
}

@Suppress("unused")
val forceReplayGainPatch = bytecodePatch(
    name = "Force ReplayGain",
    description = "Normalizes loudness across tracks using track mode with peak protection.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_VLC)
    execute {
        val ret = LibOptionsFingerprint.instructionMatches[0]
        val arr = ret.getInstruction<OneRegisterInstruction>().registerA
        LibOptionsFingerprint.method.addInstructions(
            ret.index,
            buildAddOptions(
                arr,
                listOf(
                    "--audio-replay-gain-mode=track",
                    "--audio-replay-gain-preamp=0.0",
                    "--audio-replay-gain-default=-7.0",
                    "--audio-replay-gain-peak-protection",
                )
            )
        )
    }
}

@Suppress("unused")
val disableTimeStretchPatch = bytecodePatch(
    name = "Disable audio time-stretch",
    description = "Forces time-stretching off for artifact-free playback at 1x speed.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_VLC)
    execute {
        val ret = LibOptionsFingerprint.instructionMatches[0]
        val arr = ret.getInstruction<OneRegisterInstruction>().registerA
        LibOptionsFingerprint.method.addInstructions(ret.index, buildAddOptions(arr, listOf("--no-audio-time-stretch")))
    }
}

@Suppress("unused")
val raiseNetworkCachePatch = bytecodePatch(
    name = "Raise network cache",
    description = "Buffers 3 seconds of stream ahead to reduce stalls on flaky networks.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_VLC)
    execute {
        val ret = LibOptionsFingerprint.instructionMatches[0]
        val arr = ret.getInstruction<OneRegisterInstruction>().registerA
        LibOptionsFingerprint.method.addInstructions(ret.index, buildAddOptions(arr, listOf("--network-caching=3000")))
    }
}

@Suppress("unused")
val verboseLoggingPatch = bytecodePatch(
    name = "Verbose libVLC logging",
    description = "Sets libVLC to maximum verbosity (-vvv) for troubleshooting playback.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_VLC)
    execute {
        val ret = LibOptionsFingerprint.instructionMatches[0]
        val arr = ret.getInstruction<OneRegisterInstruction>().registerA
        LibOptionsFingerprint.method.addInstructions(ret.index, buildAddOptions(arr, listOf("-vvv")))
    }
}

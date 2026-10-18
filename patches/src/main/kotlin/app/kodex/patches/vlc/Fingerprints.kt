package app.kodex.patches.vlc

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.opcode
import com.android.tools.smali.dexlib2.Opcode

/**
 * Shared fingerprints for the VLC patch set.
 *
 * VLC for Android ships unobfuscated, so these match on exact class + method names. Each is an
 * `object` so a failed match names the fingerprint in the exception stack trace.
 */
object WhatsNewLaunchFingerprint : Fingerprint(
    definingClass = "Lorg/videolan/vlc/util/WhatsNewManager;",
    name = "launchIfNeeded",
    returnType = "V",
    parameters = listOf("Landroidx/appcompat/app/AppCompatActivity;"),
)

object FeatureFlagEnabledFingerprint : Fingerprint(
    definingClass = "Lorg/videolan/vlc/util/FeatureFlagManager;",
    name = "isEnabled",
    returnType = "Z",
    parameters = listOf("Landroid/content/Context;", "Lorg/videolan/vlc/util/FeatureFlag;"),
)

object AudioDigitalOutputFingerprint : Fingerprint(
    definingClass = "Lorg/videolan/resources/VLCOptions;",
    name = "isAudioDigitalOutputEnabled",
    returnType = "Z",
    parameters = listOf("Landroid/content/SharedPreferences;"),
)

object AoutFingerprint : Fingerprint(
    definingClass = "Lorg/videolan/resources/VLCOptions;",
    name = "getAout",
    returnType = "Ljava/lang/String;",
    parameters = listOf("Landroid/content/SharedPreferences;"),
)

/**
 * The libVLC option builder. Options injected before its single `return-object` are appended last,
 * so they win over the built-in values (libVLC resolves later flags over earlier ones).
 */
object LibOptionsFingerprint : Fingerprint(
    definingClass = "Lorg/videolan/resources/VLCOptions;",
    name = "getLibOptions",
    returnType = "Ljava/util/ArrayList;",
    parameters = listOf(),
    filters = listOf(
        opcode(Opcode.RETURN_OBJECT)
    ),
)

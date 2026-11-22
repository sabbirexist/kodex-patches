package app.kodex.patches.vlc

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.opcode
import app.morphe.patcher.string
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

/**
 * `VideoPlayerOverlayDelegate.initOverlay()`:
 * VLC checks `player.settings.getBoolean("enable_seek_buttons", false)` to decide whether
 * rewind and forward skip buttons are enabled in the player HUD. We replace `false` (0x0) with `true` (0x1)
 * so seek buttons are enabled out of the box.
 */
object OverlayInitSeekButtonsFingerprint : Fingerprint(
    definingClass = "Lorg/videolan/vlc/gui/video/VideoPlayerOverlayDelegate;",
    name = "initOverlay",
    returnType = "V",
    parameters = listOf(),
    filters = listOf(
        string("enable_seek_buttons"),
        methodCall(
            definingClass = "Landroid/content/SharedPreferences;",
            name = "getBoolean",
        ),
    ),
)

/**
 * `VideoPlayerActivity.onChangedControlSetting(String)`:
 * Called when settings change. VLC calls `settings.getBoolean("enable_seek_buttons", false)`.
 * We replace `false` (0x0) with `true` (0x1) so the setting default remains true.
 */
object ActivityControlSettingSeekButtonsFingerprint : Fingerprint(
    definingClass = "Lorg/videolan/vlc/gui/video/VideoPlayerActivity;",
    name = "onChangedControlSetting",
    returnType = "V",
    parameters = listOf("Ljava/lang/String;"),
    filters = listOf(
        string("enable_seek_buttons"),
        methodCall(
            definingClass = "Landroid/content/SharedPreferences;",
            name = "getBoolean",
        ),
    ),
)

/**
 * `VideoTouchDelegate.onTouchEvent(MotionEvent)`:
 * Stock VLC checks `event.x < range / 4f` (left 25%) to rewind and `event.x > range * 0.75` (right 25%)
 * to forward, with the center 50% reserved for play/pause.
 * We locate the two `seekDelta` calls and patch:
 * - Divisor 4.0f -> 2.0f (`range / 2f`, i.e. 50%) for rewind.
 * - Multiplier 0.75 -> 0.50 (`range * 0.50`, i.e. 50%) for forward.
 * This expands double-tap seek to the entire left half (rewind) and right half (forward) in Netflix style.
 */
object TouchDelegateDoubleTapFingerprint : Fingerprint(
    definingClass = "Lorg/videolan/vlc/gui/video/VideoTouchDelegate;",
    name = "onTouchEvent",
    returnType = "Z",
    parameters = listOf("Landroid/view/MotionEvent;"),
    filters = listOf(
        methodCall(
            definingClass = "Lorg/videolan/vlc/gui/video/VideoTouchDelegate;",
            name = "seekDelta\$vlc_android_release",
        ),
    ),
)

/**
 * `VideoPlayerActivity.switchToPopup()`:
 * VLC hardcodes `paramBuilder.setActions(emptyList())`, leaving Picture-in-Picture mode with zero controls.
 * We replace the `emptyList()` call with `VlcControls.getPipActions(this)` to supply dedicated RemoteActions:
 * - Rewind (customizable interval via Settings)
 * - Play/Pause
 * - Forward (customizable interval via Settings)
 * - Next track
 */
object SwitchToPopupActionsFingerprint : Fingerprint(
    definingClass = "Lorg/videolan/vlc/gui/video/VideoPlayerActivity;",
    name = "switchToPopup",
    returnType = "V",
    parameters = listOf(),
    filters = listOf(
        methodCall(
            definingClass = "Lkotlin/collections/CollectionsKt;",
            name = "emptyList",
        ),
    ),
)

/**
 * `PopupManager.onDoubleTap(MotionEvent)`:
 * VLC hardcodes double-tap to maximize the floating popup into fullscreen (`expandToVideoPlayer()`).
 * We replace that call with `VlcControls.onPopupDoubleTap(this, event)`:
 * - Double-tap left half rewinds by `videoDoubleTapJumpDelay` (default 10s, configurable to 5s)
 * - Double-tap right half forwards by `videoDoubleTapJumpDelay` (default 10s, configurable to 5s)
 */
object PopupDoubleTapFingerprint : Fingerprint(
    definingClass = "Lorg/videolan/vlc/gui/video/PopupManager;",
    name = "onDoubleTap",
    returnType = "Z",
    parameters = listOf("Landroid/view/MotionEvent;"),
    filters = listOf(
        methodCall(
            definingClass = "Lorg/videolan/vlc/gui/video/PopupManager;",
            name = "expandToVideoPlayer",
        ),
    ),
)

/**
 * `PopupManager.showPopup()`:
 * Called when the floating popup window is constructed. Right before returning, it sets up VLC vout.
 * We replace `popupLayout.setVLCVOut(vlcVout)` with `VlcControls.initPopup(popupLayout, vlcVout, this)`
 * to preserve the original vout setup AND attach dedicated rewind and forward skip buttons to the popup overlay.
 */
object PopupShowFingerprint : Fingerprint(
    definingClass = "Lorg/videolan/vlc/gui/video/PopupManager;",
    name = "showPopup",
    returnType = "V",
    parameters = listOf(),
    filters = listOf(
        methodCall(
            definingClass = "Lorg/videolan/vlc/gui/view/PopupLayout;",
            name = "setVLCVOut",
        ),
    ),
)

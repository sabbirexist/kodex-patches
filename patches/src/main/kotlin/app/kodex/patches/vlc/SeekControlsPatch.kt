package app.kodex.patches.vlc

import app.kodex.patches.shared.Constants.COMPATIBILITY_VLC
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val EXTENSION_CLASS = "Lapp/kodex/extension/VlcControls;"

/**
 * Player Seek & Media Controls (Fullscreen, PiP & Floating Popup):
 *
 * 1. Dedicated Skip Buttons (Fullscreen Player HUD):
 *    Enables VLC's built-in 10-second forward and rewind skip buttons (`player_overlay_forward`
 *    and `player_overlay_rewind`) by changing `getBoolean("enable_seek_buttons", false)`
 *    fallback to `true` (0x1) in `VideoPlayerOverlayDelegate.initOverlay()` and
 *    `VideoPlayerActivity.onChangedControlSetting()`.
 *    The skip interval is customizable from 5 to 60+ seconds in Settings -> Video -> Controls -> Jump delay.
 *
 * 2. Netflix-Style Fullscreen Double-Tap:
 *    Patches `VideoTouchDelegate.onTouchEvent(MotionEvent)`:
 *    - Rewind threshold: `range / 4.0f` -> `range / 2.0f` (left 50% rewinds)
 *    - Forward threshold: `range * 0.75` -> `range * 0.50` (right 50% forwards)
 *    Interval is customizable via Settings -> Video -> Controls -> Double tap seek delay.
 *
 * 3. Dedicated Skip & Media Controls (Android Picture-in-Picture):
 *    Replaces `emptyList()` in `VideoPlayerActivity.switchToPopup()` with
 *    `VlcControls.getPipActions(Context)` to supply dedicated RemoteActions:
 *    - Rewind (interval dynamically read from Settings.videoJumpDelay)
 *    - Play/Pause toggle
 *    - Fast-Forward (interval dynamically read from Settings.videoJumpDelay)
 *    - Next track
 *
 * 4. Netflix-Style Double-Tap on Floating Popup:
 *    Replaces `expandToVideoPlayer()` in `PopupManager.onDoubleTap(MotionEvent)` with
 *    `VlcControls.onPopupDoubleTap(PopupManager, MotionEvent)`:
 *    - Double-tapping left half rewinds by `Settings.videoDoubleTapJumpDelay` (default 10s, configurable to 5s)
 *    - Double-tapping right half forwards by `Settings.videoDoubleTapJumpDelay` (default 10s, configurable to 5s)
 *
 * 5. Dedicated Skip Buttons on Floating Popup:
 *    Hooks `PopupManager.showPopup()` via `VlcControls.initPopup(...)` to attach dedicated rewind
 *    and forward skip buttons matching VLC's native styling, synchronized with play/pause visibility.
 */
@Suppress("unused")
val seekControlsPatch = bytecodePatch(
    name = "Seek & gesture controls",
    description = "Enables forward/backward skip buttons and Netflix-style double-tap seeking in fullscreen, PiP, and floating popup modes.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_VLC)

    extendWith("extensions/extension.mpe")

    execute {
        // 1. Enable fullscreen seek buttons by default in VideoPlayerOverlayDelegate.initOverlay()
        val overlayGetBooleanMatch = OverlayInitSeekButtonsFingerprint.instructionMatches.getOrNull(1)
            ?: OverlayInitSeekButtonsFingerprint.instructionMatches[0]
        val overlayConstIdx = overlayGetBooleanMatch.index - 1
        val overlayConstInsn = OverlayInitSeekButtonsFingerprint.method.getInstruction<OneRegisterInstruction>(overlayConstIdx)
        OverlayInitSeekButtonsFingerprint.method.replaceInstruction(
            overlayConstIdx,
            "const/4 v${overlayConstInsn.registerA}, 0x1",
        )

        // 2. Enable fullscreen seek buttons default in VideoPlayerActivity.onChangedControlSetting()
        val activityGetBooleanMatch = ActivityControlSettingSeekButtonsFingerprint.instructionMatches.getOrNull(1)
            ?: ActivityControlSettingSeekButtonsFingerprint.instructionMatches[0]
        val activityConstIdx = activityGetBooleanMatch.index - 1
        val activityConstInsn = ActivityControlSettingSeekButtonsFingerprint.method.getInstruction<OneRegisterInstruction>(activityConstIdx)
        ActivityControlSettingSeekButtonsFingerprint.method.replaceInstruction(
            activityConstIdx,
            "const/4 v${activityConstInsn.registerA}, 0x1",
        )

        // 3. Expand fullscreen double-tap to Netflix-style left/right halves in VideoTouchDelegate.onTouchEvent()
        val rewindMatch = TouchDelegateDoubleTapFingerprint.instructionMatches[0]
        val forwardIndex = if (TouchDelegateDoubleTapFingerprint.instructionMatches.size > 1) {
            TouchDelegateDoubleTapFingerprint.instructionMatches[1].index
        } else {
            var found = -1
            val impl = TouchDelegateDoubleTapFingerprint.method.implementation!!
            for (i in (rewindMatch.index + 1) until impl.instructions.size) {
                val ins = TouchDelegateDoubleTapFingerprint.method.getInstruction(i)
                if (ins is ReferenceInstruction) {
                    val ref = ins.reference
                    if (ref is MethodReference && ref.name.startsWith("seekDelta")) {
                        found = i
                        break
                    }
                }
            }
            require(found != -1) { "Could not find forward seekDelta call" }
            found
        }

        // Replace 4.0f with 2.0f for left half rewind (event.x < range / 2.0f)
        var rewindConstIdx = -1
        for (i in rewindMatch.index - 1 downTo maxOf(0, rewindMatch.index - 15)) {
            if (TouchDelegateDoubleTapFingerprint.method.getInstruction(i).opcode == Opcode.CONST_HIGH16) {
                rewindConstIdx = i
                break
            }
        }
        require(rewindConstIdx != -1) { "Could not find CONST_HIGH16 before rewind seekDelta" }
        val rewindReg = TouchDelegateDoubleTapFingerprint.method.getInstruction<OneRegisterInstruction>(rewindConstIdx).registerA
        TouchDelegateDoubleTapFingerprint.method.replaceInstruction(
            rewindConstIdx,
            "const/high16 v$rewindReg, 0x40000000",
        )

        // Replace 0.75 with 0.50 for right half forward (event.x > range * 0.50)
        var forwardConstIdx = -1
        for (i in forwardIndex - 1 downTo maxOf(0, forwardIndex - 15)) {
            if (TouchDelegateDoubleTapFingerprint.method.getInstruction(i).opcode == Opcode.CONST_WIDE_HIGH16) {
                forwardConstIdx = i
                break
            }
        }
        require(forwardConstIdx != -1) { "Could not find CONST_WIDE_HIGH16 before forward seekDelta" }
        val forwardReg = TouchDelegateDoubleTapFingerprint.method.getInstruction<OneRegisterInstruction>(forwardConstIdx).registerA
        TouchDelegateDoubleTapFingerprint.method.replaceInstruction(
            forwardConstIdx,
            "const-wide/high16 v$forwardReg, 0x3fe0000000000000L",
        )

        // 4. Supply RemoteActions when entering PiP mode in switchToPopup()
        val emptyListMatch = SwitchToPopupActionsFingerprint.instructionMatches[0]
        SwitchToPopupActionsFingerprint.method.replaceInstruction(
            emptyListMatch.index,
            "invoke-static {p0}, $EXTENSION_CLASS->getPipActions(Landroid/content/Context;)Ljava/util/List;",
        )

        // 5. Replace popup double-tap expand with Netflix-style double-tap seeking
        val expandMatch = PopupDoubleTapFingerprint.instructionMatches[0]
        PopupDoubleTapFingerprint.method.replaceInstruction(
            expandMatch.index,
            "invoke-static {p0, p1}, $EXTENSION_CLASS->onPopupDoubleTap(Ljava/lang/Object;Landroid/view/MotionEvent;)V",
        )

        // 6. Attach dedicated skip buttons to floating popup in showPopup()
        val voutMatch = PopupShowFingerprint.instructionMatches[0]
        PopupShowFingerprint.method.replaceInstruction(
            voutMatch.index,
            "invoke-static {v0, v1, p0}, $EXTENSION_CLASS->initPopup(Landroid/view/View;Ljava/lang/Object;Ljava/lang/Object;)V",
        )
    }
}

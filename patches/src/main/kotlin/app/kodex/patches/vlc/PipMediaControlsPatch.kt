package app.kodex.patches.vlc

import app.kodex.patches.shared.Constants.COMPATIBILITY_VLC
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstruction
import app.morphe.patcher.patch.bytecodePatch

private const val EXTENSION_CLASS = "Lapp/kodex/extension/VlcPipControls;"

/**
 * Adds Previous Video, Pause/Resume, and Next Video buttons to VLC's Picture-in-Picture (PiP)
 * and floating popup modes.
 *
 * 1. Native Android PiP mode:
 *    VLC hardcodes `paramBuilder.setActions(emptyList())`, leaving the PiP window with zero controls.
 *    We replace `emptyList()` with `VlcPipControls.getPipActions(this, service)`, injecting:
 *    - Previous Video action (triggers `service.previous(false)`)
 *    - Play/Pause toggle action (triggers `service.play()` / `service.pause()`)
 *    - Next Video action (triggers `service.next()`)
 *    We also hook `onMediaPlayerEvent` so that when a video plays, pauses, or switches track,
 *    `updatePipActions` dynamically updates the OS PiP window.
 *
 * 2. Floating Popup mode:
 *    Hooks `PopupManager.showPopup()` and `PopupManager$handler$1.handleMessage()` to bind
 *    click listeners and synchronize visibility for previous/next controls.
 */
@Suppress("unused")
val pipMediaControlsPatch = bytecodePatch(
    name = "PiP & Popup media controls",
    description = "Adds previous video, pause/resume, and next video buttons to Picture-in-Picture (PiP) and floating popup mode.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_VLC)

    extendWith("extensions/extension.mpe")

    execute {
        // 1. Supply RemoteActions when entering PiP mode in switchToPopup()
        // VLC hardcodes `paramBuilder.setActions(emptyList())`.
        // We remove `emptyList()` and inject our actions list generator, whose return value
        // is captured by the existing `move-result-object` for `setActions(List)`.
        val emptyListMatch = SwitchToPopupActionsFingerprint.instructionMatches[0]
        val switchToPopupMethod = SwitchToPopupActionsFingerprint.method
        switchToPopupMethod.removeInstruction(emptyListMatch.index)
        switchToPopupMethod.addInstructions(
            emptyListMatch.index,
            """
                iget-object v0, p0, Lorg/videolan/vlc/gui/video/VideoPlayerActivity;->service:Lorg/videolan/vlc/PlaybackService;
                invoke-static {p0, v0}, $EXTENSION_CLASS->getPipActions(Landroid/content/Context;Ljava/lang/Object;)Ljava/util/List;
            """,
        )

        // 2. Refresh PiP controls dynamically on playback events (playing, paused, track change).
        // In onMediaPlayerEvent, p0 (this) is register 17 (> 15), so it cannot be encoded directly
        // in 4-bit nibble instructions like iget-object or invoke-static.
        // We use move-object/from16 (16-bit source register) to move p0 into low register v0 first.
        OnMediaPlayerEventFingerprint.method.addInstructions(
            0,
            """
                move-object/from16 v0, p0
                iget-object v1, v0, Lorg/videolan/vlc/gui/video/VideoPlayerActivity;->service:Lorg/videolan/vlc/PlaybackService;
                invoke-static {v0, v1}, $EXTENSION_CLASS->updatePipActions(Landroid/app/Activity;Ljava/lang/Object;)V
            """,
        )

        // 3. Bind popup buttons in PopupManager.showPopup()
        // Use the final return-void instruction at the end of showPopup() (avoiding early-return branches).
        val showPopupReturn = PopupManagerShowFingerprint.instructionMatches.last()
        PopupManagerShowFingerprint.method.addInstructions(
            showPopupReturn.index,
            """
                iget-object v0, p0, Lorg/videolan/vlc/gui/video/PopupManager;->rootView:Lorg/videolan/vlc/gui/view/PopupLayout;
                iget-object v1, p0, Lorg/videolan/vlc/gui/video/PopupManager;->service:Lorg/videolan/vlc/PlaybackService;
                invoke-static {v0, v1}, $EXTENSION_CLASS->setupPopupButtons(Landroid/view/View;Ljava/lang/Object;)V
            """,
        )

        // 4. Synchronize button visibility in PopupManager$handler$1.handleMessage()
        PopupManagerHandlerFingerprint.method.addInstructions(
            0,
            """
                iget-object v0, p0, Lorg/videolan/vlc/gui/video/PopupManager${'$'}handler${'$'}1;->this$0:Lorg/videolan/vlc/gui/video/PopupManager;
                iget-object v0, v0, Lorg/videolan/vlc/gui/video/PopupManager;->rootView:Lorg/videolan/vlc/gui/view/PopupLayout;
                iget v1, p1, Landroid/os/Message;->what:I
                invoke-static {v0, v1}, $EXTENSION_CLASS->onPopupHandleMessage(Landroid/view/View;I)V
            """,
        )
    }
}

package app.kodex.patches.vlc

import app.kodex.patches.shared.Constants.COMPATIBILITY_VLC
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
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
        val emptyListMatch = SwitchToPopupActionsFingerprint.instructionMatches[0]
        SwitchToPopupActionsFingerprint.method.replaceInstruction(
            emptyListMatch.index,
            """
                iget-object v0, p0, Lorg/videolan/vlc/gui/video/VideoPlayerActivity;->service:Lorg/videolan/vlc/PlaybackService;
                invoke-static {p0, v0}, $EXTENSION_CLASS->getPipActions(Landroid/content/Context;Ljava/lang/Object;)Ljava/util/List;
            """,
        )

        // 2. Refresh PiP controls dynamically on playback events (playing, paused, track change)
        OnMediaPlayerEventFingerprint.method.addInstructions(
            0,
            """
                iget-object v0, p0, Lorg/videolan/vlc/gui/video/VideoPlayerActivity;->service:Lorg/videolan/vlc/PlaybackService;
                invoke-static {p0, v0}, $EXTENSION_CLASS->updatePipActions(Landroid/app/Activity;Ljava/lang/Object;)V
            """,
        )

        // 3. Bind popup buttons in PopupManager.showPopup()
        val showPopupReturn = PopupManagerShowFingerprint.instructionMatches[0]
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

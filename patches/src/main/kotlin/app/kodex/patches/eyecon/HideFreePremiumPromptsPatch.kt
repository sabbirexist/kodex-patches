package app.kodex.patches.eyecon

import app.kodex.patches.shared.Constants.COMPATIBILITY_EYECON
import app.morphe.patcher.patch.resourcePatch

/**
 * The free-premium screen still shows "Watch a video" / "Invite a friend" earn-more prompts even
 * when premium is unlocked; tapping them plays a rewarded ad. This hides both rows in the layout so
 * the screen reads as a clean, already-active premium page.
 */
@Suppress("unused")
val hideFreePremiumPromptsPatch = resourcePatch(
    name = "Hide free-premium prompts",
    description = "Hides the \"Watch a video\" and \"Invite a friend\" earn-premium rows on the premium screen.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_EYECON)

    execute {
        val layout = get("res/layout/activity_free_premium_user_v2.xml")
        var text = layout.readText()
        text = text.replace(
            "android:id=\"@id/FL_watch_video\"",
            "android:id=\"@id/FL_watch_video\" android:visibility=\"gone\"",
        )
        text = text.replace(
            "android:id=\"@id/FL_invite\"",
            "android:id=\"@id/FL_invite\" android:visibility=\"gone\"",
        )
        layout.writeText(text)
    }
}

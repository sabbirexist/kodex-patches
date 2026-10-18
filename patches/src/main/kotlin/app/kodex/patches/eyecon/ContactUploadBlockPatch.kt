package app.kodex.patches.eyecon

import app.kodex.patches.shared.Constants.COMPATIBILITY_EYECON
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

/**
 * Blocks `Server.a(String, boolean)`, the actual `addcontacts.jsp` POST that uploads the serialized
 * contact list to Eyecon's server. `Server.c()` builds the payload then funnels through here, so a
 * no-op at this seam stops every contact upload.
 */
@Suppress("unused")
val blockContactUploadPatch = bytecodePatch(
    name = "Block contact upload",
    description = "Stops Eyecon from uploading your contacts to its server for caller ID.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_EYECON)

    execute {
        ContactUploadFingerprint.method.addInstructions(0, "return-void")
    }
}

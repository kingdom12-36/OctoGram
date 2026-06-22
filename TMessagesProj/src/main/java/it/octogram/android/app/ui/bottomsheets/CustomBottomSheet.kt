/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.app.ui.bottomsheets

import android.content.Context
import it.octogram.android.app.ui.components.NekoFloatUI
import org.telegram.ui.ActionBar.BottomSheet

open class CustomBottomSheet(
    context: Context,
    needFocus: Boolean
) : BottomSheet(context, needFocus) {

    companion object {
        private var shown = false
    }

    override fun show() {
        if (shown) {
            return
        }
        shown = true
        // Apply Neko.Float 3D card elevation to the bottom-sheet container
        containerView?.let { NekoFloatUI.applyBottomSheet(it) }
        super.show()
    }
}

/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.app.ui.components;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.graphics.Color;
import android.graphics.drawable.Drawable;

import androidx.core.graphics.ColorUtils;

import org.telegram.ui.ActionBar.Theme;

import it.octogram.android.OctoConfig;

public abstract class CustomFab {

    private static final int DEFAULT_FAB_SIZE_DP = 56;
    private static final int SMALL_FAB_SIZE_DP = 40;
    private static final int CIRCULAR_RADIUS_DP = 100;

    public static Drawable createFabBackground() {
        int defaultBackgroundColor = Theme.getColor(Theme.key_chats_actionBackground);
        int pressedBackgroundColor = Theme.getColor(Theme.key_chats_actionPressedBackground);

        return createFabBackground(DEFAULT_FAB_SIZE_DP, defaultBackgroundColor, pressedBackgroundColor);
    }

    /**
     * Returns the Neko.Float 3D ripple drawable for use as a floating FAB background.
     * Gives the FAB the same push-button 3D elevation feel as the Neko.Float system.
     * Call this instead of {@link #createFabBackground()} to opt-in to the 3D look.
     */
    public static Drawable createFloating3DBackground(android.content.Context context) {
        return androidx.core.content.ContextCompat.getDrawable(context, org.telegram.messenger.R.drawable.neko_float_ripple);
    }

    public static Drawable createFabBackground(int sizeDp, int defaultBackgroundColor, int pressedBackgroundColor) {
        int fabCornerRadiusDp;
        if (!OctoConfig.INSTANCE.useSquaredFab.getValue()) {
            fabCornerRadiusDp = CIRCULAR_RADIUS_DP;
        } else {
            fabCornerRadiusDp = (int) Math.ceil((sizeDp * 16) / 56.0f);
        }

        if (sizeDp == SMALL_FAB_SIZE_DP) {
            int defaultFabColor = Theme.key_windowBackgroundWhite;
            defaultBackgroundColor = ColorUtils.blendARGB(Theme.getColor(defaultFabColor), Color.WHITE, 0.1f);
            pressedBackgroundColor = Theme.blendOver(Theme.getColor(defaultFabColor), Theme.getColor(Theme.key_listSelector));
        }

        return Theme.createSimpleSelectorRoundRectDrawable(
                dp(fabCornerRadiusDp),
                defaultBackgroundColor,
                pressedBackgroundColor
        );
    }
}
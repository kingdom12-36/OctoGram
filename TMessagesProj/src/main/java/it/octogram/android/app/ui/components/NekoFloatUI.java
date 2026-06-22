/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.app.ui.components;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.core.content.ContextCompat;

import org.telegram.messenger.R;

/**
 * NekoFloatUI — programmatic helper for the Neko.Float 3D elevation system.
 *
 * <p>Applies the drawable/neko_float_*.xml design tokens to any view
 * with a single static call. Works alongside the XML styles defined in
 * values/styles.xml (Neko.Float, Neko.Float.Card, Neko.Float.Button).</p>
 *
 * <pre>
 *   // Card background + elevation
 *   NekoFloatUI.applyCard(myFrameLayout);
 *
 *   // Button background + ripple + elevation
 *   NekoFloatUI.applyButton(myTextView);
 *
 *   // Wrap an existing view in a 3D card shell
 *   FrameLayout card = NekoFloatUI.wrapInCard(myView, 8);
 * </pre>
 */
public final class NekoFloatUI {

    /** Elevation for a raised card — matches Neko.Float.Card. */
    public static final float CARD_ELEVATION_DP  = 6f;
    /** Elevation for a button — matches Neko.Float.Button. */
    public static final float BTN_ELEVATION_DP   = 4f;
    /** Elevation for a bottom sheet — matches Neko.Float.BottomSheet. */
    public static final float SHEET_ELEVATION_DP = 8f;

    private NekoFloatUI() {}

    /** Apply 3D card background and elevation to an existing view. */
    public static void applyCard(View view) {
        view.setBackground(ContextCompat.getDrawable(view.getContext(), R.drawable.neko_float_card));
        view.setElevation(dp(CARD_ELEVATION_DP));
        view.setClipToOutline(true);
    }

    /** Apply 3D button background (with ripple) and elevation to an existing view. */
    public static void applyButton(View view) {
        view.setBackground(ContextCompat.getDrawable(view.getContext(), R.drawable.neko_float_ripple));
        view.setElevation(dp(BTN_ELEVATION_DP));
        view.setClipToOutline(true);
    }

    /** Apply 3D bottom-sheet background and highest elevation to an existing view. */
    public static void applyBottomSheet(View view) {
        view.setBackground(ContextCompat.getDrawable(view.getContext(), R.drawable.neko_float_card));
        view.setElevation(dp(SHEET_ELEVATION_DP));
        view.setClipToOutline(true);
    }

    /**
     * Wrap {@code view} in a FrameLayout that carries the 3D card background,
     * replacing it in the view hierarchy.
     *
     * @param view   Target view — must already have a ViewGroup parent.
     * @param margin Uniform outer margin in dp on all four sides.
     * @return The new card FrameLayout, now attached to the original parent.
     */
    public static FrameLayout wrapInCard(View view, int margin) {
        return wrapInCard(view, margin, margin, margin, margin);
    }

    /**
     * Wrap {@code view} in a FrameLayout card with per-side margins.
     *
     * @param view           Target view — must already have a ViewGroup parent.
     * @param marginStartDp  Start margin in dp.
     * @param marginTopDp    Top margin in dp.
     * @param marginEndDp    End margin in dp.
     * @param marginBottomDp Bottom margin in dp.
     * @return The new card FrameLayout, now attached to the original parent.
     */
    public static FrameLayout wrapInCard(View view,
                                         int marginStartDp, int marginTopDp,
                                         int marginEndDp,   int marginBottomDp) {
        ViewGroup parent = (ViewGroup) view.getParent();
        int index = parent.indexOfChild(view);
        ViewGroup.LayoutParams originalLp = view.getLayoutParams();

        parent.removeView(view);

        FrameLayout card = new FrameLayout(view.getContext());
        applyCard(card);

        FrameLayout.LayoutParams innerLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        innerLp.setMargins(dp(marginStartDp), dp(marginTopDp),
                           dp(marginEndDp),   dp(marginBottomDp));
        card.addView(view, innerLp);
        parent.addView(card, index, originalLp);
        return card;
    }
}

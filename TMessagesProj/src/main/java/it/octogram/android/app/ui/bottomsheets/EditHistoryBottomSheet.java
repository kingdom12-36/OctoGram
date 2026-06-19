/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */
package it.octogram.android.app.ui.bottomsheets;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import it.octogram.android.utils.chat.MessageEditsDatabase;

/**
 * OctoGram: Bottom sheet that displays the locally saved edit history of a message.
 * Shown when the user taps "Edit History" in the message long-press context menu
 * (only visible when Save Edits History is enabled in General Settings).
 */
public class EditHistoryBottomSheet extends BottomSheet {

    public EditHistoryBottomSheet(BaseFragment fragment, MessageObject messageObject) {
        super(fragment.getContext(), false, fragment.getResourceProvider());

        Context context = fragment.getContext();
        List<MessageEditsDatabase.EditEntry> edits = MessageEditsDatabase.getInstance(context)
                .getEdits(messageObject.getDialogId(), messageObject.getId());

        setTitle(LocaleController.getString(R.string.OctoEditHistory), true);

        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(0, AndroidUtilities.dp(4), 0, AndroidUtilities.dp(20));

        if (edits.isEmpty()) {
            TextView empty = new TextView(context);
            empty.setText(LocaleController.getString(R.string.OctoEditHistory_Empty));
            empty.setTextSize(15);
            empty.setGravity(Gravity.CENTER_HORIZONTAL);
            empty.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2, resourcesProvider));
            empty.setPadding(AndroidUtilities.dp(20), AndroidUtilities.dp(24), AndroidUtilities.dp(20), AndroidUtilities.dp(8));
            container.addView(empty);
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("d MMM yyyy \u00b7 HH:mm", Locale.getDefault());
            for (int i = 0; i < edits.size(); i++) {
                MessageEditsDatabase.EditEntry entry = edits.get(i);

                LinearLayout row = new LinearLayout(context);
                row.setOrientation(LinearLayout.VERTICAL);
                row.setPadding(
                        AndroidUtilities.dp(20), AndroidUtilities.dp(12),
                        AndroidUtilities.dp(20), AndroidUtilities.dp(12));

                TextView header = new TextView(context);
                header.setText("Version " + (i + 1) + "  \u00b7  " + sdf.format(new Date(entry.date * 1000L)));
                header.setTextSize(12);
                header.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText3, resourcesProvider));
                row.addView(header);

                TextView body = new TextView(context);
                body.setText(entry.text);
                body.setTextSize(15);
                body.setTextIsSelectable(true);
                body.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText, resourcesProvider));
                LinearLayout.LayoutParams bodyParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                bodyParams.topMargin = AndroidUtilities.dp(4);
                row.addView(body, bodyParams);

                container.addView(row);

                if (i < edits.size() - 1) {
                    View divider = new View(context);
                    divider.setBackgroundColor(Theme.getColor(Theme.key_divider, resourcesProvider));
                    LinearLayout.LayoutParams divParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, 1);
                    divParams.leftMargin = AndroidUtilities.dp(20);
                    container.addView(divider, divParams);
                }
            }
        }

        ScrollView scrollView = new ScrollView(context);
        scrollView.addView(container, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT));
        setCustomView(scrollView);
    }
}

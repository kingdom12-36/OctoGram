/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils.chat;

import android.content.Context;
import android.view.ViewPropertyAnimator;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.ActionBarMenuSubItem;
import org.telegram.ui.ActionBar.ActionBarPopupWindow;

import java.util.ArrayList;

import it.octogram.android.OctoConfig;
import it.octogram.android.app.ui.cells.ChatSettingsPreviewsCell.ContextMenuPreviewItem;

public class ContextMenuHelper {
    private static final int MAX_SHORTCUTS = 4;
    public static final ContextMenuHelper INSTANCE = new ContextMenuHelper();

    // Briefing state constants (mirrors ContextMenuBriefingState enum ordinal/state values)
    private static final int BRIEFING_DISABLED = 0;
    private static final int BRIEFING_SUBCATEGORIES = 1;
    private static final int BRIEFING_SHORTCUTS = 2;

    public static boolean mustUseSwipeback() {
        return OctoConfig.INSTANCE.contextMenuBriefingState.getValue() == BRIEFING_SUBCATEGORIES;
    }

    public static boolean mustUseSwipeBack() {
        return mustUseSwipeback();
    }

    public static ArrayList<ContextMenuPreviewItem> fillPreviewMenu(Context context) {
        return new ContextMenuComposer(context).build();
    }

    public static void fillMenu(Context context, ArrayList<CharSequence> items, ArrayList<Integer> options, ArrayList<Integer> icons, OnItemAddReady callback) {
        new ContextMenuComposer(context, items, options, icons, callback);
    }

    private static class ContextMenuComposer {
        private final ArrayList<ContextMenuPreviewItem> finalItems = new ArrayList<>();

        /** Preview constructor — builds item list for the settings preview cell. */
        public ContextMenuComposer(Context context) {
            for (int i = 0; i < ContextMenuScanHelper.options.size(); i++) {
                CharSequence label = i < ContextMenuScanHelper.items.size()
                        ? ContextMenuScanHelper.items.get(i) : "";
                int icon = i < ContextMenuScanHelper.icons.size()
                        ? ContextMenuScanHelper.icons.get(i) : 0;
                ContextMenuPreviewItem item = new ContextMenuPreviewItem(ContextMenuPreviewItem.ITEM);
                item.name = label;
                item.icon = icon;
                finalItems.add(item);
            }
            if (!finalItems.isEmpty()) {
                // Add shortcuts row preview if in shortcuts mode
                if (OctoConfig.INSTANCE.contextMenuBriefingState.getValue() == BRIEFING_SHORTCUTS) {
                    ContextMenuPreviewItem shortcutsItem = new ContextMenuPreviewItem(ContextMenuPreviewItem.SHORTCUTS);
                    finalItems.add(0, shortcutsItem);
                }
            }
        }

        /** Real menu constructor — adds items to popup window via callback. */
        public ContextMenuComposer(Context context, ArrayList<CharSequence> items, ArrayList<Integer> options, ArrayList<Integer> icons, @Nullable OnItemAddReady callback) {
            if (callback == null || options == null || options.isEmpty()) return;

            ActionBarPopupWindow.ActionBarPopupWindowLayout layout = callback.getPopupWindowLayout();
            if (layout == null) return;

            int briefingState = OctoConfig.INSTANCE.contextMenuBriefingState.getValue();

            if (briefingState == BRIEFING_SHORTCUTS) {
                addShortcutsRow(context, items, options, icons, layout, callback);
                addRemainingItems(items, options, icons, layout, callback, true);
            } else if (briefingState == BRIEFING_SUBCATEGORIES) {
                addSubcategoryItems(items, options, icons, layout, callback);
            } else {
                // DISABLED — flat list
                addAllFlat(items, options, icons, layout, callback);
            }
        }

        private void addAllFlat(ArrayList<CharSequence> items, ArrayList<Integer> options, ArrayList<Integer> icons, ActionBarPopupWindow.ActionBarPopupWindowLayout layout, OnItemAddReady callback) {
            for (int i = 0; i < options.size(); i++) {
                addSingleItem(items, options, icons, i, layout, callback);
            }
        }

        private void addShortcutsRow(Context context, ArrayList<CharSequence> items, ArrayList<Integer> options, ArrayList<Integer> icons, ActionBarPopupWindow.ActionBarPopupWindowLayout layout, OnItemAddReady callback) {
            ArrayList<Integer> pinned = ContextMenuScanHelper.pinnedOptions;
            ArrayList<CharSequence> sItems = new ArrayList<>();
            ArrayList<Integer> sOpts = new ArrayList<>();
            ArrayList<Integer> sIcons = new ArrayList<>();

            for (int i = 0; i < options.size() && sOpts.size() < MAX_SHORTCUTS; i++) {
                if (pinned.contains(options.get(i))) {
                    sItems.add(i < items.size() ? items.get(i) : "");
                    sOpts.add(options.get(i));
                    sIcons.add(i < icons.size() ? icons.get(i) : 0);
                }
            }
            if (!sOpts.isEmpty()) {
                ShortcutsLayout sl = new ShortcutsLayout(context);
                sl.fillOptions(sItems, sOpts, sIcons);
                layout.addView(sl);
                callback.onShortcutsAdd(sl);
                callback.onSeparatorAdd();
            }
        }

        private void addRemainingItems(ArrayList<CharSequence> items, ArrayList<Integer> options, ArrayList<Integer> icons, ActionBarPopupWindow.ActionBarPopupWindowLayout layout, OnItemAddReady callback, boolean skipPinned) {
            ArrayList<Integer> pinned = ContextMenuScanHelper.pinnedOptions;
            int pinnedSkipped = 0;
            for (int i = 0; i < options.size(); i++) {
                if (skipPinned && pinned.contains(options.get(i)) && pinnedSkipped < MAX_SHORTCUTS) {
                    pinnedSkipped++;
                    continue;
                }
                addSingleItem(items, options, icons, i, layout, callback);
            }
        }

        private void addSubcategoryItems(ArrayList<CharSequence> items, ArrayList<Integer> options, ArrayList<Integer> icons, ActionBarPopupWindow.ActionBarPopupWindowLayout layout, OnItemAddReady callback) {
            if (ContextMenuScanHelper.subcategories.isEmpty()) {
                addAllFlat(items, options, icons, layout, callback);
                return;
            }
            for (ContextMenuScanHelper.SubCategories sub : ContextMenuScanHelper.subcategories) {
                ArrayList<Integer> catOpts = sub.getCategoryOptions();
                if (catOpts.isEmpty()) continue;
                callback.onSeparatorAdd();
                for (int i = 0; i < options.size(); i++) {
                    if (catOpts.contains(options.get(i))) {
                        addSingleItem(items, options, icons, i, layout, callback);
                    }
                }
            }
        }

        private void addSingleItem(ArrayList<CharSequence> items, ArrayList<Integer> options, ArrayList<Integer> icons, int i, ActionBarPopupWindow.ActionBarPopupWindowLayout layout, OnItemAddReady callback) {
            int id = options.get(i);
            CharSequence title = i < items.size() ? items.get(i) : "";
            int icon = i < icons.size() ? icons.get(i) : 0;
            ActionBarMenuSubItem subItem = ActionBarMenuItem.addItem(layout, icon, title, false, null);
            subItem.setOnClickListener(v -> callback.onItemClick(id));
            callback.onItemAdd(id, subItem);
        }

        private ArrayList<ContextMenuPreviewItem> build() {
            return finalItems;
        }
    }

    public interface OnItemAddReady {
        default ActionBarPopupWindow.ActionBarPopupWindowLayout getPopupWindowLayout() { return null; }
        default void onItemAdd(int id, ActionBarMenuSubItem item) {}
        default void onShortcutsAdd(ShortcutsLayout shortcutsLayout) {}
        default void onSeparatorAdd() {}
        default void onItemClick(int id) {}
    }

    public static class ShortcutsLayout extends LinearLayout {
        public ShortcutsLayout(Context context) {
            super(context);
            setOrientation(HORIZONTAL);
        }

        private boolean isFirstAppear = true;
        private boolean lastDrawState = false;
        private boolean isAnimatingPreview = false;
        private final ArrayList<ViewPropertyAnimator> animators = new ArrayList<>();

        public void fillOptions(ArrayList<CharSequence> items, ArrayList<Integer> options, ArrayList<Integer> icons) {
            fillOptions(items, options, icons, false);
        }

        public void fillOptions(ArrayList<CharSequence> items, ArrayList<Integer> options, ArrayList<Integer> icons, boolean faster) {
            removeAllViews();
        }

        public void fillPreviewOptions() {}

        public void setOnItemClick(Object onItemClick) {}
    }

    public Object getAccountInstance(int currentAccount) {
        return null;
    }

    public Object getMessagesController(int currentAccount) {
        return null;
    }
}

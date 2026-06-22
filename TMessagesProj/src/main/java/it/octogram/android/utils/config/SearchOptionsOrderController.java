/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils.config;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import it.octogram.android.OctoConfig;
import it.octogram.android.utils.OctoLogging;

public class SearchOptionsOrderController {
    private static final String TAG = "SearchOptionsOrderController";

    /**
     * Default search tab order — mirrors SearchViewPager.ViewPagerAdapter's null-branch default.
     *
     * Positive values = SearchViewPager.ViewPagerAdapter type constants:
     *   0 = DIALOGS_TYPE
     *   1 = CHANNELS_TYPE
     *   2 = DOWNLOADS_TYPE
     *   4 = BOTS_TYPE
     *   5 = PUBLIC_POSTS_TYPE  (skipped by pager when !expandedPublicPosts)
     *   6 = POSTS_TYPE
     *
     * Negative values encode FILTER_TYPE tab indices: filterIndex = (-option - 1)
     *   -1 → filterIndex 0 (Media)
     *   -2 → filterIndex 1 (Links)
     *   -3 → filterIndex 2 (Files)
     *   -4 → filterIndex 3 (Music)
     *   -5 → filterIndex 4 (Voice)
     */
    public static final List<Integer> DEFAULT_ORDER = Arrays.asList(
            0,  // All Chats
            5,  // Public Posts (hidden unless expandedPublicPosts)
            1,  // Channels
            4,  // Bots / Apps
            6,  // Posts
            -1, // Media
            2,  // Downloads
            -2, // Links
            -3, // Files
            -4, // Music
            -5  // Voice
    );

    private static boolean isOptionValid(int option) {
        if (option >= 0) {
            // Valid positive type constants: 0-6
            return option <= 6;
        }
        // Valid filter indices encoded as negative: -1 to -5
        return option >= -5;
    }

    public static void resetOrdering() {
        OctoConfig.INSTANCE.searchOptionsOrder.updateValue("[]");
    }

    /**
     * Returns the current search tab order.
     * Returns the default order list when no custom order has been saved.
     * Never returns null — callers that rely on null-check should use isUsingCustomVersion() instead.
     */
    public static List<Integer> getCurrentOrder() {
        String stored = OctoConfig.INSTANCE.searchOptionsOrder.getValue();
        if (stored == null || stored.isEmpty() || stored.equals("[]")) {
            return new ArrayList<>(DEFAULT_ORDER);
        }
        try {
            JSONArray arr = new JSONArray(stored);
            List<Integer> order = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                int opt = arr.getInt(i);
                if (isOptionValid(opt)) {
                    order.add(opt);
                }
            }
            if (order.isEmpty()) {
                return new ArrayList<>(DEFAULT_ORDER);
            }
            return order;
        } catch (JSONException e) {
            OctoLogging.e(TAG, "Failed to parse searchOptionsOrder: " + e.getMessage());
            return new ArrayList<>(DEFAULT_ORDER);
        }
    }

    public static void saveCurrentOrder(List<Integer> options) {
        JSONArray arr = new JSONArray();
        for (int opt : options) {
            arr.put(opt);
        }
        OctoConfig.INSTANCE.searchOptionsOrder.updateValue(arr.toString());
    }

    public static boolean isOrderValid(String items) {
        if (items == null || items.isEmpty() || items.equals("[]")) {
            return true;
        }
        try {
            JSONArray arr = new JSONArray(items);
            for (int i = 0; i < arr.length(); i++) {
                if (!isOptionValid(arr.getInt(i))) {
                    return false;
                }
            }
            return true;
        } catch (JSONException e) {
            return false;
        }
    }

    /**
     * Returns true when the user has saved a custom order that differs from the default.
     * Used by SearchViewPager to decide which view-type IDs to assign to filter tabs.
     */
    public static boolean isUsingCustomVersion() {
        String stored = OctoConfig.INSTANCE.searchOptionsOrder.getValue();
        return stored != null && !stored.isEmpty() && !stored.equals("[]");
    }
}

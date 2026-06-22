/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils.ai.linkit;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import it.octogram.android.OctoConfig;
import it.octogram.android.utils.OctoLogging;
import it.octogram.android.utils.ai.AiPrompt;
import it.octogram.android.utils.ai.MainAiHelper;

/**
 * LinkitAiHelper — integrates the Shamsaver Cloudflare AI worker as a
 * first-class OctoGram AI provider.
 *
 * <p>Endpoint: POST https://telegram-ai-bot.shamsaver1.workers.dev/api/chat
 * <br>Request:  {"messages":[{"role":"system","content":"..."},{"role":"user","content":"..."}]}
 * <br>Response: {"reply":"..."}</p>
 *
 * <p>No API key is required — the worker is already authenticated.</p>
 */
public class LinkitAiHelper {

    private static final String TAG      = "LinkitAiHelper";
    private static final String ENDPOINT = "https://telegram-ai-bot.shamsaver1.workers.dev/api/chat";

    // ── Public API ────────────────────────────────────────────────────────

    public static boolean isAvailable() {
        return OctoConfig.INSTANCE.aiFeaturesUseLinkitAI.getValue();
    }

    public static void prompt(AiPrompt aiPrompt, MainAiHelper.OnResultState callback) {
        if (!isAvailable()) {
            callback.onFailed();
            return;
        }

        new Thread(() -> {
            try {
                String reply = callWorker(aiPrompt.getPrompt(), aiPrompt.getText());
                if (reply == null || reply.isBlank()) {
                    callback.onEmptyResponse();
                } else {
                    callback.onSuccess(reply.strip());
                }
            } catch (Exception e) {
                OctoLogging.e(TAG, "Linkit AI request failed", e);
                callback.onFailed();
            }
        }).start();
    }

    // ── Internal ──────────────────────────────────────────────────────────

    private static String callWorker(String systemPrompt, String userMessage) throws Exception {
        JSONArray messages = new JSONArray();

        if (systemPrompt != null && !systemPrompt.isBlank()) {
            messages.put(new JSONObject()
                    .put("role", "system")
                    .put("content", systemPrompt));
        }

        messages.put(new JSONObject()
                .put("role", "user")
                .put("content", userMessage));

        JSONObject body = new JSONObject().put("messages", messages);

        HttpURLConnection conn = (HttpURLConnection) new URL(ENDPOINT).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(30_000);
        conn.setReadTimeout(180_000);
        conn.setDoOutput(true);

        try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), "UTF-8"))) {
            w.write(body.toString());
        }

        int code = conn.getResponseCode();
        boolean ok = code >= 200 && code < 300;
        String raw = new String(
                (ok ? conn.getInputStream() : conn.getErrorStream()).readAllBytes(),
                "UTF-8"
        ).trim();
        conn.disconnect();

        if (!ok) {
            OctoLogging.e(TAG, "HTTP " + code + ": " + raw);
            throw new Exception("HTTP " + code);
        }

        // Worker returns plain "OK" on health checks — wrap defensively
        JSONObject resp;
        try {
            resp = new JSONObject(raw);
        } catch (Exception e) {
            return raw; // plain text reply
        }

        return resp.optString("reply", raw);
    }
}

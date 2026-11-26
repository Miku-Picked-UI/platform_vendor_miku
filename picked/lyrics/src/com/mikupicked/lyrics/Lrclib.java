/*
 * SPDX-FileCopyrightText: 2026 Miku-Picked UI
 * SPDX-License-Identifier: Apache-2.0
 */

package com.mikupicked.lyrics;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lyrics from lrclib.net (free, no key), like SystemUI's status bar lyrics
 * (MediaLyricController): timed (LRC) lyrics when there are, else the plain text.
 */
final class Lrclib {

    private static final String TAG = "PickedLyrics";
    private static final String GET = "https://lrclib.net/api/get";
    // lrclib asks clients to identify themselves; no personal data, just the project.
    private static final String USER_AGENT =
            "MikuPickedUI-Lyrics (https://github.com/Miku-Picked-UI)";
    private static final int TIMEOUT_MS = 8000;

    private static final Pattern LRC_LINE =
            Pattern.compile("((?:\\[\\d{1,2}:\\d{2}(?:[.:]\\d{1,3})?\\])+)(.*)");
    private static final Pattern LRC_TAG =
            Pattern.compile("\\[(\\d{1,2}):(\\d{2})(?:[.:](\\d{1,3}))?\\]");

    /** One line; {@code timeMs} is -1 for plain (untimed) lyrics. */
    static final class Line {
        final long timeMs;
        final String text;

        Line(long timeMs, String text) {
            this.timeMs = timeMs;
            this.text = text;
        }
    }

    static final class Lyrics {
        final List<Line> lines;
        final boolean synced;

        Lyrics(List<Line> lines, boolean synced) {
            this.lines = lines;
            this.synced = synced;
        }
    }

    private Lrclib() {}

    /** Blocking; null when lrclib has nothing for the track or can't be reached. */
    static Lyrics fetch(String title, String artist, String album, long durationMs) {
        HttpURLConnection conn = null;
        try {
            StringBuilder url = new StringBuilder(GET)
                    .append("?track_name=").append(enc(title))
                    .append("&artist_name=").append(enc(artist));
            if (!TextUtils.isEmpty(album)) url.append("&album_name=").append(enc(album));
            if (durationMs > 0) url.append("&duration=").append(durationMs / 1000);
            conn = (HttpURLConnection) new URL(url.toString()).openConnection();
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) return null;
            String body;
            try (InputStream in = conn.getInputStream()) {
                body = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
            JSONObject json = new JSONObject(body);
            String synced = json.optString("syncedLyrics", "");
            if (!TextUtils.isEmpty(synced) && !"null".equals(synced)) {
                List<Line> lines = parseLrc(synced);
                if (!lines.isEmpty()) return new Lyrics(lines, true);
            }
            String plain = json.optString("plainLyrics", "");
            if (!TextUtils.isEmpty(plain) && !"null".equals(plain)) {
                List<Line> lines = new ArrayList<>();
                for (String raw : plain.split("\n")) lines.add(new Line(-1, raw.trim()));
                return new Lyrics(lines, false);
            }
            return null;
        } catch (Exception e) {
            Log.w(TAG, "lrclib request failed", e);
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private static List<Line> parseLrc(String lrc) {
        List<Line> lines = new ArrayList<>();
        for (String raw : lrc.split("\n")) {
            Matcher m = LRC_LINE.matcher(raw.trim());
            if (!m.matches()) continue;
            String text = m.group(2).trim();
            Matcher tags = LRC_TAG.matcher(m.group(1));
            while (tags.find()) {
                long min = Long.parseLong(tags.group(1));
                long sec = Long.parseLong(tags.group(2));
                long frac = 0;
                if (tags.group(3) != null) {
                    String f = tags.group(3);
                    frac = Long.parseLong(f) * (f.length() == 2 ? 10 : (f.length() == 1 ? 100 : 1));
                }
                lines.add(new Line(min * 60_000 + sec * 1000 + frac, text));
            }
        }
        lines.sort((a, b) -> Long.compare(a.timeMs, b.timeMs));
        return lines;
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}

/*
 * Copyright (C) 2026 The PenguinOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.penguin.laboratory.edgelighting;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/** Everything the settings screen saves and the provider hands to SystemUI. */
public final class EdgeLightingStore {

    static final String AUTHORITY = "com.penguin.laboratory.provider";
    static final Uri STATE_URI = Uri.parse("content://" + AUTHORITY + "/state");
    /** Asks SystemUI to play the effect now, so it can be seen without waiting for a notification. */
    static final String ACTION_PREVIEW = "com.penguin.laboratory.action.EDGE_LIGHTING_PREVIEW";

    // Motions SystemUI knows how to play
    static final String EFFECT_RAIN = "rain";
    static final String EFFECT_SNOW = "snow";
    static final String EFFECT_BUBBLES = "bubbles";
    static final String EFFECT_BURST = "burst";

    static final String KEY_ENABLED = "enabled";
    static final String KEY_EFFECT = "effect";
    static final String KEY_SIZE = "size";
    static final String KEY_AMOUNT = "amount";
    static final String KEY_DURATION = "duration";
    static final String KEY_ROTATION = "rotation";
    static final String KEY_COLOR = "color";
    static final String KEY_APP_ICON = "app_icon";
    static final String KEY_SCREEN_OFF_ONLY = "screen_off_only";
    static final String KEY_SELECTED = "selected";
    private static final String KEY_KEYWORDS = "keywords";

    private static final String PREFS = "edge_lighting";
    private static final String IMAGES_DIR = "images";
    private static final String LEGACY_CUTOUT = "cutout.png";

    private EdgeLightingStore() {}

    static SharedPreferences prefs(Context context) {
        return context.createDeviceProtectedStorageContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static boolean isEnabled(Context context) {
        return prefs(context).getBoolean(KEY_ENABLED, false);
    }

    static String effect(Context context) {
        return prefs(context).getString(KEY_EFFECT, EFFECT_RAIN);
    }

    static int level(Context context, String key, int def) {
        return prefs(context).getInt(key, def);
    }

    static boolean flag(Context context, String key, boolean def) {
        return prefs(context).getBoolean(key, def);
    }

    static int color(Context context) {
        return prefs(context).getInt(KEY_COLOR, 0xFFFF5C8A);
    }

    static void put(Context context, String key, Object value) {
        SharedPreferences.Editor editor = prefs(context).edit();
        if (value instanceof Boolean b) editor.putBoolean(key, b);
        else if (value instanceof Integer i) editor.putInt(key, i);
        else editor.putString(key, String.valueOf(value));
        editor.apply();
        notifyChanged(context);
    }

    static File imagesDir(Context context) {
        File dir = new File(context.createDeviceProtectedStorageContext().getFilesDir(), IMAGES_DIR);
        if (!dir.isDirectory()) dir.mkdirs();
        migrateLegacyCutout(context, dir);
        return dir;
    }

    /** The first version kept a single cutout outside the images directory. */
    private static void migrateLegacyCutout(Context context, File dir) {
        File legacy = new File(context.createDeviceProtectedStorageContext().getFilesDir(),
                LEGACY_CUTOUT);
        if (!legacy.exists()) return;
        String id = "cutout-" + legacy.lastModified();
        if (legacy.renameTo(new File(dir, id + ".png"))) {
            prefs(context).edit().putString(KEY_SELECTED, id).apply();
        }
    }

    static File image(Context context, String id) {
        return id == null ? null : new File(imagesDir(context), id + ".png");
    }

    static String selected(Context context) {
        String id = prefs(context).getString(KEY_SELECTED, null);
        File file = image(context, id);
        return file != null && file.exists() ? id : null;
    }

    /** User cutouts, newest first. */
    static List<String> cutouts(Context context) {
        String[] names = imagesDir(context).list((d, n) -> n.startsWith("cutout-"));
        List<String> ids = new ArrayList<>();
        if (names == null) return ids;
        Arrays.sort(names, (a, b) -> b.compareTo(a));
        for (String name : names) ids.add(name.substring(0, name.length() - 4));
        return ids;
    }

    static void delete(Context context, String id) {
        File file = image(context, id);
        if (file != null) file.delete();
        if (id.equals(prefs(context).getString(KEY_SELECTED, null))) {
            prefs(context).edit().remove(KEY_SELECTED).apply();
        }
        List<Keyword> keywords = keywords(context);
        keywords.removeIf(k -> k.imageId.equals(id));
        setKeywords(context, keywords);
        notifyChanged(context);
    }

    /** A notification whose title or text contains {@link #word} plays {@link #imageId}. */
    static final class Keyword {
        final String word;
        final String imageId;

        Keyword(String word, String imageId) {
            this.word = word;
            this.imageId = imageId;
        }
    }

    static List<Keyword> keywords(Context context) {
        List<Keyword> list = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(prefs(context).getString(KEY_KEYWORDS, "[]"));
            for (int i = 0; i < array.length(); i++) {
                JSONObject o = array.getJSONObject(i);
                list.add(new Keyword(o.getString("word"), o.getString("image")));
            }
        } catch (JSONException e) {
            // Start over rather than fail on a bad entry
        }
        return list;
    }

    static void setKeywords(Context context, List<Keyword> keywords) {
        JSONArray array = new JSONArray();
        for (Keyword k : keywords) {
            try {
                array.put(new JSONObject().put("word", k.word).put("image", k.imageId));
            } catch (JSONException e) {
                // Strings always serialise
            }
        }
        prefs(context).edit().putString(KEY_KEYWORDS, array.toString()).apply();
        notifyChanged(context);
    }

    /** Returns the image of the first keyword found in {@code text}, if its image still exists. */
    static String resolve(Context context, String text) {
        if (text == null || text.isEmpty()) return null;
        String haystack = text.toLowerCase(Locale.ROOT);
        for (Keyword k : keywords(context)) {
            File file = image(context, k.imageId);
            if (haystack.contains(k.word.toLowerCase(Locale.ROOT)) && file.exists()) {
                return k.imageId;
            }
        }
        return null;
    }

    static void notifyChanged(Context context) {
        context.getContentResolver().notifyChange(STATE_URI, null);
    }
}

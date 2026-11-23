/*
 * Copyright (C) 2026 The PenguinOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.penguin.laboratory.wonderland;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Keeps Wonderland projects, each a folder with its scene, images and thumbnail, and which one
 * the wallpaper shows.
 */
public final class WonderlandStore {

    private static final String TAG = "Wonderland";
    private static final String PREFS = "wonderland";
    private static final String KEY_ACTIVE = "active";
    private static final String SCENE = "scene.json";
    static final String THUMBNAIL = "thumbnail.png";

    /** Told when a project is saved or another one is applied. */
    public interface Listener {
        void onChanged();
    }

    // The editor and the wallpaper run in the same process
    private static final List<Listener> sListeners = new CopyOnWriteArrayList<>();
    private static final Handler sMain = new Handler(Looper.getMainLooper());

    private WonderlandStore() {}

    public static void addListener(Listener listener) {
        sListeners.add(listener);
    }

    public static void removeListener(Listener listener) {
        sListeners.remove(listener);
    }

    private static void notifyChanged() {
        sMain.post(() -> {
            for (Listener l : sListeners) l.onChanged();
        });
    }

    private static File root(Context context) {
        File dir = new File(context.getFilesDir(), "wonderland");
        dir.mkdirs();
        return dir;
    }

    public static File dir(Context context, String project) {
        return new File(root(context), project);
    }

    public static File file(Context context, String project, String name) {
        return new File(dir(context, project), name);
    }

    /** Newest first. */
    public static List<String> projects(Context context) {
        File[] dirs = root(context).listFiles(f -> new File(f, SCENE).isFile());
        if (dirs == null) return new ArrayList<>();
        Arrays.sort(dirs, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
        List<String> ids = new ArrayList<>();
        for (File d : dirs) ids.add(d.getName());
        return ids;
    }

    public static String create(Context context) throws IOException {
        String project = Scene.newId("project");
        dir(context, project).mkdirs();
        save(context, project, Scene.starter(), null);
        return project;
    }

    public static Scene load(Context context, String project) {
        if (project == null) return null;
        try {
            byte[] json = Files.readAllBytes(file(context, project, SCENE).toPath());
            return Scene.fromJson(new String(json, StandardCharsets.UTF_8));
        } catch (IOException | JSONException e) {
            Log.w(TAG, "Couldn't load " + project, e);
            return null;
        }
    }

    /** Saves the scene, its thumbnail when given, and drops images no layer uses any more. */
    public static void save(Context context, String project, Scene scene, Bitmap thumbnail)
            throws IOException {
        try {
            write(file(context, project, SCENE),
                    scene.toJson().getBytes(StandardCharsets.UTF_8));
        } catch (JSONException e) {
            throw new IOException(e);
        }
        if (thumbnail != null) writeImage(context, project, THUMBNAIL, thumbnail);
        File[] files = dir(context, project).listFiles();
        if (files != null) {
            for (File f : files) {
                String name = f.getName();
                if (name.equals(SCENE) || name.equals(THUMBNAIL)) continue;
                boolean used = false;
                for (Scene.Layer l : scene.layers) used |= name.equals(l.file);
                if (!used) f.delete();
            }
        }
        notifyChanged();
    }

    public static void delete(Context context, String project) {
        File dir = dir(context, project);
        File[] files = dir.listFiles();
        if (files != null) for (File f : files) f.delete();
        dir.delete();
        if (project.equals(active(context))) prefs(context).edit().remove(KEY_ACTIVE).apply();
        notifyChanged();
    }

    /** Saves {@code bitmap} in the project and returns its file name. */
    public static String writeImage(Context context, String project, String name, Bitmap bitmap)
            throws IOException {
        File out = file(context, project, name);
        File tmp = new File(out.getPath() + ".tmp");
        try (FileOutputStream os = new FileOutputStream(tmp)) {
            bitmap.compress(name.endsWith(".jpg") ? Bitmap.CompressFormat.JPEG
                    : Bitmap.CompressFormat.PNG, 95, os);
        }
        if (!tmp.renameTo(out)) throw new IOException("Couldn't save " + name);
        return name;
    }

    private static void write(File out, byte[] data) throws IOException {
        File tmp = new File(out.getPath() + ".tmp");
        Files.write(tmp.toPath(), data);
        if (!tmp.renameTo(out)) throw new IOException("Couldn't save " + out);
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    /** The project the wallpaper shows, if it still exists. */
    public static String active(Context context) {
        String project = prefs(context).getString(KEY_ACTIVE, null);
        if (project != null && !file(context, project, SCENE).isFile()) return null;
        return project;
    }

    public static void setActive(Context context, String project) {
        prefs(context).edit().putString(KEY_ACTIVE, project).apply();
        notifyChanged();
    }
}

/*
 * Copyright (C) 2026 The PenguinOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.penguin.laboratory.wonderland;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A Wonderland wallpaper: a background and a stack of layers that each drift with the phone's
 * tilt by their own amount.
 *
 * <p>Positions are fractions of the screen and sizes fractions of its width, so a scene looks
 * the same in the editor's preview as on the display.
 */
public final class Scene {

    /** Fills the screen behind every other layer. */
    public static final String TYPE_BACKGROUND = "background";
    public static final String TYPE_IMAGE = "image";
    public static final String TYPE_TEXT = "text";
    public static final String TYPE_PARTICLES = "particles";

    public static final String PARTICLES_SNOW = "snow";
    public static final String PARTICLES_RAIN = "rain";
    public static final String PARTICLES_BUBBLES = "bubbles";
    public static final String PARTICLES_STARS = "stars";
    public static final String PARTICLES_FIREFLIES = "fireflies";
    public static final String PARTICLES_PETALS = "petals";

    public static final String FONT_SANS = "sans";
    public static final String FONT_SERIF = "serif";
    public static final String FONT_MONO = "mono";
    public static final String FONT_CONDENSED = "condensed";

    public static final class Layer {
        public String id;
        public String type;
        /** The image file in the project, for background and image layers. */
        public String file;
        public float x = 0.5f, y = 0.5f;
        public float scale = 0.6f;
        public float rotation;
        public float alpha = 1f;
        /** How far the layer drifts with the tilt on each axis, from -1 to 1. */
        public float motionX, motionY, motionZ;
        public boolean hidden;
        /** The photo a background or cutout came from, to tell when they share one. */
        public String source;
        /**
         * A cutout stays over its subject in the background while it shares the background's
         * photo, until it's moved by hand. Its bounds in that photo, as fractions of its size.
         */
        public boolean anchored;
        public float cropLeft, cropTop, cropRight, cropBottom;

        public String text;
        public String font = FONT_SANS;
        public int color = 0xFFFFFFFF;

        public String particles = PARTICLES_SNOW;
        /** Levels from 0 to 2. */
        public int amount = 1, size = 1, speed = 1;

        Layer copy() {
            try {
                return fromJson(toJson());
            } catch (JSONException e) {
                throw new IllegalStateException(e);
            }
        }

        JSONObject toJson() throws JSONException {
            JSONObject o = new JSONObject();
            o.put("id", id);
            o.put("type", type);
            o.putOpt("file", file);
            o.put("x", x);
            o.put("y", y);
            o.put("scale", scale);
            o.put("rotation", rotation);
            o.put("alpha", alpha);
            o.put("motionX", motionX);
            o.put("motionY", motionY);
            o.put("motionZ", motionZ);
            o.put("hidden", hidden);
            o.putOpt("source", source);
            o.put("anchored", anchored);
            o.put("cropLeft", cropLeft);
            o.put("cropTop", cropTop);
            o.put("cropRight", cropRight);
            o.put("cropBottom", cropBottom);
            o.putOpt("text", text);
            o.put("font", font);
            o.put("color", color);
            o.put("particles", particles);
            o.put("amount", amount);
            o.put("size", size);
            o.put("speed", speed);
            return o;
        }

        static Layer fromJson(JSONObject o) {
            Layer l = new Layer();
            l.id = o.optString("id");
            l.type = o.optString("type", TYPE_IMAGE);
            l.file = o.optString("file", null);
            l.x = (float) o.optDouble("x", 0.5);
            l.y = (float) o.optDouble("y", 0.5);
            l.scale = (float) o.optDouble("scale", 0.6);
            l.rotation = (float) o.optDouble("rotation", 0);
            l.alpha = (float) o.optDouble("alpha", 1);
            l.motionX = (float) o.optDouble("motionX", 0);
            l.motionY = (float) o.optDouble("motionY", 0);
            l.motionZ = (float) o.optDouble("motionZ", 0);
            l.hidden = o.optBoolean("hidden");
            l.source = o.optString("source", null);
            l.anchored = o.optBoolean("anchored");
            l.cropLeft = (float) o.optDouble("cropLeft", 0);
            l.cropTop = (float) o.optDouble("cropTop", 0);
            l.cropRight = (float) o.optDouble("cropRight", 1);
            l.cropBottom = (float) o.optDouble("cropBottom", 1);
            l.text = o.optString("text", null);
            l.font = o.optString("font", FONT_SANS);
            l.color = o.optInt("color", 0xFFFFFFFF);
            l.particles = o.optString("particles", PARTICLES_SNOW);
            l.amount = o.optInt("amount", 1);
            l.size = o.optInt("size", 1);
            l.speed = o.optInt("speed", 1);
            return l;
        }
    }

    /** Shown when there is no background photo. */
    public int backgroundColor = 0xFF101828;
    /** Scales every layer's motion, from 0 to 1. */
    public float sensitivity = 0.6f;
    /** Bottom to top; a background layer, when there is one, comes first. */
    public final List<Layer> layers = new ArrayList<>();

    public Layer background() {
        return !layers.isEmpty() && TYPE_BACKGROUND.equals(layers.get(0).type)
                ? layers.get(0) : null;
    }

    public Layer find(String id) {
        for (Layer l : layers) if (l.id.equals(id)) return l;
        return null;
    }

    /** Whether anything in the scene moves by itself, so it needs drawing every frame. */
    public boolean animates() {
        for (Layer l : layers) {
            if (!l.hidden && TYPE_PARTICLES.equals(l.type)) return true;
        }
        return false;
    }

    /** Whether anything follows the tilt. */
    public boolean followsTilt() {
        if (sensitivity <= 0f) return false;
        for (Layer l : layers) {
            if (!l.hidden && (l.motionX != 0f || l.motionY != 0f || l.motionZ != 0f)) return true;
        }
        return false;
    }

    public static String newId(String prefix) {
        return prefix + "-" + Long.toString(System.nanoTime(), 36);
    }

    String toJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("backgroundColor", backgroundColor);
        o.put("sensitivity", sensitivity);
        JSONArray array = new JSONArray();
        for (Layer l : layers) array.put(l.toJson());
        o.put("layers", array);
        return o.toString();
    }

    static Scene fromJson(String json) throws JSONException {
        JSONObject o = new JSONObject(json);
        Scene s = new Scene();
        s.backgroundColor = o.optInt("backgroundColor", s.backgroundColor);
        s.sensitivity = (float) o.optDouble("sensitivity", s.sensitivity);
        JSONArray array = o.optJSONArray("layers");
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                s.layers.add(Layer.fromJson(array.getJSONObject(i)));
            }
        }
        return s;
    }

    /** A scene to start a new project from: a starry night with a little motion. */
    static Scene starter() {
        Scene s = new Scene();
        Layer stars = new Layer();
        stars.id = newId("layer");
        stars.type = TYPE_PARTICLES;
        stars.particles = PARTICLES_STARS;
        stars.motionX = stars.motionY = -0.3f;
        s.layers.add(stars);
        return s;
    }
}

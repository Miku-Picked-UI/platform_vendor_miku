/*
 * Copyright (C) 2026 The PenguinOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.penguin.laboratory.wonderland;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/** Draws a {@link Scene} for a given tilt, moving its particles along as time passes. */
final class SceneRenderer {

    // How far a layer with full motion drifts at full tilt, as a fraction of the screen width
    private static final float MAX_DRIFT = 0.07f;
    // How much a layer with full depth motion grows or shrinks at full tilt
    private static final float MAX_ZOOM = 0.12f;
    // A text layer's text size per unit of scale, as a fraction of the screen width
    static final float TEXT_SIZE = 0.3f;

    private static final int[] PARTICLE_COUNTS = {30, 70, 140};
    private static final float[] PARTICLE_SIZES = {0.6f, 1f, 1.6f};
    private static final float[] PARTICLE_SPEEDS = {0.5f, 1f, 1.8f};

    private final Context mContext;
    private final Paint mBitmapPaint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);
    private final Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mParticlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF mRect = new RectF();
    private final RectF mBackgroundRest = new RectF();
    private final Random mRandom = new Random();
    private final Map<String, Bitmap> mBitmaps = new HashMap<>();
    private final Map<String, ParticleField> mFields = new HashMap<>();

    private String mProject;
    private Scene mScene;
    private long mLastFrameNanos;

    SceneRenderer(Context context) {
        mContext = context;
        mTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    void setScene(String project, Scene scene) {
        if (project != null && !project.equals(mProject)) {
            mBitmaps.clear();
            mFields.clear();
        }
        mProject = project;
        mScene = scene;
        if (scene == null) return;
        // Keep what the scene still uses; image files are never rewritten under the same name
        Set<String> files = new HashSet<>();
        Set<String> ids = new HashSet<>();
        for (Scene.Layer l : scene.layers) {
            if (l.file != null) files.add(l.file);
            ids.add(l.id);
        }
        mBitmaps.keySet().retainAll(files);
        mFields.keySet().retainAll(ids);
    }

    Scene scene() {
        return mScene;
    }

    /** Whether the scene must be redrawn every frame even when the phone is still. */
    boolean animates() {
        return mScene != null && mScene.animates();
    }

    Bitmap bitmap(String file) {
        if (file == null || mProject == null) return null;
        Bitmap b = mBitmaps.get(file);
        if (b == null) {
            File f = WonderlandStore.file(mContext, mProject, file);
            b = BitmapFactory.decodeFile(f.getPath());
            if (b == null) return null;
            mBitmaps.put(file, b);
        }
        return b;
    }

    void draw(Canvas canvas, int w, int h, float tiltX, float tiltY, long frameNanos) {
        float dt = mLastFrameNanos == 0 ? 0f
                : Math.min(0.05f, (frameNanos - mLastFrameNanos) / 1e9f);
        mLastFrameNanos = frameNanos;
        if (mScene == null) {
            canvas.drawColor(0xFF000000);
            return;
        }
        canvas.drawColor(mScene.backgroundColor | 0xFF000000);
        float drift = MAX_DRIFT * w * mScene.sensitivity;
        Scene.Layer background = mScene.background();
        boolean hasBackground = background != null
                && backgroundRect(background, w, h, 0f, 0f, 1f, drift, mBackgroundRest);
        for (Scene.Layer l : mScene.layers) {
            if (hasBackground && l.anchored && l.source != null
                    && l.source.equals(background.source)) {
                pin(l, w, h);
            }
            if (l.hidden) continue;
            float dx = tiltX * l.motionX * drift;
            float dy = tiltY * l.motionY * drift;
            float zoom = 1f + tiltY * l.motionZ * MAX_ZOOM * mScene.sensitivity;
            switch (l.type) {
                case Scene.TYPE_BACKGROUND -> drawBackground(canvas, l, w, h, dx, dy, zoom, drift);
                case Scene.TYPE_IMAGE -> drawImage(canvas, l, w, h, dx, dy, zoom);
                case Scene.TYPE_TEXT -> drawText(canvas, l, w, h, dx, dy, zoom);
                case Scene.TYPE_PARTICLES -> drawParticles(canvas, l, w, h, dx, dy, zoom, dt);
            }
        }
    }

    private void drawBackground(Canvas canvas, Scene.Layer l, int w, int h, float dx, float dy,
            float zoom, float drift) {
        if (!backgroundRect(l, w, h, dx, dy, zoom, drift, mRect)) return;
        mBitmapPaint.setAlpha(Math.round(l.alpha * 255));
        canvas.drawBitmap(bitmap(l.file), null, mRect, mBitmapPaint);
    }

    /** Where the background photo is drawn, covering the screen however it drifts. */
    private boolean backgroundRect(Scene.Layer l, int w, int h, float dx, float dy, float zoom,
            float drift, RectF out) {
        Bitmap b = bitmap(l.file);
        if (b == null) return false;
        // Grow it enough that drifting never shows its edges
        float margin = drift * Math.max(Math.abs(l.motionX), Math.abs(l.motionY));
        float cover = Math.max((w + 2 * margin) / b.getWidth(), (h + 2 * margin) / b.getHeight());
        float scale = cover * Math.max(1f, zoom) * Math.max(1f, l.scale);
        float bw = b.getWidth() * scale, bh = b.getHeight() * scale;
        float cx = w * l.x + dx, cy = h * l.y + dy;
        out.set(cx - bw / 2, cy - bh / 2, cx + bw / 2, cy + bh / 2);
        // Never let a moved background uncover the colour behind it
        if (out.left > 0) out.offset(-out.left, 0);
        if (out.right < w) out.offset(w - out.right, 0);
        if (out.top > 0) out.offset(0, -out.top);
        if (out.bottom < h) out.offset(0, h - out.bottom);
        return true;
    }

    /** Puts an anchored cutout over its subject in the background, as it rests. */
    private void pin(Scene.Layer l, int w, int h) {
        RectF bg = mBackgroundRest;
        float left = bg.left + l.cropLeft * bg.width();
        float right = bg.left + l.cropRight * bg.width();
        float top = bg.top + l.cropTop * bg.height();
        float bottom = bg.top + l.cropBottom * bg.height();
        l.x = (left + right) / 2f / w;
        l.y = (top + bottom) / 2f / h;
        l.scale = (right - left) / w;
        l.rotation = 0f;
    }

    private void drawImage(Canvas canvas, Scene.Layer l, int w, int h, float dx, float dy,
            float zoom) {
        Bitmap b = bitmap(l.file);
        if (b == null) return;
        float bw = w * l.scale * zoom;
        float bh = bw * b.getHeight() / b.getWidth();
        float cx = w * l.x + dx, cy = h * l.y + dy;
        canvas.save();
        canvas.rotate(l.rotation, cx, cy);
        mRect.set(cx - bw / 2, cy - bh / 2, cx + bw / 2, cy + bh / 2);
        mBitmapPaint.setAlpha(Math.round(l.alpha * 255));
        canvas.drawBitmap(b, null, mRect, mBitmapPaint);
        canvas.restore();
    }

    /** A dp on a typical phone, scaled with the canvas so a thumbnail looks like the screen. */
    private static float unit(int w) {
        return w / 411f;
    }

    static Typeface typeface(String font) {
        return switch (font == null ? "" : font) {
            case Scene.FONT_SERIF -> Typeface.create(Typeface.SERIF, 700, false);
            case Scene.FONT_MONO -> Typeface.MONOSPACE;
            case Scene.FONT_CONDENSED -> Typeface.create(
                    Typeface.create("sans-serif-condensed", Typeface.NORMAL), 800, false);
            default -> Typeface.create(Typeface.SANS_SERIF, 600, false);
        };
    }

    private void drawText(Canvas canvas, Scene.Layer l, int w, int h, float dx, float dy,
            float zoom) {
        if (l.text == null || l.text.isEmpty()) return;
        mTextPaint.setTypeface(typeface(l.font));
        mTextPaint.setTextSize(w * l.scale * TEXT_SIZE * zoom);
        mTextPaint.setColor(l.color);
        mTextPaint.setAlpha(Math.round(Color.alpha(l.color) * l.alpha));
        mTextPaint.setShadowLayer(4 * unit(w), 0, unit(w), 0x55000000);
        String[] lines = l.text.split("\n");
        Paint.FontMetrics fm = mTextPaint.getFontMetrics();
        float lineHeight = fm.descent - fm.ascent;
        float cx = w * l.x + dx, cy = h * l.y + dy;
        float top = cy - lineHeight * lines.length / 2f - fm.ascent;
        canvas.save();
        canvas.rotate(l.rotation, cx, cy);
        for (int i = 0; i < lines.length; i++) {
            canvas.drawText(lines[i], cx, top + i * lineHeight, mTextPaint);
        }
        canvas.restore();
    }

    /** The untransformed size of a layer, for the editor's selection frame. */
    void layerSize(Scene.Layer l, int w, float[] out) {
        out[0] = out[1] = 0;
        switch (l.type) {
            case Scene.TYPE_IMAGE -> {
                Bitmap b = bitmap(l.file);
                if (b == null) return;
                out[0] = w * l.scale;
                out[1] = out[0] * b.getHeight() / b.getWidth();
            }
            case Scene.TYPE_TEXT -> {
                if (l.text == null) return;
                mTextPaint.setTypeface(typeface(l.font));
                mTextPaint.setTextSize(w * l.scale * TEXT_SIZE);
                Paint.FontMetrics fm = mTextPaint.getFontMetrics();
                String[] lines = l.text.split("\n");
                for (String line : lines) out[0] = Math.max(out[0], mTextPaint.measureText(line));
                out[1] = (fm.descent - fm.ascent) * lines.length;
            }
        }
    }

    // Particles live in fractions of the screen, so they survive the editor and wallpaper sizes

    private static final class Particle {
        float x, y, vx, vy, size, angle, spin, phase;
    }

    private static final class ParticleField {
        final List<Particle> particles = new ArrayList<>();
        String kind;
        int amount;
        float time;
    }

    private void drawParticles(Canvas canvas, Scene.Layer l, int w, int h, float dx, float dy,
            float zoom, float dt) {
        ParticleField field = mFields.get(l.id);
        if (field == null || !l.particles.equals(field.kind) || l.amount != field.amount) {
            field = new ParticleField();
            field.kind = l.particles;
            field.amount = l.amount;
            int count = PARTICLE_COUNTS[level(l.amount)];
            if (Scene.PARTICLES_RAIN.equals(l.particles)) count = count * 3 / 2;
            for (int i = 0; i < count; i++) {
                Particle p = new Particle();
                spawn(p, l.particles, true);
                field.particles.add(p);
            }
            mFields.put(l.id, field);
        }
        float speed = PARTICLE_SPEEDS[level(l.speed)];
        float size = PARTICLE_SIZES[level(l.size)] * unit(w) * zoom;
        float aspect = h / (float) w;
        field.time += dt * speed;
        int alpha = Math.round(l.alpha * 255);
        canvas.save();
        canvas.translate(dx, dy);
        for (Particle p : field.particles) {
            step(p, l.particles, dt * speed, field.time, aspect);
            draw(canvas, p, l, w, h, size, alpha, field.time);
        }
        canvas.restore();
    }

    private static int level(int v) {
        return Math.max(0, Math.min(2, v));
    }

    private void spawn(Particle p, String kind, boolean anywhere) {
        p.x = mRandom.nextFloat();
        p.phase = mRandom.nextFloat() * 6.283f;
        p.angle = mRandom.nextFloat() * 360f;
        p.spin = (mRandom.nextFloat() - 0.5f) * 120f;
        switch (kind) {
            case Scene.PARTICLES_RAIN -> {
                p.size = 0.6f + mRandom.nextFloat() * 0.8f;
                p.y = anywhere ? mRandom.nextFloat() : -0.05f;
                p.vy = 0.9f + mRandom.nextFloat() * 0.5f;
                p.vx = 0.08f;
            }
            case Scene.PARTICLES_BUBBLES -> {
                p.size = 0.5f + mRandom.nextFloat() * 1.5f;
                p.y = anywhere ? mRandom.nextFloat() : 1.05f;
                p.vy = -(0.06f + mRandom.nextFloat() * 0.08f);
            }
            case Scene.PARTICLES_STARS, Scene.PARTICLES_FIREFLIES -> {
                p.size = 0.4f + mRandom.nextFloat() * 1.2f;
                p.y = mRandom.nextFloat();
                p.vx = (mRandom.nextFloat() - 0.5f) * 0.04f;
                p.vy = (mRandom.nextFloat() - 0.5f) * 0.04f;
            }
            case Scene.PARTICLES_PETALS -> {
                p.size = 0.7f + mRandom.nextFloat() * 0.8f;
                p.y = anywhere ? mRandom.nextFloat() : -0.05f;
                p.vy = 0.05f + mRandom.nextFloat() * 0.05f;
            }
            default -> { // Snow
                p.size = 0.4f + mRandom.nextFloat() * 1.2f;
                p.y = anywhere ? mRandom.nextFloat() : -0.03f;
                // Bigger flakes are nearer, so they fall faster
                p.vy = 0.04f + p.size * 0.04f;
            }
        }
    }

    private void step(Particle p, String kind, float dt, float time, float aspect) {
        p.angle += p.spin * dt;
        switch (kind) {
            case Scene.PARTICLES_RAIN -> {
                p.x += p.vx * dt;
                p.y += p.vy * dt;
                if (p.y > 1.05f) spawn(p, kind, false);
            }
            case Scene.PARTICLES_BUBBLES -> {
                p.x += (float) Math.sin(time * 1.5f + p.phase) * 0.02f * dt;
                p.y += p.vy * dt;
                if (p.y < -0.05f) spawn(p, kind, false);
            }
            case Scene.PARTICLES_STARS -> {}
            case Scene.PARTICLES_FIREFLIES -> {
                p.vx += (mRandom.nextFloat() - 0.5f) * 0.05f * dt;
                p.vy += (mRandom.nextFloat() - 0.5f) * 0.05f * dt;
                p.vx = Math.max(-0.04f, Math.min(0.04f, p.vx));
                p.vy = Math.max(-0.04f, Math.min(0.04f, p.vy));
                p.x += p.vx * dt;
                p.y += p.vy * dt / aspect;
                if (p.x < 0 || p.x > 1) p.vx = -p.vx;
                if (p.y < 0 || p.y > 1) p.vy = -p.vy;
            }
            case Scene.PARTICLES_PETALS -> {
                p.x += (float) Math.sin(time * 0.9f + p.phase) * 0.05f * dt;
                p.y += p.vy * dt;
                if (p.y > 1.05f) spawn(p, kind, false);
            }
            default -> {
                p.x += (float) Math.sin(time * 1.2f + p.phase) * 0.015f * dt;
                p.y += p.vy * dt;
                if (p.y > 1.03f) spawn(p, kind, false);
            }
        }
    }

    private void draw(Canvas canvas, Particle p, Scene.Layer l, int w, int h, float size,
            int alpha, float time) {
        float x = p.x * w, y = p.y * h;
        float r = p.size * size;
        mParticlePaint.setColor(l.color);
        mParticlePaint.setStyle(Paint.Style.FILL);
        mParticlePaint.setStrokeCap(Paint.Cap.ROUND);
        switch (l.particles) {
            case Scene.PARTICLES_RAIN -> {
                mParticlePaint.setAlpha(alpha * 140 / 255);
                mParticlePaint.setStrokeWidth(r * 1.4f);
                float len = r * 14f;
                canvas.drawLine(x, y, x - len * 0.08f, y - len, mParticlePaint);
            }
            case Scene.PARTICLES_BUBBLES -> {
                mParticlePaint.setStyle(Paint.Style.STROKE);
                mParticlePaint.setStrokeWidth(Math.max(1f, r * 0.6f));
                mParticlePaint.setAlpha(alpha * 170 / 255);
                canvas.drawCircle(x, y, r * 7f, mParticlePaint);
                mParticlePaint.setStyle(Paint.Style.FILL);
                mParticlePaint.setAlpha(alpha * 120 / 255);
                canvas.drawCircle(x - r * 3f, y - r * 3f, r * 1.2f, mParticlePaint);
            }
            case Scene.PARTICLES_STARS -> {
                float twinkle = 0.35f + 0.65f * (0.5f + 0.5f * (float) Math.sin(
                        time * (1.5f + p.size) + p.phase));
                mParticlePaint.setAlpha(Math.round(alpha * twinkle));
                canvas.drawCircle(x, y, r * 1.6f, mParticlePaint);
                if (p.size > 1.3f) {
                    // The brightest ones glint
                    mParticlePaint.setStrokeWidth(r * 0.5f);
                    float arm = r * 6f * twinkle;
                    canvas.drawLine(x - arm, y, x + arm, y, mParticlePaint);
                    canvas.drawLine(x, y - arm, x, y + arm, mParticlePaint);
                }
            }
            case Scene.PARTICLES_FIREFLIES -> {
                float glow = 0.5f + 0.5f * (float) Math.sin(time * 2f + p.phase);
                mParticlePaint.setAlpha(Math.round(alpha * 0.25f * glow));
                canvas.drawCircle(x, y, r * 7f, mParticlePaint);
                mParticlePaint.setAlpha(Math.round(alpha * (0.4f + 0.6f * glow)));
                canvas.drawCircle(x, y, r * 2f, mParticlePaint);
            }
            case Scene.PARTICLES_PETALS -> {
                mParticlePaint.setAlpha(alpha * 220 / 255);
                canvas.save();
                canvas.rotate(p.angle, x, y);
                // Turning over as it falls
                float flip = 0.35f + 0.65f * Math.abs((float) Math.sin(time * 1.4f + p.phase));
                mRect.set(x - r * 6f, y - r * 3f * flip, x + r * 6f, y + r * 3f * flip);
                canvas.drawOval(mRect, mParticlePaint);
                canvas.restore();
            }
            default -> {
                mParticlePaint.setAlpha(alpha * (150 + Math.round(p.size * 60)) / 255);
                canvas.drawCircle(x, y, r * 2.5f, mParticlePaint);
            }
        }
    }
}

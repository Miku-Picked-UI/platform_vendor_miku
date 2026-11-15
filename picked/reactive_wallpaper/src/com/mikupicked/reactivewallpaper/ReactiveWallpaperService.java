/*
 * SPDX-FileCopyrightText: 2026 Miku-Picked UI
 * SPDX-License-Identifier: Apache-2.0
 */

package com.mikupicked.reactivewallpaper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

import java.io.File;
import java.util.Calendar;

/**
 * Reactive live wallpaper. Everything is event driven and only runs while the wallpaper is
 * visible: the rotation sensor (game rotation vector, no magnetometer) at UI rate for parallax,
 * TIME_TICK for the daylight, the battery broadcast; frames are only drawn while something moves
 * (parallax easing, the charging glow), so a still wallpaper costs nothing.
 */
public class ReactiveWallpaperService extends WallpaperService {

    static final String PREFS = "reactive";
    static final String KEY_PARALLAX = "parallax";
    static final String KEY_DAYLIGHT = "daylight";
    static final String KEY_BATTERY = "battery";
    static final String KEY_VERSION = "version";
    static final String PHOTO = "photo.jpg";

    static File photoFile(Context context) {
        return new File(context.getFilesDir(), PHOTO);
    }

    @Override
    public Engine onCreateEngine() {
        return new ReactiveEngine();
    }

    private class ReactiveEngine extends Engine
            implements SensorEventListener, SharedPreferences.OnSharedPreferenceChangeListener {

        private static final float MAX_SHIFT = 0.045f; // of the screen size
        private static final long FRAME_MS = 33;

        private final Handler mHandler = new Handler(Looper.getMainLooper());
        private final Paint mPaint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);
        private final Paint mFx = new Paint(Paint.ANTI_ALIAS_FLAG);
        private SharedPreferences mPrefs;
        private SensorManager mSensors;
        private Sensor mRotation;
        private Bitmap mPhoto;
        private int mWidth, mHeight;
        private boolean mVisible;
        private boolean mParallax, mDaylight, mBatteryFx;

        // Parallax: target and eased offsets in -1..1, and the resting orientation.
        private float mTargetX, mTargetY, mX, mY;
        private float[] mRest;
        private final float[] mRot = new float[9];
        private final float[] mOrient = new float[3];

        private int mLevel = 100;
        private boolean mCharging;

        private final Runnable mFrame = this::frame;
        private boolean mFramePending;

        private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                    final int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 100);
                    final int scale = Math.max(1, intent.getIntExtra(BatteryManager.EXTRA_SCALE,
                            100));
                    mLevel = level * 100 / scale;
                    mCharging = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0;
                }
                requestFrame();
            }
        };

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);
            mPrefs = getSharedPreferences(PREFS, MODE_PRIVATE);
            mPrefs.registerOnSharedPreferenceChangeListener(this);
            mSensors = getSystemService(SensorManager.class);
            mRotation = mSensors.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
            loadPrefs();
        }

        @Override
        public void onDestroy() {
            mPrefs.unregisterOnSharedPreferenceChangeListener(this);
            setActive(false);
            if (mPhoto != null) mPhoto.recycle();
            super.onDestroy();
        }

        private void loadPrefs() {
            mParallax = mPrefs.getBoolean(KEY_PARALLAX, true);
            mDaylight = mPrefs.getBoolean(KEY_DAYLIGHT, true);
            mBatteryFx = mPrefs.getBoolean(KEY_BATTERY, true);
            if (mPhoto != null) mPhoto.recycle();
            mPhoto = null;
            final File f = photoFile(ReactiveWallpaperService.this);
            if (f.isFile()) mPhoto = BitmapFactory.decodeFile(f.getPath());
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences p, String key) {
            loadPrefs();
            if (mVisible) {
                setActive(false);
                setActive(true);
            }
            requestFrame();
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            mWidth = width;
            mHeight = height;
            requestFrame();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            mVisible = visible;
            setActive(visible);
            if (visible) requestFrame();
        }

        private void setActive(boolean active) {
            if (active) {
                final IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                filter.addAction(Intent.ACTION_TIME_TICK);
                filter.addAction(Intent.ACTION_TIME_CHANGED);
                registerReceiver(mReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
                if (mParallax && mRotation != null) {
                    mRest = null;
                    mSensors.registerListener(this, mRotation, SensorManager.SENSOR_DELAY_UI,
                            mHandler);
                }
            } else {
                try {
                    unregisterReceiver(mReceiver);
                } catch (IllegalArgumentException ignored) {
                }
                mSensors.unregisterListener(this);
                mHandler.removeCallbacks(mFrame);
                mFramePending = false;
            }
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            SensorManager.getRotationMatrixFromVector(mRot, event.values);
            SensorManager.getOrientation(mRot, mOrient);
            if (mRest == null) mRest = mOrient.clone();
            // Slowly follow the way the phone is held, so the effect is relative to it.
            mRest[1] += (mOrient[1] - mRest[1]) * 0.01f;
            mRest[2] += (mOrient[2] - mRest[2]) * 0.01f;
            mTargetX = clamp((mOrient[2] - mRest[2]) / 0.35f);
            mTargetY = clamp((mOrient[1] - mRest[1]) / 0.35f);
            requestFrame();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}

        private float clamp(float v) {
            return Math.max(-1f, Math.min(1f, v));
        }

        private void requestFrame() {
            if (!mVisible || mFramePending) return;
            mFramePending = true;
            mHandler.postDelayed(mFrame, FRAME_MS);
        }

        private void frame() {
            mFramePending = false;
            mX += (mTargetX - mX) * 0.18f;
            mY += (mTargetY - mY) * 0.18f;
            draw();
            final boolean moving = Math.abs(mTargetX - mX) > 0.002f
                    || Math.abs(mTargetY - mY) > 0.002f;
            if (moving || (mBatteryFx && mCharging)) requestFrame();
        }

        private void draw() {
            if (mWidth == 0 || mHeight == 0) return;
            final SurfaceHolder holder = getSurfaceHolder();
            Canvas c = null;
            try {
                c = holder.lockHardwareCanvas();
                if (c != null) render(c);
            } finally {
                if (c != null) holder.unlockCanvasAndPost(c);
            }
        }

        /** Hour of day as a fraction 0..24. */
        private float hour() {
            final Calendar cal = Calendar.getInstance();
            return cal.get(Calendar.HOUR_OF_DAY) + cal.get(Calendar.MINUTE) / 60f;
        }

        private void render(Canvas c) {
            final float shiftX = mParallax ? -mX * MAX_SHIFT * mWidth : 0;
            final float shiftY = mParallax ? -mY * MAX_SHIFT * mHeight : 0;
            final float h = hour();

            if (mPhoto != null) {
                // Cover the screen plus the parallax margin, centred.
                final float margin = mParallax ? 1f + 2 * MAX_SHIFT : 1f;
                final float scale = Math.max(mWidth * margin / mPhoto.getWidth(),
                        mHeight * margin / mPhoto.getHeight());
                final float w = mPhoto.getWidth() * scale, ht = mPhoto.getHeight() * scale;
                final RectF dst = new RectF((mWidth - w) / 2 + shiftX, (mHeight - ht) / 2 + shiftY,
                        (mWidth + w) / 2 + shiftX, (mHeight + ht) / 2 + shiftY);
                mPaint.setColorFilter(batteryFilter());
                c.drawBitmap(mPhoto, null, dst, mPaint);
                mPaint.setColorFilter(null);
            } else {
                drawSky(c, h, shiftX, shiftY);
            }
            if (mDaylight) drawDaylight(c, h);
            if (mBatteryFx && mCharging) drawChargeGlow(c);
        }

        /** Low battery (not charging): fade the colours out, fully grey at 5 %. */
        private ColorMatrixColorFilter batteryFilter() {
            if (!mBatteryFx || mCharging || mLevel > 20) return null;
            final ColorMatrix m = new ColorMatrix();
            m.setSaturation(Math.max(0f, (mLevel - 5) / 15f));
            return new ColorMatrixColorFilter(m);
        }

        private static int lerp(int a, int b, float t) {
            t = Math.max(0f, Math.min(1f, t));
            return Color.rgb(
                    (int) (Color.red(a) + (Color.red(b) - Color.red(a)) * t),
                    (int) (Color.green(a) + (Color.green(b) - Color.green(a)) * t),
                    (int) (Color.blue(a) + (Color.blue(b) - Color.blue(a)) * t));
        }

        // Sky keyframes: {hour, top colour, bottom colour}.
        private final float[] mSkyHours = {0f, 5f, 6.5f, 9f, 16.5f, 18.5f, 20f, 24f};
        private final int[] mSkyTop = {0xff060a1e, 0xff0d1433, 0xff3b4b8c, 0xff2f7fd6,
                0xff2b74cc, 0xff4a3a8a, 0xff101634, 0xff060a1e};
        private final int[] mSkyBottom = {0xff11163a, 0xff3a2c5a, 0xfff09a6a, 0xff9fd2f5,
                0xffa8d4f2, 0xfff07a4a, 0xff2a2050, 0xff11163a};

        private int skyColor(int[] keys, float h) {
            for (int i = 0; i < mSkyHours.length - 1; i++) {
                if (h >= mSkyHours[i] && h <= mSkyHours[i + 1]) {
                    return lerp(keys[i], keys[i + 1],
                            (h - mSkyHours[i]) / (mSkyHours[i + 1] - mSkyHours[i]));
                }
            }
            return keys[0];
        }

        /** Default wallpaper: a sky gradient with the sun or moon following the time. */
        private void drawSky(Canvas c, float h, float sx, float sy) {
            int top = skyColor(mSkyTop, h), bottom = skyColor(mSkyBottom, h);
            final ColorMatrixColorFilter f = batteryFilter();
            mFx.setColorFilter(f);
            mFx.setShader(new LinearGradient(0, 0, 0, mHeight, top, bottom, Shader.TileMode.CLAMP));
            c.drawRect(0, 0, mWidth, mHeight, mFx);
            // Sun from 6:00 to 19:00, moon otherwise, on an arc across the screen.
            final boolean day = h >= 6f && h < 19f;
            final float p = day ? (h - 6f) / 13f : ((h >= 19f ? h - 19f : h + 5f) / 11f);
            final float x = mWidth * (0.1f + 0.8f * p) + sx * 2.2f;
            final float y = mHeight * (0.62f - 0.42f * (float) Math.sin(Math.PI * p)) + sy * 2.2f;
            final float r = mWidth * (day ? 0.09f : 0.06f);
            final int core = day ? 0xfffff3c4 : 0xffe8ecff;
            mFx.setShader(new RadialGradient(x, y, r * 4, (core & 0x00ffffff) | 0x66000000,
                    Color.TRANSPARENT, Shader.TileMode.CLAMP));
            c.drawCircle(x, y, r * 4, mFx);
            mFx.setShader(null);
            mFx.setColor(core);
            c.drawCircle(x, y, r, mFx);
            if (!day) { // crescent
                mFx.setColor(top);
                c.drawCircle(x + r * 0.45f, y - r * 0.2f, r * 0.9f, mFx);
                // a few stars, fixed per screen size so they do not flicker
                mFx.setColor(0xccffffff);
                final java.util.Random rnd = new java.util.Random(mWidth * 31L + mHeight);
                for (int i = 0; i < 60; i++) {
                    c.drawCircle(rnd.nextFloat() * mWidth + sx * 0.5f,
                            rnd.nextFloat() * mHeight * 0.6f + sy * 0.5f,
                            (0.6f + rnd.nextFloat()) * getResources().getDisplayMetrics().density,
                            mFx);
                }
            }
            mFx.setColorFilter(null);
        }

        /** Warm tint around sunrise/sunset, a dim blue veil at night (photos only feel it). */
        private void drawDaylight(Canvas c, float h) {
            int color = 0;
            float a = 0f;
            final float sunrise = Math.abs(h - 6.5f), sunset = Math.abs(h - 18.5f);
            final float golden = Math.min(sunrise, sunset);
            if (golden < 1.5f) {
                color = 0xffff8a3c;
                a = 0.18f * (1f - golden / 1.5f);
            } else if (h >= 20.5f || h < 5f) {
                color = 0xff0a1440;
                a = 0.32f;
            } else if (h >= 19.5f || h < 5.5f) {
                color = 0xff0a1440;
                a = 0.16f;
            }
            if (a <= 0f || mPhoto == null) return;
            mFx.setShader(null);
            mFx.setColor((color & 0x00ffffff) | ((int) (a * 255) << 24));
            c.drawRect(0, 0, mWidth, mHeight, mFx);
        }

        /** A breathing glow rising from the bottom edge while charging, up to the level. */
        private void drawChargeGlow(Canvas c) {
            final float t = (SystemClock.uptimeMillis() % 3000) / 3000f;
            final float breathe = 0.6f + 0.4f * (float) Math.sin(t * 2 * Math.PI);
            final float height = mHeight * (0.12f + 0.25f * mLevel / 100f);
            final int glow = mLevel >= 95 ? 0xff5ce08c : 0xff39c5bb;
            mFx.setShader(new LinearGradient(0, mHeight, 0, mHeight - height,
                    (glow & 0x00ffffff) | ((int) (110 * breathe) << 24), Color.TRANSPARENT,
                    Shader.TileMode.CLAMP));
            c.drawRect(0, mHeight - height, mWidth, mHeight, mFx);
            mFx.setShader(null);
        }
    }
}

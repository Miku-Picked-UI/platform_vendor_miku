/*
 * SPDX-FileCopyrightText: 2026 Miku-Picked UI
 * SPDX-License-Identifier: Apache-2.0
 */

package com.mikupicked.speakercal;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.Locale;

/**
 * What the screen shows while measuring: rings that pulse outwards with the microphone level,
 * a progress arc, the frequency the sweep is at and the mic level in dB.
 */
public class PulseView extends View {

    private final Paint mRing = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mArcBg = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mArc = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mCore = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mText = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mSmall = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF mOval = new RectF();

    private volatile float mProgress;
    private volatile double mHz;
    private volatile float mLevel;
    private float mSmoothLevel;
    private final float[] mRings = {0f, 0.33f, 0.66f};
    private long mLast;
    private String mStatus = "";
    private int mAccent = 0xff82b1ff;

    public PulseView(Context c, AttributeSet a) {
        super(c, a);
        mRing.setStyle(Paint.Style.STROKE);
        mArcBg.setStyle(Paint.Style.STROKE);
        mArc.setStyle(Paint.Style.STROKE);
        mArc.setStrokeCap(Paint.Cap.ROUND);
        mText.setTextAlign(Paint.Align.CENTER);
        mText.setFakeBoldText(true);
        mSmall.setTextAlign(Paint.Align.CENTER);
    }

    void setColors(int accent, int onSurface) {
        mAccent = accent;
        mText.setColor(onSurface);
        mSmall.setColor((onSurface & 0x00ffffff) | 0xaa000000);
        mArcBg.setColor((onSurface & 0x00ffffff) | 0x22000000);
        invalidate();
    }

    void update(float progress, double hz, float level) {
        mProgress = progress;
        mHz = hz;
        mLevel = level;
    }

    void setStatus(String s) {
        mStatus = s;
        invalidate();
    }

    void reset() {
        mProgress = 0;
        mHz = 0;
        mLevel = 0;
        mSmoothLevel = 0;
        mStatus = "";
        invalidate();
    }

    @Override
    protected void onDraw(Canvas c) {
        final long now = System.nanoTime();
        final float dt = mLast == 0 ? 0 : Math.min(0.05f, (now - mLast) / 1e9f);
        mLast = now;
        final float w = getWidth(), h = getHeight();
        final float cx = w / 2, cy = h / 2;
        final float maxR = Math.min(w, h) / 2 * 0.92f;
        final float density = getResources().getDisplayMetrics().density;

        // Mic level 0..1 on a dB scale (-60..0 dBFS), smoothed
        final double db = 20 * Math.log10(mLevel + 1e-6);
        final float lvl = (float) Math.max(0, Math.min(1, (db + 60) / 60));
        mSmoothLevel += (lvl - mSmoothLevel) * Math.min(1, dt * 12);

        // Rings travel outwards, faster and brighter when louder
        for (int i = 0; i < mRings.length; i++) {
            mRings[i] += dt * (0.25f + mSmoothLevel * 0.9f);
            if (mRings[i] > 1) mRings[i] -= 1;
            final float t = mRings[i];
            final float r = maxR * (0.35f + 0.65f * t);
            mRing.setStrokeWidth(density * (2 + 6 * mSmoothLevel * (1 - t)));
            mRing.setColor((mAccent & 0x00ffffff) | ((int) (200 * (1 - t) * (0.3f + mSmoothLevel)) << 24));
            c.drawCircle(cx, cy, r, mRing);
        }

        // Core grows with the level
        final float coreR = maxR * (0.28f + 0.08f * mSmoothLevel);
        mCore.setColor((mAccent & 0x00ffffff) | 0x33000000);
        c.drawCircle(cx, cy, coreR, mCore);

        // Progress arc
        final float arcR = maxR * 0.34f;
        mArcBg.setStrokeWidth(density * 5);
        mArc.setStrokeWidth(density * 5);
        mArc.setColor(mAccent);
        mOval.set(cx - arcR, cy - arcR, cx + arcR, cy + arcR);
        c.drawArc(mOval, 0, 360, false, mArcBg);
        c.drawArc(mOval, -90, 360 * mProgress, false, mArc);

        // Frequency and level
        mText.setTextSize(density * 26);
        mSmall.setTextSize(density * 13);
        final double hz = mHz;
        final String f = hz <= 0 ? "—" : hz >= 1000
                ? String.format(Locale.ROOT, "%.1f kHz", hz / 1000)
                : String.format(Locale.ROOT, "%.0f Hz", hz);
        c.drawText(f, cx, cy + density * 6, mText);
        c.drawText(String.format(Locale.ROOT, "mic %.0f dB", Math.max(-60, db)), cx,
                cy + density * 28, mSmall);
        if (!mStatus.isEmpty()) c.drawText(mStatus, cx, cy - density * 26, mSmall);

        if (isShown()) postInvalidateOnAnimation();
    }
}

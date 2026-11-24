/*
 * SPDX-FileCopyrightText: 2026 Miku-Picked UI
 * SPDX-License-Identifier: Apache-2.0
 */

package com.mikupicked.speakercal;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

/**
 * The result graph on a log-frequency axis (20 Hz - 20 kHz, ±20 dB around the mids): what the
 * speaker measured, the style's target, and what it should sound like with the preset.
 */
public class CurveView extends View {

    private final Paint mGrid = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mLabel = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mMeasured = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mTarget = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mCorrected = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mLegend = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mRun = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mFill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mShade = new Paint();
    private final Path mPath = new Path();

    private double[] mMeas, mTgt, mCorr;
    private double[][] mRuns;
    private int mFillTop, mFillH;
    private double mBassLimit;
    private String[] mNames = {"Measured", "Target", "With preset"};

    public CurveView(Context c, AttributeSet a) {
        super(c, a);
        final float d = getResources().getDisplayMetrics().density;
        mGrid.setStrokeWidth(d);
        mLabel.setTextSize(10 * d);
        mLegend.setTextSize(12 * d);
        for (Paint p : new Paint[] {mMeasured, mTarget, mCorrected}) {
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(2.5f * d);
            p.setStrokeJoin(Paint.Join.ROUND);
        }
        mTarget.setPathEffect(new DashPathEffect(new float[] {8 * d, 6 * d}, 0));
        mRun.setStyle(Paint.Style.STROKE);
        mRun.setStrokeWidth(1.2f * d);
        mRun.setStrokeJoin(Paint.Join.ROUND);
        mLegend.setStrokeCap(Paint.Cap.ROUND);
    }

    void setColors(int onSurface, int measured, int target, int corrected) {
        mGrid.setColor((onSurface & 0x00ffffff) | 0x22000000);
        mLabel.setColor((onSurface & 0x00ffffff) | 0x99000000);
        mLegend.setColor(onSurface);
        mMeasured.setColor(measured);
        mTarget.setColor(target);
        mCorrected.setColor(corrected);
        mRun.setColor((measured & 0x00ffffff) | 0x55000000);
        mShade.setColor((onSurface & 0x00ffffff) | 0x10000000);
        mFillH = 0; // rebuild the gradient
        invalidate();
    }

    void setNames(String measured, String target, String corrected) {
        mNames = new String[] {measured, target, corrected};
    }

    /** The single tests behind an averaged measurement, drawn faintly; null for none. */
    void setRuns(double[][] runs) {
        mRuns = runs;
        invalidate();
    }

    void setData(double[] measured, double[] target, double[] corrected, double bassLimit) {
        mMeas = measured;
        mTgt = target;
        mCorr = corrected;
        mBassLimit = bassLimit;
        invalidate();
    }

    private float x(double f, float l, float w) {
        return l + (float) (Math.log(f / Dsp.F0) / Math.log(Dsp.F1 / Dsp.F0)) * w;
    }

    private float y(double db, float t, float h) {
        final double v = Math.max(-20, Math.min(20, db));
        return t + (float) ((20 - v) / 40) * h;
    }

    @Override
    protected void onDraw(Canvas c) {
        final float d = getResources().getDisplayMetrics().density;
        final float l = 28 * d, t = 8 * d, r = 8 * d, legendH = 22 * d, b = 18 * d + legendH;
        final float w = getWidth() - l - r, h = getHeight() - t - b;

        for (int db = -20; db <= 20; db += 10) {
            final float yy = y(db, t, h);
            c.drawLine(l, yy, l + w, yy, mGrid);
            c.drawText((db > 0 ? "+" : "") + db, 2 * d, yy + 4 * d, mLabel);
        }
        final int[] fs = {20, 50, 100, 200, 500, 1000, 2000, 5000, 10000, 20000};
        final String[] fl = {"20", "50", "100", "200", "500", "1k", "2k", "5k", "10k", "20k"};
        for (int i = 0; i < fs.length; i++) {
            final float xx = x(fs[i], l, w);
            c.drawLine(xx, t, xx, t + h, mGrid);
            c.drawText(fl[i], xx - 6 * d, t + h + 13 * d, mLabel);
        }
        if (mBassLimit > 0) {
            // Shade what the speaker can't play
            c.drawRect(l, t, x(mBassLimit, l, w), t + h, mShade);
        }
        if (mRuns != null) {
            for (double[] run : mRuns) drawCurve(c, run, mRun, l, t, w, h);
        }
        if (mCorr != null) {
            // Soft fill under the "with preset" curve
            if (mFillH != (int) h || mFillTop != (int) t) {
                mFillH = (int) h;
                mFillTop = (int) t;
                final int col = mCorrected.getColor();
                mFill.setShader(new LinearGradient(0, t, 0, t + h,
                        (col & 0x00ffffff) | 0x38000000, col & 0x00ffffff, Shader.TileMode.CLAMP));
            }
            buildPath(mCorr, l, t, w, h);
            mPath.lineTo(x(Dsp.GRID[mCorr.length - 1], l, w), t + h);
            mPath.lineTo(x(Dsp.GRID[0], l, w), t + h);
            mPath.close();
            c.drawPath(mPath, mFill);
        }
        drawCurve(c, mTgt, mTarget, l, t, w, h);
        drawCurve(c, mMeas, mMeasured, l, t, w, h);
        drawCurve(c, mCorr, mCorrected, l, t, w, h);

        // Legend
        float lx = l;
        final float ly = getHeight() - 6 * d;
        final Paint[] ps = {mMeasured, mTarget, mCorrected};
        for (int i = 0; i < 3; i++) {
            c.drawLine(lx, ly - 4 * d, lx + 18 * d, ly - 4 * d, ps[i]);            lx += 22 * d;
            c.drawText(mNames[i], lx, ly, mLegend);
            lx += mLegend.measureText(mNames[i]) + 14 * d;
        }
    }

    private void drawCurve(Canvas c, double[] v, Paint p, float l, float t, float w, float h) {
        if (v == null) return;
        buildPath(v, l, t, w, h);
        c.drawPath(mPath, p);
    }

    private void buildPath(double[] v, float l, float t, float w, float h) {
        mPath.reset();
        for (int i = 0; i < v.length; i++) {
            final float xx = x(Dsp.GRID[i], l, w), yy = y(v[i], t, h);
            if (i == 0) mPath.moveTo(xx, yy);
            else mPath.lineTo(xx, yy);
        }
    }
}

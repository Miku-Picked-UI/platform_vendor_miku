/*
 * SPDX-FileCopyrightText: 2026 Miku-Picked UI
 * SPDX-License-Identifier: Apache-2.0
 */

package com.mikupicked.speakercal;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import java.util.Locale;

/** The Equalizer app's 9 band gains as bars around 0 dB (±12 dB), with their values. */
public class BandsView extends View {

    private static final String[] LABELS = {"63", "125", "250", "500", "1k", "2k", "4k", "8k", "16k"};
    private static final double RANGE_DB = 12;

    private final Paint mBoost = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mCut = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mTrack = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mZero = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mLabel = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mValue = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF mRect = new RectF();
    private double[] mGains, mFrom;
    private float mT = 1;
    private ValueAnimator mAnim;

    public BandsView(Context c, AttributeSet a) {
        super(c, a);
        final float d = getResources().getDisplayMetrics().density;
        mZero.setStrokeWidth(d);
        mLabel.setTextSize(11 * d);
        mLabel.setTextAlign(Paint.Align.CENTER);
        mValue.setTextSize(11 * d);
        mValue.setTextAlign(Paint.Align.CENTER);
        mValue.setFakeBoldText(true);
    }

    void setColors(Palette p) {
        mBoost.setColor(p.primary);
        mCut.setColor(p.measured);
        mTrack.setColor(Palette.alpha(p.onSurface, 0x12));
        mZero.setColor(Palette.alpha(p.onSurface, 0x44));
        mLabel.setColor(p.onSurfaceVar);
        mValue.setColor(p.onSurface);
        invalidate();
    }

    void setGains(double[] gains) {
        final double[] shown = shown();
        final boolean animate = shown != null && gains != null && shown.length == gains.length
                && isShown();
        mFrom = shown;
        mGains = gains;
        if (mAnim != null) mAnim.cancel();
        if (!animate) {
            mT = 1;
            invalidate();
            return;
        }
        mT = 0;
        mAnim = ValueAnimator.ofFloat(0, 1);
        mAnim.setDuration(380);
        mAnim.setInterpolator(new DecelerateInterpolator(1.6f));
        mAnim.addUpdateListener(a -> {
            mT = (float) a.getAnimatedValue();
            invalidate();
        });
        mAnim.start();
    }

    /** The gains as drawn right now, part way through an animation. */
    private double[] shown() {
        if (mGains == null || mFrom == null || mT >= 1 || mFrom.length != mGains.length) return mGains;
        final double[] out = new double[mGains.length];
        for (int i = 0; i < out.length; i++) out[i] = mFrom[i] + (mGains[i] - mFrom[i]) * mT;
        return out;
    }

    @Override
    protected void onDraw(Canvas c) {
        final double[] gains = shown();
        if (gains == null) return;
        final float d = getResources().getDisplayMetrics().density;
        final float w = getWidth();
        final float top = 18 * d, bottom = getHeight() - 34 * d;
        final float mid = (top + bottom) / 2, half = (bottom - top) / 2;
        final float col = w / LABELS.length;
        final float barW = Math.min(col * 0.42f, 16 * d);

        for (int i = 0; i < LABELS.length; i++) {
            final float cx = col * (i + 0.5f);
            mRect.set(cx - barW / 2, top, cx + barW / 2, bottom);
            c.drawRoundRect(mRect, barW / 2, barW / 2, mTrack);
        }
        c.drawLine(0, mid, w, mid, mZero);

        for (int i = 0; i < LABELS.length && i < gains.length; i++) {
            final double g = gains[i];
            final float cx = col * (i + 0.5f);
            final float y = mid - (float) (Math.max(-1, Math.min(1, g / RANGE_DB)) * half);
            float y0 = Math.min(y, mid), y1 = Math.max(y, mid);
            if (y1 - y0 < 3 * d) {
                y0 = mid - 1.5f * d;
                y1 = mid + 1.5f * d;
            }
            mRect.set(cx - barW / 2, y0, cx + barW / 2, y1);
            c.drawRoundRect(mRect, barW / 2, barW / 2, g >= 0 ? mBoost : mCut);

            final String v = Math.abs(g) < 0.05 ? "0"
                    : String.format(Locale.ROOT, "%+.1f", g);
            c.drawText(v, cx, g >= 0 ? y0 - 5 * d : y1 + 13 * d, mValue);
            c.drawText(LABELS[i], cx, getHeight() - 6 * d, mLabel);
        }
    }
}

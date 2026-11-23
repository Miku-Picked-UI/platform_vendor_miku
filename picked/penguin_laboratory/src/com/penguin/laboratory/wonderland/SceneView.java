/*
 * Copyright (C) 2026 The PenguinOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.penguin.laboratory.wonderland;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Outline;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewOutlineProvider;

/**
 * The editor's preview of a scene, shaped like the screen. Drag a layer to move it, pinch to
 * size it and twist to turn it.
 */
public class SceneView extends View {

    public interface Callback {
        void onLayerSelected(String id);

        /** A gesture finished changing the scene. */
        void onSceneEdited();
    }

    private final SceneRenderer mRenderer;
    private final TiltSensor mTilt;
    private final Paint mFramePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float[] mSize = new float[2];
    private final float mScreenAspect;

    private Callback mCallback;
    private String mProject;
    private String mSelected;
    private boolean mPreviewMotion;

    // Gesture state
    private Scene.Layer mDragging;
    private float mLastX, mLastY;
    private float mStartSpan, mStartAngle, mStartScale, mStartRotation;
    private boolean mPinching;
    private boolean mMoved;

    public SceneView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRenderer = new SceneRenderer(context);
        mTilt = new TiltSensor(context);
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        mScreenAspect = Math.max(metrics.widthPixels, metrics.heightPixels)
                / (float) Math.min(metrics.widthPixels, metrics.heightPixels);
        float density = metrics.density;
        mFramePaint.setStyle(Paint.Style.STROKE);
        mFramePaint.setStrokeWidth(2 * density);
        mFramePaint.setColor(0xFFFFFFFF);
        mFramePaint.setPathEffect(new DashPathEffect(new float[] {8 * density, 6 * density}, 0));
        setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), 24 * density);
            }
        });
        setClipToOutline(true);
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public void setScene(String project, Scene scene) {
        mProject = project;
        mRenderer.setScene(project, scene);
        invalidate();
    }

    public void setSelected(String id) {
        mSelected = id;
        invalidate();
    }

    /** Follows the phone's tilt, as the wallpaper will. */
    public void setPreviewMotion(boolean preview) {
        mPreviewMotion = preview;
        if (preview && isAttachedToWindow()) {
            mTilt.start();
        } else {
            mTilt.stop();
        }
        invalidate();
    }

    /** Renders the scene still at the given width, for the project's thumbnail. */
    public Bitmap snapshot(int width) {
        int height = Math.round(width * mScreenAspect);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        SceneRenderer renderer = new SceneRenderer(getContext());
        renderer.setScene(mProject, mRenderer.scene());
        renderer.draw(new Canvas(bitmap), width, height, 0f, 0f, System.nanoTime());
        return bitmap;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mPreviewMotion) mTilt.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        mTilt.stop();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // As big as fits while keeping the screen's shape
        int maxW = MeasureSpec.getSize(widthMeasureSpec);
        int maxH = MeasureSpec.getSize(heightMeasureSpec);
        int w = maxW, h = Math.round(maxW * mScreenAspect);
        if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.UNSPECIFIED && h > maxH) {
            h = maxH;
            w = Math.round(maxH / mScreenAspect);
        }
        setMeasuredDimension(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int w = getWidth(), h = getHeight();
        mTilt.step();
        float tx = mPreviewMotion ? mTilt.x() : 0f;
        float ty = mPreviewMotion ? mTilt.y() : 0f;
        mRenderer.draw(canvas, w, h, tx, ty, System.nanoTime());
        Scene scene = mRenderer.scene();
        Scene.Layer selected = scene != null && mSelected != null ? scene.find(mSelected) : null;
        if (selected != null && !mPreviewMotion) {
            mRenderer.layerSize(selected, w, mSize);
            if (mSize[0] > 0) {
                float cx = selected.x * w, cy = selected.y * h;
                float pad = 6 * getResources().getDisplayMetrics().density;
                canvas.save();
                canvas.rotate(selected.rotation, cx, cy);
                canvas.drawRect(cx - mSize[0] / 2 - pad, cy - mSize[1] / 2 - pad,
                        cx + mSize[0] / 2 + pad, cy + mSize[1] / 2 + pad, mFramePaint);
                canvas.restore();
            }
        }
        if (mPreviewMotion || mRenderer.animates()) postInvalidateOnAnimation();
    }

    /** The topmost movable layer under the point, if any. */
    private Scene.Layer hit(float px, float py) {
        Scene scene = mRenderer.scene();
        if (scene == null) return null;
        int w = getWidth(), h = getHeight();
        for (int i = scene.layers.size() - 1; i >= 0; i--) {
            Scene.Layer l = scene.layers.get(i);
            if (l.hidden) continue;
            mRenderer.layerSize(l, w, mSize);
            if (mSize[0] <= 0) continue;
            // Undo the layer's rotation to test against its upright frame
            double a = Math.toRadians(-l.rotation);
            float dx = px - l.x * w, dy = py - l.y * h;
            float rx = (float) (dx * Math.cos(a) - dy * Math.sin(a));
            float ry = (float) (dx * Math.sin(a) + dy * Math.cos(a));
            if (Math.abs(rx) <= mSize[0] / 2 && Math.abs(ry) <= mSize[1] / 2) return l;
        }
        return null;
    }

    private Scene.Layer selectedLayer() {
        Scene scene = mRenderer.scene();
        return scene != null && mSelected != null ? scene.find(mSelected) : null;
    }

    private static boolean movable(Scene.Layer l) {
        return l != null && !Scene.TYPE_PARTICLES.equals(l.type);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (mPreviewMotion) return false;
        int w = getWidth(), h = getHeight();
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN -> {
                Scene.Layer hit = hit(e.getX(), e.getY());
                if (hit != null && !hit.id.equals(mSelected)) {
                    mSelected = hit.id;
                    if (mCallback != null) mCallback.onLayerSelected(hit.id);
                }
                // Dragging anywhere moves the selection, so small layers are easy to handle
                mDragging = hit != null ? hit : selectedLayer();
                if (!movable(mDragging)) mDragging = null;
                // Moved by hand, it no longer follows the background
                if (mDragging != null) mDragging.anchored = false;
                mLastX = e.getX();
                mLastY = e.getY();
                mMoved = false;
                mPinching = false;
                getParent().requestDisallowInterceptTouchEvent(true);
                invalidate();
                return true;
            }
            case MotionEvent.ACTION_POINTER_DOWN -> {
                if (mDragging != null && e.getPointerCount() == 2) {
                    mPinching = true;
                    mStartSpan = span(e);
                    mStartAngle = angle(e);
                    mStartScale = mDragging.scale;
                    mStartRotation = mDragging.rotation;
                }
                return true;
            }
            case MotionEvent.ACTION_MOVE -> {
                if (mDragging == null) return true;
                if (mPinching && e.getPointerCount() >= 2) {
                    float scale = mStartScale * span(e) / Math.max(1f, mStartSpan);
                    boolean background = Scene.TYPE_BACKGROUND.equals(mDragging.type);
                    mDragging.scale = Math.max(background ? 1f : 0.05f, Math.min(4f, scale));
                    if (!background) {
                        mDragging.rotation = mStartRotation + angle(e) - mStartAngle;
                    }
                } else if (!mPinching) {
                    mDragging.x += (e.getX() - mLastX) / w;
                    mDragging.y += (e.getY() - mLastY) / h;
                    mDragging.x = Math.max(-0.2f, Math.min(1.2f, mDragging.x));
                    mDragging.y = Math.max(-0.2f, Math.min(1.2f, mDragging.y));
                }
                mLastX = e.getX();
                mLastY = e.getY();
                mMoved = true;
                invalidate();
                return true;
            }
            case MotionEvent.ACTION_POINTER_UP -> {
                // Carry on dragging with the finger that stays
                int up = e.getActionIndex();
                int keep = up == 0 ? 1 : 0;
                mLastX = e.getX(keep);
                mLastY = e.getY(keep);
                if (e.getPointerCount() <= 2) mPinching = false;
                return true;
            }
            case MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (mMoved && mCallback != null) mCallback.onSceneEdited();
                mDragging = null;
                mPinching = false;
                return true;
            }
        }
        return super.onTouchEvent(e);
    }

    private static float span(MotionEvent e) {
        return (float) Math.hypot(e.getX(1) - e.getX(0), e.getY(1) - e.getY(0));
    }

    private static float angle(MotionEvent e) {
        return (float) Math.toDegrees(Math.atan2(e.getY(1) - e.getY(0), e.getX(1) - e.getX(0)));
    }
}

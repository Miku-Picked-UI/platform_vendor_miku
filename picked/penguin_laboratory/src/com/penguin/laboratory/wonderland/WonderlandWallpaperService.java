/*
 * Copyright (C) 2026 The PenguinOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.penguin.laboratory.wonderland;

import android.graphics.Canvas;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.Choreographer;
import android.view.SurfaceHolder;

/** Shows the applied Wonderland project, drifting its layers as the phone tilts. */
public class WonderlandWallpaperService extends WallpaperService {

    private static final String TAG = "Wonderland";

    @Override
    public Engine onCreateEngine() {
        return new WonderlandEngine();
    }

    private class WonderlandEngine extends Engine
            implements Choreographer.FrameCallback, WonderlandStore.Listener {

        private final SceneRenderer mRenderer = new SceneRenderer(WonderlandWallpaperService.this);
        private final TiltSensor mTilt = new TiltSensor(WonderlandWallpaperService.this);
        private boolean mVisible;
        private boolean mFrameScheduled;
        private int mWidth, mHeight;

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            WonderlandStore.addListener(this);
            onChanged();
        }

        @Override
        public void onDestroy() {
            WonderlandStore.removeListener(this);
            stop();
            super.onDestroy();
        }

        @Override
        public void onChanged() {
            String project = WonderlandStore.active(WonderlandWallpaperService.this);
            mRenderer.setScene(project,
                    WonderlandStore.load(WonderlandWallpaperService.this, project));
            if (mVisible) start();
            draw(System.nanoTime());
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            mWidth = width;
            mHeight = height;
            draw(System.nanoTime());
        }

        @Override
        public void onSurfaceRedrawNeeded(SurfaceHolder holder) {
            super.onSurfaceRedrawNeeded(holder);
            draw(System.nanoTime());
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            mVisible = visible;
            if (visible) {
                start();
            } else {
                stop();
            }
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            stop();
            super.onSurfaceDestroyed(holder);
        }

        private void start() {
            Scene scene = mRenderer.scene();
            if (scene != null && scene.followsTilt()) {
                mTilt.start();
            } else {
                mTilt.stop();
            }
            schedule();
        }

        private void stop() {
            mTilt.stop();
            if (mFrameScheduled) {
                Choreographer.getInstance().removeFrameCallback(this);
                mFrameScheduled = false;
            }
        }

        private void schedule() {
            if (mFrameScheduled) return;
            mFrameScheduled = true;
            Choreographer.getInstance().postFrameCallback(this);
        }

        @Override
        public void doFrame(long frameTimeNanos) {
            mFrameScheduled = false;
            if (!mVisible) return;
            boolean moved = mTilt.step();
            // A still scene is only redrawn when the phone moves
            if (moved || mRenderer.animates()) draw(frameTimeNanos);
            if (mTilt.isRunning() || mRenderer.animates()) schedule();
        }

        private void draw(long frameNanos) {
            if (mWidth == 0 || mHeight == 0) return;
            SurfaceHolder holder = getSurfaceHolder();
            Canvas canvas = null;
            try {
                canvas = holder.lockHardwareCanvas();
                if (canvas == null) return;
                mRenderer.draw(canvas, mWidth, mHeight, mTilt.x(), mTilt.y(), frameNanos);
            } catch (IllegalStateException | IllegalArgumentException e) {
                Log.w(TAG, "Couldn't draw", e);
            } finally {
                if (canvas != null) holder.unlockCanvasAndPost(canvas);
            }
        }
    }
}

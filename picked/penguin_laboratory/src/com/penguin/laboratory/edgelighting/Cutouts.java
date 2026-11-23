/*
 * Copyright (C) 2026 The PenguinOS Project
 * SPDX-FileCopyrightText: 2026 Miku-Picked UI
 * SPDX-License-Identifier: Apache-2.0
 */

package com.penguin.laboratory.edgelighting;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ImageDecoder;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Size;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Lifts the main subject out of a photo.
 *
 * Miku-Picked UI: PenguinOS used ML Kit subject segmentation from Play services here. This uses
 * the ROM's own on-device cut-out instead (vendor/miku/picked/depth_cutout, U²-Net on ONNX
 * Runtime, the one the depth wallpaper uses), so it works without Play services and nothing is
 * downloaded. The model sees the photo letterboxed into 320x320; its mask is scaled back up and
 * its edges snapped to the photo with a guided filter.
 */
public final class Cutouts {

    // Decoding bigger gains nothing: the mask is 320 px
    private static final int MAX_INPUT_PX = 1600;
    // Plenty for a sprite that is drawn at most a few hundred pixels wide
    private static final int MAX_OUTPUT_PX = 512;
    // Pixels with less coverage than this don't count towards the subject's bounds
    private static final int ALPHA_THRESHOLD = 24;

    // vendor/miku/picked/depth_cutout CutoutService; keep in sync with it
    private static final ComponentName SERVICE = new ComponentName(
            "com.mikupicked.depthcutout", "com.mikupicked.depthcutout.CutoutService");
    private static final int SIZE = 320;
    private static final int MSG_CUTOUT = 1;
    private static final int RESULT_OK = 0;
    private static final String KEY_RGB = "rgb";
    private static final String KEY_MASK = "mask";
    private static final long CONNECT_TIMEOUT_S = 10;
    private static final long RUN_TIMEOUT_S = 60;

    private static final int LETTERBOX_COLOR = 0xff7c7468; // about the ImageNet mean
    // Guided filter, run with the longest side at most this
    private static final int GUIDE_MAX_PX = 800;
    private static final int GUIDE_RADIUS = 6;
    private static final float GUIDE_EPS = 1e-3f;
    // Alpha curve after the guided filter: below LOW is background, above LOW + RANGE subject
    private static final float ALPHA_LOW = 0.25f;
    private static final float ALPHA_RANGE = 0.5f;

    private Cutouts() {}

    /** Blocks until the subject of {@code source} is saved as the cutout. Call off the main thread. */
    static void create(Context context, Uri source, Runnable onDownloading) throws Exception {
        Bitmap subject = scaleDown(subject(context, source, onDownloading), MAX_OUTPUT_PX);
        String id = "cutout-" + System.currentTimeMillis();
        save(context, subject, id);
        EdgeLightingStore.put(context, EdgeLightingStore.KEY_SELECTED, id);
    }

    /**
     * Blocks until the subject of {@code source} is lifted out, cropped to its bounds. Call off
     * the main thread.
     */
    public static Bitmap subject(Context context, Uri source, Runnable onDownloading)
            throws Exception {
        return subject(context, source, onDownloading, null);
    }

    /**
     * Like {@link #subject(Context, Uri, Runnable)}, and sets {@code bounds} to where the subject
     * was in the photo, as fractions of its size. {@code onDownloading} is never called: the
     * model ships with the ROM.
     */
    public static Bitmap subject(Context context, Uri source, Runnable onDownloading,
            RectF bounds) throws Exception {
        Bitmap input = decode(context, source);
        Bitmap foreground = foreground(context, input);
        Rect box = bounds(foreground);
        if (bounds != null) {
            bounds.set(box.left / (float) foreground.getWidth(),
                    box.top / (float) foreground.getHeight(),
                    box.right / (float) foreground.getWidth(),
                    box.bottom / (float) foreground.getHeight());
        }
        return Bitmap.createBitmap(foreground, box.left, box.top, box.width(), box.height());
    }

    /** Writes {@code bitmap} as image {@code id}, replacing it atomically. */
    static void save(Context context, Bitmap bitmap, String id) throws IOException {
        File out = EdgeLightingStore.image(context, id);
        File tmp = new File(out.getPath() + ".tmp");
        try (FileOutputStream os = new FileOutputStream(tmp)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
        }
        if (!tmp.renameTo(out)) throw new IOException("Couldn't save " + id);
    }

    /** {@code photo} with everything but its subject made transparent, at a guide-sized scale. */
    private static Bitmap foreground(Context context, Bitmap photo) throws IOException {
        final int w = photo.getWidth(), h = photo.getHeight();

        // Model input: the whole photo letterboxed, as packed RGB
        final float sc = SIZE / (float) Math.max(w, h);
        final int nw = Math.max(1, Math.round(w * sc)), nh = Math.max(1, Math.round(h * sc));
        final int ox = (SIZE - nw) / 2, oy = (SIZE - nh) / 2;
        final Bitmap in = Bitmap.createBitmap(SIZE, SIZE, Bitmap.Config.ARGB_8888);
        final Canvas ic = new Canvas(in);
        ic.drawColor(LETTERBOX_COLOR);
        ic.drawBitmap(photo, null, new Rect(ox, oy, ox + nw, oy + nh),
                new Paint(Paint.FILTER_BITMAP_FLAG));
        final int[] px = new int[SIZE * SIZE];
        in.getPixels(px, 0, SIZE, 0, 0, SIZE, SIZE);
        in.recycle();
        final byte[] rgb = new byte[SIZE * SIZE * 3];
        for (int i = 0; i < px.length; i++) {
            rgb[i * 3] = (byte) (px[i] >> 16);
            rgb[i * 3 + 1] = (byte) (px[i] >> 8);
            rgb[i * 3 + 2] = (byte) px[i];
        }
        final byte[] mask = runModel(context, rgb);
        if (mask == null) throw new IOException("Cut-out service failed");

        // The mask under the photo, at the guide size
        final float gs = Math.min(1f, GUIDE_MAX_PX / (float) Math.max(w, h));
        final int gw = Math.max(1, Math.round(w * gs)), gh = Math.max(1, Math.round(h * gs));
        for (int i = 0; i < px.length; i++) {
            final int m = mask[i] & 0xff;
            px[i] = 0xff000000 | (m << 16) | (m << 8) | m;
        }
        final Bitmap maskFull = Bitmap.createBitmap(px, SIZE, SIZE, Bitmap.Config.ARGB_8888);
        final Bitmap maskPhoto = Bitmap.createBitmap(gw, gh, Bitmap.Config.ARGB_8888);
        new Canvas(maskPhoto).drawBitmap(maskFull, new Rect(ox, oy, ox + nw, oy + nh),
                new Rect(0, 0, gw, gh), new Paint(Paint.FILTER_BITMAP_FLAG));
        maskFull.recycle();

        final int n = gw * gh;
        final int[] gpx = new int[n];
        maskPhoto.getPixels(gpx, 0, gw, 0, 0, gw, gh);
        maskPhoto.recycle();
        final float[] p = new float[n];
        for (int i = 0; i < n; i++) p[i] = (gpx[i] & 0xff) / 255f;
        final Bitmap layer = Bitmap.createScaledBitmap(photo, gw, gh, true)
                .copy(Bitmap.Config.ARGB_8888, true);
        layer.getPixels(gpx, 0, gw, 0, 0, gw, gh);
        final float[] guide = new float[n];
        for (int i = 0; i < n; i++) {
            final int c = gpx[i];
            guide[i] = ((((c >> 16) & 0xff) * 77 + ((c >> 8) & 0xff) * 150 + (c & 0xff) * 29)
                    >> 8) / 255f;
        }
        final float[] q = guidedFilter(guide, p, gw, gh, GUIDE_RADIUS, GUIDE_EPS);
        for (int i = 0; i < n; i++) {
            final float a = Math.max(0f, Math.min(1f, (q[i] - ALPHA_LOW) / ALPHA_RANGE));
            gpx[i] = Math.round(a * 255f) << 24; // black with the mask as alpha
        }
        final Bitmap alpha = Bitmap.createBitmap(gpx, gw, gh, Bitmap.Config.ARGB_8888);
        final Paint dstIn = new Paint();
        dstIn.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        new Canvas(layer).drawBitmap(alpha, 0, 0, dstIn);
        alpha.recycle();
        return layer;
    }

    /** Sends the letterboxed photo to the cut-out service; null on failure. */
    private static byte[] runModel(Context context, byte[] rgb) {
        final AtomicReference<IBinder> binder = new AtomicReference<>();
        final CountDownLatch connected = new CountDownLatch(1);
        final ServiceConnection conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                binder.set(service);
                connected.countDown();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                binder.set(null);
            }

            @Override
            public void onBindingDied(ComponentName name) {
                connected.countDown();
            }

            @Override
            public void onNullBinding(ComponentName name) {
                connected.countDown();
            }
        };
        final HandlerThread thread = new HandlerThread("LabCutout");
        try {
            if (!context.bindService(new Intent().setComponent(SERVICE), conn,
                    Context.BIND_AUTO_CREATE)) {
                return null;
            }
            if (!connected.await(CONNECT_TIMEOUT_S, TimeUnit.SECONDS) || binder.get() == null) {
                return null;
            }
            thread.start();
            final AtomicReference<byte[]> result = new AtomicReference<>();
            final CountDownLatch done = new CountDownLatch(1);
            final Messenger replyTo = new Messenger(new Handler(thread.getLooper(), reply -> {
                if (reply.what == MSG_CUTOUT) {
                    if (reply.arg1 == RESULT_OK) {
                        final byte[] m = reply.getData().getByteArray(KEY_MASK);
                        if (m != null && m.length == SIZE * SIZE) result.set(m);
                    }
                    done.countDown();
                }
                return true;
            }));
            final Message msg = Message.obtain(null, MSG_CUTOUT);
            final Bundle data = new Bundle();
            data.putByteArray(KEY_RGB, rgb);
            msg.setData(data);
            msg.replyTo = replyTo;
            new Messenger(binder.get()).send(msg);
            if (!done.await(RUN_TIMEOUT_S, TimeUnit.SECONDS)) return null;
            return result.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception e) {
            return null;
        } finally {
            try {
                context.unbindService(conn);
            } catch (IllegalArgumentException e) {
                // not registered
            }
            thread.quitSafely();
        }
    }

    /** Edge-preserving smoothing of {@code p} that follows the edges of {@code guide}. */
    private static float[] guidedFilter(float[] guide, float[] p, int w, int h, int r,
            float eps) {
        final int n = w * h;
        final double[] integral = new double[(w + 1) * (h + 1)];
        final float[] tmp = new float[n];
        final float[] meanI = boxMean(guide, w, h, r, integral);
        final float[] meanP = boxMean(p, w, h, r, integral);
        for (int i = 0; i < n; i++) tmp[i] = guide[i] * p[i];
        final float[] corrIp = boxMean(tmp, w, h, r, integral);
        for (int i = 0; i < n; i++) tmp[i] = guide[i] * guide[i];
        final float[] corrII = boxMean(tmp, w, h, r, integral);
        for (int i = 0; i < n; i++) {
            final float varI = corrII[i] - meanI[i] * meanI[i];
            final float covIp = corrIp[i] - meanI[i] * meanP[i];
            final float a = covIp / (varI + eps);
            corrIp[i] = a;
            meanP[i] = meanP[i] - a * meanI[i];
        }
        final float[] meanA = boxMean(corrIp, w, h, r, integral);
        final float[] meanB = boxMean(meanP, w, h, r, integral);
        for (int i = 0; i < n; i++) meanA[i] = meanA[i] * guide[i] + meanB[i];
        return meanA;
    }

    /** Mean over the (2r+1)^2 window around each pixel, clipped at the edges. */
    private static float[] boxMean(float[] a, int w, int h, int r, double[] integral) {
        final int stride = w + 1;
        for (int y = 0; y < h; y++) {
            double row = 0;
            for (int x = 0; x < w; x++) {
                row += a[y * w + x];
                integral[(y + 1) * stride + x + 1] = integral[y * stride + x + 1] + row;
            }
        }
        final float[] out = new float[w * h];
        for (int y = 0; y < h; y++) {
            final int y0 = Math.max(0, y - r), y1 = Math.min(h, y + r + 1);
            for (int x = 0; x < w; x++) {
                final int x0 = Math.max(0, x - r), x1 = Math.min(w, x + r + 1);
                final double sum = integral[y1 * stride + x1] - integral[y0 * stride + x1]
                        - integral[y1 * stride + x0] + integral[y0 * stride + x0];
                out[y * w + x] = (float) (sum / ((y1 - y0) * (x1 - x0)));
            }
        }
        return out;
    }

    /** Decodes {@code uri} no larger than the cut-out needs. */
    public static Bitmap decode(Context context, Uri uri) throws IOException {
        return ImageDecoder.decodeBitmap(
                ImageDecoder.createSource(context.getContentResolver(), uri),
                (decoder, info, src) -> {
                    Size size = info.getSize();
                    int longest = Math.max(size.getWidth(), size.getHeight());
                    if (longest > MAX_INPUT_PX) {
                        float scale = (float) MAX_INPUT_PX / longest;
                        decoder.setTargetSize(Math.round(size.getWidth() * scale),
                                Math.round(size.getHeight() * scale));
                    }
                    decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE);
                });
    }

    /** The subject's bounds, leaving out the transparent margin around it. */
    private static Rect bounds(Bitmap bitmap) throws IOException {
        int w = bitmap.getWidth(), h = bitmap.getHeight();
        int[] row = new int[w];
        int left = w, top = h, right = -1, bottom = -1;
        for (int y = 0; y < h; y++) {
            bitmap.getPixels(row, 0, w, 0, y, w, 1);
            for (int x = 0; x < w; x++) {
                if ((row[x] >>> 24) > ALPHA_THRESHOLD) {
                    if (x < left) left = x;
                    if (x > right) right = x;
                    if (y < top) top = y;
                    bottom = y;
                }
            }
        }
        if (right < 0) throw new IOException("No subject found");
        return new Rect(left, top, right + 1, bottom + 1);
    }

    public static Bitmap scaleDown(Bitmap bitmap, int maxPx) {
        int longest = Math.max(bitmap.getWidth(), bitmap.getHeight());
        if (longest <= maxPx) return bitmap;
        float scale = (float) maxPx / longest;
        return Bitmap.createScaledBitmap(bitmap, Math.round(bitmap.getWidth() * scale),
                Math.round(bitmap.getHeight() * scale), true);
    }
}

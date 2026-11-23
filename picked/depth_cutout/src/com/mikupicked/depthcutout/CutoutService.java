/*
 * SPDX-FileCopyrightText: 2026 Miku-Picked UI
 * SPDX-License-Identifier: Apache-2.0
 */

package com.mikupicked.depthcutout;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;

/**
 * Finds the subject of a photo for the depth wallpaper (vendor/MikuSettings
 * PickedDepthWallpaperStore). Settings binds this service and sends a {@link #MSG_CUTOUT}
 * Message whose data holds {@link #KEY_RGB}: the photo letterboxed into
 * {@link Segmenter#SIZE} x {@link Segmenter#SIZE}, as packed RGB bytes. The reply has the same
 * {@code what}, {@code arg1} {@link #RESULT_OK} or {@link #RESULT_ERROR}, and on success
 * {@link #KEY_MASK}: one byte per pixel, 255 = subject.
 *
 * Only the system uid (Settings) and apps signed with the platform key (Penguin Laboratory) may
 * use it. The model stays loaded while bound.
 */
public class CutoutService extends Service {

    private static final String TAG = "PickedDepthCutout";

    public static final int MSG_CUTOUT = 1;
    public static final int RESULT_OK = 0;
    public static final int RESULT_ERROR = 1;
    public static final String KEY_RGB = "rgb";
    public static final String KEY_MASK = "mask";

    private HandlerThread mThread;
    private Messenger mMessenger;
    private Segmenter mSegmenter;

    @Override
    public void onCreate() {
        super.onCreate();
        mThread = new HandlerThread(TAG);
        mThread.start();
        mMessenger = new Messenger(new Handler(mThread.getLooper(), this::handle));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public void onDestroy() {
        // Close the model on its own thread, after any cut-out still running.
        final HandlerThread thread = mThread;
        new Handler(thread.getLooper()).post(() -> {
            if (mSegmenter != null) {
                mSegmenter.close();
                mSegmenter = null;
            }
            thread.quitSafely();
        });
        super.onDestroy();
    }

    private boolean isTrusted(int uid) {
        return uid == Process.SYSTEM_UID
                || getPackageManager().checkSignatures(Process.SYSTEM_UID, uid)
                        == PackageManager.SIGNATURE_MATCH;
    }

    private boolean handle(Message msg) {
        if (msg.what != MSG_CUTOUT || msg.replyTo == null) return true;
        if (!isTrusted(msg.sendingUid)) {
            Log.w(TAG, "Refusing uid " + msg.sendingUid);
            return true;
        }
        final Message reply = Message.obtain(null, MSG_CUTOUT);
        reply.arg1 = RESULT_ERROR;
        try {
            final byte[] rgb = msg.getData().getByteArray(KEY_RGB);
            if (rgb != null && rgb.length == Segmenter.SIZE * Segmenter.SIZE * 3) {
                if (mSegmenter == null) mSegmenter = new Segmenter(this);
                final Bundle data = new Bundle();
                data.putByteArray(KEY_MASK, mSegmenter.run(rgb));
                reply.setData(data);
                reply.arg1 = RESULT_OK;
            } else {
                Log.w(TAG, "Bad input");
            }
        } catch (Exception | OutOfMemoryError e) {
            Log.e(TAG, "Cut-out failed", e);
        }
        try {
            msg.replyTo.send(reply);
        } catch (RemoteException e) {
            Log.w(TAG, "Caller is gone", e);
        }
        return true;
    }
}

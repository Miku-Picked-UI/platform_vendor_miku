/*
 * PickedPods - AirPods controls for Miku-Picked UI
 * Copyright (C) 2025 LibrePods contributors
 * Copyright (C) 2026 Miku-Picked UI
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Connection sequence after LibrePods' AirPodsService.connectToSocket.
 */

package com.mikupicked.airpods;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.ParcelUuid;
import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * One AACP session with a pair of AirPods: opens L2CAP PSM 0x1001, says hello, and reads
 * packets on its own thread until closed. Android 16 needed the Bluetooth stack's interop fix
 * for AirPods (packages/modules/Bluetooth l2c_should_skip_ertm) to keep this channel open.
 */
final class PodsSession {

    private static final String TAG = "PickedPods";

    interface Callback {
        void onPacket(byte[] packet, int length);

        void onClosed(PodsSession session);
    }

    private final BluetoothDevice mDevice;
    private final Callback mCallback;
    private BluetoothSocket mSocket;
    private OutputStream mOut;
    private Thread mReader;
    private volatile boolean mClosed;

    PodsSession(BluetoothDevice device, Callback callback) {
        mDevice = device;
        mCallback = callback;
    }

    BluetoothDevice device() {
        return mDevice;
    }

    /** Connects and starts reading. Blocks; call off the main thread. */
    void open() throws IOException {
        final BluetoothSocket socket = createSocket(mDevice);
        socket.connect();
        mSocket = socket;
        mOut = socket.getOutputStream();
        final InputStream in = socket.getInputStream();
        mReader = new Thread(() -> read(in), "PickedPods-reader");
        mReader.start();
        // The AirPods drop packets sent before they're ready, so say it twice like LibrePods
        hello();
        SystemClock.sleep(200);
        hello();
    }

    private void hello() throws IOException {
        send(Aacp.handshake());
        send(Aacp.setFeatureFlags());
        send(Aacp.requestNotifications());
    }

    synchronized void send(byte[] packet) throws IOException {
        if (mClosed || mOut == null) throw new IOException("closed");
        mOut.write(packet);
        mOut.flush();
    }

    /** Sends without throwing; false if the session is gone. */
    boolean trySend(byte[] packet) {
        try {
            send(packet);
            return true;
        } catch (IOException e) {
            Log.w(TAG, "Send failed: " + e.getMessage());
            close();
            return false;
        }
    }

    void close() {
        if (mClosed) return;
        mClosed = true;
        try {
            if (mSocket != null) mSocket.close();
        } catch (IOException ignored) {
        }
        mCallback.onClosed(this);
    }

    boolean isOpen() {
        return !mClosed && mSocket != null && mSocket.isConnected();
    }

    private void read(InputStream in) {
        final byte[] buf = new byte[1024];
        try {
            while (!mClosed) {
                final int n = in.read(buf);
                if (n < 0) break;
                // L2CAP keeps packet boundaries: one read is one packet
                if (n > 0) mCallback.onPacket(buf.clone(), n);
            }
        } catch (IOException e) {
            if (!mClosed) Log.i(TAG, "Session ended: " + e.getMessage());
        } finally {
            close();
        }
    }

    /**
     * An L2CAP socket to the AirPods' AAP channel. The constructor is hidden; this app is platform
     * signed, so hidden API checks don't apply. Falls back to BluetoothDevice.createL2capSocket.
     */
    private static BluetoothSocket createSocket(BluetoothDevice device) throws IOException {
        try {
            final Constructor<BluetoothSocket> c = BluetoothSocket.class.getDeclaredConstructor(
                    BluetoothDevice.class, int.class, boolean.class, boolean.class, int.class,
                    ParcelUuid.class);
            c.setAccessible(true);
            return c.newInstance(device, BluetoothSocket.TYPE_L2CAP, true, true, Aacp.PSM,
                    ParcelUuid.fromString(Aacp.SERVICE_UUID));
        } catch (ReflectiveOperationException e) {
            Log.w(TAG, "No BluetoothSocket constructor, trying createL2capSocket", e);
        }
        try {
            final Method m = BluetoothDevice.class.getMethod("createL2capSocket", int.class);
            return (BluetoothSocket) m.invoke(device, Aacp.PSM);
        } catch (ReflectiveOperationException e) {
            throw new IOException("Can't create an L2CAP socket", e);
        }
    }
}

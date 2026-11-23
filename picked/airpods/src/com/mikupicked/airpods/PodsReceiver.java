/*
 * PickedPods - AirPods controls for Miku-Picked UI
 * Copyright (C) 2026 Miku-Picked UI
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.mikupicked.airpods;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.ParcelUuid;

/**
 * Starts {@link PodsService} when a device with the AirPods' AAP service connects, stops it when
 * it goes, and picks up AirPods that were already connected at boot.
 */
public class PodsReceiver extends BroadcastReceiver {

    private static final ParcelUuid AAP = ParcelUuid.fromString(Aacp.SERVICE_UUID);

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)
                || BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
            final BluetoothDevice device =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice.class);
            if (device == null || !isAirPods(device)) return;
            PodsService.start(context, device,
                    BluetoothDevice.ACTION_ACL_CONNECTED.equals(action));
        } else if (Intent.ACTION_BOOT_COMPLETED.equals(action)
                || Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) {
            final BluetoothManager bm = context.getSystemService(BluetoothManager.class);
            final BluetoothAdapter adapter = bm != null ? bm.getAdapter() : null;
            if (adapter == null || !adapter.isEnabled()) return;
            try {
                for (BluetoothDevice d : adapter.getBondedDevices()) {
                    if (d.isConnected() && isAirPods(d)) {
                        PodsService.start(context, d, true);
                        return;
                    }
                }
            } catch (SecurityException ignored) {
            }
        }
    }

    static boolean isAirPods(BluetoothDevice device) {
        try {
            final ParcelUuid[] uuids = device.getUuids();
            if (uuids == null) return false;
            for (ParcelUuid u : uuids) {
                if (AAP.equals(u)) return true;
            }
        } catch (SecurityException ignored) {
        }
        return false;
    }
}

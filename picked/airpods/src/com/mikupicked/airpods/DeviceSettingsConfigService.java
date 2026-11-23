/*
 * PickedPods - AirPods controls for Miku-Picked UI
 * Copyright (C) 2026 Miku-Picked UI
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.mikupicked.airpods;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.android.settingslib.bluetooth.devicesettings.DeviceInfo;
import com.android.settingslib.bluetooth.devicesettings.DeviceSettingItem;
import com.android.settingslib.bluetooth.devicesettings.DeviceSettingsConfig;
import com.android.settingslib.bluetooth.devicesettings.DeviceSettingsConfigServiceStatus;
import com.android.settingslib.bluetooth.devicesettings.IDeviceSettingsConfigProviderService;
import com.android.settingslib.bluetooth.devicesettings.IGetDeviceSettingsConfigCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Lays out Settings' Bluetooth device details page for AirPods (SettingsLib devicesettings, the
 * API Pixel Buds use): Settings' own header and buttons, then this app's controls from
 * {@link DeviceSettingsProviderService}, then the rest of Settings' rows. Settings hides any of
 * its rows this list leaves out, so it names them all.
 */
public class DeviceSettingsConfigService extends Service {

    private static final String TAG = "PickedPods";

    // Settings' rows: setting id and preference key (res/xml/bluetooth_device_details_fragment.xml)
    private static final Object[][] BEFORE = {
        {9001, "bluetooth_details_banner"},
        {22, "general_bluetooth_device_header"},
        {2, "advanced_bluetooth_device_header"},
        {3, "le_audio_bluetooth_device_header"},
        {6, "action_buttons"},
    };
    private static final Object[][] AFTER = {
        {12, "bluetooth_audio_device_type_group"},
        {13, "spatial_audio_group"},
        {8, "bt_extra_control"},
        {9002, "audio_sharing_control"},
        {9, "bt_device_slice_category"},
        {10, "device_companion_apps"},
        {14, "bluetooth_profiles"},
        {15, "bt_extra_options"},
        {16, "bluetooth_related_tools"},
        {9003, "live_caption"},
        {17, "data_sync_group"},
        {19, "device_details_footer"},
    };

    private final IDeviceSettingsConfigProviderService.Stub mBinder =
            new IDeviceSettingsConfigProviderService.Stub() {
                @Override
                public void getDeviceSettingsConfig(DeviceInfo device,
                        IGetDeviceSettingsConfigCallback callback) {
                    try {
                        callback.onResult(new DeviceSettingsConfigServiceStatus(true,
                                Bundle.EMPTY), config());
                    } catch (RemoteException e) {
                        Log.w(TAG, "Settings went away", e);
                    }
                }
            };

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private DeviceSettingsConfig config() {
        final List<DeviceSettingItem> items = new ArrayList<>();
        for (Object[] row : BEFORE) items.add(builtin(row));
        for (int id : DeviceSettingsProviderService.IDS) items.add(provided(id));
        for (Object[] row : AFTER) items.add(builtin(row));
        return new DeviceSettingsConfig(items, Collections.emptyList(), null, Bundle.EMPTY);
    }

    private static DeviceSettingItem builtin(Object[] row) {
        return new DeviceSettingItem((Integer) row[0], null, null, null, (String) row[1], false,
                Bundle.EMPTY);
    }

    private DeviceSettingItem provided(int id) {
        return new DeviceSettingItem(id, getPackageName(),
                DeviceSettingsProviderService.class.getName(),
                PodsService.ACTION_DEVICE_SETTINGS_PROVIDER, null, false, Bundle.EMPTY);
    }
}

/*
 * PickedPods - AirPods controls for Miku-Picked UI
 * Copyright (C) 2026 Miku-Picked UI
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.mikupicked.airpods;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import com.android.settingslib.bluetooth.devicesettings.ActionSwitchPreference;
import com.android.settingslib.bluetooth.devicesettings.ActionSwitchPreferenceState;
import com.android.settingslib.bluetooth.devicesettings.DeviceInfo;
import com.android.settingslib.bluetooth.devicesettings.DeviceSetting;
import com.android.settingslib.bluetooth.devicesettings.DeviceSettingPreference;
import com.android.settingslib.bluetooth.devicesettings.DeviceSettingPreferenceState;
import com.android.settingslib.bluetooth.devicesettings.DeviceSettingState;
import com.android.settingslib.bluetooth.devicesettings.DeviceSettingsProviderServiceStatus;
import com.android.settingslib.bluetooth.devicesettings.IDeviceSettingsListener;
import com.android.settingslib.bluetooth.devicesettings.IDeviceSettingsProviderService;
import com.android.settingslib.bluetooth.devicesettings.MultiTogglePreference;
import com.android.settingslib.bluetooth.devicesettings.MultiTogglePreferenceState;
import com.android.settingslib.bluetooth.devicesettings.ToggleInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * The AirPods controls in Settings' device details page: listening mode, conversation
 * awareness, automatic ear detection, personalized volume and microphone. Settings draws them;
 * this reports their state and applies changes through {@link PodsService}.
 */
public class DeviceSettingsProviderService extends Service implements PodsState.Listener {

    private static final String TAG = "PickedPods";

    // DeviceSettingId.DEVICE_SETTING_ID_ANC; Settings puts it in its own group at the top
    static final int ID_LISTENING_MODE = 1001;
    static final int ID_CONVERSATION_AWARENESS = 2001;
    static final int ID_EAR_DETECTION = 2002;
    static final int ID_PERSONALIZED_VOLUME = 2003;
    static final int ID_MICROPHONE = 2004;
    static final int[] IDS = {ID_LISTENING_MODE, ID_CONVERSATION_AWARENESS, ID_EAR_DETECTION,
            ID_PERSONALIZED_VOLUME, ID_MICROPHONE};

    // Toggle order on screen -> protocol values
    private static final int[] LISTENING_MODES = {Aacp.MODE_OFF, Aacp.MODE_TRANSPARENCY,
            Aacp.MODE_ADAPTIVE, Aacp.MODE_NOISE_CANCELLATION};
    private static final int[] MIC_MODES = {Aacp.MIC_AUTOMATIC, Aacp.MIC_ALWAYS_LEFT,
            Aacp.MIC_ALWAYS_RIGHT};

    private static final int ICON_PX = 96;

    private final PodsState mState = PodsState.get();
    private final RemoteCallbackList<IDeviceSettingsListener> mListeners =
            new RemoteCallbackList<>();
    private Bitmap[] mModeIcons;
    private Bitmap[] mMicIcons;

    private final IDeviceSettingsProviderService.Stub mBinder =
            new IDeviceSettingsProviderService.Stub() {
                @Override
                public DeviceSettingsProviderServiceStatus getServiceStatus() {
                    return new DeviceSettingsProviderServiceStatus(true, Bundle.EMPTY);
                }

                @Override
                public void registerDeviceSettingsListener(DeviceInfo device,
                        IDeviceSettingsListener callback) {
                    mListeners.register(callback, device.getBluetoothAddress());
                    send(callback, device.getBluetoothAddress());
                }

                @Override
                public void unregisterDeviceSettingsListener(DeviceInfo device,
                        IDeviceSettingsListener callback) {
                    mListeners.unregister(callback);
                }

                @Override
                public void updateDeviceSettings(DeviceInfo device, DeviceSettingState params) {
                    apply(device.getBluetoothAddress(), params);
                }
            };

    @Override
    public void onCreate() {
        super.onCreate();
        mModeIcons = new Bitmap[] {icon(R.drawable.ic_mode_off),
                icon(R.drawable.ic_mode_transparency), icon(R.drawable.ic_mode_adaptive),
                icon(R.drawable.ic_mode_noise_cancellation)};
        mMicIcons = new Bitmap[] {letter("A"), letter("L"), letter("R")};
        mState.addListener(this);
    }

    @Override
    public void onDestroy() {
        mState.removeListener(this);
        mListeners.kill();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onPodsChanged(String address) {
        final int n = mListeners.beginBroadcast();
        try {
            for (int i = 0; i < n; i++) {
                if (address.equalsIgnoreCase((String) mListeners.getBroadcastCookie(i))) {
                    send(mListeners.getBroadcastItem(i), address);
                }
            }
        } finally {
            mListeners.finishBroadcast();
        }
    }

    private void send(IDeviceSettingsListener callback, String address) {
        try {
            callback.onDeviceSettingsChanged(settings(address));
        } catch (RemoteException e) {
            Log.w(TAG, "Settings went away", e);
        }
    }

    /** The controls for {@code address}; empty unless a session with it is open. */
    private List<DeviceSetting> settings(String address) {
        final List<DeviceSetting> out = new ArrayList<>();
        final PodsService service = PodsService.instance();
        if (service == null || !mState.connected || !address.equalsIgnoreCase(mState.address)) {
            return out;
        }
        if (mState.listeningMode > 0) {
            final MultiTogglePreference.Builder b = new MultiTogglePreference.Builder()
                    .setTitle(getString(R.string.pods_listening_mode))
                    .setState(indexOf(LISTENING_MODES, mState.listeningMode))
                    .setIsActive(mState.listeningMode != Aacp.MODE_OFF)
                    .setAllowChangingState(true);
            final int[] labels = {R.string.pods_mode_off, R.string.pods_mode_transparency,
                    R.string.pods_mode_adaptive, R.string.pods_mode_noise_cancellation};
            for (int i = 0; i < labels.length; i++) {
                b.addToggleInfo(new ToggleInfo.Builder().setLabel(getString(labels[i]))
                        .setIcon(mModeIcons[i]).build());
            }
            out.add(setting(ID_LISTENING_MODE, b.build()));
        }
        if (mState.conversationAwareness >= 0) {
            out.add(setting(ID_CONVERSATION_AWARENESS, toggle(R.string.pods_conversation,
                    R.string.pods_conversation_summary, mState.conversationAwareness == 1)));
        }
        out.add(setting(ID_EAR_DETECTION, toggle(R.string.pods_ear_detection,
                R.string.pods_ear_detection_summary, service.isEarDetectionEnabled())));
        if (mState.adaptiveVolume >= 0) {
            out.add(setting(ID_PERSONALIZED_VOLUME, toggle(R.string.pods_personalized_volume,
                    R.string.pods_personalized_volume_summary, mState.adaptiveVolume == 1)));
        }
        if (mState.micMode >= 0) {
            final MultiTogglePreference.Builder b = new MultiTogglePreference.Builder()
                    .setTitle(getString(R.string.pods_microphone))
                    .setState(indexOf(MIC_MODES, mState.micMode))
                    .setIsActive(true)
                    .setAllowChangingState(true);
            final int[] labels = {R.string.pods_mic_automatic, R.string.pods_mic_left,
                    R.string.pods_mic_right};
            for (int i = 0; i < labels.length; i++) {
                b.addToggleInfo(new ToggleInfo.Builder().setLabel(getString(labels[i]))
                        .setIcon(mMicIcons[i]).build());
            }
            out.add(setting(ID_MICROPHONE, b.build()));
        }
        return out;
    }

    private void apply(String address, DeviceSettingState params) {
        final PodsService service = PodsService.instance();
        if (service == null || !address.equalsIgnoreCase(mState.address)) return;
        final DeviceSettingPreferenceState s = params.getPreferenceState();
        switch (params.getSettingId()) {
            case ID_LISTENING_MODE:
                if (s instanceof MultiTogglePreferenceState m) {
                    final int i = m.getState();
                    if (i >= 0 && i < LISTENING_MODES.length) {
                        service.setListeningMode(LISTENING_MODES[i]);
                    }
                }
                break;
            case ID_MICROPHONE:
                if (s instanceof MultiTogglePreferenceState m) {
                    final int i = m.getState();
                    if (i >= 0 && i < MIC_MODES.length) service.setMicMode(MIC_MODES[i]);
                }
                break;
            case ID_CONVERSATION_AWARENESS:
                if (s instanceof ActionSwitchPreferenceState a) {
                    service.setToggle(Aacp.CTRL_CONVERSATION_DETECT, a.getChecked());
                }
                break;
            case ID_PERSONALIZED_VOLUME:
                if (s instanceof ActionSwitchPreferenceState a) {
                    service.setToggle(Aacp.CTRL_ADAPTIVE_VOLUME, a.getChecked());
                }
                break;
            case ID_EAR_DETECTION:
                if (s instanceof ActionSwitchPreferenceState a) {
                    service.setEarDetectionEnabled(a.getChecked());
                }
                break;
            default:
                break;
        }
    }

    private ActionSwitchPreference toggle(int title, int summary, boolean checked) {
        return new ActionSwitchPreference.Builder()
                .setTitle(getString(title))
                .setSummary(getString(summary))
                .setHasSwitch(true)
                .setChecked(checked)
                .setAllowedChangingState(true)
                .build();
    }

    private static DeviceSetting setting(int id, DeviceSettingPreference preference) {
        return new DeviceSetting.Builder().setSettingId(id).setPreference(preference).build();
    }

    private static int indexOf(int[] values, int value) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] == value) return i;
        }
        return 0;
    }

    /** White icon bitmap from a vector drawable; Settings tints it. */
    private Bitmap icon(int res) {
        final Drawable d = getDrawable(res);
        final Bitmap b = Bitmap.createBitmap(ICON_PX, ICON_PX, Bitmap.Config.ARGB_8888);
        d.setBounds(0, 0, ICON_PX, ICON_PX);
        d.draw(new Canvas(b));
        return b;
    }

    /** A bold letter as an icon, for the microphone choices. */
    private static Bitmap letter(String text) {
        final Bitmap b = Bitmap.createBitmap(ICON_PX, ICON_PX, Bitmap.Config.ARGB_8888);
        final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(0xffffffff);
        p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        p.setTextSize(ICON_PX * 0.62f);
        final Paint.FontMetrics fm = p.getFontMetrics();
        new Canvas(b).drawText(text, ICON_PX / 2f, ICON_PX / 2f - (fm.ascent + fm.descent) / 2f, p);
        return b;
    }
}

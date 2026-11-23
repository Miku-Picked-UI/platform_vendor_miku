/*
 * PickedPods - AirPods controls for Miku-Picked UI
 * Copyright (C) 2025 LibrePods contributors
 * Copyright (C) 2026 Miku-Picked UI
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Ear detection and conversation awareness behaviour after LibrePods' AirPodsService and
 * MediaController.
 */

package com.mikupicked.airpods;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ServiceInfo;
import android.media.AudioManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Keeps an AACP session open while AirPods are connected ({@link PodsReceiver} starts and stops
 * it), applies what they report and sends what Settings asks for.
 */
public class PodsService extends Service implements PodsSession.Callback {

    private static final String TAG = "PickedPods";

    static final String ACTION_CONNECT = "com.mikupicked.airpods.action.CONNECT";
    static final String ACTION_DISCONNECT = "com.mikupicked.airpods.action.DISCONNECT";

    private static final String CHANNEL = "pods";
    private static final int NOTIFICATION_ID = 1;
    private static final int CONNECT_ATTEMPTS = 4;
    private static final long CONNECT_RETRY_MS = 2000;

    // Phone-side settings
    static final String PREFS = "pods";
    static final String PREF_EAR_DETECTION = "ear_detection";

    // Metadata 25 (BluetoothDevice.METADATA_FAST_PAIR_CUSTOMIZED_FIELDS, hidden): tells Settings
    // which service lays out the device details page (SettingsLib DeviceSettingServiceConnection)
    private static final int METADATA_FAST_PAIR_CUSTOMIZED_FIELDS = 25;
    static final String ACTION_DEVICE_SETTINGS_CONFIG =
            "com.mikupicked.airpods.action.DEVICE_SETTINGS_CONFIG";
    static final String ACTION_DEVICE_SETTINGS_PROVIDER =
            "com.mikupicked.airpods.action.DEVICE_SETTINGS_PROVIDER";

    // Conversation awareness lowers media to this share of its volume
    private static final float DUCK_FACTOR = 0.3f;

    private static volatile PodsService sInstance;

    private final PodsState mState = PodsState.get();
    private HandlerThread mThread;
    private Handler mHandler;
    private AudioManager mAudio;
    private SharedPreferences mPrefs;
    private PodsSession mSession;

    // Ear detection
    private boolean mPausedByUs;
    // Conversation awareness
    private int mDuckedFrom = -1;
    private int mDuckedTo = -1;

    static PodsService instance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        mThread = new HandlerThread(TAG);
        mThread.start();
        mHandler = new Handler(mThread.getLooper());
        mAudio = getSystemService(AudioManager.class);
        mPrefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        final NotificationManager nm = getSystemService(NotificationManager.class);
        nm.createNotificationChannel(new NotificationChannel(CHANNEL,
                getString(R.string.pods_channel), NotificationManager.IMPORTANCE_MIN));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final BluetoothDevice device = intent == null ? null
                : intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice.class);
        final String action = intent == null ? null : intent.getAction();
        startForeground(NOTIFICATION_ID, buildNotification(device),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE);
        if (device == null || ACTION_DISCONNECT.equals(action)) {
            mHandler.post(() -> disconnect(device));
        } else {
            mHandler.post(() -> connect(device));
        }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        sInstance = null;
        final PodsSession s = mSession;
        mSession = null;
        if (s != null) s.close();
        mThread.quitSafely();
        super.onDestroy();
    }

    // ---------------------------------------------------------------------------------------
    // Connection
    // ---------------------------------------------------------------------------------------

    private void connect(BluetoothDevice device) {
        if (mSession != null) {
            if (mSession.device().equals(device) && mSession.isOpen()) return;
            mSession.close();
            mSession = null;
        }
        mState.reset(device.getAddress());
        publishSettingsEndpoint(device);
        for (int attempt = 1; attempt <= CONNECT_ATTEMPTS; attempt++) {
            if (!isAclConnected(device)) break;
            final PodsSession session = new PodsSession(device, this);
            try {
                session.open();
                mSession = session;
                mState.connected = true;
                Log.i(TAG, "Connected to " + device.getAddress());
                mState.notifyChanged();
                return;
            } catch (IOException | SecurityException e) {
                Log.w(TAG, "Connect attempt " + attempt + " failed: " + e.getMessage());
                session.close();
                SystemClock.sleep(CONNECT_RETRY_MS);
            }
        }
        Log.w(TAG, "Giving up on " + device.getAddress());
        stopSelf();
    }

    private void disconnect(BluetoothDevice device) {
        final PodsSession s = mSession;
        if (s != null && (device == null || s.device().equals(device))) {
            mSession = null;
            s.close();
        }
        restoreDuck();
        mPausedByUs = false;
        stopForeground(STOP_FOREGROUND_REMOVE);
        stopSelf();
    }

    @Override
    public void onClosed(PodsSession session) {
        mHandler.post(() -> {
            if (mSession != session) return;
            mSession = null;
            mState.connected = false;
            mState.notifyChanged();
            restoreDuck();
            // Reconnect if the AirPods are still there (e.g. they came back out of the case)
            final BluetoothDevice device = session.device();
            if (isAclConnected(device)) {
                mHandler.postDelayed(() -> {
                    if (mSession == null && isAclConnected(device)) connect(device);
                }, CONNECT_RETRY_MS);
            } else {
                stopForeground(STOP_FOREGROUND_REMOVE);
                stopSelf();
            }
        });
    }

    private static boolean isAclConnected(BluetoothDevice device) {
        try {
            return device.isConnected();
        } catch (SecurityException e) {
            return false;
        }
    }

    // ---------------------------------------------------------------------------------------
    // Packets from the AirPods
    // ---------------------------------------------------------------------------------------

    @Override
    public void onPacket(byte[] p, int len) {
        mHandler.post(() -> handlePacket(p, len));
    }

    private void handlePacket(byte[] p, int len) {
        switch (Aacp.opcode(p, len)) {
            case Aacp.OP_BATTERY & 0xff:
                if (Aacp.parseBattery(p, len, mState.left, mState.right, mState.box)) {
                    publishBattery();
                    updateNotification();
                    mState.notifyChanged();
                }
                break;
            case Aacp.OP_EAR_DETECTION & 0xff: {
                final int[] ears = Aacp.parseEarDetection(p, len);
                if (ears == null) break;
                final boolean wasIn = mState.anyInEar();
                mState.ears = ears;
                onEarsChanged(wasIn, mState.anyInEar());
                mState.notifyChanged();
                break;
            }
            case Aacp.OP_CONTROL & 0xff: {
                final byte[] c = Aacp.parseControl(p, len);
                if (c == null || c.length < 2) break;
                applyControl(c[0], c[1]);
                mState.notifyChanged();
                break;
            }
            case Aacp.OP_CONVERSATION_AWARENESS & 0xff:
                onConversation(Aacp.parseConversationAwareness(p, len));
                break;
            default:
                break;
        }
    }

    private void applyControl(byte id, byte value) {
        final int v = value & 0xff;
        switch (id) {
            case Aacp.CTRL_LISTENING_MODE:
                mState.listeningMode = v;
                break;
            case Aacp.CTRL_CONVERSATION_DETECT:
                mState.conversationAwareness = v == Aacp.ON ? 1 : 0;
                break;
            case Aacp.CTRL_ADAPTIVE_VOLUME:
                mState.adaptiveVolume = v == Aacp.ON ? 1 : 0;
                break;
            case Aacp.CTRL_MIC_MODE:
                mState.micMode = v;
                break;
            case Aacp.CTRL_ALLOW_OFF_OPTION:
                mState.allowOff = v == Aacp.ON ? 1 : 0;
                break;
            default:
                break;
        }
    }

    /** Pauses media when the last bud comes out, plays again when one goes back in. */
    private void onEarsChanged(boolean wasIn, boolean isIn) {
        if (!isEarDetectionEnabled() || wasIn == isIn) return;
        if (!isIn) {
            if (mAudio.isMusicActive()) {
                mediaKey(KeyEvent.KEYCODE_MEDIA_PAUSE);
                mPausedByUs = true;
            }
        } else if (mPausedByUs) {
            mPausedByUs = false;
            mediaKey(KeyEvent.KEYCODE_MEDIA_PLAY);
        }
    }

    /** Lowers media while the user talks, if conversation awareness is on in the AirPods. */
    private void onConversation(int level) {
        if (level == 1 || level == 2) {
            if (mDuckedFrom >= 0 || !mAudio.isMusicActive()) return;
            final int vol = mAudio.getStreamVolume(AudioManager.STREAM_MUSIC);
            final int to = Math.max(1, Math.round(vol * DUCK_FACTOR));
            if (to >= vol) return;
            mDuckedFrom = vol;
            mDuckedTo = to;
            mAudio.setStreamVolume(AudioManager.STREAM_MUSIC, to, 0);
        } else if (level == 6 || level == 8 || level == 9) {
            restoreDuck();
        }
    }

    private void restoreDuck() {
        if (mDuckedFrom < 0) return;
        // Leave it alone if the user changed the volume meanwhile
        if (mAudio.getStreamVolume(AudioManager.STREAM_MUSIC) == mDuckedTo) {
            mAudio.setStreamVolume(AudioManager.STREAM_MUSIC, mDuckedFrom, 0);
        }
        mDuckedFrom = mDuckedTo = -1;
    }

    private void mediaKey(int code) {
        final long now = SystemClock.uptimeMillis();
        mAudio.dispatchMediaKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_DOWN, code, 0));
        mAudio.dispatchMediaKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_UP, code, 0));
    }

    // ---------------------------------------------------------------------------------------
    // Requests from Settings (DeviceSettingsProviderService)
    // ---------------------------------------------------------------------------------------

    boolean isEarDetectionEnabled() {
        return mPrefs.getBoolean(PREF_EAR_DETECTION, true);
    }

    void setEarDetectionEnabled(boolean enabled) {
        mPrefs.edit().putBoolean(PREF_EAR_DETECTION, enabled).apply();
        mHandler.post(() -> {
            if (!enabled) mPausedByUs = false;
            mState.notifyChanged();
        });
    }

    void setListeningMode(int mode) {
        mHandler.post(() -> {
            if (mode == Aacp.MODE_OFF && mState.allowOff != 1) {
                // Newer firmware only accepts Off once the Off option is enabled
                sendControl(Aacp.CTRL_ALLOW_OFF_OPTION, Aacp.ON);
                mState.allowOff = 1;
            }
            if (sendControl(Aacp.CTRL_LISTENING_MODE, (byte) mode)) mState.listeningMode = mode;
            mState.notifyChanged();
        });
    }

    void setToggle(byte control, boolean on) {
        mHandler.post(() -> {
            if (sendControl(control, on ? Aacp.ON : Aacp.OFF)) applyControl(control,
                    on ? Aacp.ON : Aacp.OFF);
            mState.notifyChanged();
        });
    }

    void setMicMode(int mode) {
        mHandler.post(() -> {
            if (sendControl(Aacp.CTRL_MIC_MODE, (byte) mode)) mState.micMode = mode;
            mState.notifyChanged();
        });
    }

    private boolean sendControl(byte id, byte value) {
        final PodsSession s = mSession;
        return s != null && s.trySend(Aacp.control(id, value));
    }

    // ---------------------------------------------------------------------------------------
    // What the system sees
    // ---------------------------------------------------------------------------------------

    /** Points Settings' device details page at this app's config service. */
    private void publishSettingsEndpoint(BluetoothDevice device) {
        final String fields = tag("DEVICE_SETTINGS_CONFIG_PACKAGE_NAME", getPackageName())
                + tag("DEVICE_SETTINGS_CONFIG_CLASS", DeviceSettingsConfigService.class.getName())
                + tag("DEVICE_SETTINGS_CONFIG_ACTION", ACTION_DEVICE_SETTINGS_CONFIG);
        setMetadata(device, METADATA_FAST_PAIR_CUSTOMIZED_FIELDS, mergeFields(device, fields));
        setMetadata(device, BluetoothDevice.METADATA_IS_UNTETHERED_HEADSET, "true");
        setMetadata(device, BluetoothDevice.METADATA_DEVICE_TYPE,
                BluetoothDevice.DEVICE_TYPE_UNTETHERED_HEADSET);
        setMetadata(device, BluetoothDevice.METADATA_MANUFACTURER_NAME, "Apple");
    }

    /** Left, right and case battery for Settings' header. */
    private void publishBattery() {
        final PodsSession s = mSession;
        if (s == null) return;
        final BluetoothDevice d = s.device();
        publish(d, mState.left, BluetoothDevice.METADATA_UNTETHERED_LEFT_BATTERY,
                BluetoothDevice.METADATA_UNTETHERED_LEFT_CHARGING);
        publish(d, mState.right, BluetoothDevice.METADATA_UNTETHERED_RIGHT_BATTERY,
                BluetoothDevice.METADATA_UNTETHERED_RIGHT_CHARGING);
        publish(d, mState.box, BluetoothDevice.METADATA_UNTETHERED_CASE_BATTERY,
                BluetoothDevice.METADATA_UNTETHERED_CASE_CHARGING);
    }

    private void publish(BluetoothDevice d, Aacp.Battery b, int levelKey, int chargingKey) {
        if (b.level < 0) return;
        setMetadata(d, levelKey, Integer.toString(b.level));
        setMetadata(d, chargingKey, Boolean.toString(b.charging));
    }

    private static String tag(String key, String value) {
        return "<" + key + ">" + value + "</" + key + ">";
    }

    /** Keeps other apps' fields in metadata 25 and replaces ours. */
    private static String mergeFields(BluetoothDevice device, String ours) {
        String old = null;
        try {
            final byte[] b = device.getMetadata(METADATA_FAST_PAIR_CUSTOMIZED_FIELDS);
            if (b != null) old = new String(b, StandardCharsets.UTF_8);
        } catch (RuntimeException ignored) {
        }
        if (old == null) return ours;
        for (String key : new String[] {"DEVICE_SETTINGS_CONFIG_PACKAGE_NAME",
                "DEVICE_SETTINGS_CONFIG_CLASS", "DEVICE_SETTINGS_CONFIG_ACTION"}) {
            old = old.replaceAll("<" + key + ">.*?</" + key + ">", "");
        }
        return old + ours;
    }

    private static void setMetadata(BluetoothDevice device, int key, String value) {
        try {
            final byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
            final byte[] old = device.getMetadata(key);
            if (old != null && java.util.Arrays.equals(old, bytes)) return;
            device.setMetadata(key, bytes);
        } catch (RuntimeException e) {
            Log.w(TAG, "Can't set metadata " + key + ": " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------------------------------
    // Notification (a foreground service needs one; the channel is silent and minimised)
    // ---------------------------------------------------------------------------------------

    private Notification buildNotification(BluetoothDevice device) {
        String name = null;
        try {
            if (device != null) name = device.getAlias();
        } catch (SecurityException ignored) {
        }
        final Intent details = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
        return new Notification.Builder(this, CHANNEL)
                .setSmallIcon(R.drawable.ic_pods)
                .setContentTitle(Objects.requireNonNullElse(name, getString(R.string.pods_name)))
                .setContentText(batteryText())
                .setOngoing(true)
                .setShowWhen(false)
                .setContentIntent(PendingIntent.getActivity(this, 0, details,
                        PendingIntent.FLAG_IMMUTABLE))
                .build();
    }

    private void updateNotification() {
        final PodsSession s = mSession;
        getSystemService(NotificationManager.class).notify(NOTIFICATION_ID,
                buildNotification(s != null ? s.device() : null));
    }

    private String batteryText() {
        final StringBuilder sb = new StringBuilder();
        append(sb, R.string.pods_left, mState.left);
        append(sb, R.string.pods_right, mState.right);
        append(sb, R.string.pods_case, mState.box);
        return sb.toString();
    }

    private void append(StringBuilder sb, int label, Aacp.Battery b) {
        if (b.level < 0) return;
        if (sb.length() > 0) sb.append("  ·  ");
        sb.append(getString(label, b.level));
        if (b.charging) sb.append(" ⚡");
    }

    /** Called by {@link PodsReceiver} for an AirPods connection or disconnection. */
    static void start(Context context, BluetoothDevice device, boolean connect) {
        final Intent i = new Intent(context, PodsService.class)
                .setAction(connect ? ACTION_CONNECT : ACTION_DISCONNECT)
                .putExtra(BluetoothDevice.EXTRA_DEVICE, device);
        if (connect) {
            context.startForegroundService(i);
        } else if (sInstance != null) {
            context.startService(i);
        }
    }

    static BluetoothDevice deviceFor(Context context, String address) {
        final BluetoothManager bm = context.getSystemService(BluetoothManager.class);
        return bm == null || bm.getAdapter() == null ? null
                : bm.getAdapter().getRemoteDevice(address);
    }
}

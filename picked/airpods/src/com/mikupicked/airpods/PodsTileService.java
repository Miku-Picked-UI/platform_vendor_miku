/*
 * PickedPods - AirPods controls for Miku-Picked UI
 * Copyright (C) 2026 Miku-Picked UI
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.mikupicked.airpods;

import android.graphics.drawable.Icon;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

/**
 * Quick Settings tile for the connected AirPods: shows the listening mode and the lowest bud
 * battery, and a tap steps through Noise Cancellation, Transparency, Adaptive and (when the
 * AirPods allow it) Off. Unavailable while no AirPods session is open.
 */
public class PodsTileService extends TileService implements PodsState.Listener {

    // The order a tap steps through.
    private static final int[] MODES = {Aacp.MODE_NOISE_CANCELLATION, Aacp.MODE_TRANSPARENCY,
            Aacp.MODE_ADAPTIVE, Aacp.MODE_OFF};

    private final PodsState mState = PodsState.get();

    @Override
    public void onStartListening() {
        mState.addListener(this);
        update();
    }

    @Override
    public void onStopListening() {
        mState.removeListener(this);
    }

    @Override
    public void onPodsChanged(String address) {
        // PodsState listeners can be called from the session's reader thread.
        getMainExecutor().execute(this::update);
    }

    @Override
    public void onClick() {
        PodsService service = PodsService.instance();
        if (service == null || !mState.connected) return;
        int current = mState.listeningMode;
        int index = 0;
        for (int i = 0; i < MODES.length; i++) {
            if (MODES[i] == current) index = i;
        }
        int next = MODES[(index + 1) % MODES.length];
        // Off only when the AirPods' "allow off" option is on; skip it otherwise.
        if (next == Aacp.MODE_OFF && mState.allowOff != 1) next = MODES[0];
        service.setListeningMode(next);
        update();
    }

    private void update() {
        Tile tile = getQsTile();
        if (tile == null) return;
        boolean connected = mState.connected && PodsService.instance() != null;
        int mode = mState.listeningMode;
        tile.setLabel(getString(R.string.pods_tile_label));
        if (!connected) {
            tile.setState(Tile.STATE_UNAVAILABLE);
            tile.setSubtitle(getString(R.string.pods_tile_disconnected));
            tile.setIcon(Icon.createWithResource(this, R.drawable.ic_pods));
        } else {
            tile.setState(mode > 0 && mode != Aacp.MODE_OFF ? Tile.STATE_ACTIVE
                    : Tile.STATE_INACTIVE);
            String subtitle = modeName(mode);
            int battery = lowestBud();
            if (battery >= 0) {
                subtitle = subtitle.isEmpty() ? battery + " %"
                        : getString(R.string.pods_tile_subtitle, subtitle, battery);
            }
            tile.setSubtitle(subtitle);
            tile.setIcon(Icon.createWithResource(this, modeIcon(mode)));
        }
        tile.updateTile();
    }

    private int lowestBud() {
        int level = -1;
        for (Aacp.Battery b : new Aacp.Battery[] {mState.left, mState.right}) {
            if (b.level >= 0 && (level < 0 || b.level < level)) level = b.level;
        }
        return level;
    }

    private String modeName(int mode) {
        switch (mode) {
            case Aacp.MODE_OFF: return getString(R.string.pods_mode_off);
            case Aacp.MODE_TRANSPARENCY: return getString(R.string.pods_mode_transparency);
            case Aacp.MODE_ADAPTIVE: return getString(R.string.pods_mode_adaptive);
            case Aacp.MODE_NOISE_CANCELLATION:
                return getString(R.string.pods_mode_noise_cancellation);
            default: return "";
        }
    }

    private static int modeIcon(int mode) {
        switch (mode) {
            case Aacp.MODE_OFF: return R.drawable.ic_mode_off;
            case Aacp.MODE_TRANSPARENCY: return R.drawable.ic_mode_transparency;
            case Aacp.MODE_ADAPTIVE: return R.drawable.ic_mode_adaptive;
            case Aacp.MODE_NOISE_CANCELLATION: return R.drawable.ic_mode_noise_cancellation;
            default: return R.drawable.ic_pods;
        }
    }
}

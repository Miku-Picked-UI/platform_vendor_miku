/*
 * PickedPods - AirPods controls for Miku-Picked UI
 * Copyright (C) 2026 Miku-Picked UI
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.mikupicked.airpods;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * What the connected AirPods last reported, shared by {@link PodsService} and the Settings
 * provider in this process. Values are -1 until the AirPods send them.
 */
final class PodsState {

    interface Listener {
        void onPodsChanged(String address);
    }

    private static final PodsState INSTANCE = new PodsState();

    static PodsState get() {
        return INSTANCE;
    }

    private final CopyOnWriteArrayList<Listener> mListeners = new CopyOnWriteArrayList<>();

    /** Address of the AirPods the session is open with, or null. */
    volatile String address;
    volatile boolean connected;

    final Aacp.Battery left = new Aacp.Battery();
    final Aacp.Battery right = new Aacp.Battery();
    final Aacp.Battery box = new Aacp.Battery();

    /** Ear state of the two buds ({@link Aacp#EAR_IN} etc.). */
    volatile int[] ears = {Aacp.EAR_OUT, Aacp.EAR_OUT};

    volatile int listeningMode = -1;
    volatile int conversationAwareness = -1; // 1 on, 0 off
    volatile int adaptiveVolume = -1; // 1 on, 0 off
    volatile int micMode = -1;
    volatile int allowOff = -1; // 1 on, 0 off

    private PodsState() {}

    void addListener(Listener l) {
        mListeners.addIfAbsent(l);
    }

    void removeListener(Listener l) {
        mListeners.remove(l);
    }

    void notifyChanged() {
        final String a = address;
        if (a == null) return;
        for (Listener l : mListeners) l.onPodsChanged(a);
    }

    void reset(String newAddress) {
        address = newAddress;
        connected = false;
        for (Aacp.Battery b : new Aacp.Battery[] {left, right, box}) {
            b.level = -1;
            b.charging = false;
            b.connected = false;
        }
        ears = new int[] {Aacp.EAR_OUT, Aacp.EAR_OUT};
        listeningMode = conversationAwareness = adaptiveVolume = micMode = allowOff = -1;
    }

    boolean anyInEar() {
        final int[] e = ears;
        return e[0] == Aacp.EAR_IN || e[1] == Aacp.EAR_IN;
    }
}

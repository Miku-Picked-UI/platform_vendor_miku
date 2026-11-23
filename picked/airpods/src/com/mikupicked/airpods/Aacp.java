/*
 * PickedPods - AirPods controls for Miku-Picked UI
 * Copyright (C) 2025 LibrePods contributors
 * Copyright (C) 2026 Miku-Picked UI
 *
 * The protocol here is LibrePods' (github.com/librepods-org/librepods, android/app
 * bluetooth/AACPManager.kt and data/Packets.kt), rewritten in Java.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <https://www.gnu.org/licenses/>.
 */

package com.mikupicked.airpods;

import java.util.Arrays;

/**
 * Apple Accessory Communication Protocol (AACP), spoken with AirPods over L2CAP PSM 0x1001.
 * Every data packet starts with {@link #HEADER}, then a one-byte opcode and a zero byte.
 */
final class Aacp {

    /** The AirPods' AAP service; Android 17's L2CAP interop fix keys on it too. */
    static final String SERVICE_UUID = "74ec2172-0bad-4d01-8f77-997b2be0722a";
    static final int PSM = 0x1001;

    static final byte[] HEADER = {0x04, 0x00, 0x04, 0x00};

    // Opcodes
    static final byte OP_BATTERY = 0x04;
    static final byte OP_EAR_DETECTION = 0x06;
    static final byte OP_CONTROL = 0x09;
    static final byte OP_REQUEST_NOTIFICATIONS = 0x0F;
    static final byte OP_INFORMATION = 0x1D;
    static final byte OP_CONVERSATION_AWARENESS = 0x4B;
    static final byte OP_SET_FEATURE_FLAGS = 0x4D;

    // Control command identifiers
    static final byte CTRL_MIC_MODE = 0x01;
    static final byte CTRL_LISTENING_MODE = 0x0D;
    static final byte CTRL_ONE_BUD_ANC = 0x1B;
    static final byte CTRL_ADAPTIVE_VOLUME = 0x26;
    static final byte CTRL_CONVERSATION_DETECT = 0x28;
    static final byte CTRL_ALLOW_OFF_OPTION = 0x34;

    // Listening modes (CTRL_LISTENING_MODE)
    static final int MODE_OFF = 1;
    static final int MODE_NOISE_CANCELLATION = 2;
    static final int MODE_TRANSPARENCY = 3;
    static final int MODE_ADAPTIVE = 4;

    // Microphone (CTRL_MIC_MODE)
    static final int MIC_AUTOMATIC = 0;
    static final int MIC_ALWAYS_RIGHT = 1;
    static final int MIC_ALWAYS_LEFT = 2;

    // Boolean control values
    static final byte ON = 0x01;
    static final byte OFF = 0x02;

    // Battery components and states
    static final int COMPONENT_RIGHT = 2;
    static final int COMPONENT_LEFT = 4;
    static final int COMPONENT_CASE = 8;
    static final int BATTERY_CHARGING = 1;
    static final int BATTERY_DISCONNECTED = 4;

    // Ear detection, per bud
    static final int EAR_IN = 0;
    static final int EAR_OUT = 1;
    static final int EAR_IN_CASE = 2;

    private Aacp() {}

    /** Opens the session. Not a data packet: it has its own header. */
    static byte[] handshake() {
        return new byte[] {0x00, 0x00, 0x04, 0x00, 0x01, 0x00, 0x02, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    }

    static byte[] setFeatureFlags() {
        return data(new byte[] {OP_SET_FEATURE_FLAGS, 0x00,
                (byte) 0xD7, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
    }

    /** Asks for battery, ear detection, control and conversation notifications. */
    static byte[] requestNotifications() {
        return data(new byte[] {OP_REQUEST_NOTIFICATIONS, 0x00,
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});
    }

    /** Sets control {@code id} to {@code value} (at most four bytes, zero padded). */
    static byte[] control(byte id, byte... value) {
        final byte[] payload = new byte[7];
        payload[0] = OP_CONTROL;
        payload[1] = 0x00;
        payload[2] = id;
        System.arraycopy(value, 0, payload, 3, Math.min(value.length, 4));
        return data(payload);
    }

    static byte[] data(byte[] payload) {
        final byte[] out = Arrays.copyOf(HEADER, HEADER.length + payload.length);
        System.arraycopy(payload, 0, out, HEADER.length, payload.length);
        return out;
    }

    /** The opcode of a received packet, or -1 if it isn't a data packet. */
    static int opcode(byte[] p, int len) {
        if (len < 6) return -1;
        for (int i = 0; i < HEADER.length; i++) {
            if (p[i] != HEADER[i]) return -1;
        }
        return p[4] & 0xff;
    }

    /** Battery of one component; level -1 when unknown. */
    static final class Battery {
        int level = -1;
        boolean charging;
        boolean connected;
    }

    /**
     * Parses a battery packet into left, right and case. Layout after the opcode: a count, then
     * per component {type, 0x01, level, state, 0x01}.
     */
    static boolean parseBattery(byte[] p, int len, Battery left, Battery right, Battery box) {
        if (len < 7) return false;
        final int count = p[6] & 0xff;
        int off = 7;
        for (int i = 0; i < count && off + 3 < len; i++, off += 5) {
            final int type = p[off] & 0xff;
            final int level = p[off + 2];
            final int state = p[off + 3] & 0xff;
            final Battery b = type == COMPONENT_LEFT ? left
                    : type == COMPONENT_RIGHT ? right
                    : type == COMPONENT_CASE ? box : null;
            if (b == null) continue;
            b.connected = state != BATTERY_DISCONNECTED;
            // The case reports a stale level while the buds are out of it; keep the last good one
            if (b.connected && level >= 0 && level <= 100) b.level = level;
            b.charging = b.connected && state == BATTERY_CHARGING;
        }
        return true;
    }

    /** Ear state of the {primary, secondary} buds from an ear detection packet. */
    static int[] parseEarDetection(byte[] p, int len) {
        if (len < 8) return null;
        return new int[] {p[6] & 0xff, p[7] & 0xff};
    }

    /** Control id and value bytes from a control packet. */
    static byte[] parseControl(byte[] p, int len) {
        if (len < 7) return null;
        return Arrays.copyOfRange(p, 6, Math.min(len, 11));
    }

    /** Conversation awareness level: 1-2 the user started speaking, 6-9 stopped. */
    static int parseConversationAwareness(byte[] p, int len) {
        return len >= 10 ? p[9] & 0xff : -1;
    }
}

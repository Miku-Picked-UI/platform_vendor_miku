/*
 * Copyright (C) 2026 The PenguinOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.penguin.laboratory.dex

import android.content.Context
import android.provider.Settings

/**
 * DeX's switches, shared with the framework, SystemUI, Launcher3 and PenguinDex, which each read
 * the ones they act on.
 */
object DexStore {
    /** The framework's own switch: a connected screen mirrors the phone unless this is 0. */
    private const val MIRROR = "mirror_built_in_display"

    const val DOCK = "penguin_dex_dock"
    const val LAUNCHPAD = "penguin_dex_launchpad"
    const val CONTROL_CENTRE = "penguin_dex_control_centre"
    const val TOUCHPAD = "penguin_dex_touchpad"

    fun isEnabled(context: Context) =
        Settings.Secure.getInt(context.contentResolver, MIRROR, 1) == 0

    fun setEnabled(context: Context, enabled: Boolean) {
        Settings.Secure.putInt(context.contentResolver, MIRROR, if (enabled) 0 else 1)
    }

    /** DeX's features are on until turned off. */
    fun isOn(context: Context, key: String) = Settings.Secure.getInt(context.contentResolver, key, 1) != 0

    fun set(context: Context, key: String, on: Boolean) {
        Settings.Secure.putInt(context.contentResolver, key, if (on) 1 else 0)
    }
}

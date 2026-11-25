/*
 * SPDX-FileCopyrightText: 2026 Miku-Picked UI
 * SPDX-License-Identifier: Apache-2.0
 */

package com.penguin.laboratory

/**
 * Miku-Picked UI: which PenguinOS features behind the Laboratory's switches this ROM has. A
 * switch whose SystemUI, framework or Launcher part isn't ported yet is hidden rather than
 * shown doing nothing; port the part, then turn it on here.
 */
object Backends {
    /** Experiments keys with a backend. */
    val experiments: Set<String> =
        setOf(
            "depth_wallpaper_floating",
            "ls_lock_icon_top",
            "back_to_app_indicator",
            "qs_oneui_experimental",
            "recents_frequent_apps",
            "penguin_luminous",
            "penguin_luminous_all_apps",
        )

    /** PenguinOS power menu styles (SystemUI globalactions PowerMenuStyle, power_menu_style). */
    const val POWER_MENU_STYLES = true

    /** Screen-off fingerprint icon and its schedule (SystemUI doze DozeUdfpsIconController). */
    const val SCREEN_OFF_UDFPS_ICON = true

    /** Dual tone shade behind notifications (penguin_liquid_glass_dual_tone). */
    const val GLASS_DUAL_TONE = true

    /** DeX dock, launchpad, Control Centre and touchpad (the PenguinDex app). */
    const val DEX_DESKTOP_FEATURES = false
}

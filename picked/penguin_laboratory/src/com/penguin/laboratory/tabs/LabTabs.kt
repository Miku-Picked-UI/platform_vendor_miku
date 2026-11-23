/*
 * Copyright (C) 2026 The PenguinOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.penguin.laboratory.tabs

import android.content.ContentResolver
import android.content.Context
import android.provider.Settings
import androidx.annotation.ArrayRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.penguin.laboratory.R

/** Which Settings table a key lives in. */
enum class Table {
    System,
    Secure,
    Global;

    fun get(resolver: ContentResolver, key: String): String? =
        when (this) {
            System -> Settings.System.getString(resolver, key)
            Secure -> Settings.Secure.getString(resolver, key)
            Global -> Settings.Global.getString(resolver, key)
        }

    fun put(resolver: ContentResolver, key: String, value: String) {
        when (this) {
            System -> Settings.System.putString(resolver, key, value)
            Secure -> Settings.Secure.putString(resolver, key, value)
            Global -> Settings.Global.putString(resolver, key, value)
        }
    }
}

/** One row on a Lab tab. Rows read and write the same keys Settings used before they moved. */
sealed interface LabRow {
    val title: Int

    /** A switch stored as 1 or 0. [parent] greys it out while that switch is off. */
    class Toggle(
        val table: Table,
        val key: String,
        @StringRes override val title: Int,
        @StringRes val summary: Int? = null,
        val default: Boolean = false,
        @DrawableRes val icon: Int? = null,
        val parent: String? = null,
    ) : LabRow {
        fun read(context: Context) =
            table.get(context.contentResolver, key)?.toIntOrNull()?.let { it != 0 } ?: default

        fun write(context: Context, on: Boolean) =
            table.put(context.contentResolver, key, if (on) "1" else "0")
    }

    /** One of a few values, stored as the value's string. */
    class Choice(
        val table: Table,
        val key: String,
        @StringRes override val title: Int,
        @ArrayRes val labels: Int,
        val values: List<String>,
        val default: String,
        @DrawableRes val icon: Int? = null,
        /** Greys this out while the choice with this key, in the same section, has this value. */
        val disabledWhen: Pair<String, String>? = null,
    ) : LabRow {
        fun read(context: Context) = table.get(context.contentResolver, key) ?: default

        fun write(context: Context, value: String) = table.put(context.contentResolver, key, value)
    }

    /** A page with more to it than switches, still drawn by Settings, opened in place. */
    class Link(
        val fragment: String,
        @StringRes override val title: Int,
        @StringRes val summary: Int? = null,
        @DrawableRes val icon: Int? = null,
    ) : LabRow
}

/** A group of rows. [largeScreenOnly] groups show only on tablets and foldables opened out. */
class LabSection(
    @StringRes val title: Int?,
    val rows: List<LabRow>,
    val largeScreenOnly: Boolean = false,
)

class LabTab(
    @StringRes val label: Int,
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
    val sections: List<LabSection>,
)

/**
 * The tabs after Originals. PenguinOS gathered its own options here; Miku-Picked UI lists the
 * ones this ROM has, with its own keys and Settings pages (packages/apps/Settings
 * picked.PenguinLabLinkActivity must allow every page linked here).
 */
object LabTabs {
    private const val M = "com.miku.exsettings.fragments"

    val all =
        listOf(
            LabTab(
                R.string.tab_status,
                R.string.tab_status_title,
                R.drawable.materialsymbols_ic_top_panel_open_rounded_filled,
                listOf(
                    LabSection(
                        R.string.section_status_bar,
                        listOf(
                            LabRow.Toggle(
                                Table.System,
                                "status_bar_brightness_control",
                                R.string.row_brightness_slide,
                                R.string.row_brightness_slide_summary,
                                icon = R.drawable.materialsymbols_ic_brightness_medium_rounded_filled,
                            ),
                            LabRow.Toggle(
                                Table.System,
                                "status_bar_brightness_control_lockscreen",
                                R.string.row_brightness_slide_locked,
                                parent = "status_bar_brightness_control",
                            ),
                        ),
                    ),
                    LabSection(
                        R.string.section_notifications,
                        listOf(
                            LabRow.Toggle(
                                Table.System,
                                "notification_sound_vib_screen_on",
                                R.string.row_screen_on_alerts,
                                R.string.row_screen_on_alerts_summary,
                                default = true,
                                icon = R.drawable.materialsymbols_ic_notification_sound_rounded_filled,
                            ),
                        ),
                    ),
                ),
            ),
            LabTab(
                R.string.tab_lock,
                R.string.tab_lock_title,
                R.drawable.materialsymbols_ic_lock_rounded_filled,
                listOf(
                    LabSection(
                        R.string.section_lock_more,
                        listOf(
                            LabRow.Link(
                                "$M.PickedWeather",
                                R.string.row_weather,
                                R.string.row_weather_summary,
                                R.drawable.materialsymbols_ic_partly_cloudy_day_rounded_filled,
                            ),
                            // SystemUI picked/mediaart KeyguardMediaArtSection
                            LabRow.Toggle(
                                Table.Secure,
                                "picked_lockscreen_media_art",
                                R.string.row_media_art,
                                R.string.row_media_art_summary,
                                icon = R.drawable.materialsymbols_ic_album_rounded_filled,
                            ),
                            // SystemUI picked/visualizer KeyguardVisualizerSection
                            LabRow.Toggle(
                                Table.Secure,
                                "picked_lockscreen_visualizer",
                                R.string.row_pulse,
                                R.string.row_pulse_summary,
                                icon = R.drawable.materialsymbols_ic_equalizer_rounded_filled,
                            ),
                            LabRow.Link(
                                "$M.PickedDepthWallpaper",
                                R.string.row_depth,
                                R.string.row_depth_summary,
                                R.drawable.materialsymbols_ic_layers_rounded_filled,
                            ),
                        ),
                    ),
                ),
            ),
            LabTab(
                R.string.tab_sound,
                R.string.tab_sound_title,
                R.drawable.materialsymbols_ic_graphic_eq_rounded_filled,
                listOf(
                    LabSection(
                        R.string.section_volume,
                        listOf(
                            LabRow.Link(
                                "$M.PickedAppAudio",
                                R.string.row_app_volume,
                                R.string.row_app_volume_summary,
                                R.drawable.materialsymbols_ic_tune_rounded_filled,
                            ),
                            LabRow.Toggle(
                                Table.System,
                                "multi_audio_focus_enabled",
                                R.string.row_multi_focus,
                                R.string.row_multi_focus_summary,
                                icon = R.drawable.materialsymbols_ic_queue_music_rounded_filled,
                            ),
                            LabRow.Toggle(
                                Table.System,
                                "screenshot_shutter_sound",
                                R.string.row_screenshot_sound,
                                default = true,
                                icon = R.drawable.materialsymbols_ic_screenshot_rounded_filled,
                            ),
                        ),
                    ),
                ),
            ),
            LabTab(
                R.string.tab_system,
                R.string.tab_system_title,
                R.drawable.materialsymbols_ic_display_settings_rounded_filled,
                listOf(
                    LabSection(
                        R.string.section_theme,
                        listOf(
                            LabRow.Link(
                                "$M.ThemeSettings",
                                R.string.row_theme,
                                R.string.row_theme_summary,
                                R.drawable.materialsymbols_ic_palette_rounded_filled,
                            ),
                            LabRow.Link(
                                "$M.FontsPicker",
                                R.string.row_fonts,
                                R.string.row_fonts_summary,
                                R.drawable.materialsymbols_ic_font_download_rounded_filled,
                            ),
                        ),
                    ),
                    LabSection(
                        R.string.section_display,
                        listOf(
                            LabRow.Link(
                                "$M.SmartPixels",
                                R.string.row_smart_pixels,
                                R.string.row_smart_pixels_summary,
                                R.drawable.materialsymbols_ic_blur_on_rounded_filled,
                            ),
                            LabRow.Link(
                                "$M.TaskbarSettings",
                                R.string.row_taskbar_more,
                                R.string.row_taskbar_more_summary,
                                R.drawable.materialsymbols_ic_dock_to_bottom_rounded_filled,
                            ),
                        ),
                    ),
                    LabSection(
                        R.string.section_battery,
                        listOf(
                            LabRow.Link(
                                "$M.PickedSleepMode",
                                R.string.row_sleep_mode,
                                R.string.row_sleep_mode_summary,
                                R.drawable.materialsymbols_ic_bedtime_rounded_filled,
                            ),
                        ),
                    ),
                ),
            ),
        )
}

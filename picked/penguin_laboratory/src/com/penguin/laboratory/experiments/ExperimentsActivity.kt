/*
 * Copyright (C) 2026 The PenguinOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.penguin.laboratory.experiments

import android.content.Context
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.penguin.laboratory.Backends
import com.penguin.laboratory.R
import com.penguin.laboratory.ui.LabTheme
import com.penguin.laboratory.ui.SectionHeader
import com.penguin.laboratory.ui.SegmentedGroup
import com.penguin.laboratory.ui.SwitchItem

/** Switches for features that are still being worked on, read by SystemUI and the launcher. */
object ExperimentsStore {
    /** A switch, where it lives and what it needs on to mean anything. */
    class Experiment(
        val key: String,
        val secure: Boolean,
        @StringRes val title: Int,
        @StringRes val summary: Int,
        @DrawableRes val icon: Int,
        val dependsOn: String? = null,
        val defaultOn: Boolean = false,
    )

    val lockScreen =
        listOf(
            Experiment(
                "ls_now_bar",
                secure = false,
                R.string.experiments_ls_now_bar,
                R.string.experiments_ls_now_bar_summary,
                R.drawable.materialsymbols_ic_music_note_rounded_filled,
                defaultOn = true,
            ),
            Experiment(
                "ls_now_bar_art",
                secure = false,
                R.string.experiments_ls_now_bar_art,
                R.string.experiments_ls_now_bar_art_summary,
                R.drawable.materialsymbols_ic_wallpaper_rounded_filled,
                dependsOn = "ls_now_bar",
                defaultOn = true,
            ),
            Experiment(
                "ls_lock_icon_top",
                secure = false,
                R.string.experiments_ls_lock_icon_top,
                R.string.experiments_ls_lock_icon_top_summary,
                R.drawable.materialsymbols_ic_layers_rounded_filled,
                // Miku-Picked UI: SystemUI keeps the stock place unless chosen
                defaultOn = false,
            ),
            Experiment(
                "ls_media_expand",
                secure = false,
                R.string.experiments_ls_media_expand,
                R.string.experiments_ls_media_expand_summary,
                R.drawable.materialsymbols_ic_music_note_rounded_filled,
            ),
            Experiment(
                "ls_media_motion_art",
                secure = false,
                R.string.experiments_ls_media_motion_art,
                R.string.experiments_ls_media_motion_art_summary,
                R.drawable.materialsymbols_ic_wallpaper_rounded_filled,
                dependsOn = "ls_media_expand",
            ),
            Experiment(
                "depth_wallpaper_floating",
                secure = true,
                R.string.experiments_depth_floating,
                R.string.experiments_depth_floating_summary,
                R.drawable.materialsymbols_ic_layers_rounded_filled,
            ),
        )
            // Miku-Picked UI: only what this ROM has a backend for
            .filter { it.key in Backends.experiments }

    val system =
        listOf(
            Experiment(
                "qs_oneui_experimental",
                secure = true,
                R.string.experiments_qs_oneui,
                R.string.experiments_qs_oneui_summary,
                R.drawable.materialsymbols_ic_dashboard_customize_rounded_filled,
            ),
            Experiment(
                "back_to_app_indicator",
                secure = false,
                R.string.experiments_back_to_app,
                R.string.experiments_back_to_app_summary,
                R.drawable.materialsymbols_ic_arrow_back_rounded_filled,
            ),
            // Miku-Picked UI: off unless turned on here; PenguinOS always runs system apps' view
            // animations on the render thread.
            Experiment(
                "penguin_luminous",
                secure = true,
                R.string.experiments_luminous,
                R.string.experiments_luminous_summary,
                R.drawable.materialsymbols_ic_animation_rounded_filled,
            ),
            Experiment(
                "penguin_luminous_all_apps",
                secure = true,
                R.string.experiments_luminous_all_apps,
                R.string.experiments_luminous_all_apps_summary,
                R.drawable.materialsymbols_ic_animation_rounded_filled,
                dependsOn = "penguin_luminous",
            ),
            Experiment(
                "recents_frequent_apps",
                secure = true,
                R.string.experiments_recents_frequent_apps,
                R.string.experiments_recents_frequent_apps_summary,
                R.drawable.materialsymbols_ic_history_rounded_filled,
            ),
        )
            .filter { it.key in Backends.experiments }

    val all = lockScreen + system

    fun isOn(context: Context, key: String): Boolean {
        val experiment = all.first { it.key == key }
        val resolver = context.contentResolver
        val default = if (experiment.defaultOn) 1 else 0
        return if (experiment.secure) {
            Settings.Secure.getInt(resolver, key, default) != 0
        } else {
            Settings.System.getInt(resolver, key, default) != 0
        }
    }

    fun set(context: Context, key: String, on: Boolean) {
        val experiment = all.first { it.key == key }
        val resolver = context.contentResolver
        if (experiment.secure) {
            Settings.Secure.putInt(resolver, key, if (on) 1 else 0)
        } else {
            Settings.System.putInt(resolver, key, if (on) 1 else 0)
        }
    }

    fun enabledCount(context: Context) = all.count { isOn(context, it.key) }
}

class ExperimentsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { LabTheme { Experiments() } }
    }

    @Composable
    private fun Experiments() {
        val context = this
        val states = remember {
            mutableStateMapOf<String, Boolean>().apply {
                ExperimentsStore.all.forEach { put(it.key, ExperimentsStore.isOn(context, it.key)) }
            }
        }
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            topBar = {
                MediumFlexibleTopAppBar(
                    title = { Text(stringResource(R.string.experiments_title)) },
                    subtitle = { Text(stringResource(R.string.experiments_entry_summary)) },
                    navigationIcon = {
                        IconButton(onClick = { finish() }) {
                            Icon(painterResource(R.drawable.ic_back), contentDescription = null)
                        }
                    },
                    colors =
                        TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                        ),
                    scrollBehavior = scrollBehavior,
                )
            },
        ) { padding ->
            LazyColumn(
                contentPadding =
                    PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = padding.calculateTopPadding(),
                        bottom = padding.calculateBottomPadding() + 24.dp,
                    )
            ) {
                // Miku-Picked UI: a section with nothing ported yet is left out
                if (ExperimentsStore.lockScreen.isNotEmpty()) {
                    item { SectionHeader(stringResource(R.string.experiments_lock_screen)) }
                    item { Group(ExperimentsStore.lockScreen, states) }
                }
                if (ExperimentsStore.system.isNotEmpty()) {
                    item { SectionHeader(stringResource(R.string.experiments_system)) }
                    item { Group(ExperimentsStore.system, states) }
                }
            }
        }
    }

    @Composable
    private fun Group(experiments: List<ExperimentsStore.Experiment>, states: MutableMap<String, Boolean>) {
        // A switch that only means something with another on is left out while that one is off.
        val shown = experiments.filter { e -> e.dependsOn == null || states[e.dependsOn] == true }
        SegmentedGroup {
            shown.forEachIndexed { index, experiment ->
                SwitchItem(
                    title = stringResource(experiment.title),
                    supporting = stringResource(experiment.summary),
                    checked = states[experiment.key] == true,
                    onCheckedChange = {
                        ExperimentsStore.set(this@ExperimentsActivity, experiment.key, it)
                        states[experiment.key] = it
                    },
                    index = index,
                    count = shown.size,
                    icon = experiment.icon,
                )
            }
        }
    }
}

/*
 * Copyright (C) 2026 The PenguinOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.penguin.laboratory.dex

import com.penguin.laboratory.Backends
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.penguin.laboratory.R
import com.penguin.laboratory.ui.LabTheme
import com.penguin.laboratory.ui.SectionHeader
import com.penguin.laboratory.ui.SegmentedGroup
import com.penguin.laboratory.ui.SwitchItem

/** DeX: the phone as a desktop on a connected screen or a PC, and what that desktop offers. */
class DexActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { LabTheme { Dex() } }
    }

    @Composable
    private fun Dex() {
        var enabled by remember { mutableStateOf(DexStore.isEnabled(this)) }
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            topBar = {
                MediumFlexibleTopAppBar(
                    title = { Text(stringResource(R.string.dex_title)) },
                    subtitle = { Text(stringResource(R.string.dex_entry_summary)) },
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
                        top = padding.calculateTopPadding() + 8.dp,
                        bottom = padding.calculateBottomPadding() + 24.dp,
                    )
            ) {
                item {
                    SegmentedGroup {
                        SwitchItem(
                            title = stringResource(R.string.dex_use),
                            supporting = stringResource(R.string.dex_use_summary),
                            checked = enabled,
                            onCheckedChange = {
                                DexStore.setEnabled(this@DexActivity, it)
                                enabled = it
                            },
                            index = 0,
                            count = 1,
                            icon = R.drawable.materialsymbols_ic_desktop_windows_rounded_filled,
                        )
                    }
                }
                // Miku-Picked UI: the dock, launchpad, Control Centre and touchpad come from the
                // PenguinDex app, which this ROM doesn't have; a connected screen gets AOSP's
                // desktop.
                if (Backends.DEX_DESKTOP_FEATURES) item { SectionHeader(stringResource(R.string.dex_features)) }
                if (Backends.DEX_DESKTOP_FEATURES) item {
                    SegmentedGroup {
                        Feature(DexStore.DOCK, R.string.dex_dock, R.string.dex_dock_summary,
                            R.drawable.materialsymbols_ic_dock_to_bottom_rounded_filled, enabled, 0)
                        Feature(DexStore.LAUNCHPAD, R.string.dex_launchpad, R.string.dex_launchpad_summary,
                            R.drawable.materialsymbols_ic_apps_rounded_filled, enabled, 1)
                        Feature(DexStore.CONTROL_CENTRE, R.string.dex_control_centre,
                            R.string.dex_control_centre_summary,
                            R.drawable.materialsymbols_ic_toggle_on_rounded_filled, enabled, 2)
                        Feature(DexStore.TOUCHPAD, R.string.dex_touchpad, R.string.dex_touchpad_summary,
                            R.drawable.materialsymbols_ic_touchpad_mouse_rounded_filled, enabled, 3)
                    }
                }
                if (Backends.DEX_DESKTOP_FEATURES) item {
                    Text(
                        stringResource(R.string.dex_pc_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                    )
                }
            }
        }
    }

    /** A DeX feature's switch, which only matters while DeX is on. */
    @Composable
    private fun Feature(key: String, title: Int, summary: Int, icon: Int, dexOn: Boolean, index: Int) {
        var on by remember { mutableStateOf(DexStore.isOn(this, key)) }
        SwitchItem(
            title = stringResource(title),
            supporting = stringResource(summary),
            checked = on && dexOn,
            onCheckedChange = {
                if (!dexOn) return@SwitchItem
                DexStore.set(this, key, it)
                on = it
            },
            index = index,
            count = 4,
            icon = icon,
        )
    }
}

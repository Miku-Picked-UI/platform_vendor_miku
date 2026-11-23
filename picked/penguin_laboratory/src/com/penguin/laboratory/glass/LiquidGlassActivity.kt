/*
 * Copyright (C) 2026 The PenguinOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.penguin.laboratory.glass

import com.penguin.laboratory.Backends
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import com.penguin.laboratory.ui.SegmentedListItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.penguin.laboratory.R
import com.penguin.laboratory.ui.ConnectedChoice
import com.penguin.laboratory.ui.LabTheme
import com.penguin.laboratory.ui.PercentItem
import com.penguin.laboratory.ui.SectionHeader
import com.penguin.laboratory.ui.SegmentedGroup
import com.penguin.laboratory.ui.SwitchItem
import com.penguin.laboratory.ui.segmentedShapes

/** Liquid Glass: the system's blurred surfaces drawn as glass that bends and catches light. */
object LiquidGlassStore {
    /** Read by the window manager, which hands it to SurfaceFlinger. */
    private const val KEY = "penguin_liquid_glass"

    fun isEnabled(context: Context) = Settings.Secure.getInt(context.contentResolver, KEY, 0) != 0

    fun setEnabled(context: Context, enabled: Boolean) {
        Settings.Secure.putInt(context.contentResolver, KEY, if (enabled) 1 else 0)
    }

    enum class Style { Ios, ColorOs }

    private const val STYLE_KEY = "penguin_liquid_glass_style"

    fun style(context: Context) =
        if (Settings.Secure.getInt(context.contentResolver, STYLE_KEY, 0) == 1) Style.ColorOs
        else Style.Ios

    fun setStyle(context: Context, style: Style) {
        Settings.Secure.putInt(
            context.contentResolver,
            STYLE_KEY,
            if (style == Style.ColorOs) 1 else 0,
        )
    }

    /** How strongly the iOS style's glass rims bend what lies beyond them, 0-100%. */
    private const val REFRACTION_KEY = "penguin_liquid_glass_refraction"

    fun refraction(context: Context) =
        Settings.Secure.getInt(context.contentResolver, REFRACTION_KEY, 60).coerceIn(0, 100)

    fun setRefraction(context: Context, percent: Int) {
        Settings.Secure.putInt(context.contentResolver, REFRACTION_KEY, percent.coerceIn(0, 100))
    }

    /** A faint accent tint behind the notifications in the shade, read by SystemUI. */
    private const val DUAL_TONE_KEY = "penguin_liquid_glass_dual_tone"

    fun isDualTone(context: Context) =
        Settings.Secure.getInt(context.contentResolver, DUAL_TONE_KEY, 0) == 1

    fun setDualTone(context: Context, enabled: Boolean) {
        Settings.Secure.putInt(context.contentResolver, DUAL_TONE_KEY, if (enabled) 1 else 0)
    }

}

class LiquidGlassActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { LabTheme { LiquidGlass() } }
    }

    @Composable
    private fun LiquidGlass() {
        var enabled by remember { mutableStateOf(LiquidGlassStore.isEnabled(this)) }
        var tabBars by remember { mutableStateOf(TabBarStore.isEnabled(this)) }
        var style by remember { mutableStateOf(LiquidGlassStore.style(this)) }
        var refraction by remember { mutableIntStateOf(LiquidGlassStore.refraction(this)) }
        var dualTone by remember { mutableStateOf(LiquidGlassStore.isDualTone(this)) }
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            topBar = {
                MediumFlexibleTopAppBar(
                    title = { Text(stringResource(R.string.liquid_glass_title)) },
                    subtitle = { Text(stringResource(R.string.liquid_glass_entry_summary)) },
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
                            title = stringResource(R.string.liquid_glass_use),
                            supporting = stringResource(R.string.liquid_glass_use_summary),
                            checked = enabled,
                            onCheckedChange = {
                                LiquidGlassStore.setEnabled(this@LiquidGlassActivity, it)
                                enabled = it
                            },
                            index = 0,
                            count = 1,
                            icon = R.drawable.materialsymbols_ic_water_drop_rounded_filled,
                        )
                    }
                }
                item { SectionHeader(stringResource(R.string.liquid_glass_style_header)) }
                item {
                    ConnectedChoice(
                        options = LiquidGlassStore.Style.entries,
                        selected = style,
                        onSelect = {
                            LiquidGlassStore.setStyle(this@LiquidGlassActivity, it)
                            style = it
                        },
                        enabled = enabled,
                        label = {
                            stringResource(
                                when (it) {
                                    LiquidGlassStore.Style.Ios -> R.string.liquid_glass_style_ios
                                    LiquidGlassStore.Style.ColorOs ->
                                        R.string.liquid_glass_style_coloros
                                }
                            )
                        },
                    )
                }
                item {
                    Text(
                        stringResource(
                            if (style == LiquidGlassStore.Style.ColorOs)
                                R.string.liquid_glass_style_coloros_summary
                            else R.string.liquid_glass_style_ios_summary
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
                // ColorOS's glass is frosted and does not refract.
                if (style == LiquidGlassStore.Style.Ios) {
                    item { SectionHeader(stringResource(R.string.liquid_glass_refraction_header)) }
                    item {
                        SegmentedGroup {
                            PercentItem(
                                title = stringResource(R.string.liquid_glass_refraction_header),
                                value = refraction / 100f,
                                onValueChange = {
                                    val percent = Math.round(it * 100)
                                    if (percent != refraction) {
                                        LiquidGlassStore.setRefraction(this@LiquidGlassActivity, percent)
                                        refraction = percent
                                    }
                                },
                                index = 0,
                                count = 1,
                                enabled = enabled,
                            )
                        }
                    }
                    item {
                        Text(
                            stringResource(R.string.liquid_glass_refraction_summary),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                }
                // Miku-Picked UI: no dual tone shade in this SystemUI yet
                if (Backends.GLASS_DUAL_TONE) item { SectionHeader(stringResource(R.string.liquid_glass_shade_header)) }
                if (Backends.GLASS_DUAL_TONE) item {
                    SegmentedGroup {
                        SwitchItem(
                            title = stringResource(R.string.liquid_glass_dual_tone),
                            supporting = stringResource(R.string.liquid_glass_dual_tone_summary),
                            checked = dualTone,
                            onCheckedChange = {
                                LiquidGlassStore.setDualTone(this@LiquidGlassActivity, it)
                                dualTone = it
                            },
                            index = 0,
                            count = 1,
                        )
                    }
                }
                item { SectionHeader(stringResource(R.string.tab_bars_header)) }
                item {
                    SegmentedGroup {
                        SwitchItem(
                            title = stringResource(R.string.tab_bars_use),
                            supporting = stringResource(R.string.tab_bars_use_summary),
                            checked = tabBars,
                            onCheckedChange = {
                                TabBarStore.setEnabled(this@LiquidGlassActivity, it)
                                tabBars = it
                            },
                            index = 0,
                            count = 2,
                            icon = R.drawable.materialsymbols_ic_dock_to_bottom_rounded_filled,
                        )
                        SegmentedListItem(
                            onClick = {
                                startActivity(
                                    Intent(this@LiquidGlassActivity, TabBarAppsActivity::class.java)
                                )
                            },
                            shapes = ListItemDefaults.segmentedShapes(1, 2),
                            enabled = tabBars,
                            leadingContent = {
                                Icon(
                                    painterResource(R.drawable.materialsymbols_ic_apps_rounded_filled),
                                    contentDescription = null,
                                )
                            },
                            supportingContent = {
                                Text(stringResource(R.string.tab_bars_apps_summary))
                            },
                        ) {
                            Text(stringResource(R.string.tab_bars_apps_title))
                        }
                    }
                }
                item {
                    Text(
                        stringResource(R.string.liquid_glass_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                    )
                }
            }
        }
    }
}

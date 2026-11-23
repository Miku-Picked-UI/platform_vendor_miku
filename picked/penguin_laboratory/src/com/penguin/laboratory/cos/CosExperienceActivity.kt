/*
 * Copyright (C) 2026 The PenguinOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.penguin.laboratory.cos

import android.content.Context
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

object CosExperienceStore {
    private const val ALL = "penguin_cos_experience"

    enum class Part(
        val key: String,
        val title: Int,
        val summary: Int,
        val icon: Int,
        val defaultOn: Boolean = false,
    ) {
        AppMotion(
            "penguin_cos_app_motion",
            R.string.cos_app_motion,
            R.string.cos_app_motion_summary,
            R.drawable.materialsymbols_ic_animation_rounded_filled,
            defaultOn = true,
        ),
        Recents(
            "penguin_cos_recents",
            R.string.cos_recents,
            R.string.cos_recents_summary,
            R.drawable.materialsymbols_ic_tab_recent_rounded_filled,
        ),
        Shade(
            "penguin_cos_shade",
            R.string.cos_shade,
            R.string.cos_shade_summary,
            R.drawable.materialsymbols_ic_swipe_up_rounded_filled,
        ),
        QuickSettings(
            "penguin_cos_qs",
            R.string.cos_qs,
            R.string.cos_qs_summary,
            R.drawable.materialsymbols_ic_toggle_on_rounded_filled,
        ),
        Volume(
            "penguin_cos_volume",
            R.string.cos_volume,
            R.string.cos_volume_summary,
            R.drawable.materialsymbols_ic_volume_up_rounded_filled,
        ),
        MediaLights(
            "penguin_cos_media",
            R.string.cos_media,
            R.string.cos_media_summary,
            R.drawable.materialsymbols_ic_graphic_eq_rounded_filled,
        ),
        LockscreenMedia(
            "penguin_cos_lockscreen_media",
            R.string.cos_lockscreen_media,
            R.string.cos_lockscreen_media_summary,
            R.drawable.materialsymbols_ic_music_note_rounded_filled,
        ),
        LockscreenGlass(
            "penguin_cos_lockscreen_glass",
            R.string.cos_lockscreen_glass,
            R.string.cos_lockscreen_glass_summary,
            R.drawable.materialsymbols_ic_blur_on_rounded_filled,
        ),
        Clock(
            "penguin_cos_clock",
            R.string.cos_clock,
            R.string.cos_clock_summary,
            R.drawable.materialsymbols_ic_schedule_rounded_filled,
        ),
    }

    private fun all(context: Context, part: Part) =
        Settings.Secure.getInt(context.contentResolver, ALL, if (part.defaultOn) 1 else 0)

    fun isOn(context: Context, part: Part) =
        Settings.Secure.getInt(context.contentResolver, part.key, all(context, part)) == 1

    fun setOn(context: Context, part: Part, on: Boolean) {
        Settings.Secure.putInt(context.contentResolver, part.key, if (on) 1 else 0)
    }

    fun onCount(context: Context) = Part.entries.count { isOn(context, it) }

    fun setAll(context: Context, on: Boolean) {
        Settings.Secure.putInt(context.contentResolver, ALL, if (on) 1 else 0)
        Part.entries.forEach { setOn(context, it, on) }
    }
}

class CosExperienceActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { LabTheme { CosExperience() } }
    }

    @Composable
    private fun CosExperience() {
        val parts = CosExperienceStore.Part.entries
        var on by remember {
            mutableStateOf(parts.associateWith { CosExperienceStore.isOn(this, it) })
        }
        val everything = on.values.all { it }
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            topBar = {
                MediumFlexibleTopAppBar(
                    title = { Text(stringResource(R.string.cos_title)) },
                    subtitle = { Text(stringResource(R.string.cos_entry_summary)) },
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
                            title = stringResource(R.string.cos_all),
                            supporting = stringResource(R.string.cos_all_summary),
                            checked = everything,
                            onCheckedChange = { checked ->
                                CosExperienceStore.setAll(this@CosExperienceActivity, checked)
                                on = parts.associateWith { checked }
                            },
                            index = 0,
                            count = 1,
                            icon = R.drawable.materialsymbols_ic_auto_awesome_rounded_filled,
                        )
                    }
                }
                item { SectionHeader(stringResource(R.string.cos_parts_header)) }
                item {
                    SegmentedGroup {
                        parts.forEachIndexed { index, part ->
                            SwitchItem(
                                title = stringResource(part.title),
                                supporting = stringResource(part.summary),
                                checked = on.getValue(part),
                                onCheckedChange = { checked ->
                                    CosExperienceStore.setOn(this@CosExperienceActivity, part, checked)
                                    on = on + (part to checked)
                                },
                                index = index,
                                count = parts.size,
                                icon = part.icon,
                            )
                        }
                    }
                }
                item {
                    Text(
                        stringResource(R.string.cos_footer),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    )
                }
            }
        }
    }
}

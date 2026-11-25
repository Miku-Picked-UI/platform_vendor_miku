/*
 * Copyright (C) 2026 The PenguinOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.penguin.laboratory.tabs

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import com.penguin.laboratory.ui.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.penguin.laboratory.R
import com.penguin.laboratory.ui.ConnectedChoice
import com.penguin.laboratory.ui.DropdownItem
import com.penguin.laboratory.ui.SectionHeader
import com.penguin.laboratory.ui.SegmentedBlock
import com.penguin.laboratory.ui.SegmentedGroup
import com.penguin.laboratory.ui.segmentedShapes

/** Settings' entry for opening one of its pages from the Lab, guarded by WRITE_SECURE_SETTINGS. */
private const val SETTINGS_LINK = "com.android.settings.action.PENGUIN_LAB_LINK"
private const val EXTRA_FRAGMENT = "fragment"
private const val EXTRA_TITLE = "title"

fun openSettingsPage(context: Context, fragment: String, title: String) {
    val intent =
        Intent(SETTINGS_LINK)
            .setPackage("com.android.settings")
            .putExtra(EXTRA_FRAGMENT, fragment)
            .putExtra(EXTRA_TITLE, title)
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, R.string.tab_link_missing, Toast.LENGTH_SHORT).show()
    }
}

/**
 * One tab's sections, each a segmented group. [generation] changes when the Lab resumes, so
 * values changed from Settings or Quick Settings meanwhile are read again.
 */
@Composable
fun LabTabPage(tab: LabTab, padding: PaddingValues, state: LazyListState, generation: Int) {
    val context = LocalContext.current
    val toggles = remember(generation) { mutableStateMapOf<String, Boolean>() }
    val choices = remember(generation) { mutableStateMapOf<String, String>() }
    val largeScreen = LocalConfiguration.current.smallestScreenWidthDp >= 600
    // Fill the page: a pager centres a page shorter than the screen, which left a gap under
    // the title on short tabs.
    LazyColumn(modifier = Modifier.fillMaxSize(), state = state, contentPadding = padding) {
        tab.sections.filter { largeScreen || !it.largeScreenOnly }.forEach { section ->
            section.title?.let { item { SectionHeader(stringResource(it)) } }
            item {
                SegmentedGroup {
                    section.rows.forEachIndexed { index, row ->
                        val count = section.rows.size
                        when (row) {
                            is LabRow.Toggle -> {
                                val on = toggles.getOrPut(row.key) { row.read(context) }
                                val parentOn =
                                    row.parent?.let { key ->
                                        toggles.getOrPut(key) {
                                            section.rows
                                                .filterIsInstance<LabRow.Toggle>()
                                                .first { it.key == key }
                                                .read(context)
                                        }
                                    } ?: true
                                ToggleRow(row, on, parentOn, index, count) {
                                    row.write(context, it)
                                    toggles[row.key] = it
                                }
                            }
                            is LabRow.Choice -> {
                                val value = choices.getOrPut(row.key) { row.read(context) }
                                val enabled =
                                    row.disabledWhen?.let { (key, off) ->
                                        val other =
                                            choices.getOrPut(key) {
                                                section.rows
                                                    .filterIsInstance<LabRow.Choice>()
                                                    .first { it.key == key }
                                                    .read(context)
                                            }
                                        other != off
                                    } ?: true
                                ChoiceRow(row, value, enabled, index, count) {
                                    row.write(context, it)
                                    choices[row.key] = it
                                }
                            }
                            is LabRow.Link -> LinkRow(row, index, count)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ToggleRow(
    row: LabRow.Toggle,
    on: Boolean,
    parentOn: Boolean,
    index: Int,
    count: Int,
    onChange: (Boolean) -> Unit,
) {
    // A dependent switch can't be changed while its parent is off, but keeps showing its value.
    SegmentedListItem(
        checked = on,
        onCheckedChange = onChange,
        enabled = parentOn,
        shapes = ListItemDefaults.segmentedShapes(index, count),
        leadingContent = row.icon?.let { { Icon(painterResource(it), contentDescription = null) } },
        supportingContent = row.summary?.let { { Text(stringResource(it)) } },
        trailingContent = {
            androidx.compose.material3.Switch(checked = on, onCheckedChange = null, enabled = parentOn)
        },
    ) {
        Text(stringResource(row.title))
    }
}

@Composable
private fun ChoiceRow(
    row: LabRow.Choice,
    value: String,
    enabled: Boolean,
    index: Int,
    count: Int,
    onChange: (String) -> Unit,
) {
    val labels = stringArrayResource(row.labels)
    val labelOf = { v: String -> labels.getOrNull(row.values.indexOf(v)) ?: v }
    // Two to four short options read best as connected buttons; longer lists get a menu.
    if (row.values.size <= 4 && labels.all { it.length <= 10 }) {
        SegmentedBlock(index, count) {
            Text(
                stringResource(row.title),
                style = MaterialTheme.typography.bodyLarge,
                color =
                    if (enabled) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            )
            ConnectedChoice(
                options = row.values,
                selected = value,
                onSelect = onChange,
                label = { labelOf(it) },
                enabled = enabled,
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
            )
        }
    } else {
        DropdownItem(
            title = stringResource(row.title),
            options = row.values,
            selected = value,
            onSelect = onChange,
            label = { labelOf(it) },
            index = index,
            count = count,
            icon = row.icon,
            enabled = enabled,
        )
    }
}

@Composable
private fun LinkRow(row: LabRow.Link, index: Int, count: Int) {
    val context = LocalContext.current
    val title = stringResource(row.title)
    SegmentedListItem(
        onClick = { openSettingsPage(context, row.fragment, title) },
        shapes = ListItemDefaults.segmentedShapes(index, count),
        leadingContent = row.icon?.let { { Icon(painterResource(it), contentDescription = null) } },
        supportingContent = row.summary?.let { { Text(stringResource(it)) } },
        trailingContent = {
            Icon(
                painterResource(R.drawable.materialsymbols_ic_chevron_right_rounded_filled),
                contentDescription = null,
            )
        },
    ) {
        Text(title)
    }
}

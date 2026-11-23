/*
 * Copyright (C) 2026 The PenguinOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.penguin.laboratory.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.ToggleButtonShapes
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.toPath
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import com.penguin.laboratory.R

/** Penguin Laboratory's theme: wallpaper colours with the expressive motion scheme. */
@Composable
fun LabTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val colors =
        if (isSystemInDarkTheme()) dynamicDarkColorScheme(context)
        else dynamicLightColorScheme(context)
    MaterialExpressiveTheme(
        colorScheme = colors,
        motionScheme = MotionScheme.expressive(),
        content = content,
    )
}

/** A shape partway through morphing between two Material shapes. */
class MorphShape(private val morph: Morph, private val progress: Float) : Shape {
    private val path = Path()
    private val matrix = Matrix()

    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density):
        Outline {
        morph.toPath(progress, path)
        matrix.reset()
        matrix.scale(size.width, size.height)
        path.transform(matrix)
        return Outline.Generic(path)
    }
}

/**
 * Morphs from [from] to [to] as [selected] turns on, on the spatial spring so the shape settles
 * with a little bounce.
 */
@Composable
fun rememberMorphShape(from: RoundedPolygon, to: RoundedPolygon, selected: Boolean): Shape {
    val morph = remember(from, to) { Morph(from, to) }
    val progress by
        animateFloatAsState(
            targetValue = if (selected) 1f else 0f,
            animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
            label = "morph",
        )
    return remember(morph, progress) { MorphShape(morph, progress) }
}

/** A label above a group, in the accent colour so groups are easy to find. */
@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmallEmphasized,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp),
    )
}

/** Stacks segmented list items with the segmented gap, so they read as one group. */
@Composable
fun SegmentedGroup(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
        content = content,
    )
}

/** A segmented list item with a switch that toggles when the row is tapped. */
@Composable
fun SwitchItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    index: Int,
    count: Int,
    supporting: String? = null,
    icon: Int? = null,
    enabled: Boolean = true,
) {
    SegmentedListItem(
        checked = checked,
        onCheckedChange = onCheckedChange,
        shapes = ListItemDefaults.segmentedShapes(index, count),
        enabled = enabled,
        leadingContent = icon?.let { { Icon(painterResource(it), contentDescription = null) } },
        supportingContent = supporting?.let { { Text(it) } },
        trailingContent = { Switch(checked = checked, onCheckedChange = null, enabled = enabled) },
    ) {
        Text(title)
    }
}

/**
 * A segmented row that isn't itself clickable, for controls such as sliders that handle their own
 * touches. Matches [SegmentedListItem]'s shape and colour.
 */
@Composable
fun SegmentedBlock(
    index: Int,
    count: Int,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    // segmentedShapes().shape is the plain resting shape, which a bare Surface draws square; round
    // the outer corners of the group like the list items around it.
    val outer = MaterialTheme.shapes.large.topStart
    val inner = MaterialTheme.shapes.extraSmall.topStart
    Surface(
        shape =
            RoundedCornerShape(
                topStart = if (index == 0) outer else inner,
                topEnd = if (index == 0) outer else inner,
                bottomStart = if (index == count - 1) outer else inner,
                bottomEnd = if (index == count - 1) outer else inner,
            ),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp), content = content)
    }
}

@Composable
private fun SliderTitle(title: String, value: String, icon: Int?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) {
            Icon(
                painterResource(icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(16.dp))
        }
        Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text(
            value,
            style = MaterialTheme.typography.labelLargeEmphasized,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

/** A stepped slider whose steps run from 0 to labels.size - 1, naming the current step. */
@Composable
fun LevelItem(
    title: String,
    labels: List<String>,
    value: Int,
    onValueChange: (Int) -> Unit,
    index: Int,
    count: Int,
    icon: Int? = null,
) {
    SegmentedBlock(index, count) {
        SliderTitle(title, labels[value.coerceIn(0, labels.lastIndex)], icon)
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(Math.round(it)) },
            valueRange = 0f..labels.lastIndex.toFloat(),
            steps = (labels.size - 2).coerceAtLeast(0),
        )
    }
}

/** A continuous slider from 0 to 1, shown as a percentage. */
@Composable
fun PercentItem(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    index: Int,
    count: Int,
    icon: Int? = null,
    enabled: Boolean = true,
) {
    var current by remember(value) { mutableFloatStateOf(value) }
    SegmentedBlock(index, count) {
        SliderTitle(title, "${Math.round(current * 100)}%", icon)
        Slider(
            value = current,
            onValueChange = {
                current = it
                onValueChange(it)
            },
            enabled = enabled,
        )
    }
}

/**
 * A slider from -1 to 1, for motion amounts where the sign picks the direction. Snaps to 0 near
 * the middle.
 */
@Composable
fun SignedSliderItem(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    index: Int,
    count: Int,
    icon: Int? = null,
) {
    var current by remember(value) { mutableFloatStateOf(value) }
    SegmentedBlock(index, count) {
        SliderTitle(title, "%+d".format(Math.round(current * 100)), icon)
        Slider(
            value = current,
            onValueChange = {
                current = if (kotlin.math.abs(it) < 0.06f) 0f else it
                onValueChange(current)
            },
            valueRange = -1f..1f,
        )
    }
}

private val PillShape = RoundedCornerShape(percent = 50)

/**
 * A single-select connected button group spanning the width it's given. Unselected buttons sit on
 * the highest container tone so they stand out from the surface container screens use.
 */
@Composable
fun <T> ConnectedChoice(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: @Composable (T) -> String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
        options.forEachIndexed { index, option ->
            ToggleButton(
                checked = option == selected,
                onCheckedChange = { onSelect(option) },
                enabled = enabled,
                modifier = Modifier.weight(1f).semantics { role = Role.RadioButton },
                // Every option is a full pill, like the selected one, so the row has one outline.
                shapes = ToggleButtonShapes(PillShape, PillShape, PillShape),
                colors =
                    ToggleButtonDefaults.toggleButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    ),
            ) {
                Text(label(option), maxLines = 1)
            }
        }
    }
}

/**
 * A segmented row that opens a menu of all options, for choices too long for a [ConnectedChoice].
 */
@Composable
fun <T> DropdownItem(
    title: String,
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: @Composable (T) -> String,
    index: Int,
    count: Int,
    enabled: Boolean = true,
    supporting: String? = null,
    icon: Int? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded && enabled, onExpandedChange = { expanded = it }) {
        // The menu anchor opens and closes the menu on tap, so the row's own click does nothing.
        SegmentedListItem(
            onClick = {},
            shapes = ListItemDefaults.segmentedShapes(index, count),
            enabled = enabled,
            modifier =
                Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled),
            leadingContent = icon?.let { { Icon(painterResource(it), contentDescription = null) } },
            supportingContent = { Text(supporting ?: label(selected)) },
            trailingContent = { ExposedDropdownMenuDefaults.TrailingIcon(expanded && enabled) },
        ) {
            Text(title)
        }
        ExposedDropdownMenu(expanded = expanded && enabled, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(label(option)) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                    trailingIcon =
                        if (option == selected) {
                            { Icon(painterResource(R.drawable.ic_check), contentDescription = null) }
                        } else {
                            null
                        },
                )
            }
        }
    }
}

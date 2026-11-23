/*
 * SPDX-FileCopyrightText: 2026 Miku-Picked UI
 * SPDX-License-Identifier: Apache-2.0
 */

package com.penguin.laboratory.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/*
 * Miku-Picked UI: PenguinOS builds against a newer Material 3 (Android 17's prebuilts) with
 * segmented list items. AOSP 16 ships material3 1.4.0-alpha17, which has none of
 * SegmentedListItem, ListItemDefaults.segmentedShapes or ListItemDefaults.SegmentedGap, so these
 * stand-ins draw the same thing: list rows in one rounded group, the outer corners large and the
 * inner ones small, a 2 dp gap between rows.
 */

/** The shape of one row in a segmented group. */
class SegmentedShapes(val shape: Shape)

/** Space between rows of a segmented group. */
val ListItemDefaults.SegmentedGap: Dp
    get() = 2.dp

/** The shape of row [index] of [count] in a segmented group. */
@Composable
fun ListItemDefaults.segmentedShapes(index: Int, count: Int): SegmentedShapes {
    val outer = MaterialTheme.shapes.large.topStart
    val inner = MaterialTheme.shapes.extraSmall.topStart
    return SegmentedShapes(
        RoundedCornerShape(
            topStart = if (index == 0) outer else inner,
            topEnd = if (index == 0) outer else inner,
            bottomStart = if (index == count - 1) outer else inner,
            bottomEnd = if (index == count - 1) outer else inner,
        )
    )
}

/** A clickable row of a segmented group. */
@Composable
fun SegmentedListItem(
    onClick: () -> Unit,
    shapes: SegmentedShapes,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingContent: (@Composable () -> Unit)? = null,
    supportingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    SegmentedRow(
        shapes = shapes,
        enabled = enabled,
        modifier = modifier.clickable(enabled = enabled, onClick = onClick),
        leadingContent = leadingContent,
        supportingContent = supportingContent,
        trailingContent = trailingContent,
        content = content,
    )
}

/** A row of a segmented group that toggles, such as one with a switch. */
@Composable
fun SegmentedListItem(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    shapes: SegmentedShapes,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingContent: (@Composable () -> Unit)? = null,
    supportingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    SegmentedRow(
        shapes = shapes,
        enabled = enabled,
        modifier =
            modifier.toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Switch,
                onValueChange = onCheckedChange,
            ),
        leadingContent = leadingContent,
        supportingContent = supportingContent,
        trailingContent = trailingContent,
        content = content,
    )
}

@Composable
private fun SegmentedRow(
    shapes: SegmentedShapes,
    enabled: Boolean,
    modifier: Modifier,
    leadingContent: (@Composable () -> Unit)?,
    supportingContent: (@Composable () -> Unit)?,
    trailingContent: (@Composable () -> Unit)?,
    content: @Composable () -> Unit,
) {
    Surface(shape = shapes.shape, color = MaterialTheme.colorScheme.surface) {
        ListItem(
            headlineContent = content,
            modifier = modifier.alpha(if (enabled) 1f else 0.38f),
            leadingContent = leadingContent,
            supportingContent = supportingContent,
            trailingContent = trailingContent,
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
    }
}

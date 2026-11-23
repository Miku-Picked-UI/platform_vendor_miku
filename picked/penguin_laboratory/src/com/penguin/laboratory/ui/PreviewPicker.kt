/*
 * Copyright (C) 2026 The PenguinOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.penguin.laboratory.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.penguin.laboratory.R

/**
 * A scrolling row of pictures to pick from, for options that look different enough that their
 * names alone don't say what you'll get. The chosen one is outlined in the primary colour, its
 * corners round further, and its label gets a tick.
 */
@Composable
fun <T> PreviewPicker(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: @Composable (T) -> String,
    preview: @Composable (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    width: Dp = 96.dp,
    height: Dp = 144.dp,
) {
    // Start with the chosen picture in view, one step in so there's a hint of the ones before it.
    val state =
        rememberLazyListState(
            initialFirstVisibleItemIndex = (options.indexOf(selected) - 1).coerceAtLeast(0)
        )
    LazyRow(
        state = state,
        modifier = modifier.alpha(if (enabled) 1f else 0.38f),
        contentPadding = PaddingValues(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        itemsIndexed(options) { _, option ->
            val chosen = option == selected
            val corner by animateDpAsState(if (chosen) 24.dp else 14.dp, label = "corner")
            val stroke by animateDpAsState(if (chosen) 3.dp else 1.dp, label = "stroke")
            val shape = RoundedCornerShape(corner)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier =
                    Modifier.width(width)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(enabled = enabled) { onSelect(option) }
                        .semantics {
                            role = Role.RadioButton
                            this.selected = chosen
                        }
                        .padding(vertical = 4.dp),
            ) {
                Box(
                    Modifier.size(width, height)
                        .border(
                            stroke,
                            if (chosen) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant,
                            shape,
                        )
                        .padding(stroke)
                        .clip(shape)
                ) {
                    Box(Modifier.fillMaxSize()) { preview(option) }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(top = 6.dp, bottom = 4.dp),
                ) {
                    if (chosen) {
                        Icon(
                            painterResource(R.drawable.ic_check),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp).padding(end = 2.dp),
                        )
                    }
                    Text(
                        label(option),
                        style = MaterialTheme.typography.labelLarge,
                        color =
                            if (chosen) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

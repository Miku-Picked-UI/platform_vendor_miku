/*
 * Copyright (C) 2026 The PenguinOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.penguin.laboratory.styles

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.penguin.laboratory.R
import com.penguin.laboratory.ui.LabTheme
import com.penguin.laboratory.ui.rememberMorphShape

private val Stage = Color.Black

private const val TILE_MAX_PX = 200
private const val TILE_MAX_FRAMES = 18
private const val PREVIEW_MAX_PX = 480
private const val PREVIEW_MAX_FRAMES = 60

// Roughly as SystemUI draws the icon on the sensor.
private const val ICON_IN_ANIMATION = 0.4f

class UdfpsPickerActivity : ComponentActivity() {

    companion object {
        private const val EXTRA_PACK = "pack"

        fun intent(context: Context, pack: UdfpsPack): Intent =
            Intent(context, UdfpsPickerActivity::class.java).putExtra(EXTRA_PACK, pack.name)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val pack =
            intent.getStringExtra(EXTRA_PACK)?.let { name ->
                UdfpsPack.values().firstOrNull { it.name == name }
            }
        val icons = UdfpsPack.ICON.load(this)
        val animations = UdfpsPack.ANIMATION.load(this)
        val entries = if (pack == UdfpsPack.ICON) icons else animations
        if (pack == null || entries.isEmpty()) {
            finish()
            return
        }
        setContent { LabTheme { Picker(pack, entries, icons, animations) } }
    }

    @Composable
    private fun Picker(
        pack: UdfpsPack,
        entries: List<UdfpsPack.Entry>,
        icons: List<UdfpsPack.Entry>,
        animations: List<UdfpsPack.Entry>,
    ) {
        val context = this
        var selected by remember { mutableIntStateOf(pack.get(context).coerceIn(0, entries.lastIndex)) }
        var playing by remember { mutableStateOf(true) }
        LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { playing = true }
        LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) { playing = false }

        val savedIcon = remember { UdfpsPack.ICON.get(context) }
        val savedAnimation = remember { UdfpsPack.ANIMATION.get(context) }
        val iconIndex = if (pack == UdfpsPack.ICON) selected else savedIcon
        val animationIndex = if (pack == UdfpsPack.ANIMATION) selected else savedAnimation

        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            topBar = {
                MediumFlexibleTopAppBar(
                    title = {
                        Text(
                            stringResource(
                                if (pack == UdfpsPack.ICON) R.string.styles_udfps_icon
                                else R.string.styles_udfps_animation
                            )
                        )
                    },
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
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 104.dp),
                contentPadding =
                    PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = padding.calculateTopPadding() + 8.dp,
                        bottom = padding.calculateBottomPadding() + 24.dp,
                    ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }, contentType = "preview") {
                    Preview(
                        animation = animations.getOrNull(animationIndex)?.takeIf { animationIndex != 0 },
                        icon = icons.getOrNull(iconIndex),
                        playing = playing,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                }
                itemsIndexed(entries, key = { i, _ -> i }, contentType = { _, _ -> "tile" }) {
                    index,
                    entry ->
                    Tile(
                        selected = index == selected,
                        description = entry.description,
                        onClick = {
                            pack.put(context, index)
                            selected = index
                        },
                    ) { sizePx ->
                        when {
                            pack == UdfpsPack.ANIMATION && index == 0 ->
                                Icon(
                                    painterResource(R.drawable.materialsymbols_ic_block_rounded_filled),
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.size(40.dp),
                                )
                            pack == UdfpsPack.ANIMATION ->
                                PackImage(
                                    pack,
                                    entry.drawable,
                                    sizePx,
                                    TILE_MAX_FRAMES,
                                    playing,
                                    Modifier.fillMaxSize(),
                                )
                            else ->
                                PackImage(
                                    pack,
                                    entry.drawable,
                                    sizePx,
                                    TILE_MAX_FRAMES,
                                    playing,
                                    Modifier.fillMaxSize(0.62f),
                                )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun Preview(
        animation: UdfpsPack.Entry?,
        icon: UdfpsPack.Entry?,
        playing: Boolean,
        modifier: Modifier = Modifier,
    ) {
        BoxWithConstraints(
            modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(Stage),
            contentAlignment = Alignment.Center,
        ) {
            val side: Dp = minOf(maxWidth, maxHeight) - 24.dp
            val px = with(LocalDensity.current) { side.roundToPx() }.coerceIn(1, PREVIEW_MAX_PX)
            if (animation != null) {
                PackImage(
                    UdfpsPack.ANIMATION,
                    animation.drawable,
                    px,
                    PREVIEW_MAX_FRAMES,
                    playing,
                    Modifier.size(side),
                )
            }
            if (icon != null) {
                PackImage(
                    UdfpsPack.ICON,
                    icon.drawable,
                    (px * ICON_IN_ANIMATION).toInt().coerceAtLeast(1),
                    PREVIEW_MAX_FRAMES,
                    playing,
                    Modifier.size(side * ICON_IN_ANIMATION),
                )
            }
        }
    }

    @Composable
    private fun Tile(
        selected: Boolean,
        description: String,
        onClick: () -> Unit,
        content: @Composable (sizePx: Int) -> Unit,
    ) {
        val shape = rememberMorphShape(MaterialShapes.Square, MaterialShapes.Cookie9Sided, selected)
        BoxWithConstraints(
            Modifier.fillMaxWidth().aspectRatio(1f),
            contentAlignment = Alignment.Center,
        ) {
            val px = with(LocalDensity.current) { maxWidth.roundToPx() }.coerceIn(1, TILE_MAX_PX)
            Box(
                Modifier.fillMaxSize()
                    .clip(shape)
                    .background(Stage)
                    .then(
                        if (selected) Modifier.border(3.dp, MaterialTheme.colorScheme.primary, shape)
                        else Modifier
                    )
                    .clickable(role = Role.RadioButton, onClick = onClick)
                    .semantics {
                        contentDescription = description
                        this.selected = selected
                    },
                contentAlignment = Alignment.Center,
            ) {
                content(px)
            }
            if (selected) {
                Box(
                    Modifier.align(Alignment.BottomEnd)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painterResource(R.drawable.ic_check),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

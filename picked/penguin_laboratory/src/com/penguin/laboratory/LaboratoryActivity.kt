/*
 * Copyright (C) 2026 The PenguinOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.penguin.laboratory

import android.content.Intent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import com.penguin.laboratory.tabs.LabTabPage
import com.penguin.laboratory.tabs.LabTabs
import com.penguin.laboratory.ui.FloatingTabBar
import com.penguin.laboratory.ui.TabBarClearance
import com.penguin.laboratory.ui.TabItem
import com.penguin.laboratory.ui.backdropSource
import com.penguin.laboratory.ui.rememberBackdrop
import kotlinx.coroutines.launch
import androidx.compose.animation.core.spring
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.graphics.shapes.RoundedPolygon
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.penguin.laboratory.cos.CosExperienceActivity
import com.penguin.laboratory.cos.CosExperienceStore
import com.penguin.laboratory.dex.DexActivity
import com.penguin.laboratory.dex.DexStore
import com.penguin.laboratory.edgelighting.EdgeLightingActivity
import com.penguin.laboratory.edgelighting.EdgeLightingStore
import com.penguin.laboratory.experiments.ExperimentsActivity
import com.penguin.laboratory.experiments.ExperimentsStore
import com.penguin.laboratory.glass.LiquidGlassActivity
import com.penguin.laboratory.glass.LiquidGlassStore
import com.penguin.laboratory.styles.StylesActivity
import com.penguin.laboratory.styles.StylesStore
import com.penguin.laboratory.ui.LabTheme
import com.penguin.laboratory.ui.rememberMorphShape
import com.penguin.laboratory.wonderland.WonderlandActivity
import com.penguin.laboratory.wonderland.WonderlandStore

/** Lists the experiments in Penguin Laboratory. */
class LaboratoryActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { LabTheme { Laboratory() } }
    }

    @Composable
    private fun Laboratory() {
        // Re-read the experiments' state whenever we come back from one
        var generation by remember { mutableIntStateOf(0) }
        LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { generation++ }
        val edgeOn = remember(generation) { EdgeLightingStore.isEnabled(this) }
        val wallpapers = remember(generation) { WonderlandStore.projects(this).size }
        val dexOn = remember(generation) { DexStore.isEnabled(this) }
        val glassOn = remember(generation) { LiquidGlassStore.isEnabled(this) }
        val cosParts = remember(generation) { CosExperienceStore.onCount(this) }
        val stylesChanged = remember(generation) { StylesStore.customised(this) }
        val experimentsOn = remember(generation) { ExperimentsStore.enabledCount(this) }

        val tabs = LabTabs.all
        val items =
            listOf(TabItem(R.string.tab_originals, R.drawable.materialsymbols_ic_science_rounded_filled)) +
                tabs.map { TabItem(it.label, it.icon) }
        val pager = rememberPagerState { items.size }
        val scope = rememberCoroutineScope()
        val backdrop = rememberBackdrop()
        val listStates = remember { List(items.size) { LazyListState() } }
        // Reading down a tab shrinks the bar to the current tab; scrolling back up brings it back.
        var collapsed by remember { mutableStateOf(false) }
        val hideOnScroll = remember {
            object : NestedScrollConnection {
                override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                    if (consumed.y < -12f) collapsed = true else if (consumed.y > 12f) collapsed = false
                    return Offset.Zero
                }
            }
        }
        LaunchedEffect(pager.currentPage) { collapsed = false }
        val page = pager.currentPage
        // The tab picked from the bar while the pager is still on its way there.
        var jumpTo by remember { mutableIntStateOf(-1) }

        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        Scaffold(
            modifier =
                Modifier.nestedScroll(scrollBehavior.nestedScrollConnection).nestedScroll(hideOnScroll),
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            topBar = {
                LargeFlexibleTopAppBar(
                    title = {
                        Text(
                            if (page == 0) stringResource(R.string.laboratory_title)
                            else stringResource(tabs[page - 1].title)
                        )
                    },
                    subtitle = { if (page == 0) Text(stringResource(R.string.laboratory_summary)) },
                    colors =
                        TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                        ),
                    scrollBehavior = scrollBehavior,
                )
            },
        ) { padding ->
            val listPadding =
                PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = padding.calculateTopPadding() + 8.dp,
                    bottom = padding.calculateBottomPadding() + TabBarClearance,
                )
            Box(Modifier.fillMaxSize()) {
                HorizontalPager(
                    state = pager,
                    modifier = Modifier.fillMaxSize().backdropSource(backdrop),
                    beyondViewportPageCount = 1,
                ) { index ->
                    if (index == 0) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            state = listStates[0],
                            contentPadding = listPadding,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                        item {
                            Experiment(
                                title = stringResource(R.string.edge_lighting_title),
                                summary = stringResource(R.string.edge_lighting_entry_summary),
                                status =
                                    stringResource(
                                        if (edgeOn) R.string.laboratory_status_on
                                        else R.string.laboratory_status_off
                                    ),
                                active = edgeOn,
                                icon = R.drawable.materialsymbols_ic_bolt_rounded_filled,
                                restShape = MaterialShapes.Cookie9Sided,
                                pressedShape = MaterialShapes.Sunny,
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ) {
                                startActivity(Intent(this@LaboratoryActivity, EdgeLightingActivity::class.java))
                            }
                        }
                        item {
                            Experiment(
                                title = stringResource(R.string.wonderland_title),
                                summary = stringResource(R.string.wonderland_entry_summary),
                                status =
                                    resources.getQuantityString(
                                        R.plurals.laboratory_wallpaper_count,
                                        wallpapers,
                                        wallpapers,
                                    ),
                                active = wallpapers > 0,
                                icon = R.drawable.materialsymbols_ic_wallpaper_rounded_filled,
                                restShape = MaterialShapes.Flower,
                                pressedShape = MaterialShapes.SoftBurst,
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            ) {
                                startActivity(Intent(this@LaboratoryActivity, WonderlandActivity::class.java))
                            }
                        }
                        item {
                            Experiment(
                                title = stringResource(R.string.dex_title),
                                summary = stringResource(R.string.dex_entry_summary),
                                status =
                                    stringResource(
                                        if (dexOn) R.string.laboratory_status_on
                                        else R.string.laboratory_status_off
                                    ),
                                active = dexOn,
                                icon = R.drawable.materialsymbols_ic_desktop_windows_rounded_filled,
                                restShape = MaterialShapes.Slanted,
                                pressedShape = MaterialShapes.Pill,
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            ) {
                                startActivity(Intent(this@LaboratoryActivity, DexActivity::class.java))
                            }
                        }
                        item {
                            Experiment(
                                title = stringResource(R.string.liquid_glass_title),
                                summary = stringResource(R.string.liquid_glass_entry_summary),
                                status =
                                    stringResource(
                                        if (glassOn) R.string.laboratory_status_on
                                        else R.string.laboratory_status_off
                                    ),
                                active = glassOn,
                                icon = R.drawable.materialsymbols_ic_water_drop_rounded_filled,
                                restShape = MaterialShapes.Gem,
                                pressedShape = MaterialShapes.Circle,
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ) {
                                startActivity(Intent(this@LaboratoryActivity, LiquidGlassActivity::class.java))
                            }
                        }
                        item {
                            Experiment(
                                title = stringResource(R.string.cos_title),
                                summary = stringResource(R.string.cos_entry_summary),
                                status =
                                    resources.getQuantityString(R.plurals.cos_status, cosParts, cosParts),
                                active = cosParts > 0,
                                icon = R.drawable.materialsymbols_ic_motion_photos_on_rounded_filled,
                                restShape = MaterialShapes.Cookie6Sided,
                                pressedShape = MaterialShapes.Oval,
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            ) {
                                startActivity(Intent(this@LaboratoryActivity, CosExperienceActivity::class.java))
                            }
                        }
                        item {
                            Experiment(
                                title = stringResource(R.string.styles_title),
                                summary = stringResource(R.string.styles_entry_summary),
                                status =
                                    resources.getQuantityString(
                                        R.plurals.styles_status,
                                        stylesChanged,
                                        stylesChanged,
                                    ),
                                active = stylesChanged > 0,
                                icon = R.drawable.materialsymbols_ic_palette_rounded_filled,
                                restShape = MaterialShapes.Clover4Leaf,
                                pressedShape = MaterialShapes.Square,
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            ) {
                                startActivity(Intent(this@LaboratoryActivity, StylesActivity::class.java))
                            }
                        }
                        // Miku-Picked UI: hidden until an experiment has a backend here
                        if (ExperimentsStore.all.isNotEmpty()) item {
                            Experiment(
                                title = stringResource(R.string.experiments_title),
                                summary = stringResource(R.string.experiments_entry_summary),
                                status =
                                    resources.getQuantityString(
                                        R.plurals.experiments_status,
                                        experimentsOn,
                                        experimentsOn,
                                    ),
                                active = experimentsOn > 0,
                                icon = R.drawable.materialsymbols_ic_science_rounded_filled,
                                restShape = MaterialShapes.Puffy,
                                pressedShape = MaterialShapes.Cookie4Sided,
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            ) {
                                startActivity(Intent(this@LaboratoryActivity, ExperimentsActivity::class.java))
                            }
                        }
                        }
                    } else {
                        LabTabPage(tabs[index - 1], listPadding, listStates[index], generation)
                    }
                }
                FloatingTabBar(
                    items = items,
                    selected = if (jumpTo >= 0) jumpTo else pager.targetPage,
                    onSelect = { target ->
                        collapsed = false
                        jumpTo = target
                        scope.launch {
                            try {
                                // Land next to a far tab first so only one page slides in.
                                val from = pager.currentPage
                                if (target - from > 1) pager.scrollToPage(target - 1)
                                else if (from - target > 1) pager.scrollToPage(target + 1)
                                pager.animateScrollToPage(
                                    target,
                                    animationSpec = spring(dampingRatio = 1f, stiffness = 300f),
                                )
                            } finally {
                                if (jumpTo == target) jumpTo = -1
                            }
                        }
                    },
                    glass = glassOn,
                    collapsed = collapsed,
                    onExpand = { collapsed = false },
                    backdrop = backdrop,
                    // The user's own swipe (and its settle), not paging started from the bar.
                    pagePosition =
                        if (pager.isScrollInProgress && jumpTo < 0) {
                            { pager.currentPage + pager.currentPageOffsetFraction }
                        } else null,
                    modifier =
                        Modifier.align(Alignment.BottomCenter)
                            .padding(bottom = padding.calculateBottomPadding() + 12.dp),
                )
            }
        }
    }

    /**
     * An experiment's entry. Its emblem morphs while pressed, the one playful touch on an
     * otherwise plain list.
     */
    @Composable
    private fun Experiment(
        title: String,
        summary: String,
        status: String,
        active: Boolean,
        icon: Int,
        restShape: RoundedPolygon,
        pressedShape: RoundedPolygon,
        containerColor: Color,
        contentColor: Color,
        onClick: () -> Unit,
    ) {
        val interaction = remember { MutableInteractionSource() }
        val pressed by interaction.collectIsPressedAsState()
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            interactionSource = interaction,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = rememberMorphShape(restShape, pressedShape, pressed),
                    color = containerColor,
                    contentColor = contentColor,
                    modifier = Modifier.size(88.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painterResource(icon),
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                        )
                    }
                }
                Spacer(Modifier.width(20.dp))
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleLargeEmphasized)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(50),
                        color =
                            if (active) MaterialTheme.colorScheme.secondaryContainer
                            else MaterialTheme.colorScheme.surfaceContainerHighest,
                        contentColor =
                            if (active) MaterialTheme.colorScheme.onSecondaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                    ) {
                        Text(
                            status,
                            style = MaterialTheme.typography.labelMediumEmphasized,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        )
                    }
                }
            }
        }
    }
}

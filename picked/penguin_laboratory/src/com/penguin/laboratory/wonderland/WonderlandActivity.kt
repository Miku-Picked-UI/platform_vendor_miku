/*
 * Copyright (C) 2026 The PenguinOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.penguin.laboratory.wonderland

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.penguin.laboratory.R
import com.penguin.laboratory.ui.LabTheme
import java.io.IOException

/** Lists the Wonderland wallpapers made so far. */
class WonderlandActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { LabTheme { Projects() } }
    }

    private fun open(project: String) {
        startActivity(
            Intent(this, WonderlandEditorActivity::class.java)
                .putExtra(WonderlandEditorActivity.EXTRA_PROJECT, project)
        )
    }

    private fun create() {
        try {
            open(WonderlandStore.create(this))
        } catch (e: IOException) {
            Log.w(TAG, "Couldn't create a project", e)
        }
    }

    /** Whether the home or lock screen shows Wonderland. */
    private fun inUse(): Boolean {
        val component = ComponentName(this, WonderlandWallpaperService::class.java)
        val wm = WallpaperManager.getInstance(this)
        return listOf(WallpaperManager.FLAG_SYSTEM, WallpaperManager.FLAG_LOCK).any {
            wm.getWallpaperInfo(it)?.component == component
        }
    }

    @Composable
    private fun Projects() {
        var generation by remember { mutableIntStateOf(0) }
        LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { generation++ }
        DisposableEffect(Unit) {
            val listener = WonderlandStore.Listener { generation++ }
            WonderlandStore.addListener(listener)
            onDispose { WonderlandStore.removeListener(listener) }
        }
        val projects = remember(generation) { WonderlandStore.projects(this) }
        val active = remember(generation) { if (inUse()) WonderlandStore.active(this) else null }
        var deleting by remember { mutableStateOf<String?>(null) }

        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        val grid = rememberLazyGridState()
        // The FAB shortens to its icon once the list scrolls, to stay out of the way
        val fabExpanded by remember { derivedStateOf { grid.firstVisibleItemIndex == 0 } }
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            topBar = {
                MediumFlexibleTopAppBar(
                    title = { Text(stringResource(R.string.wonderland_title)) },
                    subtitle = { Text(stringResource(R.string.wonderland_entry_summary)) },
                    navigationIcon = {
                        IconButton(onClick = { finish() }) {
                            Icon(
                                painterResource(R.drawable.ic_back),
                                contentDescription = null,
                            )
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
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = ::create,
                    expanded = fabExpanded,
                    icon = {
                        Icon(painterResource(R.drawable.materialsymbols_ic_add_rounded_filled), null)
                    },
                    text = { Text(stringResource(R.string.wonderland_create)) },
                )
            },
        ) { padding ->
            if (projects.isEmpty()) {
                Empty(Modifier.padding(padding))
                return@Scaffold
            }
            LazyVerticalGrid(
                state = grid,
                columns = GridCells.Adaptive(minSize = 160.dp),
                contentPadding =
                    PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = padding.calculateTopPadding() + 8.dp,
                        // Clear the FAB
                        bottom = padding.calculateBottomPadding() + 96.dp,
                    ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        stringResource(R.string.wonderland_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 8.dp),
                    )
                }
                items(projects, key = { it }) { project ->
                    ProjectCard(
                        project = project,
                        inUse = project == active,
                        generation = generation,
                        onClick = { open(project) },
                        onLongClick = { deleting = project },
                    )
                }
            }
        }

        deleting?.let { project ->
            AlertDialog(
                onDismissRequest = { deleting = null },
                icon = { Icon(painterResource(R.drawable.materialsymbols_ic_delete_rounded_filled), null) },
                title = { Text(stringResource(R.string.wonderland_delete_project)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            WonderlandStore.delete(this, project)
                            deleting = null
                        }
                    ) {
                        Text(stringResource(R.string.wonderland_delete))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deleting = null }) {
                        Text(stringResource(android.R.string.cancel))
                    }
                },
            )
        }
    }

    @Composable
    private fun ProjectCard(
        project: String,
        inUse: Boolean,
        generation: Int,
        onClick: () -> Unit,
        onLongClick: () -> Unit,
    ) {
        val thumbnail =
            remember(project, generation) {
                val file = WonderlandStore.file(this, project, WonderlandStore.THUMBNAIL)
                if (file.exists()) BitmapFactory.decodeFile(file.path) else null
            }
        Box(
            Modifier.fillMaxWidth()
                .aspectRatio(9f / 19.5f)
                .clip(RoundedCornerShape(28.dp))
                .combinedClickable(onClick = onClick, onLongClick = onLongClick)
        ) {
            Surface(color = MaterialTheme.colorScheme.surfaceContainerHighest, modifier = Modifier.fillMaxSize()) {
                thumbnail?.let {
                    Image(
                        it.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
            if (inUse) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(12.dp).align(Alignment.TopStart),
                ) {
                    Text(
                        stringResource(R.string.wonderland_in_use),
                        style = MaterialTheme.typography.labelMediumEmphasized,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }

    @Composable
    private fun Empty(modifier: Modifier) {
        Column(
            modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Surface(
                shape = MaterialShapes.Flower.toShape(),
                color = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(160.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painterResource(R.drawable.materialsymbols_ic_wallpaper_rounded_filled),
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
            Text(
                stringResource(R.string.wonderland_empty),
                style = MaterialTheme.typography.headlineSmallEmphasized,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.wonderland_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }

    companion object {
        private const val TAG = "Wonderland"
    }
}

/*
 * Copyright (C) 2026 The PenguinOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.penguin.laboratory.edgelighting

import android.app.BroadcastOptions
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import com.penguin.laboratory.ui.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.penguin.laboratory.R
import com.penguin.laboratory.ui.ConnectedChoice
import com.penguin.laboratory.ui.LabTheme
import com.penguin.laboratory.ui.LevelItem
import com.penguin.laboratory.ui.SectionHeader
import com.penguin.laboratory.ui.SegmentedGroup
import com.penguin.laboratory.ui.SwitchItem
import com.penguin.laboratory.ui.rememberMorphShape
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.penguin.laboratory.ui.segmentedShapes

/** Edits the edge lighting style: image, colour, motion and when it plays. */
class EdgeLightingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { LabTheme { EdgeLighting() } }
    }

    private fun preview() {
        sendBroadcast(
            Intent(EdgeLightingStore.ACTION_PREVIEW).setPackage(SYSTEMUI),
            null,
            BroadcastOptions.makeBasic().setShareIdentityEnabled(true).toBundle(),
        )
    }

    private fun loadBitmap(id: String?): Bitmap? {
        if (id == null) return null
        val file = EdgeLightingStore.image(this, id)
        return if (file.exists()) BitmapFactory.decodeFile(file.path) else null
    }

    @Composable
    private fun EdgeLighting() {
        val scope = rememberCoroutineScope()
        // Bumped after every store change that the screen shows
        var generation by remember { mutableIntStateOf(0) }
        fun changed() = generation++

        var enabled by remember { mutableStateOf(EdgeLightingStore.isEnabled(this)) }
        var busy by remember { mutableStateOf<Int?>(null) }
        var error by remember { mutableStateOf<Int?>(null) }
        var addingKeyword by remember { mutableStateOf(false) }
        var deleting by remember { mutableStateOf<String?>(null) }

        val selected = remember(generation) { EdgeLightingStore.selected(this) }
        val preview = remember(generation) { loadBitmap(selected) }
        val cutouts = remember(generation) { EdgeLightingStore.cutouts(this) }
        val keywords = remember(generation) { EdgeLightingStore.keywords(this) }
        val color = remember(generation) { EdgeLightingStore.color(this) }

        fun io(action: () -> Unit) {
            scope.launch {
                withContext(Dispatchers.IO) {
                    try {
                        action()
                    } catch (e: Exception) {
                        Log.w(TAG, "Couldn't update the image", e)
                    }
                }
                changed()
            }
        }

        val picker =
            rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                if (uri != null) {
                    busy = R.string.edge_lighting_cutting_out
                    error = null
                    scope.launch {
                        val ok = cutOut(uri) { busy = R.string.edge_lighting_downloading }
                        busy = null
                        if (!ok) error = R.string.edge_lighting_cutout_failed
                        changed()
                    }
                }
            }

        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            topBar = {
                MediumFlexibleTopAppBar(
                    title = { Text(stringResource(R.string.edge_lighting_title)) },
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
        ) { padding ->
            LazyColumn(
                contentPadding =
                    PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = padding.calculateTopPadding(),
                        bottom = padding.calculateBottomPadding() + 32.dp,
                    )
            ) {
                item {
                    MainSwitch(enabled) {
                        enabled = it
                        EdgeLightingStore.put(this@EdgeLightingActivity, EdgeLightingStore.KEY_ENABLED, it)
                    }
                }
                item {
                    Stage(
                        bitmap = preview,
                        busy = busy,
                        message =
                            when {
                                error != null -> error!!
                                selected == null -> R.string.edge_lighting_no_image
                                enabled -> R.string.edge_lighting_ready
                                else -> R.string.edge_lighting_off
                            },
                        canPlay = busy == null && selected != null,
                        onPlay = ::preview,
                    )
                }

                item { SectionHeader(stringResource(R.string.edge_lighting_image)) }
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp),
                    ) {
                        item {
                            FilledTonalIconButton(
                                onClick = {
                                    picker.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                },
                                enabled = busy == null,
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.size(TILE),
                            ) {
                                Icon(
                                    painterResource(R.drawable.materialsymbols_ic_add_photo_alternate_rounded_filled),
                                    contentDescription = stringResource(R.string.edge_lighting_add_photo),
                                )
                            }
                        }
                        items(cutouts, key = { it }) { id ->
                            val bitmap = remember(id, generation) { loadBitmap(id) }
                            ImageTile(
                                selected = id == selected,
                                onClick = {
                                    EdgeLightingStore.put(this@EdgeLightingActivity, EdgeLightingStore.KEY_SELECTED, id)
                                    changed()
                                },
                                onLongClick = { deleting = id },
                            ) {
                                bitmap?.let {
                                    Image(
                                        it.asImageBitmap(),
                                        contentDescription = null,
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.fillMaxSize().padding(8.dp),
                                    )
                                }
                            }
                        }
                        items(Stickers.NAMES.size) { index ->
                            val id = Stickers.id(Stickers.NAMES[index])
                            ImageTile(
                                selected = id == selected,
                                onClick = {
                                    io { Stickers.select(this@EdgeLightingActivity, index, EdgeLightingStore.color(this@EdgeLightingActivity)) }
                                },
                            ) {
                                Icon(
                                    painterResource(Stickers.DRAWABLES[index]),
                                    contentDescription = null,
                                    tint = Color(color),
                                    modifier = Modifier.fillMaxSize().padding(16.dp),
                                )
                            }
                        }
                    }
                }

                item { SectionHeader(stringResource(R.string.edge_lighting_color)) }
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp),
                    ) {
                        items(COLORS) { c ->
                            Swatch(Color(c), selected = c == color) {
                                EdgeLightingStore.put(this@EdgeLightingActivity, EdgeLightingStore.KEY_COLOR, c)
                                io { Stickers.recolor(this@EdgeLightingActivity, c) }
                            }
                        }
                    }
                }

                item { SectionHeader(stringResource(R.string.edge_lighting_effect)) }
                item {
                    var effect by remember { mutableStateOf(EdgeLightingStore.effect(this@EdgeLightingActivity)) }
                    ConnectedChoice(
                        options = EFFECTS,
                        selected = effect,
                        onSelect = {
                            effect = it
                            EdgeLightingStore.put(this@EdgeLightingActivity, EdgeLightingStore.KEY_EFFECT, it)
                        },
                        label = { stringResource(effectLabel(it)) },
                    )
                }

                item { SectionHeader(stringResource(R.string.edge_lighting_motion)) }
                item {
                    SegmentedGroup {
                        Level(R.string.edge_lighting_size, EdgeLightingStore.KEY_SIZE, 2, SIZE_LABELS, 0, R.drawable.materialsymbols_ic_open_in_full_rounded_filled)
                        Level(R.string.edge_lighting_amount, EdgeLightingStore.KEY_AMOUNT, 1, AMOUNT_LABELS, 1, R.drawable.materialsymbols_ic_grain_rounded_filled)
                        Level(R.string.edge_lighting_duration, EdgeLightingStore.KEY_DURATION, 1, DURATION_LABELS, 2, R.drawable.materialsymbols_ic_timer_rounded_filled)
                        Level(R.string.edge_lighting_rotation, EdgeLightingStore.KEY_ROTATION, 1, ROTATION_LABELS, 3, R.drawable.materialsymbols_ic_rotate_right_rounded_filled)
                    }
                }

                item { SectionHeader(stringResource(R.string.edge_lighting_when)) }
                item {
                    SegmentedGroup {
                        Flag(R.string.edge_lighting_screen_off_only, EdgeLightingStore.KEY_SCREEN_OFF_ONLY, true, 0, 2, R.drawable.materialsymbols_ic_mobile_off_rounded_filled)
                        Flag(R.string.edge_lighting_app_icon, EdgeLightingStore.KEY_APP_ICON, false, 1, 2, R.drawable.materialsymbols_ic_apps_rounded_filled)
                    }
                }

                item { SectionHeader(stringResource(R.string.edge_lighting_keywords)) }
                item {
                    Text(
                        stringResource(R.string.edge_lighting_keywords_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                    )
                }
                item {
                    SegmentedGroup {
                        val count = keywords.size + 1
                        keywords.forEachIndexed { index, k ->
                            val thumb = remember(k.imageId, generation) { loadBitmap(k.imageId) }
                            SegmentedListItem(
                                onClick = {},
                                shapes = ListItemDefaults.segmentedShapes(index, count),
                                leadingContent = {
                                    Box(
                                        Modifier.size(40.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        thumb?.let {
                                            Image(it.asImageBitmap(), null, Modifier.padding(4.dp))
                                        }
                                    }
                                },
                                trailingContent = {
                                    IconButton(
                                        onClick = {
                                            val list = EdgeLightingStore.keywords(this@EdgeLightingActivity)
                                            list.removeIf { it.word == k.word }
                                            EdgeLightingStore.setKeywords(this@EdgeLightingActivity, list)
                                            changed()
                                        }
                                    ) {
                                        Icon(
                                            painterResource(R.drawable.materialsymbols_ic_delete_rounded_filled),
                                            contentDescription = stringResource(R.string.edge_lighting_remove),
                                        )
                                    }
                                },
                            ) {
                                Text(k.word)
                            }
                        }
                        SegmentedListItem(
                            onClick = {
                                if (selected == null) error = R.string.edge_lighting_keyword_needs_image
                                else addingKeyword = true
                            },
                            shapes = ListItemDefaults.segmentedShapes(count - 1, count),
                            leadingContent = {
                                Icon(painterResource(R.drawable.materialsymbols_ic_add_rounded_filled), null)
                            },
                        ) {
                            Text(stringResource(R.string.edge_lighting_add_keyword))
                        }
                    }
                }
            }
        }

        if (addingKeyword) {
            KeywordDialog(
                onDismiss = { addingKeyword = false },
                onAdd = { word ->
                    addingKeyword = false
                    val image = EdgeLightingStore.selected(this) ?: return@KeywordDialog
                    val list = EdgeLightingStore.keywords(this)
                    list.removeIf { it.word.equals(word, ignoreCase = true) }
                    list.add(EdgeLightingStore.Keyword(word, image))
                    EdgeLightingStore.setKeywords(this, list)
                    changed()
                },
            )
        }

        deleting?.let { id ->
            AlertDialog(
                onDismissRequest = { deleting = null },
                text = { Text(stringResource(R.string.edge_lighting_delete_image)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            EdgeLightingStore.delete(this, id)
                            deleting = null
                            changed()
                        }
                    ) {
                        Text(stringResource(R.string.edge_lighting_delete))
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

    /** Cuts the subject out of [uri] and selects it. Returns whether it worked. */
    private suspend fun cutOut(uri: Uri, onDownloading: () -> Unit): Boolean =
        withContext(Dispatchers.IO) {
            try {
                Cutouts.create(this@EdgeLightingActivity, uri) { runOnUiThread { onDownloading() } }
                true
            } catch (e: Exception) {
                Log.w(TAG, "Couldn't cut out $uri", e)
                false
            }
        }

    @Composable
    private fun Level(title: Int, key: String, def: Int, labels: List<Int>, index: Int, icon: Int) {
        var value by remember { mutableIntStateOf(EdgeLightingStore.level(this, key, def)) }
        LevelItem(
            title = stringResource(title),
            labels = labels.map { stringResource(it) },
            value = value,
            onValueChange = {
                if (it != value) {
                    value = it
                    EdgeLightingStore.put(this, key, it)
                }
            },
            index = index,
            count = 4,
            icon = icon,
        )
    }

    @Composable
    private fun Flag(title: Int, key: String, def: Boolean, index: Int, count: Int, icon: Int) {
        var checked by remember { mutableStateOf(EdgeLightingStore.flag(this, key, def)) }
        SwitchItem(
            title = stringResource(title),
            checked = checked,
            onCheckedChange = {
                checked = it
                EdgeLightingStore.put(this, key, it)
            },
            index = index,
            count = count,
            icon = icon,
        )
    }

    companion object {
        private const val TAG = "EdgeLighting"
        private const val SYSTEMUI = "com.android.systemui"
        private val TILE = 76.dp

        private val COLORS =
            listOf(
                0xFFFF5C8A.toInt(), 0xFFFF8A3D.toInt(), 0xFFFFD43B.toInt(), 0xFF69DB7C.toInt(),
                0xFF38D9A9.toInt(), 0xFF4DABF7.toInt(), 0xFF9775FA.toInt(), 0xFFFFFFFF.toInt(),
            )
        private val EFFECTS =
            listOf(
                EdgeLightingStore.EFFECT_RAIN, EdgeLightingStore.EFFECT_SNOW,
                EdgeLightingStore.EFFECT_BUBBLES, EdgeLightingStore.EFFECT_BURST,
            )
        private val SIZE_LABELS =
            listOf(R.string.level_small, R.string.level_medium, R.string.level_large, R.string.level_huge)
        private val AMOUNT_LABELS = listOf(R.string.level_few, R.string.level_some, R.string.level_lots)
        private val DURATION_LABELS =
            listOf(R.string.level_3_seconds, R.string.level_5_seconds, R.string.level_8_seconds)
        private val ROTATION_LABELS = listOf(R.string.level_none, R.string.level_gentle, R.string.level_spin)

        private fun effectLabel(effect: String) =
            when (effect) {
                EdgeLightingStore.EFFECT_SNOW -> R.string.edge_lighting_effect_snow
                EdgeLightingStore.EFFECT_BUBBLES -> R.string.edge_lighting_effect_bubbles
                EdgeLightingStore.EFFECT_BURST -> R.string.edge_lighting_effect_burst
                else -> R.string.edge_lighting_effect_rain
            }
    }
}

/**
 * The on/off switch for the whole feature, sized up and tinted when on so its state reads from
 * across the room.
 */
@Composable
private fun MainSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val container by
        animateColorAsState(
            if (checked) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceContainerHighest,
            MaterialTheme.motionScheme.defaultEffectsSpec(),
            label = "mainSwitch",
        )
    Surface(
        checked = checked,
        onCheckedChange = onCheckedChange,
        shape = RoundedCornerShape(percent = 50),
        color = container,
        contentColor =
            if (checked) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
    ) {
        Row(
            Modifier.padding(horizontal = 28.dp, vertical = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.edge_lighting_switch),
                style = MaterialTheme.typography.titleLargeEmphasized,
                modifier = Modifier.weight(1f),
            )
            Switch(checked = checked, onCheckedChange = null)
        }
    }
}

/**
 * The preview stage: the selected image on black, as it will play over the dozing display, with
 * the screen's one prominent action.
 */
@Composable
private fun Stage(bitmap: Bitmap?, busy: Int?, message: Int, canPlay: Boolean, onPlay: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier.fillMaxWidth()
                .aspectRatio(1.4f)
                .clip(RoundedCornerShape(32.dp))
                .background(Color.Black),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedContent(targetState = busy to bitmap, label = "stage") { (busy, bitmap) ->
                when {
                    busy != null -> LoadingIndicator(Modifier.size(72.dp))
                    bitmap != null ->
                        Image(
                            bitmap.asImageBitmap(),
                            contentDescription = stringResource(R.string.edge_lighting_preview),
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize().padding(28.dp),
                        )
                    else ->
                        Icon(
                            painterResource(R.drawable.materialsymbols_ic_image_rounded_filled),
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(56.dp),
                        )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            stringResource(busy ?: message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onPlay,
            enabled = canPlay,
            modifier = Modifier.height(ButtonDefaults.MediumContainerHeight),
            contentPadding = ButtonDefaults.contentPaddingFor(ButtonDefaults.MediumContainerHeight),
        ) {
            Icon(
                painterResource(R.drawable.materialsymbols_ic_play_arrow_rounded_filled),
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.iconSizeFor(ButtonDefaults.MediumContainerHeight)),
            )
            Spacer(Modifier.size(ButtonDefaults.iconSpacingFor(ButtonDefaults.MediumContainerHeight)))
            Text(
                stringResource(R.string.edge_lighting_play),
                style = ButtonDefaults.textStyleFor(ButtonDefaults.MediumContainerHeight),
            )
        }
    }
}

/** An image choice. The selected one morphs from a rounded square to a scalloped cookie. */
@Composable
private fun ImageTile(
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val shape = rememberMorphShape(MaterialShapes.Square, MaterialShapes.Cookie9Sided, selected)
    val container by
        animateColorAsState(
            if (selected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceContainerHighest,
            MaterialTheme.motionScheme.fastEffectsSpec(),
            label = "tile",
        )
    Box(
        Modifier.size(76.dp)
            .clip(shape)
            .background(container)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

/** A colour choice; the chosen one shows a check. */
@Composable
private fun Swatch(color: Color, selected: Boolean, onClick: () -> Unit) {
    val shape = rememberMorphShape(MaterialShapes.Circle, MaterialShapes.Cookie6Sided, selected)
    Box(
        Modifier.size(44.dp).clip(shape).background(color).combinedClickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Icon(
                painterResource(R.drawable.ic_check),
                contentDescription = null,
                tint = if (color.luminance() > 0.6f) Color.Black else Color.White,
            )
        }
    }
}

private fun Color.luminance(): Float = 0.2126f * red + 0.7152f * green + 0.0722f * blue

@Composable
private fun KeywordDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var word by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(painterResource(R.drawable.materialsymbols_ic_sell_rounded_filled), null) },
        title = { Text(stringResource(R.string.edge_lighting_add_keyword)) },
        text = {
            OutlinedTextField(
                value = word,
                onValueChange = { word = it },
                singleLine = true,
                label = { Text(stringResource(R.string.edge_lighting_keyword_hint)) },
            )
        },
        confirmButton = {
            TextButton(onClick = { onAdd(word.trim()) }, enabled = word.isNotBlank()) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.cancel)) }
        },
    )
}

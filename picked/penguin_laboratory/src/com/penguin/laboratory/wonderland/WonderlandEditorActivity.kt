/*
 * Copyright (C) 2026 The PenguinOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

@file:OptIn(
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class,
)

package com.penguin.laboratory.wonderland

import android.app.WallpaperInfo
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import com.penguin.laboratory.ui.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.penguin.laboratory.R
import com.penguin.laboratory.edgelighting.Cutouts
import com.penguin.laboratory.ui.ConnectedChoice
import com.penguin.laboratory.ui.LabTheme
import com.penguin.laboratory.ui.LevelItem
import com.penguin.laboratory.ui.PercentItem
import com.penguin.laboratory.ui.SectionHeader
import com.penguin.laboratory.ui.SegmentedGroup
import com.penguin.laboratory.ui.SignedSliderItem
import com.penguin.laboratory.ui.rememberMorphShape
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.penguin.laboratory.ui.segmentedShapes

/** Builds a Wonderland wallpaper out of a background and layers that drift with the tilt. */
class WonderlandEditorActivity : ComponentActivity() {

    private lateinit var project: String
    private lateinit var scene: Scene
    private var sceneView: SceneView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        project = intent.getStringExtra(EXTRA_PROJECT) ?: return finish()
        scene = WonderlandStore.load(this, project) ?: return finish()
        setContent { LabTheme { Editor() } }
    }

    override fun onPause() {
        super.onPause()
        if (::scene.isInitialized) save()
    }

    private fun save() {
        val copy =
            try {
                Scene.fromJson(scene.toJson())
            } catch (e: org.json.JSONException) {
                Log.w(TAG, "Couldn't save", e)
                return
            }
        val thumbnail = sceneView?.snapshot(THUMBNAIL_PX)
        Thread {
                try {
                    WonderlandStore.save(this, project, copy, thumbnail)
                } catch (e: java.io.IOException) {
                    Log.w(TAG, "Couldn't save $project", e)
                }
            }
            .start()
    }

    private fun apply() {
        save()
        WonderlandStore.setActive(this, project)
        val component = ComponentName(this, WonderlandWallpaperService::class.java)
        val wm = WallpaperManager.getInstance(this)
        val home = isOurs(wm.getWallpaperInfo(WallpaperManager.FLAG_SYSTEM), component)
        // Without its own wallpaper the lock screen shows the home screen's
        val lock =
            if (wm.getWallpaperId(WallpaperManager.FLAG_LOCK) < 0) home
            else isOurs(wm.getWallpaperInfo(WallpaperManager.FLAG_LOCK), component)
        if (home && lock) {
            Toast.makeText(this, R.string.wonderland_applied, Toast.LENGTH_SHORT).show()
            return
        }
        // Lets the user choose the home screen, lock screen or both
        startActivity(
            Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
                .putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, component)
        )
    }

    private fun isOurs(info: WallpaperInfo?, component: ComponentName) = info?.component == component

    private fun newLayer(type: String) = Scene.Layer().apply {
        id = Scene.newId("layer")
        this.type = type
    }

    /** Copies the picked photo into the project as a new layer. Runs off the main thread. */
    private fun importImage(kind: Kind, uri: Uri, onDownloading: () -> Unit): Scene.Layer {
        val l: Scene.Layer
        val bitmap: android.graphics.Bitmap
        val name: String
        when (kind) {
            Kind.BACKGROUND -> {
                l = newLayer(Scene.TYPE_BACKGROUND)
                l.scale = 1f
                l.motionX = 0.3f
                l.motionY = 0.3f
                bitmap = Images.decode(this, uri, MAX_BACKGROUND_PX)
                l.source = uri.toString()
                name = l.id + ".jpg"
            }
            Kind.SUBJECT -> {
                l = newLayer(Scene.TYPE_IMAGE)
                l.y = 0.62f
                l.scale = 0.8f
                l.motionX = -0.2f
                l.motionY = -0.2f
                val bounds = RectF()
                val subject = Cutouts.subject(this, uri, { runOnUiThread { onDownloading() } }, bounds)
                l.source = uri.toString()
                l.anchored = true
                l.cropLeft = bounds.left
                l.cropTop = bounds.top
                l.cropRight = bounds.right
                l.cropBottom = bounds.bottom
                bitmap = Cutouts.scaleDown(subject, MAX_LAYER_PX)
                name = l.id + ".png"
            }
            else -> {
                l = newLayer(Scene.TYPE_IMAGE)
                l.scale = 0.6f
                l.motionX = -0.3f
                l.motionY = -0.3f
                bitmap = Images.decode(this, uri, MAX_LAYER_PX)
                name = l.id + ".png"
            }
        }
        l.file = WonderlandStore.writeImage(this, project, name, bitmap)
        return l
    }

    private enum class Kind { BACKGROUND, SUBJECT, PHOTO }

    @Composable
    private fun Editor() {
        val scope = rememberCoroutineScope()
        // Bumped on every edit; drives redraws and the debounced save
        var revision by remember { mutableIntStateOf(0) }
        // Bumped when layers are added, removed or reordered, which rebuilds the controls
        var structure by remember { mutableIntStateOf(0) }
        var selected by remember { mutableStateOf<String?>(null) }
        var previewMotion by remember { mutableStateOf(false) }
        var busy by remember { mutableStateOf<Int?>(null) }
        var adding by remember { mutableStateOf(false) }
        var editingText by remember { mutableStateOf<Scene.Layer?>(null) }
        var newText by remember { mutableStateOf(false) }

        fun edited() {
            sceneView?.invalidate()
            revision++
        }
        fun restructured(select: String? = selected) {
            selected = select?.takeIf { scene.find(it) != null }
            structure++
            edited()
        }

        LaunchedEffect(revision) {
            if (revision == 0) return@LaunchedEffect
            delay(SAVE_DELAY_MS)
            save()
        }

        var pendingKind by remember { mutableStateOf(Kind.PHOTO) }
        val picker =
            rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                if (uri == null) return@rememberLauncherForActivityResult
                val kind = pendingKind
                busy =
                    if (kind == Kind.SUBJECT) R.string.wonderland_cutting_out
                    else R.string.wonderland_loading
                scope.launch {
                    val layer =
                        withContext(Dispatchers.IO) {
                            try {
                                importImage(kind, uri) { busy = R.string.wonderland_downloading }
                            } catch (e: Exception) {
                                Log.w(TAG, "Couldn't import $uri", e)
                                null
                            }
                        }
                    busy = null
                    if (layer == null) {
                        Toast.makeText(
                                this@WonderlandEditorActivity,
                                if (kind == Kind.SUBJECT) R.string.wonderland_cutout_failed
                                else R.string.wonderland_load_failed,
                                Toast.LENGTH_LONG,
                            )
                            .show()
                        return@launch
                    }
                    if (layer.type == Scene.TYPE_BACKGROUND) {
                        // One background, always at the bottom
                        scene.background()?.let { scene.layers.remove(it) }
                        scene.layers.add(0, layer)
                    } else {
                        scene.layers.add(layer)
                    }
                    restructured(layer.id)
                }
            }
        fun pick(kind: Kind) {
            pendingKind = kind
            picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        Scaffold(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.wonderland_title)) },
                    navigationIcon = {
                        IconButton(onClick = { finish() }) {
                            Icon(painterResource(R.drawable.ic_back), null)
                        }
                    },
                    actions = {
                        IconToggleButton(checked = previewMotion, onCheckedChange = { previewMotion = it }) {
                            Icon(
                                painterResource(R.drawable.materialsymbols_ic_screen_rotation_rounded_filled),
                                contentDescription = stringResource(R.string.wonderland_preview_motion),
                            )
                        }
                        Button(onClick = ::apply, modifier = Modifier.padding(end = 8.dp)) {
                            Text(stringResource(R.string.wonderland_apply))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                )
            },
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(top = padding.calculateTopPadding())) {
                Box(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    AndroidView(
                        factory = { context ->
                            SceneView(context, null).also { view ->
                                sceneView = view
                                view.setScene(project, scene)
                                view.setCallback(
                                    object : SceneView.Callback {
                                        override fun onLayerSelected(id: String?) {
                                            selected = id
                                        }

                                        override fun onSceneEdited() {
                                            // Dragging may detach an anchored cutout, so rebuild
                                            structure++
                                            revision++
                                        }
                                    }
                                )
                            }
                        },
                        update = { view ->
                            revision.let { view.setScene(project, scene) }
                            view.setSelected(selected)
                            view.setPreviewMotion(previewMotion)
                        },
                        modifier = Modifier.fillMaxHeight().align(Alignment.Center),
                    )
                    busy?.let { message ->
                        Surface(
                            shape = RoundedCornerShape(28.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            modifier = Modifier.align(Alignment.Center),
                        ) {
                            Column(
                                Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                LoadingIndicator(Modifier.size(56.dp))
                                Spacer(Modifier.height(12.dp))
                                Text(stringResource(message), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                key(structure) {
                    LayerStrip(
                        selected = selected,
                        onSelect = { selected = it },
                        onAdd = { adding = true },
                    )
                }

                Surface(
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth().height(320.dp).padding(top = 8.dp),
                ) {
                    Column(
                        Modifier.verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                            .navigationBarsPadding()
                            .padding(bottom = 16.dp)
                    ) {
                        key(structure, selected) {
                            val layer = selected?.let { scene.find(it) }
                            if (layer == null) {
                                SceneControls(onEdited = ::edited)
                            } else {
                                LayerControls(
                                    layer = layer,
                                    onEdited = ::edited,
                                    onRestructured = { restructured(it) },
                                    onEditText = { editingText = layer },
                                    onChangePhoto = { pick(Kind.BACKGROUND) },
                                )
                            }
                        }
                    }
                }
            }
        }

        if (adding) {
            AddLayerSheet(
                onDismiss = { adding = false },
                onPick = { choice ->
                    adding = false
                    when (choice) {
                        0 -> pick(Kind.BACKGROUND)
                        1 -> pick(Kind.SUBJECT)
                        2 -> pick(Kind.PHOTO)
                        3 -> newText = true
                        4 -> {
                            val l = newLayer(Scene.TYPE_PARTICLES)
                            l.motionX = 0.5f
                            l.motionY = 0.5f
                            scene.layers.add(l)
                            restructured(l.id)
                        }
                    }
                },
            )
        }

        if (newText || editingText != null) {
            TextDialog(
                initial = editingText?.text ?: "",
                isNew = editingText == null,
                onDismiss = {
                    newText = false
                    editingText = null
                },
                onDone = { text ->
                    val existing = editingText
                    if (existing != null) {
                        existing.text = text
                        restructured(existing.id)
                    } else {
                        val l = newLayer(Scene.TYPE_TEXT)
                        l.text = text
                        l.y = 0.3f
                        l.scale = 0.5f
                        l.motionX = 0.3f
                        l.motionY = 0.3f
                        scene.layers.add(l)
                        restructured(l.id)
                    }
                    newText = false
                    editingText = null
                },
            )
        }
    }

    @Composable
    private fun LayerStrip(selected: String?, onSelect: (String?) -> Unit, onAdd: () -> Unit) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            item {
                FilledIconButton(
                    onClick = onAdd,
                    shape = MaterialShapes.Cookie4Sided.toShape(),
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        painterResource(R.drawable.materialsymbols_ic_add_rounded_filled),
                        contentDescription = stringResource(R.string.wonderland_add_layer),
                    )
                }
            }
            item {
                LayerChip(
                    label = stringResource(R.string.wonderland_scene),
                    icon = R.drawable.materialsymbols_ic_tune_rounded_filled,
                    selected = selected == null,
                    hidden = false,
                ) {
                    onSelect(null)
                }
            }
            // Top layer first, as they stack
            items(scene.layers.reversed(), key = { it.id }) { l ->
                LayerChip(
                    label = label(l),
                    icon = layerIcon(l),
                    selected = l.id == selected,
                    hidden = l.hidden,
                ) {
                    onSelect(l.id)
                }
            }
        }
    }

    @Composable
    private fun LayerChip(label: String, icon: Int, selected: Boolean, hidden: Boolean, onClick: () -> Unit) {
        FilterChip(
            selected = selected,
            onClick = onClick,
            label = { Text(label, maxLines = 1) },
            leadingIcon = {
                Icon(
                    painterResource(if (hidden) R.drawable.materialsymbols_ic_visibility_off_rounded_filled else icon),
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                )
            },
        )
    }

    @Composable
    private fun label(l: Scene.Layer): String =
        when (l.type) {
            Scene.TYPE_BACKGROUND -> stringResource(R.string.wonderland_layer_background)
            Scene.TYPE_TEXT ->
                l.text?.takeIf { it.isNotEmpty() }?.lines()?.first()?.let {
                    if (it.length > 14) it.take(14) + "…" else it
                } ?: stringResource(R.string.wonderland_layer_text)
            Scene.TYPE_PARTICLES -> stringResource(particleLabel(l.particles))
            else -> stringResource(R.string.wonderland_layer_image)
        }

    private fun layerIcon(l: Scene.Layer): Int =
        when (l.type) {
            Scene.TYPE_BACKGROUND -> R.drawable.materialsymbols_ic_wallpaper_rounded_filled
            Scene.TYPE_TEXT -> R.drawable.materialsymbols_ic_text_fields_rounded_filled
            Scene.TYPE_PARTICLES -> R.drawable.materialsymbols_ic_grain_rounded_filled
            else ->
                if (l.anchored || l.cropRight > 0f) R.drawable.materialsymbols_ic_person_rounded_filled
                else R.drawable.materialsymbols_ic_image_rounded_filled
        }

    @Composable
    private fun SceneControls(onEdited: () -> Unit) {
        SectionHeader(stringResource(R.string.wonderland_motion))
        SegmentedGroup {
            PercentItem(
                title = stringResource(R.string.wonderland_sensitivity),
                value = scene.sensitivity,
                onValueChange = {
                    scene.sensitivity = it
                    onEdited()
                },
                index = 0,
                count = 1,
                icon = R.drawable.materialsymbols_ic_screen_rotation_rounded_filled,
            )
        }
        SectionHeader(stringResource(R.string.wonderland_background_color))
        Swatches(scene.backgroundColor) {
            scene.backgroundColor = it
            onEdited()
        }
        Text(
            stringResource(R.string.wonderland_scene_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
        )
    }

    @Composable
    private fun LayerControls(
        layer: Scene.Layer,
        onEdited: () -> Unit,
        onRestructured: (String?) -> Unit,
        onEditText: () -> Unit,
        onChangePhoto: () -> Unit,
    ) {
        Actions(layer, onRestructured)

        when (layer.type) {
            Scene.TYPE_TEXT -> {
                SectionHeader(stringResource(R.string.wonderland_layer_text))
                SegmentedGroup {
                    SegmentedListItem(
                        onClick = onEditText,
                        shapes = ListItemDefaults.segmentedShapes(0, 1),
                        leadingContent = { Icon(painterResource(R.drawable.materialsymbols_ic_edit_rounded_filled), null) },
                        supportingContent = { Text(layer.text ?: "", maxLines = 2) },
                    ) {
                        Text(stringResource(R.string.wonderland_edit_text))
                    }
                }
                SectionHeader(stringResource(R.string.wonderland_font))
                var font by remember { mutableStateOf(layer.font) }
                ConnectedChoice(
                    options = FONTS,
                    selected = font,
                    onSelect = {
                        font = it
                        layer.font = it
                        onEdited()
                    },
                    label = { stringResource(fontLabel(it)) },
                )
                SectionHeader(stringResource(R.string.wonderland_color))
                Swatches(layer.color) {
                    layer.color = it
                    onEdited()
                }
            }
            Scene.TYPE_PARTICLES -> {
                SectionHeader(stringResource(R.string.wonderland_particles))
                var kind by remember { mutableStateOf(layer.particles) }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 4.dp),
                ) {
                    PARTICLES.forEach { p ->
                        FilterChip(
                            selected = p == kind,
                            onClick = {
                                if (p == Scene.PARTICLES_PETALS && layer.color == WHITE) layer.color = PETAL_PINK
                                kind = p
                                layer.particles = p
                                onRestructured(layer.id)
                            },
                            label = { Text(stringResource(particleLabel(p))) },
                        )
                    }
                }
                SectionHeader(stringResource(R.string.wonderland_look))
                SegmentedGroup {
                    Level(R.string.wonderland_amount, layer.amount, listOf(R.string.level_few, R.string.level_some, R.string.level_lots), 0, 3) {
                        layer.amount = it
                        onEdited()
                    }
                    Level(R.string.wonderland_size, layer.size, listOf(R.string.level_small, R.string.level_medium, R.string.level_large), 1, 3) {
                        layer.size = it
                        onEdited()
                    }
                    Level(R.string.wonderland_speed, layer.speed, listOf(R.string.level_slow, R.string.level_normal, R.string.level_fast), 2, 3) {
                        layer.speed = it
                        onEdited()
                    }
                }
                SectionHeader(stringResource(R.string.wonderland_color))
                Swatches(layer.color) {
                    layer.color = it
                    onEdited()
                }
            }
            Scene.TYPE_BACKGROUND -> {
                SectionHeader(stringResource(R.string.wonderland_layer_background))
                SegmentedGroup {
                    SegmentedListItem(
                        onClick = onChangePhoto,
                        shapes = ListItemDefaults.segmentedShapes(0, 1),
                        leadingContent = { Icon(painterResource(R.drawable.materialsymbols_ic_photo_library_rounded_filled), null) },
                    ) {
                        Text(stringResource(R.string.wonderland_change_background))
                    }
                }
            }
        }

        SectionHeader(stringResource(R.string.wonderland_look))
        SegmentedGroup {
            PercentItem(
                title = stringResource(R.string.wonderland_transparency),
                value = 1f - layer.alpha,
                onValueChange = {
                    layer.alpha = 1f - it
                    onEdited()
                },
                index = 0,
                count = 1,
                icon = R.drawable.materialsymbols_ic_opacity_rounded_filled,
            )
        }

        SectionHeader(stringResource(R.string.wonderland_motion))
        Text(
            stringResource(R.string.wonderland_motion_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
        )
        SegmentedGroup {
            SignedSliderItem(stringResource(R.string.wonderland_motion_x), layer.motionX, {
                layer.motionX = it
                onEdited()
            }, 0, 3, R.drawable.ic_swap_horiz)
            SignedSliderItem(stringResource(R.string.wonderland_motion_y), layer.motionY, {
                layer.motionY = it
                onEdited()
            }, 1, 3, R.drawable.materialsymbols_ic_swap_vert_rounded_filled)
            SignedSliderItem(stringResource(R.string.wonderland_motion_z), layer.motionZ, {
                layer.motionZ = it
                onEdited()
            }, 2, 3, R.drawable.materialsymbols_ic_3d_rotation_rounded_filled)
        }
    }

    /** The selected layer's housekeeping actions, ending with the destructive one. */
    @Composable
    private fun Actions(layer: Scene.Layer, onRestructured: (String?) -> Unit) {
        val background = layer.type == Scene.TYPE_BACKGROUND
        Row(
            Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        ) {
            ActionButton(
                if (layer.hidden) R.drawable.materialsymbols_ic_visibility_rounded_filled
                else R.drawable.materialsymbols_ic_visibility_off_rounded_filled,
                if (layer.hidden) R.string.wonderland_show else R.string.wonderland_hide,
            ) {
                layer.hidden = !layer.hidden
                onRestructured(layer.id)
            }
            if (!background) {
                ActionButton(R.drawable.materialsymbols_ic_arrow_upward_rounded_filled, R.string.wonderland_forward) {
                    move(layer, 1)
                    onRestructured(layer.id)
                }
                ActionButton(R.drawable.materialsymbols_ic_arrow_downward_rounded_filled, R.string.wonderland_backward) {
                    move(layer, -1)
                    onRestructured(layer.id)
                }
                ActionButton(R.drawable.materialsymbols_ic_content_copy_rounded_filled, R.string.wonderland_duplicate) {
                    val copy = layer.copy()
                    copy.id = Scene.newId("layer")
                    copy.anchored = false
                    copy.x = minOf(1f, copy.x + 0.05f)
                    copy.y = minOf(1f, copy.y + 0.05f)
                    scene.layers.add(copy)
                    onRestructured(copy.id)
                }
            }
            FilledIconButton(
                onClick = {
                    scene.layers.remove(layer)
                    onRestructured(null)
                },
                colors =
                    IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
                modifier = Modifier.size(52.dp),
            ) {
                Icon(
                    painterResource(R.drawable.materialsymbols_ic_delete_rounded_filled),
                    contentDescription = stringResource(R.string.wonderland_delete_layer),
                )
            }
        }
    }

    @Composable
    private fun ActionButton(icon: Int, label: Int, onClick: () -> Unit) {
        FilledTonalIconButton(onClick = onClick, modifier = Modifier.size(52.dp)) {
            Icon(painterResource(icon), contentDescription = stringResource(label))
        }
    }

    /** Moves a layer up or down the stack, never under the background. */
    private fun move(l: Scene.Layer, by: Int) {
        val from = scene.layers.indexOf(l)
        val to = from + by
        val bottom = if (scene.background() != null) 1 else 0
        if (to < bottom || to >= scene.layers.size) return
        scene.layers.removeAt(from)
        scene.layers.add(to, l)
    }

    @Composable
    private fun Level(title: Int, value: Int, labels: List<Int>, index: Int, count: Int, onChange: (Int) -> Unit) {
        var current by remember { mutableIntStateOf(value) }
        LevelItem(
            title = stringResource(title),
            labels = labels.map { stringResource(it) },
            value = current,
            onValueChange = {
                if (it != current) {
                    current = it
                    onChange(it)
                }
            },
            index = index,
            count = count,
        )
    }

    @Composable
    private fun Swatches(current: Int, onPick: (Int) -> Unit) {
        var chosen by remember { mutableIntStateOf(current) }
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 4.dp),
        ) {
            items(COLORS) { c ->
                val shape = rememberMorphShape(MaterialShapes.Circle, MaterialShapes.Cookie6Sided, c == chosen)
                Box(
                    Modifier.size(40.dp)
                        .clip(shape)
                        .background(Color(c))
                        .clickable {
                            chosen = c
                            onPick(c)
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    if (c == chosen) {
                        Icon(
                            painterResource(R.drawable.ic_check),
                            contentDescription = null,
                            tint = if (c == WHITE || c == PETAL_PINK) Color.Black else Color.White,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun AddLayerSheet(onDismiss: () -> Unit, onPick: (Int) -> Unit) {
        val choices =
            listOf(
                Triple(R.string.wonderland_add_background, R.string.wonderland_add_background_summary, R.drawable.materialsymbols_ic_wallpaper_rounded_filled),
                Triple(R.string.wonderland_add_subject, R.string.wonderland_add_subject_summary, R.drawable.materialsymbols_ic_person_rounded_filled),
                Triple(R.string.wonderland_add_photo, R.string.wonderland_add_photo_summary, R.drawable.materialsymbols_ic_image_rounded_filled),
                Triple(R.string.wonderland_add_text, R.string.wonderland_add_text_summary, R.drawable.materialsymbols_ic_text_fields_rounded_filled),
                Triple(R.string.wonderland_add_particles, R.string.wonderland_add_particles_summary, R.drawable.materialsymbols_ic_grain_rounded_filled),
            )
        ModalBottomSheet(onDismissRequest = onDismiss) {
            Text(
                stringResource(R.string.wonderland_add_layer),
                style = MaterialTheme.typography.headlineSmallEmphasized,
                modifier = Modifier.padding(start = 24.dp, bottom = 16.dp),
            )
            SegmentedGroup(Modifier.padding(horizontal = 16.dp).padding(bottom = 24.dp)) {
                choices.forEachIndexed { index, (title, summary, icon) ->
                    SegmentedListItem(
                        onClick = { onPick(index) },
                        shapes = ListItemDefaults.segmentedShapes(index, choices.size),
                        leadingContent = {
                            Box(
                                Modifier.size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.secondaryContainer),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    painterResource(icon),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                )
                            }
                        },
                        supportingContent = { Text(stringResource(summary)) },
                    ) {
                        Text(stringResource(title))
                    }
                }
            }
        }
    }

    @Composable
    private fun TextDialog(initial: String, isNew: Boolean, onDismiss: () -> Unit, onDone: (String) -> Unit) {
        var text by remember { mutableStateOf(initial) }
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = { Icon(painterResource(R.drawable.materialsymbols_ic_text_fields_rounded_filled), null) },
            title = {
                Text(stringResource(if (isNew) R.string.wonderland_add_text else R.string.wonderland_edit_text))
            },
            text = {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text(stringResource(R.string.wonderland_text_hint)) },
                    minLines = 1,
                    maxLines = 4,
                )
            },
            confirmButton = {
                TextButton(onClick = { onDone(text.trim()) }, enabled = text.isNotBlank()) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.cancel)) }
            },
        )
    }

    companion object {
        const val EXTRA_PROJECT = "project"

        private const val TAG = "Wonderland"
        // Big enough for a sharp full screen background with room to drift
        private const val MAX_BACKGROUND_PX = 2560
        private const val MAX_LAYER_PX = 1600
        private const val THUMBNAIL_PX = 270
        private const val SAVE_DELAY_MS = 400L

        private const val WHITE = 0xFFFFFFFF.toInt()
        private const val PETAL_PINK = 0xFFFFC9DE.toInt()
        private val COLORS =
            listOf(
                WHITE, 0xFF101828.toInt(), 0xFF000000.toInt(), 0xFFFF5C8A.toInt(),
                0xFFFF8A3D.toInt(), 0xFFFFD43B.toInt(), 0xFF69DB7C.toInt(), 0xFF38D9A9.toInt(),
                0xFF4DABF7.toInt(), 0xFF9775FA.toInt(), PETAL_PINK,
            )
        private val PARTICLES =
            listOf(
                Scene.PARTICLES_SNOW, Scene.PARTICLES_RAIN, Scene.PARTICLES_BUBBLES,
                Scene.PARTICLES_STARS, Scene.PARTICLES_FIREFLIES, Scene.PARTICLES_PETALS,
            )
        private val FONTS = listOf(Scene.FONT_SANS, Scene.FONT_SERIF, Scene.FONT_CONDENSED, Scene.FONT_MONO)

        private fun particleLabel(p: String) =
            when (p) {
                Scene.PARTICLES_RAIN -> R.string.wonderland_particles_rain
                Scene.PARTICLES_BUBBLES -> R.string.wonderland_particles_bubbles
                Scene.PARTICLES_STARS -> R.string.wonderland_particles_stars
                Scene.PARTICLES_FIREFLIES -> R.string.wonderland_particles_fireflies
                Scene.PARTICLES_PETALS -> R.string.wonderland_particles_petals
                else -> R.string.wonderland_particles_snow
            }

        private fun fontLabel(f: String) =
            when (f) {
                Scene.FONT_SERIF -> R.string.wonderland_font_serif
                Scene.FONT_CONDENSED -> R.string.wonderland_font_condensed
                Scene.FONT_MONO -> R.string.wonderland_font_mono
                else -> R.string.wonderland_font_sans
            }
    }
}

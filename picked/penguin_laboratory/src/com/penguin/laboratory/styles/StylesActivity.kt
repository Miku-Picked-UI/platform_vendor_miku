/*
 * Copyright (C) 2026 The PenguinOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.penguin.laboratory.styles

import com.penguin.laboratory.Backends
import android.app.TimePickerDialog
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.text.format.DateFormat
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RadioButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import com.penguin.laboratory.ui.SegmentedListItem
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.penguin.laboratory.R
import com.penguin.laboratory.ui.ConnectedChoice
import com.penguin.laboratory.ui.LabTheme
import com.penguin.laboratory.ui.PreviewPicker
import com.penguin.laboratory.ui.SectionHeader
import com.penguin.laboratory.ui.SegmentedBlock
import com.penguin.laboratory.ui.SegmentedGroup
import com.penguin.laboratory.ui.SwitchItem
import com.penguin.laboratory.ui.segmentedShapes

/** The looks PenguinSystemUI can draw its panels in. All in Settings.Secure, read by SystemUI. */
object StylesStore {
    const val QS_PANEL = "qs_panel_style"
    const val QS_FOLDER = "qs_connectivity_folder"
    const val QS_ONE_UI = "qs_oneui_experimental"
    const val VOLUME_PANEL = "volume_panel_style"
    const val POWER_MENU = "power_menu_style"
    const val UDFPS_SCREEN_OFF_ICON = "doze_udfps_icon"
    // Read by SystemUI (doze/PenguinDozeTap.java).
    const val DOZE_TAP = "doze_tap_gesture"
    const val TAP_ACTION = "penguin_doze_tap_action"
    const val UDFPS_SCHEDULE = "penguin_doze_udfps_icon_schedule"
    const val UDFPS_START = "penguin_doze_udfps_icon_start"
    const val UDFPS_END = "penguin_doze_udfps_icon_end"
    const val TAP_SENSOR_TYPE = "org.penguin.sensor.single_tap"

    /** Single tap on the dark screen: nothing, show icon, ambient display, wake. */
    val tapActions =
        listOf(
            Choice(0, R.string.styles_tap_none),
            Choice(1, R.string.styles_tap_icon),
            Choice(2, R.string.styles_tap_ambient),
            Choice(3, R.string.styles_tap_wake),
        )

    fun tapAction(context: Context): Int =
        if (get(context, DOZE_TAP, 0) == 0) 0 else get(context, TAP_ACTION, 2)

    fun setTapAction(context: Context, action: Int) {
        if (action != 0) put(context, TAP_ACTION, action)
        put(context, DOZE_TAP, if (action != 0) 1 else 0)
    }

    fun tapSensorAvailable(context: Context): Boolean =
        context.getSystemService(SensorManager::class.java)?.getSensorList(Sensor.TYPE_ALL)?.any {
            it.stringType == TAP_SENSOR_TYPE
        } == true

    fun formatMinutes(context: Context, minutes: Int): String {
        val c = java.util.Calendar.getInstance()
        c.set(java.util.Calendar.HOUR_OF_DAY, minutes / 60)
        c.set(java.util.Calendar.MINUTE, minutes % 60)
        return DateFormat.getTimeFormat(context).format(c.time)
    }

    /** A style with the value SystemUI reads and its label. */
    class Choice(val value: Int, @StringRes val label: Int)

    // Miku-Picked UI: the styles this SystemUI draws (QsPanelStyle). 7 is ColorCC's Control
    // Centre; MyUI, HarmonyOS and ColorOS aren't ported.
    val qsPanel =
        listOf(
            Choice(1, R.string.styles_qs_penguin),
            Choice(7, R.string.styles_qs_colorcc),
            Choice(6, R.string.styles_qs_stock),
        )
    const val QS_PANEL_DEFAULT = 6
    val volumePanel =
        listOf(
            Choice(1, R.string.styles_volume_expandable),
            Choice(2, R.string.styles_volume_oneui),
            Choice(3, R.string.styles_volume_myui),
            // Miku-Picked UI: our SystemUI also has PenguinOS' ColorOS volume panel
            Choice(4, R.string.styles_qs_coloros),
        )
    val powerMenu =
        listOf(Choice(0, R.string.styles_power_penguin), Choice(1, R.string.styles_power_ios))

    fun get(context: Context, key: String, default: Int) =
        Settings.Secure.getInt(context.contentResolver, key, default)

    fun put(context: Context, key: String, value: Int) {
        Settings.Secure.putInt(context.contentResolver, key, value)
    }

    /** How many styles are in use other than AOSP's, for the Laboratory's list. */
    fun customised(context: Context) =
        listOf(
                get(context, QS_PANEL, QS_PANEL_DEFAULT) != QS_PANEL_DEFAULT,
                get(context, VOLUME_PANEL, 0) != 0,
                Backends.POWER_MENU_STYLES && get(context, POWER_MENU, 0) != 0,
                UdfpsPack.ICON.get(context) != 0,
                UdfpsPack.ANIMATION.get(context) != 0,
            )
            .count { it }
}

class StylesActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { LabTheme { Styles() } }
    }

    @Composable
    private fun Styles() {
        val context = this
        var generation by remember { mutableIntStateOf(0) }
        LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { generation++ }
        val oneUi = remember(generation) { StylesStore.get(context, StylesStore.QS_ONE_UI, 0) != 0 }
        var qs by
            remember(generation) {
                mutableStateOf(
                    StylesStore.get(context, StylesStore.QS_PANEL, StylesStore.QS_PANEL_DEFAULT)
                )
            }
        var folder by
            remember(generation) {
                mutableStateOf(StylesStore.get(context, StylesStore.QS_FOLDER, 1) != 0)
            }
        var volume by
            remember(generation) { mutableStateOf(StylesStore.get(context, StylesStore.VOLUME_PANEL, 0)) }
        var power by
            remember(generation) { mutableStateOf(StylesStore.get(context, StylesStore.POWER_MENU, 0)) }
        val udfpsIcons = remember { UdfpsPack.ICON.load(context) }
        val tapAvailable = remember { StylesStore.tapSensorAvailable(context) }
        var tapAction by remember(generation) { mutableIntStateOf(StylesStore.tapAction(context)) }
        var tapDialog by remember { mutableStateOf(false) }
        var schedule by
            remember(generation) {
                mutableStateOf(StylesStore.get(context, StylesStore.UDFPS_SCHEDULE, 0) != 0)
            }
        var scheduleStart by
            remember(generation) {
                mutableIntStateOf(StylesStore.get(context, StylesStore.UDFPS_START, 7 * 60))
            }
        var scheduleEnd by
            remember(generation) {
                mutableIntStateOf(StylesStore.get(context, StylesStore.UDFPS_END, 23 * 60))
            }
        val udfpsAnimations = remember { UdfpsPack.ANIMATION.load(context) }
        val udfpsIcon = remember(generation) { UdfpsPack.ICON.get(context) }
        val udfpsAnimation = remember(generation) { UdfpsPack.ANIMATION.get(context) }
        var udfpsScreenOff by
            remember(generation) {
                mutableStateOf(StylesStore.get(context, StylesStore.UDFPS_SCREEN_OFF_ICON, 0) != 0)
            }
        // With Always-on display on, the fingerprint icon is always on the screen-off display.
        val alwaysOnDisplay =
            remember(generation) {
                android.provider.Settings.Secure.getInt(
                    context.contentResolver,
                    "doze_always_on",
                    0,
                ) != 0
            }
        var playing by remember { mutableStateOf(true) }
        LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { playing = true }
        LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) { playing = false }

        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            topBar = {
                MediumFlexibleTopAppBar(
                    title = { Text(stringResource(R.string.styles_title)) },
                    subtitle = { Text(stringResource(R.string.styles_entry_summary)) },
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
                        top = padding.calculateTopPadding(),
                        bottom = padding.calculateBottomPadding() + 24.dp,
                    )
            ) {
                item { SectionHeader(stringResource(R.string.styles_qs_title)) }
                item {
                    SegmentedGroup {
                        // The styles differ in layout more than in name, so pictures of each.
                        val labels = StylesStore.qsPanel.associate { it.value to it.label }
                        SegmentedBlock(index = 0, count = 2) {
                            Text(
                                stringResource(R.string.styles_qs_style),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            if (oneUi) {
                                Text(
                                    stringResource(R.string.styles_qs_oneui_active),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            PreviewPicker(
                                options = labels.keys.toList(),
                                selected = qs,
                                onSelect = {
                                    StylesStore.put(context, StylesStore.QS_PANEL, it)
                                    qs = it
                                },
                                label = { stringResource(labels[it] ?: R.string.styles_default) },
                                preview = { QsStylePreview(it) },
                                enabled = !oneUi,
                                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                            )
                        }
                        SwitchItem(
                            title = stringResource(R.string.styles_qs_folder),
                            supporting = stringResource(R.string.styles_qs_folder_summary),
                            checked = folder,
                            onCheckedChange = {
                                StylesStore.put(context, StylesStore.QS_FOLDER, if (it) 1 else 0)
                                folder = it
                            },
                            index = 1,
                            count = 2,
                            icon = R.drawable.materialsymbols_ic_grid_view_rounded_filled,
                        )
                    }
                }
                item { SectionHeader(stringResource(R.string.styles_volume_title)) }
                item {
                    SegmentedBlock(index = 0, count = 1) {
                        StyleChoice(
                            choices = StylesStore.volumePanel,
                            selected = volume,
                            // AOSP's own volume panel, for when none of these is wanted.
                            default = Pair(0, R.string.styles_default),
                        ) {
                            StylesStore.put(context, StylesStore.VOLUME_PANEL, it)
                            volume = it
                        }
                    }
                }
                if (Backends.POWER_MENU_STYLES) item { SectionHeader(stringResource(R.string.styles_power_title)) }
                if (Backends.POWER_MENU_STYLES) item {
                    SegmentedBlock(index = 0, count = 1) {
                        StyleChoice(choices = StylesStore.powerMenu, selected = power) {
                            StylesStore.put(context, StylesStore.POWER_MENU, it)
                            power = it
                        }
                    }
                }
                val udfps =
                    listOfNotNull(
                        Triple(UdfpsPack.ICON, udfpsIcons, udfpsIcon).takeIf { udfpsIcons.isNotEmpty() },
                        Triple(UdfpsPack.ANIMATION, udfpsAnimations, udfpsAnimation)
                            .takeIf { udfpsAnimations.isNotEmpty() },
                    )
                if (udfps.isNotEmpty() || Backends.SCREEN_OFF_UDFPS_ICON) {
                    item { SectionHeader(stringResource(R.string.styles_udfps_title)) }
                    item {
                        SegmentedGroup {
                            val scheduleRows =
                                when {
                                    alwaysOnDisplay -> 0
                                    schedule -> 3
                                    else -> 1
                                }
                            // Miku-Picked UI: the screen-off icon rows wait for the doze part
                            val screenOffRows =
                                if (Backends.SCREEN_OFF_UDFPS_ICON) 1 + scheduleRows else 0
                            val count = udfps.size + screenOffRows + (if (tapAvailable) 1 else 0)
                            udfps.forEachIndexed { index, (pack, entries, chosen) ->
                                UdfpsItem(pack, entries.getOrNull(chosen), chosen, playing, index, count)
                            }
                            var row = udfps.size
                            if (Backends.SCREEN_OFF_UDFPS_ICON) {
                            SwitchItem(
                                title = stringResource(R.string.styles_udfps_screen_off),
                                supporting =
                                    stringResource(
                                        if (alwaysOnDisplay) R.string.styles_udfps_screen_off_aod
                                        else R.string.styles_udfps_screen_off_summary
                                    ),
                                checked = udfpsScreenOff || alwaysOnDisplay,
                                enabled = !alwaysOnDisplay,
                                onCheckedChange = {
                                    StylesStore.put(
                                        context,
                                        StylesStore.UDFPS_SCREEN_OFF_ICON,
                                        if (it) 1 else 0,
                                    )
                                    udfpsScreenOff = it
                                },
                                index = udfps.size,
                                count = count,
                                icon = R.drawable.materialsymbols_ic_mobile_off_rounded_filled,
                            )
                            row++
                            if (!alwaysOnDisplay) {
                                SwitchItem(
                                    title = stringResource(R.string.styles_udfps_schedule),
                                    supporting = stringResource(R.string.styles_udfps_schedule_summary),
                                    checked = schedule,
                                    enabled = udfpsScreenOff,
                                    onCheckedChange = {
                                        StylesStore.put(context, StylesStore.UDFPS_SCHEDULE, if (it) 1 else 0)
                                        schedule = it
                                    },
                                    index = row++,
                                    count = count,
                                    icon = R.drawable.materialsymbols_ic_schedule_rounded_filled,
                                )
                                if (schedule) {
                                    TimeItem(
                                        stringResource(R.string.styles_udfps_schedule_from),
                                        scheduleStart,
                                        udfpsScreenOff,
                                        row++,
                                        count,
                                    ) {
                                        StylesStore.put(context, StylesStore.UDFPS_START, it)
                                        scheduleStart = it
                                    }
                                    TimeItem(
                                        stringResource(R.string.styles_udfps_schedule_to),
                                        scheduleEnd,
                                        udfpsScreenOff,
                                        row++,
                                        count,
                                    ) {
                                        StylesStore.put(context, StylesStore.UDFPS_END, it)
                                        scheduleEnd = it
                                    }
                                }
                            }
                            }
                            if (tapAvailable) {
                                val label =
                                    StylesStore.tapActions.firstOrNull { it.value == tapAction }?.label
                                        ?: R.string.styles_tap_none
                                SegmentedListItem(
                                    onClick = { tapDialog = true },
                                    shapes = ListItemDefaults.segmentedShapes(row, count),
                                    leadingContent = {
                                        Icon(
                                            painterResource(R.drawable.materialsymbols_ic_touch_app_rounded_filled),
                                            contentDescription = null,
                                        )
                                    },
                                    supportingContent = { Text(stringResource(label)) },
                                ) {
                                    Text(stringResource(R.string.styles_tap_title))
                                }
                            }
                        }
                        if (tapDialog) {
                            TapActionDialog(tapAction, onDismiss = { tapDialog = false }) {
                                StylesStore.setTapAction(context, it)
                                tapAction = it
                                tapDialog = false
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun TimeItem(
        title: String,
        minutes: Int,
        enabled: Boolean,
        index: Int,
        count: Int,
        onChange: (Int) -> Unit,
    ) {
        val context = this
        SegmentedListItem(
            onClick = {
                TimePickerDialog(
                        context,
                        { _, hour, minute -> onChange(hour * 60 + minute) },
                        minutes / 60,
                        minutes % 60,
                        DateFormat.is24HourFormat(context),
                    )
                    .show()
            },
            enabled = enabled,
            shapes = ListItemDefaults.segmentedShapes(index, count),
            leadingContent = {
                Icon(painterResource(R.drawable.materialsymbols_ic_lock_clock_rounded_filled), null)
            },
            supportingContent = { Text(StylesStore.formatMinutes(context, minutes)) },
        ) {
            Text(title)
        }
    }

    @Composable
    private fun TapActionDialog(current: Int, onDismiss: () -> Unit, onChoose: (Int) -> Unit) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.styles_tap_title)) },
            text = {
                Column {
                    StylesStore.tapActions.forEach { choice ->
                        Row(
                            Modifier.fillMaxWidth()
                                .clickable(role = Role.RadioButton) { onChoose(choice.value) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected = choice.value == current, onClick = null)
                            Text(stringResource(choice.label), Modifier.padding(start = 16.dp))
                        }
                    }
                    // The panel can't tell a double tap from two single taps while the single
                    // tap gesture is armed, so the power service holds double tap to wake off.
                    Text(
                        stringResource(R.string.styles_tap_double_tap_note),
                        Modifier.padding(top = 8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.cancel)) }
            },
        )
    }

    @Composable
    private fun UdfpsItem(
        pack: UdfpsPack,
        chosen: UdfpsPack.Entry?,
        chosenIndex: Int,
        playing: Boolean,
        index: Int,
        count: Int,
    ) {
        val icon = pack == UdfpsPack.ICON
        SegmentedListItem(
            onClick = { startActivity(UdfpsPickerActivity.intent(this, pack)) },
            shapes = ListItemDefaults.segmentedShapes(index, count),
            leadingContent = {
                Icon(
                    painterResource(
                        if (icon) R.drawable.materialsymbols_ic_fingerprint_rounded_filled
                        else R.drawable.materialsymbols_ic_animation_rounded_filled
                    ),
                    contentDescription = null,
                )
            },
            trailingContent = {
                Box(
                    Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(Color.Black),
                    contentAlignment = Alignment.Center,
                ) {
                    val px = with(LocalDensity.current) { 48.dp.roundToPx() }
                    when {
                        chosen == null || (!icon && chosenIndex == 0) ->
                            Icon(
                                painterResource(R.drawable.materialsymbols_ic_block_rounded_filled),
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(20.dp),
                            )
                        else ->
                            PackImage(
                                pack,
                                chosen.drawable,
                                if (icon) px * 2 / 3 else px,
                                18,
                                playing,
                                if (icon) Modifier.size(32.dp) else Modifier.fillMaxSize(),
                            )
                    }
                }
            },
        ) {
            Text(stringResource(if (icon) R.string.styles_udfps_icon else R.string.styles_udfps_animation))
        }
    }

    /** One button per style, [default] first when AOSP's look is one of the options. */
    @Composable
    private fun StyleChoice(
        choices: List<StylesStore.Choice>,
        selected: Int,
        default: Pair<Int, Int>? = null,
        onSelect: (Int) -> Unit,
    ) {
        val options = listOfNotNull(default?.first) + choices.map { it.value }
        val labels = (listOfNotNull(default) + choices.map { it.value to it.label }).toMap()
        // Miku-Picked UI: five pills in a row cut their labels short ("Defa", "Expa"), so more
        // than four options wrap into rows of three.
        val rows = if (options.size > 4) options.chunked(3) else listOf(options)
        rows.forEachIndexed { i, row ->
            ConnectedChoice(
                options = row,
                selected = selected,
                onSelect = onSelect,
                label = { stringResource(labels.getValue(it)) },
                modifier = Modifier.padding(bottom = if (i == rows.lastIndex) 12.dp else 8.dp),
            )
        }
    }
}

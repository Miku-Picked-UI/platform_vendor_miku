/*
 * Copyright (C) 2026 The PenguinOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.penguin.laboratory.glass

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.penguin.laboratory.R
import com.penguin.laboratory.ui.ConnectedChoice
import com.penguin.laboratory.ui.LabTheme
import com.penguin.laboratory.ui.SegmentedBlock

/** Which apps get glass tab bars, and which also run their content under the bar. */
object TabBarStore {
    /** Read in every app's process by the framework, so it isn't declared in Settings. */
    const val ENABLED = "penguin_glass_tab_bars"
    private const val EXCLUDED = "penguin_glass_tab_bars_excluded"
    private const val FLOAT = "penguin_glass_tab_bars_float"

    enum class Mode { OFF, GLASS, FLOAT }

    fun isEnabled(context: Context) =
        Settings.Secure.getInt(context.contentResolver, ENABLED, 0) != 0

    fun setEnabled(context: Context, enabled: Boolean) {
        Settings.Secure.putInt(context.contentResolver, ENABLED, if (enabled) 1 else 0)
    }

    private fun list(context: Context, key: String): Set<String> =
        Settings.Secure.getString(context.contentResolver, key)
            ?.split(",")
            ?.filter { it.isNotEmpty() }
            ?.toSet() ?: emptySet()

    private fun putList(context: Context, key: String, list: Set<String>) {
        Settings.Secure.putString(context.contentResolver, key, list.joinToString(","))
    }

    private const val SEEN = "seen"

    private fun prefs(context: Context) = context.getSharedPreferences("tab_bars", Context.MODE_PRIVATE)

    fun seen(context: Context): Set<String> = prefs(context).getStringSet(SEEN, null) ?: emptySet()

    fun addSeen(context: Context, pkg: String) {
        val seen = seen(context)
        if (pkg !in seen) prefs(context).edit().putStringSet(SEEN, seen + pkg).apply()
    }

    fun mode(context: Context, pkg: String) =
        when (pkg) {
            in list(context, EXCLUDED) -> Mode.OFF
            in list(context, FLOAT) -> Mode.FLOAT
            else -> Mode.GLASS
        }

    fun setMode(context: Context, pkg: String, mode: Mode) {
        val excluded = list(context, EXCLUDED) - pkg
        val floating = list(context, FLOAT) - pkg
        putList(context, EXCLUDED, if (mode == Mode.OFF) excluded + pkg else excluded)
        putList(context, FLOAT, if (mode == Mode.FLOAT) floating + pkg else floating)
    }
}

class TabBarAppsActivity : ComponentActivity() {

    private class App(val pkg: String, val label: String, val icon: ImageBitmap)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        // Apps opened meanwhile may have shown their bar
        val apps = loadApps()
        setContent { LabTheme { Apps(apps) } }
    }

    private fun loadApps(): List<App> {
        val size = (40 * resources.displayMetrics.density).toInt()
        val launcher = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val seen = TabBarStore.seen(this)
        return packageManager
            .queryIntentActivities(launcher, 0)
            .map { it.activityInfo.applicationInfo }
            .distinctBy { it.packageName }
            .filter { it.packageName in seen }
            .map { info ->
                val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
                packageManager.getApplicationIcon(info).apply {
                    setBounds(0, 0, size, size)
                    draw(Canvas(bitmap))
                }
                App(info.packageName, info.loadLabel(packageManager).toString(), bitmap.asImageBitmap())
            }
            .sortedBy { it.label.lowercase() }
    }

    @Composable
    private fun Apps(apps: List<App>) {
        val context = this
        val modes = remember {
            mutableStateMapOf<String, TabBarStore.Mode>().apply {
                apps.forEach { put(it.pkg, TabBarStore.mode(context, it.pkg)) }
            }
        }
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            topBar = {
                MediumFlexibleTopAppBar(
                    title = { Text(stringResource(R.string.tab_bars_apps_title)) },
                    subtitle = { Text(stringResource(R.string.tab_bars_apps_summary)) },
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
                        top = padding.calculateTopPadding() + 8.dp,
                        bottom = padding.calculateBottomPadding() + 24.dp,
                    )
            ) {
                if (apps.isEmpty()) {
                    item {
                        Text(
                            stringResource(R.string.tab_bars_apps_empty),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
                itemsIndexed(apps, key = { _, app -> app.pkg }) { index, app ->
                    SegmentedBlock(index = index, count = apps.size, modifier = Modifier.padding(bottom = 2.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(app.icon, contentDescription = null, modifier = Modifier.size(40.dp))
                            Spacer(Modifier.width(16.dp))
                            Text(app.label, style = MaterialTheme.typography.bodyLarge)
                        }
                        ConnectedChoice(
                            options = TabBarStore.Mode.entries,
                            selected = modes[app.pkg] ?: TabBarStore.Mode.GLASS,
                            onSelect = {
                                TabBarStore.setMode(context, app.pkg, it)
                                modes[app.pkg] = it
                            },
                            label = {
                                stringResource(
                                    when (it) {
                                        TabBarStore.Mode.OFF -> R.string.tab_bars_mode_off
                                        TabBarStore.Mode.GLASS -> R.string.tab_bars_mode_glass
                                        TabBarStore.Mode.FLOAT -> R.string.tab_bars_mode_float
                                    }
                                )
                            },
                            modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                        )
                    }
                }
            }
        }
    }
}

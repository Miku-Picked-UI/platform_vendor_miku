/*
 * Copyright (C) 2026 The PenguinOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.penguin.laboratory.styles

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.provider.Settings
import android.util.TypedValue
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import java.util.concurrent.Executors
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser

/** Index 0 is SystemUI's own icon, or no animation. */
enum class UdfpsPack(val pkg: String, val names: String, val titles: String?, val key: String) {
    // Miku-Picked UI: the ROM ships Evolution X's pack (vendor/miku/udfps_icons), same array and key
    ICON("org.evolution.udfps.icons", "udfps_icons", null, "udfps_icon"),
    ANIMATION(
        "com.crdroid.udfps.animations",
        "udfps_animation_styles",
        "udfps_animation_titles",
        "udfps_anim_style",
    );

    class Entry(val drawable: String, val description: String)

    fun resources(context: Context): Resources? =
        try {
            context.packageManager.getResourcesForApplication(pkg)
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }

    /** In SystemUI's index order; empty without the pack. */
    fun load(context: Context): List<Entry> {
        val res = resources(context) ?: return emptyList()
        val drawables = res.stringArray(pkg, names)
        val spoken = titles?.let { res.stringArray(pkg, it) }.orEmpty()
        return drawables.mapIndexed { i, name -> Entry(name, spoken.getOrNull(i) ?: describe(name)) }
    }

    fun get(context: Context) = Settings.System.getInt(context.contentResolver, key, 0)

    fun put(context: Context, value: Int) {
        Settings.System.putInt(context.contentResolver, key, value)
    }

    private fun Resources.stringArray(pkg: String, name: String): List<String> {
        val id = getIdentifier(name, "array", pkg)
        return if (id == 0) emptyList() else getStringArray(id).toList()
    }

    /** "udfps_icon_oneplus_red" reads as "Oneplus red". */
    private fun describe(name: String) =
        name.removePrefix("udfps_icon_").removePrefix("udfps_").replace('_', ' ').trim()
            .replaceFirstChar { it.uppercase() }
            .ifEmpty { name }
}

class PackFrames(val images: List<ImageBitmap>, val durations: IntArray)

// Frames are up to 1000 px; decode a few at a time so a grid doesn't stall the app.
private val decodeDispatcher = Executors.newFixedThreadPool(3).asCoroutineDispatcher()

// AnimationDrawable shows a frame for at least one vsync whatever the pack asks for.
private const val MIN_FRAME_MS = 16

/** Keeps at most [maxFrames] frames, adding the skipped frames' time to the kept ones. */
fun loadPackFrames(
    context: Context,
    pack: UdfpsPack,
    name: String,
    sizePx: Int,
    maxFrames: Int,
): PackFrames? {
    return try {
        val res = pack.resources(context) ?: return null
        val id = res.getIdentifier(name, "drawable", pack.pkg)
        if (id == 0) return null
        val frames = animationFrames(res, id)
        if (frames == null) {
            decodeFrame(res, id, sizePx)?.let { PackFrames(listOf(it), intArrayOf(0)) }
        } else {
            val step = (frames.size + maxFrames - 1) / maxFrames
            val images = ArrayList<ImageBitmap>()
            val durations = ArrayList<Int>()
            for (start in frames.indices step step) {
                val image = decodeFrame(res, frames[start].first, sizePx) ?: continue
                images += image
                durations +=
                    (start until minOf(start + step, frames.size))
                        .sumOf { frames[it].second.coerceAtLeast(MIN_FRAME_MS) }
            }
            if (images.isEmpty()) null else PackFrames(images, durations.toIntArray())
        }
    } catch (_: Exception) {
        null
    }
}

private fun animationFrames(res: Resources, id: Int): List<Pair<Int, Int>>? {
    val value = TypedValue()
    res.getValue(id, value, true)
    if (value.string?.endsWith(".xml") != true) return null
    val parser = res.getXml(id)
    try {
        var type = parser.next()
        while (type != XmlPullParser.START_TAG && type != XmlPullParser.END_DOCUMENT) {
            type = parser.next()
        }
        if (type != XmlPullParser.START_TAG || parser.name != "animation-list") return null
        val frames = ArrayList<Pair<Int, Int>>()
        while (parser.next().also { type = it } != XmlPullParser.END_DOCUMENT) {
            if (type != XmlPullParser.START_TAG || parser.name != "item") continue
            var drawable = 0
            var duration = 0
            for (i in 0 until parser.attributeCount) {
                when (parser.getAttributeNameResource(i)) {
                    android.R.attr.drawable -> drawable = parser.getAttributeResourceValue(i, 0)
                    android.R.attr.duration -> duration = parser.getAttributeIntValue(i, 0)
                }
            }
            if (drawable != 0) frames += drawable to duration
        }
        return frames.ifEmpty { null }
    } finally {
        parser.close()
    }
}

// getDrawable would decode the whole frame, scaled up from mdpi.
private fun decodeFrame(res: Resources, id: Int, sizePx: Int): ImageBitmap? {
    val bitmap =
        try {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(res, id)) { decoder, info, _ ->
                val longest = maxOf(info.size.width, info.size.height).coerceAtLeast(1)
                val scale = sizePx.toFloat() / longest
                decoder.setTargetSize(
                    (info.size.width * scale).toInt().coerceAtLeast(1),
                    (info.size.height * scale).toInt().coerceAtLeast(1),
                )
            }
        } catch (_: Exception) {
            res.getDrawable(id, null)?.let { drawable ->
                Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888).also {
                    drawable.setBounds(0, 0, sizePx, sizePx)
                    drawable.draw(Canvas(it))
                }
            }
        }
    return bitmap?.asImageBitmap()
}

@Composable
fun PackImage(
    pack: UdfpsPack,
    name: String,
    sizePx: Int,
    maxFrames: Int,
    playing: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val frames by
        produceState<PackFrames?>(null, pack, name, sizePx) {
            value = withContext(decodeDispatcher) {
                loadPackFrames(context, pack, name, sizePx, maxFrames)
            }
        }
    val shown by animateFloatAsState(if (frames == null) 0f else 1f, label = "fade")
    val loaded = frames ?: return
    var index by remember(loaded) { mutableIntStateOf(0) }
    if (playing && loaded.images.size > 1) {
        LaunchedEffect(loaded) {
            var last = withFrameMillis { it }
            var elapsed = 0L
            while (true) {
                withFrameMillis { now ->
                    elapsed += now - last
                    last = now
                    var next = index
                    while (elapsed >= loaded.durations[next]) {
                        elapsed -= loaded.durations[next]
                        next = (next + 1) % loaded.images.size
                    }
                    index = next
                }
            }
        }
    }
    Image(
        bitmap = loaded.images[index.coerceIn(0, loaded.images.lastIndex)],
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = modifier.alpha(shown),
    )
}

/*
 * Copyright (C) 2026 The PenguinOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.penguin.laboratory.styles

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

/**
 * A sketch of the Quick Settings layout PenguinSystemUI draws for a qs_panel_style value, on a
 * dark shade whatever the app's theme, since that's where it is seen. Drawn on a 100 x 150 grid.
 */
@Composable
fun QsStylePreview(style: Int, modifier: Modifier = Modifier) {
    val accent = MaterialTheme.colorScheme.primary
    val accentSoft = MaterialTheme.colorScheme.primaryContainer
    Canvas(modifier.fillMaxSize()) {
        drawRect(Color(0xFF16161A))
        val sketch = Sketch(this, size.width / 100f, size.height / 150f, accent, accentSoft)
        when (style) {
            1 -> sketch.penguin()
            2 -> sketch.myUi()
            3 -> sketch.harmony()
            5 -> sketch.colorOs()
            // Miku-Picked UI: ColorCC's Control Centre is ColorOS-like
            7 -> sketch.colorOs()
            else -> sketch.stock()
        }
    }
}

private class Sketch(
    val scope: DrawScope,
    val sx: Float,
    val sy: Float,
    val accent: Color,
    val accentSoft: Color,
) {
    val tile = Color.White.copy(alpha = 0.16f)
    val faint = Color.White.copy(alpha = 0.08f)
    val light = Color.White.copy(alpha = 0.88f)

    fun box(x: Float, y: Float, w: Float, h: Float, r: Float, color: Color = tile) =
        scope.drawRoundRect(
            color,
            Offset(x * sx, y * sy),
            Size(w * sx, h * sy),
            CornerRadius(r * sx, r * sx),
        )

    fun dot(x: Float, y: Float, r: Float, color: Color = tile) =
        scope.drawCircle(color, r * sx, Offset(x * sx, y * sy))

    /** AOSP: brightness on top, wide tiles beside round ones, media below. */
    fun stock() {
        box(6f, 8f, 74f, 10f, 5f)
        box(6f, 8f, 30f, 10f, 5f, light)
        dot(89f, 13f, 5f)
        box(6f, 24f, 43f, 14f, 7f, light)
        box(51f, 24f, 43f, 14f, 7f, light)
        for (row in 1..3) {
            val y = 24f + row * 18f
            box(6f, y, 43f, 14f, 7f, if (row == 1) light else tile)
            dot(62f, y + 7f, 7f)
            dot(83f, y + 7f, 7f)
        }
        dot(47f, 98f, 1.6f, light)
        dot(53f, 98f, 1.6f)
        box(6f, 106f, 88f, 38f, 10f, accentSoft)
        dot(82f, 132f, 6f, light)
    }

    /** Control Centre: a connectivity cluster, media beside it, round toggles, tall sliders. */
    fun penguin() {
        box(6f, 8f, 42f, 42f, 11f)
        dot(17f, 19f, 7f, faint)
        dot(37f, 19f, 7f, Color(0xFF34C759))
        dot(17f, 39f, 7f, accent)
        dot(37f, 39f, 7f, faint)
        box(52f, 8f, 42f, 42f, 11f)
        box(56f, 12f, 11f, 11f, 3f, accentSoft)
        dot(73f, 41f, 3.5f, light)
        dot(17f, 62f, 8f)
        dot(37f, 62f, 8f)
        box(6f, 76f, 42f, 18f, 9f)
        box(52f, 54f, 19f, 42f, 9f)
        box(52f, 74f, 19f, 22f, 9f, light)
        box(75f, 54f, 19f, 42f, 9f)
        for (x in listOf(17f, 39f, 61f, 83f)) {
            dot(x, 108f, 8f)
            dot(x, 131f, 8f, if (x == 39f) light else tile)
        }
    }

    /** MyUI: a list card of radios, two tall sliders, wide media, square tiles. */
    fun myUi() {
        box(6f, 8f, 46f, 48f, 9f)
        for (i in 0..2) {
            val y = 19f + i * 14f
            dot(15f, y, 4.5f, faint)
            box(23f, y - 2f, 22f, 4f, 2f, faint)
        }
        box(56f, 8f, 17f, 48f, 8f)
        box(77f, 8f, 17f, 48f, 8f)
        box(77f, 30f, 17f, 26f, 8f, light)
        box(6f, 62f, 88f, 26f, 8f, accentSoft)
        box(12f, 80f, 60f, 2f, 1f, light)
        for (y in listOf(94f, 118f)) {
            for (x in listOf(6f, 29f, 52f, 75f)) box(x, y, 19f, 20f, 5f)
        }
    }

    /** HarmonyOS: media and two toggles on top, one card of round toggles and a slider. */
    fun harmony() {
        box(6f, 8f, 46f, 34f, 9f)
        box(10f, 12f, 9f, 9f, 2f, accentSoft)
        dot(29f, 34f, 3f, light)
        for (y in listOf(8f, 27f)) {
            box(56f, y, 38f, 15f, 7.5f)
            dot(64f, y + 7.5f, 4.5f, accent)
        }
        box(6f, 48f, 88f, 46f, 11f)
        for (x in listOf(16f, 33f, 50f, 67f, 84f)) dot(x, 61f, 6.5f, if (x == 16f) accent else tile)
        box(12f, 76f, 76f, 11f, 5.5f, faint)
        box(12f, 76f, 32f, 11f, 5.5f, accent)
        box(6f, 100f, 88f, 44f, 11f, faint)
        dot(50f, 120f, 5f)
    }

    /** ColorOS: Wi-Fi and Bluetooth pills, media and sliders, then a labelled round grid. */
    fun colorOs() {
        for (x in listOf(6f, 52f)) {
            box(x, 8f, 42f, 16f, 8f)
            dot(x + 8f, 16f, 5f, light)
        }
        box(6f, 30f, 42f, 40f, 10f)
        box(10f, 34f, 10f, 10f, 2.5f, accentSoft)
        dot(27f, 62f, 3f, light)
        box(52f, 30f, 19f, 40f, 9f)
        box(52f, 48f, 19f, 22f, 9f, light)
        box(75f, 30f, 19f, 40f, 9f)
        box(6f, 76f, 42f, 16f, 8f)
        for (x in listOf(15f, 27f, 39f)) dot(x, 84f, 3f, if (x == 39f) Color(0xFFFF6B6B) else faint)
        box(52f, 76f, 42f, 16f, 8f)
        dot(60f, 84f, 5f, faint)
        for (y in listOf(106f, 131f)) {
            for (x in listOf(17f, 39f, 61f, 83f)) {
                dot(x, y, 7.5f, Color.Black.copy(alpha = 0.6f))
                dot(x, y, 2.5f, light)
                box(x - 6f, y + 10f, 12f, 2f, 1f, faint)
            }
        }
    }
}

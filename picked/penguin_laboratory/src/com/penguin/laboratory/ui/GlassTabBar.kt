/*
 * Copyright (C) 2026 The PenguinOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.penguin.laboratory.ui

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.graphics.Shader
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.roundToInt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

// A droplet of glass: magnifies what's under it, pulls the rim in like the edge of a lens, and
// splits the colours slightly there.
private const val BUBBLE =
    """
uniform shader content;
uniform float2 size;
uniform float radius;
uniform float depth;
uniform float zoom;
uniform float chroma;
half4 main(float2 p) {
    float2 c = size * 0.5;
    float2 q = abs(p - c) - (c - radius);
    float d = length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - radius;
    float t = clamp(1.0 + d / depth, 0.0, 1.0);
    t = t * t;
    float2 dir = p - c;
    dir = dir / max(length(dir), 1.0);
    float2 s = c + (p - c) / zoom - dir * t * depth * 0.9;
    if (chroma <= 0.0) {
        return content.eval(s);
    }
    float2 ca = dir * t * chroma;
    half4 g = content.eval(s);
    return half4(content.eval(s + ca).r, g.g, content.eval(s - ca).b, g.a);
}
"""

private val Pill = RoundedCornerShape(percent = 50)

// Moving between tabs: a ~0.35 s glide that settles without a visible bounce.
private val Glide = spring<Float>(dampingRatio = 0.82f, stiffness = 280f)
// Under the finger the lens trails by about this long, enough to hide touch jitter.
private const val FOLLOW_LAG_MS = 28f
// The droplet swelling up and settling back into the bar.
private val SwellIn = spring<Float>(dampingRatio = 0.8f, stiffness = 520f)
private val SwellOut = spring<Float>(dampingRatio = 0.9f, stiffness = 320f)

/** Where the lens is this frame. Read only while laying out or drawing, never in composition. */
private class LensFrame(val x: Float, val y: Float, val w: Float, val h: Float, val swell: Float)

/**
 * Tabs with a lens under the chosen one that can be dragged between tabs. While the pages are
 * being swiped ([pagePosition] not null), the lens rides along with them.
 */
@Composable
internal fun GlassTabBar(
    items: List<TabItem>,
    selected: Int,
    onSelect: (Int) -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    pagePosition: (() -> Float)? = null,
) {
    val haptics = LocalHapticFeedback.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val colors = MaterialTheme.colorScheme
    val dark = colors.surface.luminance() < 0.5f
    // The gesture below outlives recompositions, so it reads these through state.
    val current by rememberUpdatedState(selected)
    val select by rememberUpdatedState(onSelect)
    val swipe by rememberUpdatedState(pagePosition)
    val riding = pagePosition != null
    var rowOrigin by remember { mutableStateOf(Offset.Zero) }
    val lens = remember { Animatable(Float.NaN) }
    val swell = remember { Animatable(0f) }
    // The tab the lens is heading for, so a page change we caused doesn't send it off again.
    var aimed by remember { mutableIntStateOf(selected) }
    var pressing by remember { mutableStateOf(false) }
    // The lens's speed while it follows a finger, in px/s; the springs report their own.
    var followSpeed by remember { mutableFloatStateOf(0f) }
    val moves = remember { arrayOfNulls<Job>(2) }
    val backShader = remember { RuntimeShader(BUBBLE) }
    val frontShader = remember { RuntimeShader(BUBBLE) }

    GlassSurface(true, backdrop, modifier.fillMaxWidth().height(BarHeight)) {
        BoxWithConstraints(
            Modifier.fillMaxSize().padding(4.dp).onGloballyPositioned {
                rowOrigin = it.positionInRoot()
            }
        ) {
            val itemWidth = maxWidth / items.size
            val itemPx = with(density) { itemWidth.toPx() }
            val rowPx = with(density) { maxHeight.toPx() }
            val totalPx = itemPx * items.size

            /** Glides the lens to [index], swelling it on the way like a lifted droplet. */
            fun glideTo(index: Int) {
                aimed = index
                val to = itemPx * index
                val far = lens.value.isNaN() || abs(lens.value - to) > itemPx * 0.5f
                moves[0]?.cancel()
                moves[1]?.cancel()
                val handoff = followSpeed
                followSpeed = 0f
                moves[0] = scope.launch { lens.animateTo(to, Glide, initialVelocity = handoff) }
                moves[1] =
                    scope.launch {
                        if (far) swell.animateTo(1f, SwellIn)
                        moves[0]?.join()
                        if (!pressing) swell.animateTo(0f, SwellOut)
                    }
            }

            LaunchedEffect(selected, itemPx, riding) {
                when {
                    lens.value.isNaN() -> {
                        lens.snapTo(itemPx * selected)
                        aimed = selected
                    }
                    // Mid-swipe the target page flips with every wobble of the finger; the lens
                    // follows the pages below instead, and catches up here once they let go.
                    riding -> Unit
                    // Any new page, even mid-glide: the glide just retargets. (Paging from the bar
                    // keeps [selected] on its target, so the tabs in between never show up here.)
                    !pressing && selected != aimed -> glideTo(selected)
                    // Rotation or a new width: keep the lens on its tab.
                    !pressing && moves[0]?.isActive != true -> lens.snapTo(itemPx * aimed)
                }
            }

            // Swiping the pages carries the lens with them, so it can't run ahead to a page the
            // swipe then turns back from, or fall behind a quick run of swipes.
            LaunchedEffect(itemPx) {
                var following = false
                snapshotFlow { swipe?.invoke() ?: Float.NaN }
                    .collect { position ->
                        if (position.isNaN() || pressing || lens.value.isNaN()) {
                            following = false
                            return@collect
                        }
                        val to = itemPx * position.coerceIn(0f, items.lastIndex.toFloat())
                        moves[0]?.cancel()
                        if (!following) {
                            // Riding with the pages, the lens stays flat in the bar.
                            following = true
                            moves[1]?.cancel()
                            if (swell.value != 0f) moves[1] = scope.launch { swell.animateTo(0f, SwellOut) }
                        }
                        followSpeed = 0f
                        lens.snapTo(to)
                        aimed = position.roundToInt().coerceIn(0, items.lastIndex)
                    }
            }

            // Everything that changes per frame is read here, from layout and draw only, so the
            // tabs aren't recomposed on every frame of the glide.
            fun frame(): LensFrame {
                val left = lens.value.let { if (it.isNaN()) itemPx * selected else it }
                val s = swell.value
                // Speed pulls the droplet long and thin; px/s, from the spring itself.
                val speed = maxOf(abs(lens.velocity), abs(followSpeed))
                val stretch = (speed / 6000f).coerceAtMost(0.22f)
                val w = itemPx * (1f + 0.30f * s) * (1f + stretch)
                val h = rowPx * (1f + 0.38f * s) * (1f - stretch * 0.35f)
                return LensFrame(left + itemPx / 2f - w / 2f, rowPx / 2f - h / 2f, w, h, s)
            }
            fun RuntimeShader.configure(f: LensFrame) {
                setFloatUniform("size", f.w, f.h)
                setFloatUniform("radius", f.h / 2f)
                setFloatUniform("depth", with(density) { (7.dp + 7.dp * f.swell).toPx() })
                setFloatUniform("zoom", 1f + 0.2f * f.swell)
                setFloatUniform("chroma", with(density) { 2.5.dp.toPx() } * f.swell)
            }
            val frost = if (dark) colors.surfaceContainerHigh else Color.White
            val accent = colors.primary

            TabRow(items, selected = -1, itemWidth, tint = colors.onSurfaceVariant)

            // The lens, centred on its tab and grown by the swell and the speed.
            Box(
                Modifier.placeAt { val f = frame(); Placement(f.x, f.y, f.w, f.h) }
                    .graphicsLayer {
                        shadowElevation = 10.dp.toPx() * swell.value
                        shape = Pill
                        clip = false
                    }
                    .drawWithContent {
                        drawContent()
                        val s = swell.value
                        val outline = Pill.createOutline(size, layoutDirection, this)
                        val rim =
                            Brush.verticalGradient(
                                0f to Color.White.copy(alpha = if (dark) 0.5f else 0.95f),
                                0.5f to Color.White.copy(alpha = 0.15f + 0.2f * s),
                                1f to Color.White.copy(alpha = if (dark) 0.3f else 0.6f),
                            )
                        drawOutline(outline, rim, style = Stroke((1f + 0.6f * s).dp.toPx()))
                    }
                    .clip(Pill)
            ) {
                // What's behind the bar: frosted at rest, clear and magnified while swollen.
                Box(
                    Modifier.fillMaxSize()
                        .graphicsLayer {
                            val f = frame()
                            backShader.configure(f)
                            val blur = (10.dp * (1f - f.swell) + 4.dp * f.swell).toPx()
                            val bubble = RenderEffect.createRuntimeShaderEffect(backShader, "content")
                            renderEffect =
                                (if (blur > 0.5f) {
                                        RenderEffect.createChainEffect(
                                            bubble,
                                            RenderEffect.createBlurEffect(blur, blur, Shader.TileMode.CLAMP),
                                        )
                                    } else bubble)
                                    .asComposeRenderEffect()
                        }
                        .drawBehind {
                            val f = frame()
                            drawRect(colors.surfaceContainer)
                            translate(backdrop.origin.x - rowOrigin.x - f.x, backdrop.origin.y - rowOrigin.y - f.y) {
                                drawLayer(backdrop.layer)
                            }
                        }
                )
                // The frost and the accent tint fade as the droplet clears.
                Box(
                    Modifier.fillMaxSize().drawBehind {
                        val s = swell.value
                        drawRect(frost.copy(alpha = (if (dark) 0.5f else 0.55f) * (1f - s) + 0.22f * s))
                        drawRect(accent.copy(alpha = 0.10f * (1f - s)))
                    }
                )
                // The tabs again, in the accent colour, magnified with the backdrop.
                Box(
                    Modifier.fillMaxSize().graphicsLayer {
                        frontShader.configure(frame())
                        renderEffect =
                            RenderEffect.createRuntimeShaderEffect(frontShader, "content")
                                .asComposeRenderEffect()
                    }
                ) {
                    Box(Modifier.placeAt { val f = frame(); Placement(-f.x, -f.y, totalPx, rowPx) }) {
                        TabRow(items, selected = selected, itemWidth, tint = colors.primary)
                    }
                }
            }

            // Touch: press, slide, release.
            Box(
                Modifier.fillMaxSize().pointerInput(items.size, itemPx) {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        pressing = true
                        moves[1]?.cancel()
                        moves[1] = scope.launch { swell.animateTo(1f, SwellIn) }
                        var fingerX = down.position.x
                        var over = (fingerX / itemPx).toInt().coerceIn(0, items.lastIndex)
                        val slack = itemPx * 0.2f
                        fun target(fx: Float) =
                            (fx - itemPx / 2f).coerceIn(-slack, itemPx * items.lastIndex + slack)
                        // Only start following once the finger actually slides, so a tap glides
                        // straight to its tab instead of lunging at the finger first.
                        var sliding = false
                        var aim = 0f
                        val finished = try {
                        drag(down.id) { change ->
                            fingerX = change.position.x
                            if (!sliding && abs(fingerX - down.position.x) > viewConfiguration.touchSlop) {
                                sliding = true
                                moves[0]?.cancel()
                                // One loop for the whole drag: each frame the lens closes most of the
                                // gap to the finger, so it never stalls and never jitters.
                                moves[0] =
                                    scope.launch {
                                        lens.stop()
                                        var last = 0L
                                        while (isActive) {
                                            val now = withFrameNanos { it }
                                            val dt = if (last == 0L) 8f else (now - last) / 1_000_000f
                                            last = now
                                            val from = lens.value
                                            val next = from + (aim - from) * (1f - exp(-dt / FOLLOW_LAG_MS))
                                            lens.snapTo(next)
                                            followSpeed = (next - from) / dt * 1000f
                                        }
                                    }
                            }
                            aim = target(fingerX)
                            if (sliding) {
                                val now = (fingerX / itemPx).toInt().coerceIn(0, items.lastIndex)
                                if (now != over) {
                                    over = now
                                    haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                }
                            }
                            change.consume()
                        }
                        } catch (e: CancellationException) {
                            // The gesture was torn down mid-press: put the lens back on its tab.
                            pressing = false
                            glideTo(aimed)
                            throw e
                        }
                        pressing = false
                        if (!finished) {
                            // Another handler took the pointer: nothing was chosen.
                            glideTo(aimed)
                            return@awaitEachGesture
                        }
                        val chosen = (fingerX / itemPx).toInt().coerceIn(0, items.lastIndex)
                        if (chosen != current) {
                            haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        }
                        glideTo(chosen)
                        select(chosen)
                    }
                }
            )
        }
    }
}

@Composable
private fun TabRow(items: List<TabItem>, selected: Int, itemWidth: Dp, tint: Color) {
    Row(Modifier.fillMaxSize()) {
        items.forEachIndexed { index, item ->
            Column(
                Modifier.width(itemWidth).fillMaxHeight().semantics {
                    role = Role.Tab
                    this.selected = index == selected
                },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    painterResource(item.icon),
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(24.dp),
                )
                Text(
                    stringResource(item.label),
                    color = tint,
                    fontSize = 11.sp,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                )
            }
        }
    }
}

private class Placement(val x: Float, val y: Float, val w: Float, val h: Float)

/** Places this where [at] says, taking no space in the parent. [at] is read during layout only. */
private fun Modifier.placeAt(at: () -> Placement): Modifier = layout { m, _ ->
    val p = at()
    val placeable =
        m.measure(Constraints.fixed(p.w.roundToInt().coerceAtLeast(1), p.h.roundToInt().coerceAtLeast(1)))
    layout(0, 0) { placeable.place(p.x.roundToInt(), p.y.roundToInt()) }
}

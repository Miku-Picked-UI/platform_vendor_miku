/*
 * Copyright (C) 2026 The PenguinOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.penguin.laboratory.ui

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.graphics.Shader
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** A tab in the floating bar. */
class TabItem(val label: Int, val icon: Int)

/** The content under the tab bar, recorded each frame for the glass to draw. */
@Stable
class Backdrop(val layer: GraphicsLayer) {
    var origin by mutableStateOf(Offset.Zero)
}

@Composable
fun rememberBackdrop(): Backdrop {
    val layer = rememberGraphicsLayer()
    return remember(layer) { Backdrop(layer) }
}

/** Records this content into [backdrop] while still drawing it as usual. */
fun Modifier.backdropSource(backdrop: Backdrop): Modifier =
    onGloballyPositioned { backdrop.origin = it.positionInRoot() }
        .drawWithContent {
            backdrop.layer.record { this@drawWithContent.drawContent() }
            drawLayer(backdrop.layer)
        }

// Pulls what's under the rim of a rounded shape in towards the middle, as the edge of a lens does.
private const val LENS =
    """
uniform shader content;
uniform float2 size;
uniform float radius;
uniform float depth;
half4 main(float2 p) {
    float2 c = size * 0.5;
    float2 q = abs(p - c) - (c - radius);
    float d = length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - radius;
    float t = clamp(1.0 + d / depth, 0.0, 1.0);
    float2 dir = p - c;
    dir = dir / max(length(dir), 1.0);
    return content.eval(p - dir * t * t * depth * 0.55);
}
"""

private val BarShape = RoundedCornerShape(percent = 50)
internal val BarHeight = 64.dp

/** A Liquid Glass surface over [backdrop] when [glass] is on, else a floating toolbar. */
@Composable
fun GlassSurface(
    glass: Boolean,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    shape: Shape = BarShape,
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val dark = colors.surface.luminance() < 0.5f
    val density = LocalDensity.current
    var origin by remember { mutableStateOf(Offset.Zero) }
    val lens = remember { RuntimeShader(LENS) }
    Box(
        modifier
            .shadow(
                if (glass) 14.dp else 6.dp,
                shape,
                // The tab bar's lens swells past the edge while pressed.
                clip = false,
                ambientColor = Color.Black.copy(alpha = if (dark) 0.5f else 0.18f),
                spotColor = Color.Black.copy(alpha = if (dark) 0.5f else 0.18f),
            )
            .onGloballyPositioned { origin = it.positionInRoot() }
    ) {
        if (glass) {
            Box(
                Modifier.matchParentSize()
                    .graphicsLayer {
                        clip = true
                        this.shape = shape
                        lens.setFloatUniform("size", size.width, size.height)
                        lens.setFloatUniform("radius", size.height / 2f)
                        lens.setFloatUniform("depth", with(density) { 14.dp.toPx() })
                        val blur = with(density) { 10.dp.toPx() }
                        renderEffect =
                            RenderEffect.createChainEffect(
                                    RenderEffect.createRuntimeShaderEffect(lens, "content"),
                                    RenderEffect.createBlurEffect(blur, blur, Shader.TileMode.CLAMP),
                                )
                                .asComposeRenderEffect()
                    }
                    .drawBehind {
                        drawRect(colors.surfaceContainer)
                        translate(backdrop.origin.x - origin.x, backdrop.origin.y - origin.y) {
                            drawLayer(backdrop.layer)
                        }
                    }
            )
            // A light wash, then the rim: brightest along the top where the light falls.
            Box(
                Modifier.matchParentSize()
                    .background(
                        if (dark) colors.surfaceContainerHigh.copy(alpha = 0.55f)
                        else Color.White.copy(alpha = 0.42f),
                        shape,
                    )
                    .drawWithCache {
                        val outline = shape.createOutline(size, layoutDirection, this)
                        val rim =
                            Brush.verticalGradient(
                                0f to Color.White.copy(alpha = if (dark) 0.42f else 0.95f),
                                0.5f to Color.White.copy(alpha = if (dark) 0.08f else 0.25f),
                                1f to Color.White.copy(alpha = if (dark) 0.2f else 0.6f),
                            )
                        val stroke = Stroke(width = 1.2.dp.toPx())
                        onDrawBehind { drawOutline(outline, rim, style = stroke) }
                    }
            )
        } else {
            Box(Modifier.matchParentSize().background(colors.surfaceContainerHigh, shape))
        }
        content()
    }
}

/**
 * The Lab's floating tab bar. While [collapsed] only the current tab shows. [pagePosition] is the
 * fractional page while the user swipes the pages, else null.
 */
@Composable
fun FloatingTabBar(
    items: List<TabItem>,
    selected: Int,
    onSelect: (Int) -> Unit,
    glass: Boolean,
    collapsed: Boolean,
    onExpand: () -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    pagePosition: (() -> Float)? = null,
) {
    AnimatedContent(
        targetState = collapsed,
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        contentAlignment = Alignment.BottomStart,
        transitionSpec = {
            (fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) +
                scaleIn(spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMediumLow), 0.8f))
                .togetherWith(fadeOut() + scaleOut(targetScale = 0.8f))
                .using(SizeTransform(clip = false))
        },
        label = "tabbar",
    ) { small ->
        if (small) {
            MiniBar(items[selected], glass, backdrop, onExpand)
        } else if (glass) {
            GlassTabBar(items, selected, onSelect, backdrop, pagePosition = pagePosition)
        } else {
            MaterialBar(items, selected, onSelect, backdrop)
        }
    }
}

@Composable
private fun MiniBar(item: TabItem, glass: Boolean, backdrop: Backdrop, onExpand: () -> Unit) {
    val label = stringResource(item.label)
    GlassSurface(
        glass,
        backdrop,
        Modifier.height(56.dp).clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClickLabel = stringResource(com.penguin.laboratory.R.string.tab_expand),
            onClick = onExpand,
        ),
    ) {
        Row(
            Modifier.fillMaxHeight().padding(start = 16.dp, end = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painterResource(item.icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun MaterialBar(
    items: List<TabItem>,
    selected: Int,
    onSelect: (Int) -> Unit,
    backdrop: Backdrop,
) {
    val haptics = LocalHapticFeedback.current
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        GlassSurface(false, backdrop, Modifier.height(BarHeight)) {
            Row(
                Modifier.fillMaxHeight().padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                items.forEachIndexed { index, item ->
                    val chosen = index == selected
                    Row(
                        Modifier.height(48.dp)
                            .background(
                                if (chosen) MaterialTheme.colorScheme.secondaryContainer
                                else Color.Transparent,
                                BarShape,
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) {
                                if (!chosen) haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                onSelect(index)
                            }
                            .semantics {
                                role = Role.Tab
                                this.selected = chosen
                            }
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val tint =
                            if (chosen) MaterialTheme.colorScheme.onSecondaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        Icon(painterResource(item.icon), contentDescription = stringResource(item.label), tint = tint)
                        AnimatedVisibility(
                            chosen,
                            enter = expandHorizontally() + fadeIn(),
                            exit = shrinkHorizontally() + fadeOut(),
                        ) {
                            Text(
                                stringResource(item.label),
                                color = tint,
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(start = 8.dp),
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }
    }
}

/** How far the bar reaches up from the bottom, for the content's bottom padding. */
val TabBarClearance = BarHeight + 28.dp


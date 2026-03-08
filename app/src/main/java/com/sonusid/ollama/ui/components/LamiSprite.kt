package com.sonusid.ollama.ui.components

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.annotation.DrawableRes
import com.sonusid.ollama.R
import com.sonusid.ollama.data.SpriteSheetConfig
import com.sonusid.ollama.data.normalize
import com.sonusid.ollama.sprite.SpriteSheetData
import com.sonusid.ollama.sprite.SpriteSheetFrameRegion
import com.sonusid.ollama.sprite.SpriteSheetLoadResult
import com.sonusid.ollama.sprite.rememberLamiSpriteSheetState
import com.sonusid.ollama.viewmodels.LamiState
import com.sonusid.ollama.ui.components.mapToLamiSpriteStatus
import com.sonusid.ollama.ui.components.rememberSpriteFrameMaps
import kotlin.math.roundToInt

private val DefaultSpriteSheetConfig = SpriteSheetConfig.default3x3()

@Composable
fun LamiSprite(
    spriteRes: Int,
    frameIndex: Int,
    frameWidth: Int,
    frameHeight: Int,
    columns: Int,
    modifier: Modifier = Modifier,
) {
    val bitmap = rememberSpriteSheet(spriteRes)
    val resolvedFrameWidth = frameWidth.takeIf { it > 0 } ?: bitmap.width
    val resolvedFrameHeight = frameHeight.takeIf { it > 0 } ?: bitmap.height

    val cols = columns.coerceAtLeast(1)
    val safeFrame = frameIndex.coerceAtLeast(0)
    val col = safeFrame % cols
    val row = safeFrame / cols

    val srcX = col * resolvedFrameWidth
    val srcY = row * resolvedFrameHeight

    Canvas(modifier = modifier) {
        val dstW = size.width.roundToInt().coerceAtLeast(1)
        val dstH = size.height.roundToInt().coerceAtLeast(1)
        val side = dstW.coerceAtMost(dstH)
        val offsetX = (dstW - side) / 2
        val offsetY = (dstH - side) / 2

        drawIntoCanvas { canvas ->
            canvas.drawImageRect(
                image = bitmap,
                srcOffset = IntOffset(srcX, srcY),
                srcSize = IntSize(resolvedFrameWidth, resolvedFrameHeight),
                dstOffset = IntOffset(offsetX, offsetY),
                dstSize = IntSize(side, side),
                paint = Paint()
            )
        }
    }
}

@Composable
fun LamiSprite3x3(
    frameIndex: Int,
    sizeDp: Dp = 48.dp,
    modifier: Modifier = Modifier,
    contentOffsetDp: Dp = 0.dp,
    contentOffsetYDp: Dp = 0.dp,
    frameXOffsetPxMap: Map<Int, Int> = emptyMap(),
    frameYOffsetPxMap: Map<Int, Int> = emptyMap(),
    frameSrcOffsetMap: Map<Int, IntOffset> = emptyMap(),
    frameSrcSizeMap: Map<Int, IntSize> = emptyMap(),
    autoCropTransparentArea: Boolean = false,
    frameSizePx: IntSize? = null,
    frameMaps: LamiSpriteFrameMaps? = null,
    spriteSheetConfig: SpriteSheetConfig = DefaultSpriteSheetConfig,
) {
    val normalizedConfig = remember(spriteSheetConfig) {
        spriteSheetConfig.normalize(DefaultSpriteSheetConfig)
    }
    val spriteSheetState by rememberLamiSpriteSheetState(normalizedConfig)
    val spriteSheetData: SpriteSheetData? = (spriteSheetState as? SpriteSheetLoadResult.Success)?.data
    val safeFrameIndexBound = (normalizedConfig.frameCount - 1).coerceAtLeast(0)
    val safeFrameIndex = frameIndex.coerceIn(0, safeFrameIndexBound)
    val sheetFrameRegion: SpriteSheetFrameRegion? = remember(spriteSheetData, safeFrameIndex, normalizedConfig) {
        spriteSheetData?.frameRegion(frameIndex = safeFrameIndex)
    }
    val bitmap = spriteSheetData?.imageBitmap ?: return
    val defaultFrameSize = frameSizePx
        ?: frameMaps?.frameSize
        ?: sheetFrameRegion?.srcSize
        ?: IntSize(width = normalizedConfig.frameWidth, height = normalizedConfig.frameHeight)
    val baseOffset = frameMaps?.offsetMap?.get(safeFrameIndex)
        ?: frameSrcOffsetMap[safeFrameIndex]
        ?: sheetFrameRegion?.srcOffset
        ?: IntOffset.Zero
    val baseSize = frameMaps?.sizeMap?.get(safeFrameIndex)
        ?: frameSrcSizeMap[safeFrameIndex]
        ?: sheetFrameRegion?.srcSize
        ?: defaultFrameSize
    if (baseSize.width <= 0 || baseSize.height <= 0) {
        return
    }
    val srcOffset = if (autoCropTransparentArea) {
        val srcOffsetAdjustment = frameSrcOffsetMap[safeFrameIndex] ?: IntOffset.Zero
        IntOffset(
            x = baseOffset.x + srcOffsetAdjustment.x,
            y = baseOffset.y + srcOffsetAdjustment.y,
        )
    } else {
        baseOffset
    }
    val srcSize = if (autoCropTransparentArea) {
        frameSrcSizeMap[safeFrameIndex] ?: baseSize
    } else {
        baseSize
    }
    val frameRegion = remember(sheetFrameRegion, srcOffset, srcSize) {
        SpriteFrameRegion(
            srcOffset = srcOffset,
            srcSize = srcSize,
        )
    }

    val dstSize = with(LocalDensity.current) {
        val sizePx = sizeDp.roundToPx().coerceAtLeast(1)
        IntSize(sizePx, sizePx)
    }

    val dstOffset = with(LocalDensity.current) {
        val baseOffsetX = contentOffsetDp.roundToPx()
        val baseOffsetY = contentOffsetYDp.roundToPx()
        val frameXOffsetPx = frameXOffsetPxMap[safeFrameIndex] ?: 0
        val frameYOffsetPx = frameYOffsetPxMap[safeFrameIndex] ?: 0
        val scaleX = dstSize.width.toFloat() / srcSize.width
        val scaleY = dstSize.height.toFloat() / srcSize.height
        val resolvedXOffset = baseOffsetX + (frameXOffsetPx * scaleX).roundToInt()
        val resolvedYOffset = baseOffsetY + (frameYOffsetPx * scaleY).roundToInt()
        if (frameXOffsetPx != 0 || frameYOffsetPx != 0) {
            Log.d(
                "SpriteRuntime",
                "frame=$safeFrameIndex frameXOffsetPx=$frameXOffsetPx srcOffsetX=${srcOffset.x} dstOffsetX=$resolvedXOffset"
            )
        }
        IntOffset(x = resolvedXOffset, y = resolvedYOffset)
    }

    Canvas(modifier = modifier.size(sizeDp)) {
        drawFrameRegion(
            sheet = bitmap,
            region = frameRegion,
            dstSize = dstSize,
            dstOffset = dstOffset,
            placeholder = { offset, size -> drawFramePlaceholder(offset, size) },
        )
    }
}

@Immutable
data class LamiSpriteFrameMaps(
    val offsetMap: Map<Int, IntOffset>,
    val sizeMap: Map<Int, IntSize>,
    val frameSize: IntSize,
    val columns: Int,
)

fun LamiSpriteFrameMaps.toFrameXOffsetPxMap(): Map<Int, Int> {
    val frameWidth = frameSize.width.coerceAtLeast(1)
    val safeColumns = columns.coerceAtLeast(1)
    return offsetMap.mapValues { (index, offset) ->
        val colLeft = (index % safeColumns) * frameWidth
        offset.x - colLeft
    }
}

fun LamiSpriteFrameMaps.toFrameYOffsetPxMap(): Map<Int, Int> {
    val frameHeight = frameSize.height.coerceAtLeast(1)
    val safeColumns = columns.coerceAtLeast(1)
    return offsetMap.mapValues { (index, offset) ->
        val rowTop = (index / safeColumns) * frameHeight
        offset.y - rowTop
    }
}

@Composable
private fun rememberSpriteSheet(@DrawableRes resId: Int): ImageBitmap {
    val context = LocalContext.current
    return remember(resId) {
        BitmapFactory
            .decodeResource(context.resources, resId)
            .asImageBitmap()
    }
}

@Composable
fun LamiSprite(
    state: LamiState,
    sizeDp: Dp,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp),
    animationsEnabled: Boolean = true,
    replacementEnabled: Boolean = true,
    blinkEffectEnabled: Boolean = true,
) {
    val backgroundColor = when (state) {
        is LamiState.Thinking -> MaterialTheme.colorScheme.secondaryContainer
        is LamiState.Speaking -> MaterialTheme.colorScheme.tertiaryContainer
        LamiState.Idle -> MaterialTheme.colorScheme.primaryContainer
    }
    val spriteStatus = mapToLamiSpriteStatus(lamiState = state)

    val contentPadding = 6.dp
    val spriteSize = sizeDp - (contentPadding * 2)

    Box(
        modifier = modifier
            .size(sizeDp)
            .background(backgroundColor, shape)
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        LamiStatusSprite(
            status = spriteStatus,
            sizeDp = spriteSize,
            modifier = Modifier.clip(shape),
            animationsEnabled = animationsEnabled,
            replacementEnabled = replacementEnabled,
            blinkEffectEnabled = blinkEffectEnabled,
        )
    }
}

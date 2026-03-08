package com.sonusid.ollama.ui.components

import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Paint as AndroidPaint
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt

data class SpriteFrameRegion(
    val srcOffset: IntOffset,
    val srcSize: IntSize,
) {
    fun estimatedByteSize(): Long = srcSize.width.toLong() * srcSize.height.toLong() * 4L
}

fun ContentScale.toDstRect(srcSize: IntSize, canvasSize: Size): Pair<IntSize, IntOffset> {
    val safeSrcWidth = srcSize.width.coerceAtLeast(1)
    val safeSrcHeight = srcSize.height.coerceAtLeast(1)
    val scale = computeScaleFactor(
        srcSize = Size(safeSrcWidth.toFloat(), safeSrcHeight.toFloat()),
        dstSize = canvasSize,
    )
    val dstWidth = (srcSize.width * scale.scaleX).roundToInt().coerceAtLeast(1)
    val dstHeight = (srcSize.height * scale.scaleY).roundToInt().coerceAtLeast(1)
    val offsetX = ((canvasSize.width - dstWidth) / 2f).roundToInt()
    val offsetY = ((canvasSize.height - dstHeight) / 2f).roundToInt()
    return IntSize(width = dstWidth, height = dstHeight) to IntOffset(x = offsetX, y = offsetY)
}

fun DrawScope.drawFramePlaceholder(
    offset: IntOffset,
    size: IntSize,
    color: Color = Color.LightGray.copy(alpha = 0.35f),
    borderColor: Color = Color.Gray.copy(alpha = 0.65f),
) {
    drawRect(
        color = color,
        topLeft = androidx.compose.ui.geometry.Offset(offset.x.toFloat(), offset.y.toFloat()),
        size = Size(size.width.toFloat(), size.height.toFloat()),
    )
    drawRect(
        color = borderColor,
        topLeft = androidx.compose.ui.geometry.Offset(offset.x.toFloat(), offset.y.toFloat()),
        size = Size(size.width.toFloat(), size.height.toFloat()),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f),
    )
}

fun DrawScope.drawFrameRegion(
    sheet: ImageBitmap?,
    region: SpriteFrameRegion?,
    dstSize: IntSize,
    dstOffset: IntOffset = IntOffset.Zero,
    alpha: Float = 1f,
    placeholder: (DrawScope.(IntOffset, IntSize) -> Unit)? = null,
): Boolean {
    val safeDstSize = IntSize(dstSize.width.coerceAtLeast(1), dstSize.height.coerceAtLeast(1))
    if (sheet == null || region == null || region.srcSize.width <= 0 || region.srcSize.height <= 0) {
        placeholder?.invoke(this, dstOffset, safeDstSize)
        return false
    }
    val srcWidth = region.srcSize.width.coerceIn(1, sheet.width)
    val srcHeight = region.srcSize.height.coerceIn(1, sheet.height)
    val maxOffsetX = (sheet.width - srcWidth).coerceAtLeast(0)
    val maxOffsetY = (sheet.height - srcHeight).coerceAtLeast(0)
    val srcOffset = IntOffset(
        x = region.srcOffset.x.coerceIn(0, maxOffsetX),
        y = region.srcOffset.y.coerceIn(0, maxOffsetY),
    )
    return runCatching<Unit> {
        val bitmap = sheet.asAndroidBitmap()
        val srcRect = Rect(
            srcOffset.x,
            srcOffset.y,
            srcOffset.x + srcWidth,
            srcOffset.y + srcHeight,
        )
        val dstRect = RectF(
            dstOffset.x.toFloat(),
            dstOffset.y.toFloat(),
            (dstOffset.x + safeDstSize.width).toFloat(),
            (dstOffset.y + safeDstSize.height).toFloat(),
        )
        val paint = AndroidPaint().apply {
            this.alpha = (alpha * 255f).roundToInt().coerceIn(0, 255)
            this.isFilterBitmap = false
        }
        drawIntoCanvas { canvas ->
            canvas.nativeCanvas.drawBitmap(bitmap, srcRect, dstRect, paint)
        }
    }.onFailure {
        placeholder?.invoke(this, dstOffset, safeDstSize)
    }.isSuccess
}

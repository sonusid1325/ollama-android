package com.sonusid.ollama.util

import android.graphics.Bitmap
import android.graphics.Point
import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

/**
 * スプライトの ROI を抽出してソーベル変換・2値化後の一致率を計算するユーティリティ。
 */
object SpriteAnalysis {
    data class GradientRoi(
        val width: Int,
        val height: Int,
        val mask: BooleanArray,
        val magnitude: FloatArray,
    )

    data class PixelOffset(val dx: Int, val dy: Int) {
        fun toPoint(centerX: Int, centerY: Int): Point = Point(centerX + dx, centerY + dy)
    }

    data class SpriteMatch(val score: Float, val offset: PixelOffset, val position: Point)

    data class SpriteAnalysisResult(
        val scores: List<Float>,
        val bestOffsets: List<PixelOffset>,
    )

    /**
        * ROI 中心座標とサイズを指定してグレースケール画像とソーベル勾配の強度を計算する。
        * 透明ピクセルは mask=false として無視する。
        */
    @VisibleForTesting
    fun prepareGradientRoi(
        bitmap: Bitmap,
        centerX: Int,
        centerY: Int,
        roiWidth: Int,
        roiHeight: Int,
    ): GradientRoi? {
        val width = roiWidth.coerceAtLeast(1)
        val height = roiHeight.coerceAtLeast(1)
        val halfW = width / 2
        val halfH = height / 2
        val startX = centerX - halfW
        val startY = centerY - halfH
        val endX = startX + width
        val endY = startY + height

        if (startX < 0 || startY < 0 || endX > bitmap.width || endY > bitmap.height) {
            return null
        }

        val grayscale = FloatArray(width * height)
        val mask = BooleanArray(width * height)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = bitmap.getPixel(startX + x, startY + y)
                val alpha = (pixel ushr 24) and 0xFF
                val index = y * width + x
                if (alpha != 0) {
                    val r = (pixel shr 16) and 0xFF
                    val g = (pixel shr 8) and 0xFF
                    val b = pixel and 0xFF
                    grayscale[index] = 0.299f * r + 0.587f * g + 0.114f * b
                    mask[index] = true
                }
            }
        }

        val magnitude = sobelMagnitude(width, height, grayscale, mask)
        return GradientRoi(
            width = width,
            height = height,
            mask = mask,
            magnitude = magnitude,
        )
    }

    private fun sobelMagnitude(
        width: Int,
        height: Int,
        grayscale: FloatArray,
        mask: BooleanArray,
    ): FloatArray {
        fun sample(x: Int, y: Int): Float {
            if (x !in 0 until width || y !in 0 until height) return 0f
            val index = y * width + x
            return if (mask[index]) grayscale[index] else 0f
        }

        val magnitude = FloatArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                val idx = y * width + x
                if (!mask[idx]) {
                    magnitude[idx] = 0f
                    continue
                }
                val gx = -sample(x - 1, y - 1) - 2 * sample(x - 1, y) - sample(x - 1, y + 1) +
                    sample(x + 1, y - 1) + 2 * sample(x + 1, y) + sample(x + 1, y + 1)
                val gy = sample(x - 1, y - 1) + 2 * sample(x, y - 1) + sample(x + 1, y - 1) -
                    sample(x - 1, y + 1) - 2 * sample(x, y + 1) - sample(x + 1, y + 1)
                magnitude[idx] = hypot(gx, gy)
            }
        }
        return magnitude
    }

    /**
        * 2 枚の ROI の一致率を IoU もしくは最小値/最大値比で算出する。
        * @param threshold 勾配強度の下限。未満の画素は 0 として扱う。
        * @param useIoU true: 2 値化した IoU、false: sum(min)/sum(max) を返す。
        */
    fun calculateMatchRate(
        reference: GradientRoi,
        target: GradientRoi,
        threshold: Float,
        useIoU: Boolean,
    ): Float {
        require(reference.width == target.width && reference.height == target.height) {
            "ROI size must match"
        }

        val size = reference.width * reference.height
        var intersection = 0
        var union = 0
        var sumMin = 0f
        var sumMax = 0f

        for (i in 0 until size) {
            val valid = reference.mask[i] && target.mask[i]
            if (!valid) continue
            val refMag = if (reference.magnitude[i] >= threshold) reference.magnitude[i] else 0f
            val tgtMag = if (target.magnitude[i] >= threshold) target.magnitude[i] else 0f
            if (useIoU) {
                val refEdge = refMag > 0f
                val tgtEdge = tgtMag > 0f
                if (refEdge || tgtEdge) union++
                if (refEdge && tgtEdge) intersection++
            } else {
                val maxVal = max(refMag, tgtMag)
                val minVal = min(refMag, tgtMag)
                sumMax += maxVal
                sumMin += minVal
            }
        }

        return if (useIoU) {
            if (union == 0) 0f else intersection.toFloat() / union.toFloat()
        } else {
            if (sumMax == 0f) 0f else sumMin / sumMax
        }
    }

    /**
        * 指定した中心座標の ROI を基準に、近傍の候補を探索して最良のスコアを返す。
        * @param searchRadius dx, dy の探索半径 [-r, r]。
        */
    suspend fun searchBestOffset(
        reference: Bitmap,
        target: Bitmap,
        centerX: Int,
        centerY: Int,
        roiWidth: Int,
        roiHeight: Int,
        searchRadius: Int,
        threshold: Float,
        useIoU: Boolean = true,
        dispatcher: CoroutineDispatcher = Dispatchers.Default,
    ): SpriteMatch? = withContext(dispatcher) {
        val baseRoi = prepareGradientRoi(reference, centerX, centerY, roiWidth, roiHeight) ?: return@withContext null
        var bestScore = Float.NEGATIVE_INFINITY
        var bestOffset = PixelOffset(0, 0)
        for (dy in -searchRadius..searchRadius) {
            for (dx in -searchRadius..searchRadius) {
                val candidate = prepareGradientRoi(target, centerX + dx, centerY + dy, roiWidth, roiHeight)
                    ?: continue
                val score = calculateMatchRate(baseRoi, candidate, threshold, useIoU)
                if (score > bestScore || (score == bestScore && (abs(dx) + abs(dy)) < (abs(bestOffset.dx) + abs(bestOffset.dy)))) {
                    bestScore = score
                    bestOffset = PixelOffset(dx, dy)
                }
            }
        }
        if (bestScore == Float.NEGATIVE_INFINITY) {
            null
        } else {
            SpriteMatch(score = bestScore, offset = bestOffset, position = bestOffset.toPoint(centerX, centerY))
        }
    }

    /**
        * 複数フレームの連続差分 (i, i+1) を評価し、スコアと最良オフセットをまとめて返す。
        */
    suspend fun analyzeConsecutiveFrames(
        frames: List<Bitmap>,
        centerX: Int,
        centerY: Int,
        roiWidth: Int,
        roiHeight: Int,
        searchRadius: Int,
        threshold: Float,
        useIoU: Boolean = true,
        dispatcher: CoroutineDispatcher = Dispatchers.Default,
    ): SpriteAnalysisResult = withContext(dispatcher) {
        if (frames.size < 2) {
            return@withContext SpriteAnalysisResult(scores = emptyList(), bestOffsets = emptyList())
        }

        val scores = mutableListOf<Float>()
        val offsets = mutableListOf<PixelOffset>()
        for (i in 0 until frames.lastIndex) {
            val result = searchBestOffset(
                reference = frames[i],
                target = frames[i + 1],
                centerX = centerX,
                centerY = centerY,
                roiWidth = roiWidth,
                roiHeight = roiHeight,
                searchRadius = searchRadius,
                threshold = threshold,
                useIoU = useIoU,
                dispatcher = dispatcher,
            )
            if (result != null) {
                scores += result.score
                offsets += result.offset
            } else {
                scores += 0f
                offsets += PixelOffset(0, 0)
            }
        }
        SpriteAnalysisResult(scores = scores, bestOffsets = offsets)
    }
}

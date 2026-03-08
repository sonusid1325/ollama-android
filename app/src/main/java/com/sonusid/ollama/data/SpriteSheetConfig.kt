package com.sonusid.ollama.data

import android.os.Parcelable
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.parcelize.Parcelize
import kotlin.math.roundToInt

@Parcelize
data class BoxPosition(
    val frameIndex: Int,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
) : Parcelable

@Parcelize
data class SpriteSheetConfig(
    val rows: Int,
    val cols: Int,
    val frameWidth: Int,
    val frameHeight: Int,
    val boxes: List<BoxPosition>,
    val insertionEnabled: Boolean = false,
) : Parcelable {
    val frameCount: Int = (rows * cols).coerceAtLeast(0)

    fun toJson(gson: Gson = Gson()): String = gson.toJson(this)

    fun validate(): String? {
        if (rows <= 0) return "rows は1以上にしてください"
        if (cols <= 0) return "cols は1以上にしてください"
        if (frameWidth <= 0 || frameHeight <= 0) {
            return "frameWidth と frameHeight は1以上の整数にしてください"
        }
        if (boxes.size != frameCount) {
            return "boxes の数は rows×cols (${rows * cols}) と一致させてください"
        }
        val frameIndexRangeMessage = "frameIndex は 1..${frameCount} または 0..${frameCount - 1} の範囲にしてください"
        val seenFrames = mutableSetOf<Int>()
        boxes.forEach { box ->
            if (box.width <= 0 || box.height <= 0) {
                return "各 box の width と height は1以上にしてください"
            }
            if (box.x < 0 || box.y < 0) {
                return "各 box の x, y は0以上にしてください"
            }
            val internalIndex = toInternalFrameIndex(box.frameIndex)
                ?: return frameIndexRangeMessage
            if (!seenFrames.add(internalIndex)) {
                return "frameIndex ${box.frameIndex} が重複しています"
            }
        }
        return null
    }

    companion object {
        val DEFAULT: SpriteSheetConfig = SpriteSheetConfig(
            rows = 3,
            cols = 3,
            frameWidth = 88,
            frameHeight = 88,
            boxes = listOf(
                BoxPosition(frameIndex = 0, height = 88, width = 88, x = 3, y = 9),
                BoxPosition(frameIndex = 1, height = 88, width = 88, x = 96, y = 9),
                BoxPosition(frameIndex = 2, height = 88, width = 88, x = 188, y = 9),
                BoxPosition(frameIndex = 3, height = 88, width = 88, x = 3, y = 103),
                BoxPosition(frameIndex = 4, height = 88, width = 88, x = 95, y = 103),
                BoxPosition(frameIndex = 5, height = 88, width = 88, x = 188, y = 103),
                BoxPosition(frameIndex = 6, height = 88, width = 88, x = 3, y = 196),
                BoxPosition(frameIndex = 7, height = 88, width = 88, x = 95, y = 196),
                BoxPosition(frameIndex = 8, height = 88, width = 88, x = 188, y = 196),
            ),
            insertionEnabled = false,
        )

        fun default3x3(frameSize: Int = DEFAULT.frameWidth): SpriteSheetConfig {
            if (frameSize == DEFAULT.frameWidth) return DEFAULT
            val scale = frameSize.toFloat() / DEFAULT.frameWidth.toFloat()
            val scaledBoxes = DEFAULT.boxes.map { box ->
                box.copy(
                    x = (box.x * scale).roundToInt(),
                    y = (box.y * scale).roundToInt(),
                    width = (box.width * scale).roundToInt().coerceAtLeast(1),
                    height = (box.height * scale).roundToInt().coerceAtLeast(1),
                )
            }
            return DEFAULT.copy(
                frameWidth = frameSize,
                frameHeight = frameSize,
                boxes = scaledBoxes,
                insertionEnabled = DEFAULT.insertionEnabled,
            )
        }

        fun fromJson(json: String, gson: Gson = Gson()): SpriteSheetConfig? {
            return try {
                gson.fromJson(json, SpriteSheetConfig::class.java)
            } catch (ex: JsonSyntaxException) {
                null
            }
        }
    }
}

fun SpriteSheetConfig.toInternalFrameIndex(frameIndex: Int): Int? {
    if (frameCount <= 0) return null
    return when {
        frameIndex in 0 until frameCount -> frameIndex
        frameIndex in 1..frameCount -> frameIndex - 1
        else -> null
    }
}

fun SpriteSheetConfig.boxesWithInternalIndex(): List<BoxPosition> {
    return boxes.mapNotNull { box ->
        val internalIndex = toInternalFrameIndex(box.frameIndex) ?: return@mapNotNull null
        box.copy(frameIndex = internalIndex)
    }
}

fun SpriteSheetConfig.isUninitialized(): Boolean {
    return frameWidth <= 0 || frameHeight <= 0 || boxes.isEmpty()
}

fun SpriteSheetConfig.toSpriteBoxes(): List<BoxPosition> = boxesWithInternalIndex()

fun SpriteSheetConfig.normalize(defaultConfig: SpriteSheetConfig = SpriteSheetConfig.default3x3()): SpriteSheetConfig {
    val validationError = validate()
    if (isUninitialized() || validationError != null) return defaultConfig
    return copy(boxes = boxesWithInternalIndex())
}

package com.sonusid.ollama.sprite

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.sonusid.ollama.R
import com.sonusid.ollama.data.SpriteSheetConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

private const val TAG = "LamiSpriteSheetRepo"

data class SpriteSheetFrameRegion(
    val srcOffset: IntOffset,
    val srcSize: IntSize,
)

data class SpriteSheetData(
    val bitmap: Bitmap,
    val imageBitmap: ImageBitmap,
    val rows: Int,
    val cols: Int,
) {
    val cellSize: IntSize = IntSize(
        width = (bitmap.width / cols).coerceAtLeast(1),
        height = (bitmap.height / rows).coerceAtLeast(1),
    )
    val frameCount: Int = (rows * cols).coerceAtLeast(0)

    fun matches(config: SpriteSheetConfig): Boolean {
        return rows == config.rows && cols == config.cols
    }

    fun frameRegion(frameIndex: Int, offsetX: Int = 0, offsetY: Int = 0): SpriteSheetFrameRegion? {
        return calculateFrameRegion(
            bitmapSize = IntSize(width = bitmap.width, height = bitmap.height),
            frameIndex = frameIndex,
            rows = rows,
            cols = cols,
            offsetX = offsetX,
            offsetY = offsetY,
        )
    }
}

sealed interface SpriteSheetLoadResult {
    object Idle : SpriteSheetLoadResult
    object Loading : SpriteSheetLoadResult
    data class Success(val data: SpriteSheetData) : SpriteSheetLoadResult
    data class Error(val message: String, val throwable: Throwable? = null) : SpriteSheetLoadResult
}

object LamiSpriteSheetRepository {
    private val loadMutex = Mutex()
    private val loadState = MutableStateFlow<SpriteSheetLoadResult>(SpriteSheetLoadResult.Idle)

    val state: StateFlow<SpriteSheetLoadResult> = loadState

    suspend fun loadLamiSpriteSheet(
        context: Context,
        config: SpriteSheetConfig = SpriteSheetConfig.default3x3(),
        forceReload: Boolean = false,
    ): SpriteSheetLoadResult {
        val current = loadState.value
        if (!forceReload && current is SpriteSheetLoadResult.Success && current.data.matches(config)) {
            return current
        }
        return loadMutex.withLock {
            val lockedCurrent = loadState.value
            if (!forceReload && lockedCurrent is SpriteSheetLoadResult.Success && lockedCurrent.data.matches(config)) {
                return@withLock lockedCurrent
            }
            loadState.value = SpriteSheetLoadResult.Loading
            val bitmap = decodeResource(context) ?: run {
                val errorMessage = "Failed to decode lami_sprite_3x3_288"
                Log.w(TAG, errorMessage)
                val errorResult = SpriteSheetLoadResult.Error(errorMessage)
                loadState.value = errorResult
                return@withLock errorResult
            }

            val cellWidth = (bitmap.width / config.cols).coerceAtLeast(0)
            val cellHeight = (bitmap.height / config.rows).coerceAtLeast(0)
            if (cellWidth <= 0 || cellHeight <= 0) {
                val errorMessage = "Invalid sprite sheet size: bitmap=${bitmap.width}x${bitmap.height}, rows=${config.rows}, cols=${config.cols}"
                Log.w(TAG, errorMessage)
                val errorResult = SpriteSheetLoadResult.Error(errorMessage)
                loadState.value = errorResult
                return@withLock errorResult
            }
            val imageBitmap = bitmap.asImageBitmap()
            val data = SpriteSheetData(
                bitmap = bitmap,
                imageBitmap = imageBitmap,
                rows = config.rows,
                cols = config.cols,
            )
            val successResult = SpriteSheetLoadResult.Success(data)
            loadState.value = successResult
            return@withLock successResult
        }
    }

    suspend fun reload(
        context: Context,
        config: SpriteSheetConfig = SpriteSheetConfig.default3x3(),
    ): SpriteSheetLoadResult {
        return loadLamiSpriteSheet(context = context, config = config, forceReload = true)
    }

    fun cachedData(): SpriteSheetData? = (loadState.value as? SpriteSheetLoadResult.Success)?.data

    private suspend fun decodeResource(context: Context): Bitmap? = withContext(Dispatchers.IO) {
        BitmapFactory.decodeResource(context.resources, R.drawable.lami_sprite_3x3_288)?.also { bitmap ->
            if (bitmap.width <= 0 || bitmap.height <= 0) {
                Log.w(TAG, "Decoded bitmap is empty: ${bitmap.width}x${bitmap.height}")
                return@withContext null
            }
            Log.d(TAG, "Loaded sprite sheet lami_sprite_3x3_288 (${bitmap.width}x${bitmap.height})")
        }
    }
}

fun calculateFrameRegion(
    bitmapSize: IntSize,
    frameIndex: Int,
    rows: Int,
    cols: Int,
    offsetX: Int = 0,
    offsetY: Int = 0,
): SpriteSheetFrameRegion? {
    val frameCount = rows * cols
    if (rows <= 0 || cols <= 0 || bitmapSize.width <= 0 || bitmapSize.height <= 0) {
        Log.w(TAG, "Invalid frame region arguments: rows=$rows, cols=$cols, size=$bitmapSize")
        return null
    }
    if (frameIndex !in 0 until frameCount) {
        Log.w(TAG, "frameIndex out of range: $frameIndex / $frameCount")
        return null
    }
    val cellWidth = (bitmapSize.width / cols).coerceAtLeast(1)
    val cellHeight = (bitmapSize.height / rows).coerceAtLeast(1)
    val col = frameIndex % cols
    val row = frameIndex / cols
    val baseOffset = IntOffset(x = col * cellWidth, y = row * cellHeight)
    val safeOffset = IntOffset(x = baseOffset.x + offsetX, y = baseOffset.y + offsetY)
    return SpriteSheetFrameRegion(
        srcOffset = safeOffset,
        srcSize = IntSize(width = cellWidth, height = cellHeight),
    )
}

@Composable
fun rememberLamiSpriteSheetState(
    config: SpriteSheetConfig = SpriteSheetConfig.default3x3(),
): State<SpriteSheetLoadResult> {
    val context = LocalContext.current
    val stateFlow = remember { LamiSpriteSheetRepository.state }
    LaunchedEffect(context, config) {
        LamiSpriteSheetRepository.loadLamiSpriteSheet(context = context, config = config)
    }
    return stateFlow.collectAsState()
}

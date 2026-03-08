package com.sonusid.ollama.ui.components
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.sonusid.ollama.data.BoxPosition
import com.sonusid.ollama.data.SpriteSheetConfig
import com.sonusid.ollama.data.boxesWithInternalIndex
import com.sonusid.ollama.data.normalize
import com.sonusid.ollama.data.toInternalFrameIndex
import com.sonusid.ollama.data.toSpriteBoxes
import com.sonusid.ollama.ui.screens.settings.SettingsPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class SpriteFrameRepository(
    private val settingsPreferences: SettingsPreferences,
) {
    private val defaultConfig = SpriteSheetConfig.default3x3()
    private val defaultFrameMaps = defaultConfig
        .normalize(defaultConfig)
        .toSpriteBoxes()
        .toFrameMaps(defaultConfig)

    val spriteSheetConfig: Flow<SpriteSheetConfig> = settingsPreferences.spriteSheetConfig
        .map { config -> config.normalize(defaultConfig) }
        .onStart { emit(defaultConfig) }
        .catch { emit(defaultConfig) }

    val frameMaps: Flow<LamiSpriteFrameMaps> = spriteSheetConfig
        .map { config ->
            config.toSpriteBoxes().toFrameMaps(config)
        }
        .onStart { emit(defaultFrameMaps) }
        .catch { emit(defaultFrameMaps) }

    suspend fun latestFrameMaps(): LamiSpriteFrameMaps = frameMaps.first()
    suspend fun latestConfig(): SpriteSheetConfig = spriteSheetConfig.first()
}

@Composable
fun rememberSpriteFrameRepository(): SpriteFrameRepository {
    val context = LocalContext.current
    return remember {
        SpriteFrameRepository(
            settingsPreferences = SettingsPreferences(context.applicationContext),
        )
    }
}

@Composable
fun rememberSpriteFrameMaps(repository: SpriteFrameRepository = rememberSpriteFrameRepository()): LamiSpriteFrameMaps {
    val defaultConfig = remember { SpriteSheetConfig.default3x3() }
    val defaultMaps = remember {
        defaultConfig
            .normalize(defaultConfig)
            .toSpriteBoxes()
            .toFrameMaps(defaultConfig)
    }
    val frameMaps by repository.frameMaps.collectAsState(initial = defaultMaps)
    return frameMaps
}

fun List<BoxPosition>.toFrameMaps(config: SpriteSheetConfig): LamiSpriteFrameMaps {
    val frameWidth = config.frameWidth.coerceAtLeast(1)
    val frameHeight = config.frameHeight.coerceAtLeast(1)
    val columns = config.cols.coerceAtLeast(1)
    val offsetMap = mutableMapOf<Int, IntOffset>()
    val sizeMap = mutableMapOf<Int, IntSize>()
    val boxesWithInternalIndex = if (isNotEmpty()) {
        mapNotNull { box ->
            val internalIndex = config.toInternalFrameIndex(box.frameIndex) ?: return@mapNotNull null
            box.copy(frameIndex = internalIndex)
        }
    } else {
        config.boxesWithInternalIndex()
    }
    boxesWithInternalIndex.forEach { box ->
        offsetMap[box.frameIndex] = IntOffset(x = box.x.coerceAtLeast(0), y = box.y.coerceAtLeast(0))
        sizeMap[box.frameIndex] = IntSize(width = box.width.coerceAtLeast(1), height = box.height.coerceAtLeast(1))
    }
    return LamiSpriteFrameMaps(
        offsetMap = offsetMap,
        sizeMap = sizeMap,
        frameSize = IntSize(width = frameWidth, height = frameHeight),
        columns = columns,
    )
}

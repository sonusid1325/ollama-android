package com.sonusid.ollama.ui.screens.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.sonusid.ollama.data.SpriteSheetConfig
import com.sonusid.ollama.data.normalize
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.random.Random

private const val SETTINGS_DATA_STORE_NAME = "ollama_settings"
private val Context.dataStore by preferencesDataStore(
    name = SETTINGS_DATA_STORE_NAME
)

data class ReadyAnimationSettings(
    val frameSequence: List<Int>,
    val intervalMs: Int,
) {
    companion object {
        val DEFAULT = ReadyAnimationSettings(
            frameSequence = listOf(0, 1, 2, 1),
            intervalMs = 700,
        )

        const val MIN_INTERVAL_MS: Int = 50
        const val MAX_INTERVAL_MS: Int = 5_000
    }
}

data class InsertionAnimationSettings(
    val enabled: Boolean,
    val frameSequence: List<Int>,
    val intervalMs: Int,
    val everyNLoops: Int,
    val probabilityPercent: Int,
    val cooldownLoops: Int,
    val exclusive: Boolean,
) {
    companion object {
        val DEFAULT = InsertionAnimationSettings(
            enabled = false,
            frameSequence = listOf(3, 4, 5),
            intervalMs = 200,
            everyNLoops = 1,
            probabilityPercent = 50,
            cooldownLoops = 0,
            exclusive = false,
        )

        const val MIN_INTERVAL_MS: Int = ReadyAnimationSettings.MIN_INTERVAL_MS
        const val MAX_INTERVAL_MS: Int = ReadyAnimationSettings.MAX_INTERVAL_MS
        const val MIN_EVERY_N_LOOPS: Int = 1
        const val MIN_PROBABILITY_PERCENT: Int = 0
        const val MAX_PROBABILITY_PERCENT: Int = 100
        const val MIN_COOLDOWN_LOOPS: Int = 0
    }
}

class SettingsPreferences(private val context: Context) {

    private val dynamicColorKey = booleanPreferencesKey("dynamic_color_enabled")
    private val spriteSheetConfigKey = stringPreferencesKey("sprite_sheet_config")
    private val readyFrameSequenceKey = stringPreferencesKey("ready_frame_sequence")
    private val readyIntervalMsKey = intPreferencesKey("ready_interval_ms")
    private val readyInsertionEnabledKey = booleanPreferencesKey("insertion_enabled")
    private val readyInsertionFrameSequenceKey = stringPreferencesKey("insertion_frame_sequence")
    private val readyInsertionIntervalMsKey = intPreferencesKey("insertion_interval_ms")
    private val readyInsertionEveryNLoopsKey = intPreferencesKey("insertion_every_n_loops")
    private val readyInsertionProbabilityKey = intPreferencesKey("insertion_probability_percent")
    private val readyInsertionCooldownLoopsKey = intPreferencesKey("insertion_cooldown_loops")
    private val readyInsertionExclusiveKey = booleanPreferencesKey("insertion_exclusive")
    private val talkingFrameSequenceKey = stringPreferencesKey("talking_frame_sequence")
    private val talkingIntervalMsKey = intPreferencesKey("talking_interval_ms")
    private val talkingInsertionEnabledKey = booleanPreferencesKey("talking_insertion_enabled")
    private val talkingInsertionFrameSequenceKey = stringPreferencesKey("talking_insertion_frame_sequence")
    private val talkingInsertionIntervalMsKey = intPreferencesKey("talking_insertion_interval_ms")
    private val talkingInsertionEveryNLoopsKey = intPreferencesKey("talking_insertion_every_n_loops")
    private val talkingInsertionProbabilityKey = intPreferencesKey("talking_insertion_probability_percent")
    private val talkingInsertionCooldownLoopsKey = intPreferencesKey("talking_insertion_cooldown_loops")
    private val talkingInsertionExclusiveKey = booleanPreferencesKey("talking_insertion_exclusive")

    val settingsData: Flow<SettingsData> = context.dataStore.data.map { preferences ->
        SettingsData(
            useDynamicColor = preferences[dynamicColorKey] ?: false
        )
    }

    val spriteSheetConfig: Flow<SpriteSheetConfig> = context.dataStore.data.map { preferences ->
        val json = preferences[spriteSheetConfigKey]
        val parsed = json?.let { SpriteSheetConfig.fromJson(it) }
        parsed?.normalize(SpriteSheetConfig.default3x3()) ?: SpriteSheetConfig.default3x3()
    }

    val readyAnimationSettings: Flow<ReadyAnimationSettings> = context.dataStore.data.map { preferences ->
        val frameSequenceString = preferences[readyFrameSequenceKey]
        val parsedFrames = frameSequenceString
            ?.split(",")
            ?.mapNotNull { value -> value.trim().toIntOrNull() }
            ?.filter { it in 0..8 }
            .orEmpty()
            .ifEmpty { ReadyAnimationSettings.DEFAULT.frameSequence }

        val intervalMs = preferences[readyIntervalMsKey]
            ?.coerceIn(ReadyAnimationSettings.MIN_INTERVAL_MS, ReadyAnimationSettings.MAX_INTERVAL_MS)
            ?: ReadyAnimationSettings.DEFAULT.intervalMs

        ReadyAnimationSettings(
            frameSequence = parsedFrames,
            intervalMs = intervalMs,
        )
    }

    val talkingAnimationSettings: Flow<ReadyAnimationSettings> = context.dataStore.data.map { preferences ->
        val frameSequenceString = preferences[talkingFrameSequenceKey]
        val parsedFrames = frameSequenceString
            ?.split(",")
            ?.mapNotNull { value -> value.trim().toIntOrNull() }
            ?.filter { it in 0 until SpriteSheetConfig.default3x3().frameCount }
            .orEmpty()
            .ifEmpty { ReadyAnimationSettings.DEFAULT.frameSequence }

        val intervalMs = preferences[talkingIntervalMsKey]
            ?.coerceIn(ReadyAnimationSettings.MIN_INTERVAL_MS, ReadyAnimationSettings.MAX_INTERVAL_MS)
            ?: ReadyAnimationSettings.DEFAULT.intervalMs

        ReadyAnimationSettings(
            frameSequence = parsedFrames,
            intervalMs = intervalMs,
        )
    }

    val readyInsertionAnimationSettings: Flow<InsertionAnimationSettings> = context.dataStore.data.map { preferences ->
        val frameSequenceString = preferences[readyInsertionFrameSequenceKey]
        val parsedFrames = frameSequenceString
            ?.split(",")
            ?.mapNotNull { value -> value.trim().toIntOrNull() }
            ?.filter { it in 0 until SpriteSheetConfig.default3x3().frameCount }
            .orEmpty()
            .ifEmpty { InsertionAnimationSettings.DEFAULT.frameSequence }

        val intervalMs = preferences[readyInsertionIntervalMsKey]
            ?.coerceIn(InsertionAnimationSettings.MIN_INTERVAL_MS, InsertionAnimationSettings.MAX_INTERVAL_MS)
            ?: InsertionAnimationSettings.DEFAULT.intervalMs

        val everyNLoops = preferences[readyInsertionEveryNLoopsKey]
            ?.coerceAtLeast(InsertionAnimationSettings.MIN_EVERY_N_LOOPS)
            ?: InsertionAnimationSettings.DEFAULT.everyNLoops

        val probabilityPercent = preferences[readyInsertionProbabilityKey]
            ?.coerceIn(
                InsertionAnimationSettings.MIN_PROBABILITY_PERCENT,
                InsertionAnimationSettings.MAX_PROBABILITY_PERCENT
            )
            ?: InsertionAnimationSettings.DEFAULT.probabilityPercent

        val cooldownLoops = preferences[readyInsertionCooldownLoopsKey]
            ?.coerceAtLeast(InsertionAnimationSettings.MIN_COOLDOWN_LOOPS)
            ?: InsertionAnimationSettings.DEFAULT.cooldownLoops

        val enabled = preferences[readyInsertionEnabledKey] ?: InsertionAnimationSettings.DEFAULT.enabled
        val exclusive = preferences[readyInsertionExclusiveKey] ?: InsertionAnimationSettings.DEFAULT.exclusive

        InsertionAnimationSettings(
            enabled = enabled,
            frameSequence = parsedFrames,
            intervalMs = intervalMs,
            everyNLoops = everyNLoops,
            probabilityPercent = probabilityPercent,
            cooldownLoops = cooldownLoops,
            exclusive = exclusive,
        )
    }

    val talkingInsertionAnimationSettings: Flow<InsertionAnimationSettings> = context.dataStore.data.map { preferences ->
        val frameSequenceString = preferences[talkingInsertionFrameSequenceKey]
        val parsedFrames = frameSequenceString
            ?.split(",")
            ?.mapNotNull { value -> value.trim().toIntOrNull() }
            ?.filter { it in 0 until SpriteSheetConfig.default3x3().frameCount }
            .orEmpty()
            .ifEmpty { InsertionAnimationSettings.DEFAULT.frameSequence }

        val intervalMs = preferences[talkingInsertionIntervalMsKey]
            ?.coerceIn(InsertionAnimationSettings.MIN_INTERVAL_MS, InsertionAnimationSettings.MAX_INTERVAL_MS)
            ?: InsertionAnimationSettings.DEFAULT.intervalMs

        val everyNLoops = preferences[talkingInsertionEveryNLoopsKey]
            ?.coerceAtLeast(InsertionAnimationSettings.MIN_EVERY_N_LOOPS)
            ?: InsertionAnimationSettings.DEFAULT.everyNLoops

        val probabilityPercent = preferences[talkingInsertionProbabilityKey]
            ?.coerceIn(
                InsertionAnimationSettings.MIN_PROBABILITY_PERCENT,
                InsertionAnimationSettings.MAX_PROBABILITY_PERCENT
            )
            ?: InsertionAnimationSettings.DEFAULT.probabilityPercent

        val cooldownLoops = preferences[talkingInsertionCooldownLoopsKey]
            ?.coerceAtLeast(InsertionAnimationSettings.MIN_COOLDOWN_LOOPS)
            ?: InsertionAnimationSettings.DEFAULT.cooldownLoops

        val enabled = preferences[talkingInsertionEnabledKey] ?: InsertionAnimationSettings.DEFAULT.enabled
        val exclusive = preferences[talkingInsertionExclusiveKey] ?: InsertionAnimationSettings.DEFAULT.exclusive

        InsertionAnimationSettings(
            enabled = enabled,
            frameSequence = parsedFrames,
            intervalMs = intervalMs,
            everyNLoops = everyNLoops,
            probabilityPercent = probabilityPercent,
            cooldownLoops = cooldownLoops,
            exclusive = exclusive,
        )
    }

    suspend fun updateDynamicColor(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[dynamicColorKey] = enabled
        }
    }

    suspend fun saveSpriteSheetConfig(config: SpriteSheetConfig) {
        context.dataStore.edit { preferences ->
            preferences[spriteSheetConfigKey] = config.toJson()
        }
    }

    suspend fun resetSpriteSheetConfig() {
        saveSpriteSheetConfig(SpriteSheetConfig.default3x3())
    }

    suspend fun saveReadyAnimationSettings(settings: ReadyAnimationSettings) {
        context.dataStore.edit { preferences ->
            preferences[readyFrameSequenceKey] = settings.frameSequence.joinToString(separator = ",")
            preferences[readyIntervalMsKey] = settings.intervalMs
        }
    }

    suspend fun saveTalkingAnimationSettings(settings: ReadyAnimationSettings) {
        context.dataStore.edit { preferences ->
            preferences[talkingFrameSequenceKey] = settings.frameSequence.joinToString(separator = ",")
            preferences[talkingIntervalMsKey] = settings.intervalMs
        }
    }

    suspend fun saveReadyInsertionAnimationSettings(settings: InsertionAnimationSettings) {
        context.dataStore.edit { preferences ->
            preferences[readyInsertionEnabledKey] = settings.enabled
            preferences[readyInsertionFrameSequenceKey] = settings.frameSequence.joinToString(separator = ",")
            preferences[readyInsertionIntervalMsKey] = settings.intervalMs
            preferences[readyInsertionEveryNLoopsKey] = settings.everyNLoops
            preferences[readyInsertionProbabilityKey] = settings.probabilityPercent
            preferences[readyInsertionCooldownLoopsKey] = settings.cooldownLoops
            preferences[readyInsertionExclusiveKey] = settings.exclusive
        }
    }

    suspend fun saveTalkingInsertionAnimationSettings(settings: InsertionAnimationSettings) {
        context.dataStore.edit { preferences ->
            preferences[talkingInsertionEnabledKey] = settings.enabled
            preferences[talkingInsertionFrameSequenceKey] = settings.frameSequence.joinToString(separator = ",")
            preferences[talkingInsertionIntervalMsKey] = settings.intervalMs
            preferences[talkingInsertionEveryNLoopsKey] = settings.everyNLoops
            preferences[talkingInsertionProbabilityKey] = settings.probabilityPercent
            preferences[talkingInsertionCooldownLoopsKey] = settings.cooldownLoops
            preferences[talkingInsertionExclusiveKey] = settings.exclusive
        }
    }
}

/**
 * TODO: ランタイムの挿入判定に統合する。
 * この関数は設定値に基づき「挿入を試行してよいか」を返す。
 */
fun InsertionAnimationSettings.shouldAttemptInsertion(
    loopCount: Int,
    lastInsertionLoop: Int?,
    isReadyPlaying: Boolean,
    random: Random = Random(System.currentTimeMillis()),
): Boolean {
    if (!enabled) return false
    if (exclusive && isReadyPlaying) return false
    val hasCooldown = cooldownLoops > 0 && lastInsertionLoop != null
    if (hasCooldown && (loopCount - lastInsertionLoop) < cooldownLoops) return false
    if (everyNLoops <= 0) return false
    if (loopCount % everyNLoops != 0) return false
    if (probabilityPercent <= 0) return false
    val normalizedProbability = probabilityPercent / 100f
    return random.nextFloat() < normalizedProbability.coerceIn(0f, 1f)
}

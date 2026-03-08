package com.sonusid.ollama.viewmodels

import com.sonusid.ollama.UiState
import com.sonusid.ollama.viewmodels.LamiStatus.CONNECTING
import com.sonusid.ollama.viewmodels.LamiStatus.DEGRADED
import com.sonusid.ollama.viewmodels.LamiStatus.ERROR
import com.sonusid.ollama.viewmodels.LamiStatus.NO_MODELS
import com.sonusid.ollama.viewmodels.LamiStatus.OFFLINE
import com.sonusid.ollama.viewmodels.LamiStatus.READY
import com.sonusid.ollama.viewmodels.LamiStatus.TALKING

sealed interface LamiState {
    data object Idle : LamiState
    data object Thinking : LamiState
    data class Speaking(val textLength: Int) : LamiState
}

data class LamiUiState(
    val state: LamiState = LamiState.Idle,
    val lastInteractionTimeMs: Long = System.currentTimeMillis(),
)

fun bucket(len: Int): Int {
    return when {
        len <= 0 -> 0
        len in 1..30 -> 1
        len in 31..120 -> 2
        else -> 3
    }
}

fun mapToLamiState(uiState: UiState, selectedModel: String?): LamiState {
    if (selectedModel.isNullOrBlank()) {
        return LamiState.Idle
    }
    return when (uiState) {
        UiState.Loading -> LamiState.Thinking
        is UiState.Error -> LamiState.Idle
        is UiState.Success -> LamiState.Idle
        is UiState.ModelsLoaded -> LamiState.Idle
        UiState.Initial -> LamiState.Idle
    }
}

fun mapToLamiState(
    lamiStatus: LamiStatus,
    selectedModel: String?,
    lastError: String?,
): LamiState {
    if (selectedModel.isNullOrBlank()) {
        return LamiState.Idle
    }

    return when (lamiStatus) {
        TALKING -> LamiState.Thinking
        CONNECTING -> LamiState.Thinking
        READY -> LamiState.Idle
        DEGRADED -> LamiState.Idle
        NO_MODELS -> LamiState.Idle
        OFFLINE -> if (lastError.isNullOrBlank()) {
            LamiState.Idle
        } else {
            LamiState.Idle
        }
        ERROR -> LamiState.Idle
    }
}

package com.sonusid.ollama.viewmodels

import com.sonusid.ollama.UiState
import org.junit.Assert.assertEquals
import org.junit.Test

class LamiAnimationStatusMapperTest {

    @Test
    fun `prioritizes talking when speaking state`() {
        val result = mapToAnimationLamiStatus(
            lamiState = LamiState.Speaking(12),
            uiState = UiState.Loading,
            selectedModel = "llama3",
        )

        assertEquals(LamiStatus.TALKING, result)
    }

    @Test
    fun `returns connecting when loading`() {
        val result = mapToAnimationLamiStatus(
            lamiState = LamiState.Thinking,
            uiState = UiState.Loading,
            selectedModel = "llama3",
        )

        assertEquals(LamiStatus.CONNECTING, result)
    }

    @Test
    fun `returns error when uiState has error`() {
        val result = mapToAnimationLamiStatus(
            lamiState = LamiState.Idle,
            uiState = UiState.Error("network"),
            selectedModel = "llama3",
        )

        assertEquals(LamiStatus.ERROR, result)
    }

    @Test
    fun `returns no models when selection missing`() {
        val result = mapToAnimationLamiStatus(
            lamiState = LamiState.Idle,
            uiState = UiState.Success("ok"),
            selectedModel = null,
        )

        assertEquals(LamiStatus.NO_MODELS, result)
    }

    @Test
    fun `returns ready on normal path`() {
        val result = mapToAnimationLamiStatus(
            lamiState = LamiState.Idle,
            uiState = UiState.Success("ok"),
            selectedModel = "llama3",
        )

        assertEquals(LamiStatus.READY, result)
    }
}

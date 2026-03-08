package com.sonusid.ollama.viewmodels

import org.junit.Assert.assertEquals
import org.junit.Test

class LamiStatusMapperTest {

    @Test
    fun `talking has highest priority`() {
        val state = GatewayStatusState(isConnecting = true, lastError = "network", isTtsPlaying = true)

        val result = mapToLamiStatus(state)

        assertEquals(LamiStatus.TALKING, result)
    }

    @Test
    fun `connecting is chosen before error`() {
        val state = GatewayStatusState(isConnecting = true, lastError = "anything")

        val result = mapToLamiStatus(state)

        assertEquals(LamiStatus.CONNECTING, result)
    }

    @Test
    fun `error is returned when not connecting but error exists`() {
        val state = GatewayStatusState(isOnline = true, lastError = "timeout")

        val result = mapToLamiStatus(state)

        assertEquals(LamiStatus.ERROR, result)
    }

    @Test
    fun `offline is returned when not online`() {
        val state = GatewayStatusState()

        val result = mapToLamiStatus(state)

        assertEquals(LamiStatus.OFFLINE, result)
    }

    @Test
    fun `no models when online but empty list`() {
        val state = GatewayStatusState(isOnline = true, hasModels = false)

        val result = mapToLamiStatus(state)

        assertEquals(LamiStatus.NO_MODELS, result)
    }

    @Test
    fun `degraded when fallback used with models`() {
        val state = GatewayStatusState(isOnline = true, hasModels = true, usedFallback = true)

        val result = mapToLamiStatus(state)

        assertEquals(LamiStatus.DEGRADED, result)
    }

    @Test
    fun `ready when online with models and no fallback`() {
        val state = GatewayStatusState(isOnline = true, hasModels = true)

        val result = mapToLamiStatus(state)

        assertEquals(LamiStatus.READY, result)
    }
}

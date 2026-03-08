package com.sonusid.ollama.viewmodels

data class GatewayStatusState(
    val isConnecting: Boolean = false,
    val isOnline: Boolean = false,
    val hasModels: Boolean = false,
    val usedFallback: Boolean = false,
    val lastError: String? = null,
    val isTtsPlaying: Boolean = false,
)

enum class LamiStatus {
    CONNECTING,
    READY,
    DEGRADED,
    NO_MODELS,
    OFFLINE,
    ERROR,
    TALKING,
}

data class StatusRule(
    val status: LamiStatus,
    val predicate: (GatewayStatusState) -> Boolean,
)

object LamiStatusRules {
    val orderedRules: List<StatusRule> = listOf(
        StatusRule(LamiStatus.TALKING) { state -> state.isTtsPlaying },
        StatusRule(LamiStatus.CONNECTING) { state -> state.isConnecting },
        StatusRule(LamiStatus.ERROR) { state -> state.lastError != null && !state.isConnecting },
        StatusRule(LamiStatus.OFFLINE) { state -> !state.isOnline },
        StatusRule(LamiStatus.NO_MODELS) { state -> state.isOnline && !state.hasModels },
        StatusRule(LamiStatus.DEGRADED) { state -> state.isOnline && state.hasModels && state.usedFallback },
        StatusRule(LamiStatus.READY) { state -> state.isOnline && state.hasModels && !state.usedFallback },
    )
}

fun mapToLamiStatus(state: GatewayStatusState): LamiStatus {
    return LamiStatusRules.orderedRules.firstOrNull { rule -> rule.predicate(state) }?.status
        ?: LamiStatus.ERROR
}

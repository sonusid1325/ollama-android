package com.sonusid.ollama.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sonusid.ollama.viewmodels.LamiState
import com.sonusid.ollama.viewmodels.LamiStatus

data class LamiStatusUi(
    val title: String,
    val titleColor: Color,
    val subtitle: String?,
)

@Composable
fun LamiStatusPanel(
    status: LamiStatus,
    lamiState: LamiState,
    modifier: Modifier = Modifier,
    spriteSize: Dp = 56.dp,
) {
    val statusState = rememberUpdatedState(status)
    val statusUi = rememberLamiStatusUi(status, lamiState)

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        tonalElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            LamiStatusSprite(
                status = statusState,
                sizeDp = spriteSize
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = statusUi.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = statusUi.titleColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = statusUi.subtitle.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.alpha(if (statusUi.subtitle == null) 0f else 1f)
                )
            }
        }
    }
}

@Composable
fun rememberLamiStatusUi(
    status: LamiStatus,
    lamiState: LamiState,
): LamiStatusUi {
    val colorScheme = MaterialTheme.colorScheme

    val statusLabel = when (status) {
        LamiStatus.TALKING -> "Talking"
        LamiStatus.CONNECTING -> "Connecting"
        LamiStatus.READY -> "Ready"
        LamiStatus.DEGRADED -> "Degraded"
        LamiStatus.NO_MODELS -> "No models"
        LamiStatus.OFFLINE -> "Offline"
        LamiStatus.ERROR -> "Error"
    }

    val stateLabel = when (lamiState) {
        is LamiState.Speaking -> "Responding..."
        LamiState.Thinking -> "Thinking..."
        LamiState.Idle -> null
        else -> null
    }

    val title = stateLabel ?: statusLabel
    val titleColor = when {
        lamiState is LamiState.Speaking -> colorScheme.primary
        lamiState is LamiState.Thinking -> colorScheme.tertiary
        status == LamiStatus.TALKING -> colorScheme.primary
        status == LamiStatus.CONNECTING -> colorScheme.tertiary
        status == LamiStatus.READY -> colorScheme.secondary
        status == LamiStatus.DEGRADED -> colorScheme.tertiary
        status == LamiStatus.NO_MODELS -> colorScheme.error
        status == LamiStatus.OFFLINE -> colorScheme.error
        status == LamiStatus.ERROR -> colorScheme.error
        else -> colorScheme.onSurface
    }

    val subtitle = statusLabel.takeIf { it != title }

    return LamiStatusUi(
        title = title,
        titleColor = titleColor,
        subtitle = subtitle.takeIf { it != title }
    )
}

@Preview
@Composable
fun LamiStatusPanelIdlePreview() {
    LamiStatusPanel(
        status = LamiStatus.READY,
        lamiState = LamiState.Idle
    )
}

@Preview
@Composable
fun LamiStatusPanelThinkingPreview() {
    LamiStatusPanel(
        status = LamiStatus.CONNECTING,
        lamiState = LamiState.Thinking
    )
}

@Preview
@Composable
fun LamiStatusPanelTalkingPreview() {
    LamiStatusPanel(
        status = LamiStatus.TALKING,
        lamiState = LamiState.Speaking(textLength = 42)
    )
}

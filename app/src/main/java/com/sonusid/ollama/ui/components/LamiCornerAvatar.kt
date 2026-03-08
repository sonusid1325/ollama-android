package com.sonusid.ollama.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BoxScope.LamiCornerAvatar(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    avatarContent: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .align(Alignment.BottomStart)
            .padding(contentPadding)
    ) {
        avatarContent()
    }
}

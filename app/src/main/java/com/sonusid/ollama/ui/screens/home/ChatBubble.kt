package com.sonusid.ollama.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.jeziellago.compose.markdowntext.MarkdownText


@Composable
fun ChatBubble(
    message: String,
    isSentByMe: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        horizontalArrangement = if (isSentByMe) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 250.dp)
                .background(
                    color = if (isSentByMe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(12.dp)
        ) {
            MarkdownText(
                message
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatPreview() {
    MaterialTheme(colorScheme = darkColorScheme()){
        Scaffold { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
                ChatBubble("Heyy", isSentByMe = true)
                ChatBubble("**Heyy**", isSentByMe = false)
            }
        }
    }
}
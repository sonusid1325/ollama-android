package com.sonusid.ollama.ui.screens.settings

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.text.AnnotatedString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun copyJsonToClipboard(
    clipboardManager: ClipboardManager,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    json: String,
    message: String = "クリップボードへコピーしました",
) {
    clipboardManager.setText(AnnotatedString(json))
    scope.launch { snackbarHostState.showSnackbar(message) }
}

fun pasteJsonFromClipboard(
    clipboardManager: ClipboardManager,
    onPaste: (String) -> Unit,
    onEmpty: () -> Unit,
) {
    val clipText = clipboardManager.getText()?.text
    if (clipText.isNullOrBlank()) {
        onEmpty()
    } else {
        onPaste(clipText)
    }
}

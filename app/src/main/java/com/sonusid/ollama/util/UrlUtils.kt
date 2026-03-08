package com.sonusid.ollama.util

import java.net.MalformedURLException
import java.net.URL
import java.text.Normalizer

const val PORT_ERROR_MESSAGE = "URLは http://host:port 形式で入力してください。半角数字でポートを入力してください"

fun normalizeUrlInput(input: String): String {
    return Normalizer.normalize(input.trim(), Normalizer.Form.NFKC)
}

data class UrlValidationResult(
    val normalizedUrl: String,
    val isValid: Boolean,
    val errorMessage: String? = null
)

fun validateUrlFormat(urlString: String): UrlValidationResult {
    val normalized = normalizeUrlInput(urlString)
    if (normalized.isBlank()) {
        return UrlValidationResult(normalized, false, PORT_ERROR_MESSAGE)
    }

    return try {
        val portMatch = Regex("^https?://[^/:]+:([^/]+)", RegexOption.IGNORE_CASE).find(urlString.trim())
        val originalPort = portMatch?.groupValues?.getOrNull(1)
        if (originalPort != null && originalPort.any { !it.isDigit() }) {
            return UrlValidationResult(normalized, false, PORT_ERROR_MESSAGE)
        }

        val url = URL(normalized)
        val isValid = url.protocol in listOf("http", "https") && url.host.isNotBlank()
        UrlValidationResult(normalized, isValid, errorMessage = if (isValid) null else PORT_ERROR_MESSAGE)
    } catch (e: MalformedURLException) {
        UrlValidationResult(normalized, false, PORT_ERROR_MESSAGE)
    } catch (e: IllegalArgumentException) {
        UrlValidationResult(normalized, false, PORT_ERROR_MESSAGE)
    }
}

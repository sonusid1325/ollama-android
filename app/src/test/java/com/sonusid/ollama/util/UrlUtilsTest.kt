package com.sonusid.ollama.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UrlUtilsTest {
    @Test
    fun `normalizeUrlInput trims whitespace and normalizes unicode`() {
        val input = "  http://Example.com：11434/　"

        val normalized = normalizeUrlInput(input)

        assertEquals("http://Example.com:11434/", normalized)
    }

    @Test
    fun `validateUrlFormat accepts http and https with host`() {
        val httpResult = validateUrlFormat("http://example.com:11434")
        val httpsResult = validateUrlFormat("https://example.com")

        assertTrue(httpResult.isValid)
        assertNull(httpResult.errorMessage)
        assertEquals("http://example.com:11434", httpResult.normalizedUrl)
        assertTrue(httpsResult.isValid)
        assertNull(httpsResult.errorMessage)
        assertEquals("https://example.com", httpsResult.normalizedUrl)
    }

    @Test
    fun `validateUrlFormat rejects blank or incomplete urls`() {
        val blankResult = validateUrlFormat("   ")
        val missingSchemeResult = validateUrlFormat("example.com:11434")

        assertFalse(blankResult.isValid)
        assertEquals(PORT_ERROR_MESSAGE, blankResult.errorMessage)
        assertFalse(missingSchemeResult.isValid)
        assertEquals(PORT_ERROR_MESSAGE, missingSchemeResult.errorMessage)
    }

    @Test
    fun `validateUrlFormat rejects urls with mixed width port numbers`() {
        val result = validateUrlFormat("http://localhost:1３434")

        assertFalse(result.isValid)
        assertEquals(PORT_ERROR_MESSAGE, result.errorMessage)
    }
}

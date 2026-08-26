package com.caloriecalc.app.data.remote

import org.junit.Assert.assertEquals
import org.junit.Test

class GeminiApiTest {

    @Test
    fun `uses an available vision model`() {
        assertEquals("gemini-2.5-flash", GeminiApi.DEFAULT_MODEL)
    }
}

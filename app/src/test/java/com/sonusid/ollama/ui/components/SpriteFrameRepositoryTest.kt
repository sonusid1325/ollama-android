package com.sonusid.ollama.ui.components

import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.sonusid.ollama.data.SpriteSheetConfig
import com.sonusid.ollama.ui.screens.debug.SpriteBox
import org.junit.Assert.assertEquals
import org.junit.Test

class SpriteFrameRepositoryTest {

    @Test
    fun `frame y offset is derived from rect top`() {
        val config = SpriteSheetConfig.default3x3()
        val boxes = listOf(
            SpriteBox(index = 0, x = 0f, y = 0f, width = 90f, height = 90f),
            SpriteBox(index = 1, x = 96f, y = 6f, width = 88f, height = 90f),
            SpriteBox(index = 2, x = 192f, y = 0f, width = 86f, height = 88f),
            SpriteBox(index = 3, x = 0f, y = 98f, width = 90f, height = 90f),
        )
        val frameMaps = boxes.toFrameMaps(config)

        assertEquals(IntSize(96, 96), frameMaps.frameSize)
        assertEquals(3, frameMaps.columns)
        assertEquals(IntOffset(96, 6), frameMaps.offsetMap[1])

        val yOffsetMap = frameMaps.toFrameYOffsetPxMap()
        assertEquals(0, yOffsetMap[0])
        assertEquals(6, yOffsetMap[1])
        assertEquals(2, yOffsetMap[3])
    }
}

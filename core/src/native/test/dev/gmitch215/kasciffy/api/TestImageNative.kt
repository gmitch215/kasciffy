package dev.gmitch215.kasciffy.api

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestImageNative {

    @Test
    fun testFile() = runTest {
        val image = Image("src/common/test-resources/gmitch215.png")
        assertTrue { image.width > 0 }
        assertTrue { image.height > 0 }
        assertTrue { image.rgbValues.isNotEmpty() }

        val asciffy = asciffy0(image, TEN_NUMERIC, 1)
        assertFalse { asciffy.isEmpty() }
    }

    @Test
    fun testTransformed() = runTest {
        val image = Image("src/common/test-resources/gmitch215.png")
        assertTrue { image.width > 0 }
        assertTrue { image.height > 0 }
        assertTrue { image.rgbValues.isNotEmpty() }

        val transformed = image.asciffy(TEN_NUMERIC, 1)
        assertEquals(image.name, transformed.name)
        assertEquals(image.extension, transformed.extension)
        assertEquals(image.creationDate, transformed.creationDate)
    }

}
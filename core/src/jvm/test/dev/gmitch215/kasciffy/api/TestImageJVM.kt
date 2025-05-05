package dev.gmitch215.kasciffy.api

import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestImageJVM {

    @Test
    fun testFile() = runTest {
        val image = Image(javaClass.getResourceAsStream("/gmitch215.png")!!)
        assertNotNull(image.bufferedImage)

        val asciffy = asciffy0(image, TEN_NUMERIC, 1)
        assertFalse { asciffy.isEmpty() }
    }

    @Test
    fun testTransform() = runTest {
        val image = Image(javaClass.getResourceAsStream("/gmitch215.png")!!)
        assertNotNull(image.bufferedImage)

        val transformed = image.asciffy(TEN_NUMERIC, 1)
        assertEquals(image.name, transformed.name)
        assertEquals(image.extension, transformed.extension)
        assertEquals(image.creationDate, transformed.creationDate)
    }

}
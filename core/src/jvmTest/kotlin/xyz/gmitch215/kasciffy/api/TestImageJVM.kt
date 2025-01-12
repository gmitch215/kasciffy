package xyz.gmitch215.kasciffy.api

import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.jvm.javaClass
import kotlin.test.assertNotNull

class TestImageJVM {

    @Test
    fun testFile() = runTest {
        val image = Image(javaClass.getResourceAsStream("/gmitch215.png")!!)
        assertNotNull(image.bufferedImage)

        val asciffy = asciffy(image, TEN_NUMERIC, 1)
    }

}
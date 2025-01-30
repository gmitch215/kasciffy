package dev.gmitch215.kasciffy.api

import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertFalse

class TestAnimatedMediaJVM {

    @Test
    fun testFile() = runTest {
        val gif = AnimatedImage(javaClass.getResourceAsStream("/giorno.gif")!!)
        assertFalse { gif.frames.isEmpty() }
        assertFalse { gif.bufferedImages.isEmpty() }
        assertFalse { gif.fps == 0 }

        val frames = asciffy(gif, FIFTEEN, 6)
        assertFalse { frames.isEmpty() }
    }

}
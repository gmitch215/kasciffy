package dev.gmitch215.kasciffy.api

import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

class TestAnimatedMediaJs {

    @Test
    fun testFile() = runTest {
        val gif = AnimatedImage("${window.location.origin}/base/kotlin/giorno.gif").await()
        assertFalse { gif.frames.isEmpty() }
        assertFalse { gif.fps == 0 }

        val frames = asciffy0(gif, FIFTEEN, 6)
        assertFalse { frames.isEmpty() }
    }

//    @Test
//    fun testTransform() = runTest(timeout = 10.minutes) {
//        val image = AnimatedImage("${window.location.origin}/base/kotlin/giorno.gif").await()
//        assertTrue { image.toDataURL().await().isNotEmpty() }
//
//        val transformed = image.asciffy(TEN_NUMERIC).await()
//        assertTrue { transformed.toDataURL().await().isNotEmpty() }
//        assertEquals(image.name, transformed.name)
//        assertEquals(image.extension, transformed.extension)
//        assertEquals(image.creationDateDouble, transformed.creationDateDouble)
//    }

}
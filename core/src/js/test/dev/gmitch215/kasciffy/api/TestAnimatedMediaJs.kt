package dev.gmitch215.kasciffy.api

import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.time.Duration.Companion.minutes

class TestAnimatedMediaJs {

    @Test
    fun testFile() = runTest(timeout = 10.minutes) {
        val gif = AnimatedImage("${window.location.origin}/base/kotlin/giorno.gif").await()
        assertFalse { gif.frames.isEmpty() }
        assertFalse { gif.fps == 0 }

        val frames = asciffy0(gif, FIFTEEN, 6)
        assertFalse { frames.isEmpty() }
    }

}
package dev.gmitch215.kasciffy.api

import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

class TestImageJs {

    @Test
    fun testFile() = runTest(timeout = 10.minutes) {
        val image = Image("${window.location.origin}/base/kotlin/gmitch215.png").await()
        assertTrue { image.toDataURL().isNotEmpty() }

        val asciffy = asciffy0(image, TEN_NUMERIC, 1)
        assertFalse { asciffy.isEmpty() }
        println(asciffy)
    }

}
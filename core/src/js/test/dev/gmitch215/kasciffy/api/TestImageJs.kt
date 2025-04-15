package dev.gmitch215.kasciffy.api

import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestImageJs {

    @Suppress("HttpUrlsUsage")
    @Test
    fun testFile() = runTest {
        // FIXME: can't find resources
        val image = Image("http://${window.location.host}/src/common/test-resources/gmitch215.png").await()
        assertTrue { image.toDataURL().isNotEmpty() }

        val asciffy = asciffy0(image, TEN_NUMERIC, 1)
        assertFalse { asciffy.isEmpty() }
    }

}
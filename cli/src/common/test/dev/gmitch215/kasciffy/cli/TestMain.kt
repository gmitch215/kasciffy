package dev.gmitch215.kasciffy.cli

import kotlin.test.Test
import kotlin.test.assertFalse

class TestMain {

    @Test
    fun testHelp() {
        assertFalse { Kasciffy.HELP.isEmpty() }
    }

}
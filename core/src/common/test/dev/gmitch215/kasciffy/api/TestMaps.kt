package dev.gmitch215.kasciffy.api

import kotlin.test.Test
import kotlin.test.assertEquals

class TestMaps {

    @Test
    fun testMaps() {
        assertEquals(30, THIRTY.length)
        assertEquals(20, TWENTY.length)
        assertEquals(15, FIFTEEN.length)
        assertEquals(12, TWELVE.length)
        assertEquals(10, TEN.length)
        assertEquals(10, TEN_NUMERIC.length)
        assertEquals(5, FIVE.length)
        assertEquals(5, FIVE_BLOCK.length)
        assertEquals(4, FOUR_BLOCK.length)
        assertEquals(2, TWO.length)
        assertEquals(2, TWO_BLOCK.length)
    }

}
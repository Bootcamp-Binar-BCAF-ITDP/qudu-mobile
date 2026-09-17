package com.example.test2.ui.apply

import org.junit.Assert.assertEquals
import org.junit.Test

class FormatRupiahTest {

    @Test
    fun `thousands are grouped with dots, the Indonesian separator`() {
        assertEquals("Rp10.000.000", formatRupiah(10_000_000))
    }

    @Test
    fun `the prefix can be left off for an input field`() {
        assertEquals("1.234.567", formatRupiah(1_234_567, withPrefix = false))
    }

    @Test
    fun `an amount under a thousand gets no separator`() {
        assertEquals("Rp999", formatRupiah(999))
        assertEquals("Rp0", formatRupiah(0))
    }

    @Test
    fun `groups start from the right whatever the length`() {
        assertEquals("1.000", formatRupiah(1_000, withPrefix = false))
        assertEquals("10.000", formatRupiah(10_000, withPrefix = false))
        assertEquals("100.000", formatRupiah(100_000, withPrefix = false))
        assertEquals("1.200.000.000", formatRupiah(1_200_000_000, withPrefix = false))
    }

    @Test
    fun `a negative amount keeps its sign when the length happens to work out`() {
        assertEquals("-5.000", formatRupiah(-5_000, withPrefix = false))
    }

    @Test
    fun `a negative amount can get a separator straight after the minus sign`() {
        assertEquals("-.500", formatRupiah(-500, withPrefix = false))
    }
}

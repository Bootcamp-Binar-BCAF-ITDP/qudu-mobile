package com.example.test2.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LoanTierTest {

    private val tiers = PlafondTiers.FALLBACK
    private val bronze = tiers.first()

    @Test
    fun `a tier covers both of its bounds`() {
        assertTrue(bronze.covers(1_000_000))
        assertTrue(bronze.covers(10_000_000))
    }

    @Test
    fun `a tier does not cover one rupiah outside either bound`() {
        assertFalse(bronze.covers(999_999))
        assertFalse(bronze.covers(10_000_001))
    }

    @Test
    fun `a tenor below the minimum is raised to it`() {
        assertEquals(6, tiers[2].clampTenor(1))
    }

    @Test
    fun `a tenor above the maximum is lowered to it`() {
        assertEquals(12, bronze.clampTenor(60))
    }

    @Test
    fun `a tenor inside the range is left alone`() {
        assertEquals(7, bronze.clampTenor(7))
    }

    @Test
    fun `a whole rate is labelled without decimals`() {
        assertEquals("15%", bronze.rateLabel)
    }

    @Test
    fun `a fractional rate keeps its decimal`() {
        assertEquals("9.5%", tiers[3].rateLabel)
    }

    @Test
    fun `the built-in rates read cleanly except Titanium, where floating point adds a trailing zero`() {
        assertEquals(
            listOf("15%", "13%", "11%", "9.5%", "8.5%", "7.0%"),
            tiers.map { it.rateLabel },
        )
    }

    @Test
    fun `a rate with more than two decimals is rounded to two`() {
        assertEquals("12.35%", bronze.copy(annualRate = 0.123456).rateLabel)
    }

    @Test
    fun `the fallback ladder has six tiers in level order`() {
        assertEquals(listOf(1, 2, 3, 4, 5, 6), tiers.map { it.level })
    }

    @Test
    fun `the fallback ladder leaves no gap between tiers`() {
        tiers.zipWithNext().forEach { (lower, upper) ->
            assertEquals("gap after ${lower.name}", lower.maxAmount + 1, upper.minAmount)
        }
    }

    @Test
    fun `the fallback rate falls as the tier rises`() {
        tiers.zipWithNext().forEach { (lower, upper) ->
            assertTrue(upper.annualRate < lower.annualRate)
        }
    }

    @Test
    fun `tierFor picks the tier that covers the amount`() {
        assertEquals("Silver", tiers.tierFor(25_000_000)?.name)
    }

    @Test
    fun `tierFor picks the upper tier exactly one rupiah past a boundary`() {
        assertEquals("Bronze", tiers.tierFor(10_000_000)?.name)
        assertEquals("Silver", tiers.tierFor(10_000_001)?.name)
    }

    @Test
    fun `tierFor snaps an amount below the ladder to the lowest tier`() {
        assertEquals("Bronze", tiers.tierFor(10)?.name)
    }

    @Test
    fun `tierFor snaps an amount above the ladder to the highest tier`() {
        assertEquals("Titanium", tiers.tierFor(5_000_000_000)?.name)
    }

    @Test
    fun `tierFor snaps into the nearest tier across a gap in a server ladder`() {
        val gappy = listOf(
            LoanTier(1, "A", 1_000_000, 5_000_000, 1, 12, 0.1, 0),
            LoanTier(2, "B", 20_000_000, 30_000_000, 1, 12, 0.1, 0),
        )

        assertEquals("A", gappy.tierFor(6_000_000)?.name)
        assertEquals("B", gappy.tierFor(19_000_000)?.name)
    }

    @Test
    fun `tierFor has nothing to offer on an empty ladder`() {
        assertNull(emptyList<LoanTier>().tierFor(1_000_000))
    }

    @Test
    fun `floor and ceiling come from the ladder`() {
        assertEquals(1_000_000L, tiers.floorAmount())
        assertEquals(1_200_000_000L, tiers.ceilingAmount())
    }

    @Test
    fun `floor and ceiling fall back to fixed values on an empty ladder`() {
        assertEquals(1_000_000L, emptyList<LoanTier>().floorAmount())
        assertEquals(1_200_000_000L, emptyList<LoanTier>().ceilingAmount())
    }

    @Test
    fun `the instalment follows the annuity formula, rounded up to the next thousand`() {
        assertEquals(903_000L, monthlyInstalmentFor(10_000_000, 12, 0.15))
    }

    @Test
    fun `the instalment is never rounded down, so the estimate does not undersell the cost`() {
        val exact = 10_000_000 * (0.15 / 12) / (1 - Math.pow(1 + 0.15 / 12, -12.0))

        assertTrue(monthlyInstalmentFor(10_000_000, 12, 0.15) >= exact)
    }

    @Test
    fun `an already round instalment is not bumped up another thousand`() {
        assertEquals(1_000_000L, monthlyInstalmentFor(12_000_000, 12, 0.0))
    }

    @Test
    fun `a zero rate splits the principal evenly`() {
        assertEquals(834_000L, monthlyInstalmentFor(10_000_000, 12, 0.0))
    }

    @Test
    fun `a longer tenor lowers the monthly instalment`() {
        assertTrue(monthlyInstalmentFor(10_000_000, 24, 0.15) < monthlyInstalmentFor(10_000_000, 12, 0.15))
    }

    @Test
    fun `no tenor or no principal means no instalment`() {
        assertEquals(0L, monthlyInstalmentFor(10_000_000, 0, 0.15))
        assertEquals(0L, monthlyInstalmentFor(10_000_000, -3, 0.15))
        assertEquals(0L, monthlyInstalmentFor(0, 12, 0.15))
        assertEquals(0L, monthlyInstalmentFor(-5, 12, 0.15))
    }

    @Test
    fun `every instalment is a whole thousand`() {
        listOf(1_234_567L, 9_999_999L, 50_000_001L).forEach { principal ->
            assertEquals(0L, monthlyInstalmentFor(principal, 7, 0.13) % 1000)
        }
    }
}

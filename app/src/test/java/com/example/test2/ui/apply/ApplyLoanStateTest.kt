package com.example.test2.ui.apply

import com.example.test2.core.PlafondTiers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ApplyLoanStateTest {

    private fun state(limit: Long? = null) = ApplyLoanState().apply {
        tiers = PlafondTiers.FALLBACK
        creditLimit = limit
    }


    @Test
    fun `typing above the credit limit is capped at the limit`() {
        val s = state(limit = 10_000_000L)

        s.updateLoanAmount(999_000_000L)

        assertEquals(10_000_000L, s.loanAmount)
    }

    @Test
    fun `the slider cannot travel past the credit limit either`() {
        val s = state(limit = 10_000_000L)

        assertEquals(10_000_000L, s.maxAmount)
    }

    @Test
    fun `without a known limit the tier ceiling applies`() {
        val s = state(limit = null)

        s.updateLoanAmount(99_999_999_999L)

        assertEquals(1_200_000_000L, s.loanAmount)
    }

    @Test
    fun `a limit below the smallest loan still leaves a usable range`() {
        val s = state(limit = 100_000L)

        assertTrue("max must not fall below min", s.maxAmount >= s.minAmount)
    }


    @Test
    fun `raising the amount into a new tier pulls the tenor into its window`() {
        val s = state()
        s.updateLoanAmount(25_000_000L)
        s.updateTermMonths(24)

        s.updateLoanAmount(300_000_000L)

        assertEquals("Silver's 24 months is legal on Diamond, so it stays", 24, s.termMonths)
        assertEquals(0.085, s.annualRate, 0.0001)
    }

    @Test
    fun `a tenor typed above the tier maximum is capped`() {
        val s = state()
        s.updateLoanAmount(5_000_000L)

        s.updateTermMonths(72)

        assertEquals(12, s.termMonths)
    }

    @Test
    fun `dropping to a shorter tier shortens an over-long tenor`() {
        val s = state()
        s.updateLoanAmount(300_000_000L)
        s.updateTermMonths(60)

        s.updateLoanAmount(5_000_000L)

        assertEquals(12, s.termMonths)
    }


    private fun complete() = state(limit = 100_000_000L).apply {
        updateLoanAmount(25_000_000L)
        updateTermMonths(12)
        purpose = "Education"
        monthlyIncome = "8.500.000"
        bank = "BCA"
        bankAccountNumber = "1234567890"
        bankAccountName = "Budi"
    }

    @Test
    fun `a fully filled form has no errors`() {
        assertFalse(complete().detailErrors().hasAny)
    }

    @Test
    fun `every mandatory field is reported when blank`() {
        val errors = state().detailErrors()

        assertNotNull("purpose", errors.purpose)
        assertNotNull("income", errors.income)
        assertNotNull("bank", errors.bank)
        assertNotNull("account number", errors.accountNumber)
        assertNotNull("account holder", errors.accountName)
    }

    @Test
    fun `an amount over the limit is reported rather than silently accepted`() {
        val s = complete()
        s.loanAmount = 500_000_000L

        assertNotNull(s.detailErrors().amount)
    }

    @Test
    fun `an amount below the smallest loan is reported`() {
        val s = complete()
        s.loanAmount = 500_000L

        assertNotNull(s.detailErrors().amount)
    }

    @Test
    fun `a tenor below the tier minimum is reported`() {
        val s = complete()
        s.updateLoanAmount(300_000_000L)
        s.termMonths = 3

        assertNotNull(s.detailErrors().tenor)
    }


    @Test
    fun `choosing Other without describing it is refused`() {
        val s = complete()
        s.purpose = ApplyLoanState.PURPOSE_OTHER

        assertNotNull(s.detailErrors().purposeDetail)
        assertTrue(s.detailErrors().hasAny)
    }

    @Test
    fun `describing Other satisfies it and both parts are submitted`() {
        val s = complete()
        s.purpose = ApplyLoanState.PURPOSE_OTHER
        s.purposeDetail = "  Wedding costs  "

        assertNull(s.detailErrors().purposeDetail)
        assertEquals("Other - Wedding costs", s.purposeForSubmission)
    }

    @Test
    fun `a named purpose is submitted unchanged`() {
        assertEquals("Education", complete().purposeForSubmission)
    }


    @Test
    fun `reset clears the Other explanation too`() {
        val s = complete()
        s.purpose = ApplyLoanState.PURPOSE_OTHER
        s.purposeDetail = "Wedding"

        s.reset()

        assertEquals("", s.purposeDetail)
        assertEquals("", s.purpose)
        assertEquals(1, s.step)
    }
}

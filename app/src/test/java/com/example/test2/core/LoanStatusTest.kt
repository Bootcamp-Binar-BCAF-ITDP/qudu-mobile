package com.example.test2.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class LoanStatusTest {

    private val all = listOf(
        LoanStatus.CHECKING,
        LoanStatus.REJECTED_BY_MARKETING,
        LoanStatus.PENDING_BRANCH_MANAGER,
        LoanStatus.REJECTED_BY_BRANCH_MANAGER,
        LoanStatus.PENDING_BACK_OFFICE,
        LoanStatus.VERIFIED,
        LoanStatus.DISBURSED,
        LoanStatus.REJECTED_BY_BACK_OFFICE,
    )

    @Test
    fun `the three rejections count as rejected`() {
        assertTrue(LoanStatus.isRejected(LoanStatus.REJECTED_BY_MARKETING))
        assertTrue(LoanStatus.isRejected(LoanStatus.REJECTED_BY_BRANCH_MANAGER))
        assertTrue(LoanStatus.isRejected(LoanStatus.REJECTED_BY_BACK_OFFICE))
    }

    @Test
    fun `nothing else counts as rejected`() {
        listOf(
            LoanStatus.CHECKING,
            LoanStatus.PENDING_BRANCH_MANAGER,
            LoanStatus.PENDING_BACK_OFFICE,
            LoanStatus.VERIFIED,
            LoanStatus.DISBURSED,
            null,
            "",
        ).forEach { assertFalse("$it", LoanStatus.isRejected(it)) }
    }

    @Test
    fun `a disbursed loan is closed`() {
        assertTrue(LoanStatus.isClosed(LoanStatus.DISBURSED))
    }

    @Test
    fun `a rejected loan is closed`() {
        assertTrue(LoanStatus.isClosed(LoanStatus.REJECTED_BY_BACK_OFFICE))
    }

    @Test
    fun `a verified loan is still open, since the money has not moved`() {
        assertFalse(LoanStatus.isClosed(LoanStatus.VERIFIED))
    }

    @Test
    fun `no application is not a closed one`() {
        assertFalse(LoanStatus.isClosed(null))
    }

    @Test
    fun `every status has a readable label rather than the raw enum`() {
        all.forEach { assertNotEquals(it, LoanStatus.label(it)) }
    }

    @Test
    fun `no two statuses share a label`() {
        assertEquals(all.size, all.map { LoanStatus.label(it) }.toSet().size)
    }

    @Test
    fun `a missing status reads as no application yet`() {
        assertEquals("No application yet", LoanStatus.label(null))
    }

    @Test
    fun `an unknown status is shown as it came rather than hidden`() {
        assertEquals("SOMETHING_NEW", LoanStatus.label("SOMETHING_NEW"))
    }

    @Test
    fun `the tracker starts at zero with no application`() {
        assertEquals(0, LoanStatus.trackerStep(null))
    }

    @Test
    fun `every in-progress status sits on the middle step`() {
        listOf(
            LoanStatus.CHECKING,
            LoanStatus.PENDING_BRANCH_MANAGER,
            LoanStatus.PENDING_BACK_OFFICE,
            LoanStatus.VERIFIED,
        ).forEach { assertEquals("$it", 1, LoanStatus.trackerStep(it)) }
    }

    @Test
    fun `every closed status reaches the last step`() {
        listOf(
            LoanStatus.DISBURSED,
            LoanStatus.REJECTED_BY_MARKETING,
            LoanStatus.REJECTED_BY_BRANCH_MANAGER,
            LoanStatus.REJECTED_BY_BACK_OFFICE,
        ).forEach { assertEquals("$it", 2, LoanStatus.trackerStep(it)) }
    }

    @Test
    fun `an unknown status puts the tracker back at zero`() {
        assertEquals(0, LoanStatus.trackerStep("SOMETHING_NEW"))
    }

    @Test
    fun `the tracker step agrees with isClosed`() {
        all.forEach { status ->
            assertEquals("$status", LoanStatus.isClosed(status), LoanStatus.trackerStep(status) == 2)
        }
    }

    @Test
    fun `rupiah is grouped with dots and carries no decimals`() {
        val text = BigDecimal("10000000").asRupiah()

        assertTrue(text, text.contains("10.000.000"))
        assertFalse(text, text.contains(",00"))
    }

    @Test
    fun `rupiah rounds a fractional amount away`() {
        assertFalse(BigDecimal("1500.75").asRupiah().contains(","))
    }

    @Test
    fun `a missing amount shows a dash rather than Rp 0`() {
        assertEquals("-", (null as BigDecimal?).asRupiah())
    }
}

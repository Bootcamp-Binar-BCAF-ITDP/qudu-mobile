package com.example.test2.core

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * One plafond tier, flattened for the simulator.
 *
 * Mirrors com.delvin.loan.dto.response.plafond.PlafondResponse, but with the
 * money as Long rupiah and the rate as a Double: the simulator does arithmetic
 * on every slider move, and BigDecimal there buys precision nobody can see on
 * an estimate that is rounded to the nearest thousand anyway.
 */
data class LoanTier(
    val level: Int,
    val name: String,
    val minAmount: Long,
    val maxAmount: Long,
    val minTenor: Int,
    val maxTenor: Int,
    val annualRate: Double,
    val adminFee: Long,
) {
    fun covers(amount: Long): Boolean = amount in minAmount..maxAmount

    fun clampTenor(months: Int): Int = months.coerceIn(minTenor, maxTenor)

    /** "15%", "9.5%" - trailing ".0" trimmed, because "9.50%" reads like precision. */
    val rateLabel: String
        get() {
            val percent = annualRate * 100
            return if (percent % 1.0 == 0.0) "${percent.toInt()}%"
            else "${(Math.round(percent * 100) / 100.0)}%"
        }
}

object PlafondTiers {

    /**
     * The published rate card as of 2026-09-08, used only when
     * GET /api/plafonds/catalog cannot be reached.
     *
     * It exists so the simulator still works offline and before sign-in, not as
     * a second source of truth: the screen says plainly when it is quoting from
     * here, because staff can and do revise these (level 6 is already a
     * revision) and a silently stale rate is worse than an honest one.
     */
    val FALLBACK = listOf(
        LoanTier(1, "Bronze", 1_000_000, 10_000_000, 1, 12, 0.15, 100_000),
        LoanTier(2, "Silver", 10_000_001, 50_000_000, 1, 24, 0.13, 200_000),
        LoanTier(3, "Gold", 50_000_001, 100_000_000, 6, 36, 0.11, 350_000),
        LoanTier(4, "Platinum", 100_000_001, 250_000_000, 6, 48, 0.095, 500_000),
        LoanTier(5, "Diamond", 250_000_001, 500_000_000, 12, 60, 0.085, 750_000),
        LoanTier(6, "Titanium", 500_000_001, 1_200_000_000, 12, 72, 0.07, 1_100_000),
    )
}

/**
 * The tier that covers [amount], or the nearest one when it falls in a gap.
 *
 * Gaps are real: tier 1 ends at 10,000,000 and tier 2 starts at 10,000,001, so
 * a slider stepping in millions never lands between them - but a hand-typed
 * amount can, and quoting nothing at all there would be worse than quoting the
 * tier the customer is standing next to.
 */
fun List<LoanTier>.tierFor(amount: Long): LoanTier? {
    if (isEmpty()) return null
    return firstOrNull { it.covers(amount) }
        ?: minByOrNull { tier ->
            when {
                amount < tier.minAmount -> tier.minAmount - amount
                else -> amount - tier.maxAmount
            }
        }
}

/** Lowest amount any tier will lend, for the slider's floor. */
fun List<LoanTier>.floorAmount(): Long = minOfOrNull { it.minAmount } ?: 1_000_000L

/** Highest amount any tier will lend, for the slider's ceiling. */
fun List<LoanTier>.ceilingAmount(): Long = maxOfOrNull { it.maxAmount } ?: 1_200_000_000L

/**
 * Annuity instalment, rounded up to the nearest thousand.
 *
 * Rounded up rather than to nearest on purpose: an estimate that lands below
 * the real instalment is the one that misleads.
 */
fun monthlyInstalmentFor(principal: Long, months: Int, annualRate: Double): Long {
    if (months <= 0 || principal <= 0) return 0
    val monthlyRate = annualRate / 12.0
    val raw = if (monthlyRate == 0.0) {
        principal.toDouble() / months
    } else {
        principal * monthlyRate / (1 - Math.pow(1 + monthlyRate, -months.toDouble()))
    }
    return BigDecimal(raw)
        .divide(BigDecimal(1000), 0, RoundingMode.CEILING)
        .toLong() * 1000
}

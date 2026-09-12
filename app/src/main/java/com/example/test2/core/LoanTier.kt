package com.example.test2.core

import java.math.BigDecimal
import java.math.RoundingMode

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

    val rateLabel: String
        get() {
            val percent = annualRate * 100
            return if (percent % 1.0 == 0.0) "${percent.toInt()}%"
            else "${(Math.round(percent * 100) / 100.0)}%"
        }
}

object PlafondTiers {

    val FALLBACK = listOf(
        LoanTier(1, "Bronze", 1_000_000, 10_000_000, 1, 12, 0.15, 100_000),
        LoanTier(2, "Silver", 10_000_001, 50_000_000, 1, 24, 0.13, 200_000),
        LoanTier(3, "Gold", 50_000_001, 100_000_000, 6, 36, 0.11, 350_000),
        LoanTier(4, "Platinum", 100_000_001, 250_000_000, 6, 48, 0.095, 500_000),
        LoanTier(5, "Diamond", 250_000_001, 500_000_000, 12, 60, 0.085, 750_000),
        LoanTier(6, "Titanium", 500_000_001, 1_200_000_000, 12, 72, 0.07, 1_100_000),
    )
}

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

fun List<LoanTier>.floorAmount(): Long = minOfOrNull { it.minAmount } ?: 1_000_000L

fun List<LoanTier>.ceilingAmount(): Long = maxOfOrNull { it.maxAmount } ?: 1_200_000_000L

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

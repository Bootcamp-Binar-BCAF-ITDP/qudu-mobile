package com.example.test2.data.repository

import com.example.test2.core.LoanTier
import com.example.test2.core.Outcome
import com.example.test2.core.PlafondTiers
import com.example.test2.data.remote.ApiService
import com.example.test2.data.dto.PlafondDto

/**
 * The plafond rate card for the simulator.
 *
 * Unlike the other repositories this one never reports failure to the caller:
 * a simulator that shows nothing is useless, and the tiers are published
 * product terms rather than the customer's own data. It says *which* source it
 * used instead, so the screen can admit when the numbers are the bundled ones.
 */
class PlafondCatalogRepository(private val api: ApiService) {

    data class Catalog(val tiers: List<LoanTier>, val live: Boolean)

    suspend fun load(): Catalog {

        val result = apiCall { api.plafondCatalog() }

        val tiers = (result as? Outcome.Success)
            ?.value
            ?.data
            .orEmpty()
            .mapNotNull { it.toTier() }
            .sortedBy { it.level }

        // An empty list is treated as a failure on purpose: it means every tier
        // was deactivated or the payload changed shape, and quoting nothing is
        // worse than quoting the last published card.
        return if (tiers.isEmpty()) {
            Catalog(PlafondTiers.FALLBACK, live = false)
        } else {
            Catalog(tiers, live = true)
        }
    }
}

/**
 * Drops any tier missing a field the simulator needs to do arithmetic - a
 * half-populated tier would quote a zero-rate loan, which is worse than not
 * offering that tier at all.
 */
private fun PlafondDto.toTier(): LoanTier? {
    val level = level ?: return null
    val min = minimumAmount?.toLong() ?: return null
    val max = maxAmount?.toLong() ?: return null
    val minTenor = minTenor ?: return null
    val maxTenor = maxTenor ?: return null
    val rate = interestRate?.toDouble() ?: return null

    return LoanTier(
        level = level,
        name = description?.takeIf { it.isNotBlank() } ?: "Level $level",
        minAmount = min,
        maxAmount = max,
        minTenor = minTenor,
        maxTenor = maxTenor,
        annualRate = rate,
        adminFee = adminFee?.toLong() ?: 0L,
    )
}

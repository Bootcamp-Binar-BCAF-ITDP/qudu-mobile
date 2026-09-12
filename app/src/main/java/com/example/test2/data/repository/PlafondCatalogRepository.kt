package com.example.test2.data.repository

import com.example.test2.core.LoanTier
import com.example.test2.core.Outcome
import com.example.test2.core.PlafondTiers
import com.example.test2.data.remote.ApiService
import com.example.test2.data.dto.PlafondDto

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

        return if (tiers.isEmpty()) {
            Catalog(PlafondTiers.FALLBACK, live = false)
        } else {
            Catalog(tiers, live = true)
        }
    }
}

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

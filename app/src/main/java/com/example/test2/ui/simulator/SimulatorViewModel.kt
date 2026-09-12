package com.example.test2.ui.simulator

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test2.core.LoanTier
import com.example.test2.core.PlafondTiers
import com.example.test2.core.ceilingAmount
import com.example.test2.core.floorAmount
import com.example.test2.core.monthlyInstalmentFor
import com.example.test2.core.tierFor
import com.example.test2.data.repository.PlafondCatalogRepository
import kotlinx.coroutines.launch

class SimulatorViewModel(
    private val repository: PlafondCatalogRepository,
) : ViewModel() {

    var tiers by mutableStateOf(PlafondTiers.FALLBACK)
        private set

    var live by mutableStateOf(false)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    var amount by mutableStateOf(25_000_000L)
        private set

    var tenor by mutableStateOf(24)
        private set

    val tier: LoanTier?
        get() = tiers.tierFor(amount)

    val minAmount: Long get() = tiers.floorAmount()
    val maxAmount: Long get() = tiers.ceilingAmount()

    val monthlyInstalment: Long
        get() = monthlyInstalmentFor(amount, tenor, tier?.annualRate ?: 0.0)

    val totalRepayment: Long
        get() = monthlyInstalment * tenor

    val adminFee: Long
        get() = tier?.adminFee ?: 0L

    val totalCost: Long
        get() = (totalRepayment - amount).coerceAtLeast(0) + adminFee

    fun load(userInitiated: Boolean = false) {
        if (isLoading) return
        isLoading = true
        if (userInitiated) isRefreshing = true

        viewModelScope.launch {
            try {
                val catalog = repository.load()
                tiers = catalog.tiers
                live = catalog.live
                updateAmount(amount)
            } finally {
                isLoading = false
                isRefreshing = false
            }
        }
    }

    fun updateAmount(value: Long) {
        amount = value.coerceIn(minAmount, maxAmount)
        tier?.let { tenor = it.clampTenor(tenor) }
    }

    fun updateTenor(value: Int) {
        tenor = tier?.clampTenor(value) ?: value
    }
}

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

/**
 * The loan simulator's state.
 *
 * Lives in a ViewModel rather than in the screen because the tier table is
 * fetched, and because the simulator sits on two destinations - the Home card
 * and its own tab - which should agree with each other rather than each hold
 * their own copy.
 */
class SimulatorViewModel(
    private val repository: PlafondCatalogRepository,
) : ViewModel() {

    var tiers by mutableStateOf(PlafondTiers.FALLBACK)
        private set

    /** False while the bundled rate card is standing in for the server's. */
    var live by mutableStateOf(false)
        private set

    var isLoading by mutableStateOf(false)
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

    /** What the loan costs beyond the principal: interest plus the one-off fee. */
    val totalCost: Long
        get() = (totalRepayment - amount).coerceAtLeast(0) + adminFee

    fun load() {
        if (isLoading) return
        isLoading = true

        viewModelScope.launch {
            val catalog = repository.load()
            tiers = catalog.tiers
            live = catalog.live
            // The tier that covers the current amount may have a different
            // tenor window than the bundled one did, so re-clamp before the
            // screen recomposes with numbers from a range no longer on offer.
            updateAmount(amount)
            isLoading = false
        }
    }

    /**
     * Moving the amount can move the tier, and each tier has its own tenor
     * window - so the tenor is pulled back into range here. Without this, a
     * customer who sets 24 months on Silver and then slides up to Diamond would
     * be quoted a 24-month Diamond loan the bank does not sell.
     */
    fun updateAmount(value: Long) {
        amount = value.coerceIn(minAmount, maxAmount)
        tier?.let { tenor = it.clampTenor(tenor) }
    }

    fun updateTenor(value: Int) {
        tenor = tier?.clampTenor(value) ?: value
    }
}

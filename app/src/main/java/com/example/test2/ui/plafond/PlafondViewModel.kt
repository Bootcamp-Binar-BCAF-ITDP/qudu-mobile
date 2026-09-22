package com.example.test2.ui.plafond

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test2.core.Outcome
import com.example.test2.data.dto.PlafondRequestDto
import com.example.test2.data.repository.LoanRepository
import kotlinx.coroutines.launch
import java.math.BigDecimal
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PlafondViewModel @Inject constructor(private val repository: LoanRepository) : ViewModel() {

    var isBusy by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var requests by mutableStateOf<List<PlafondRequestDto>>(emptyList())
        private set

    var lastSyncedAt by mutableStateOf<Long?>(null)
        private set

    var showingCached by mutableStateOf(false)
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    var isLoadingRequests by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            repository.cachedUpgradeRequests.collect { cached ->
                requests = cached.items
                cached.fetchedAt?.let { lastSyncedAt = it }
            }
        }
    }

    fun loadRequests(userInitiated: Boolean = false) {
        if (isLoadingRequests) return
        isLoadingRequests = true
        if (userInitiated) isRefreshing = true

        viewModelScope.launch {
            try {
                when (val result = repository.myUpgradeRequests()) {
                    is Outcome.Success -> showingCached = false
                    is Outcome.Failure -> {
                        showingCached = requests.isNotEmpty()
                        if (requests.isEmpty()) error = result.message
                    }
                }
            } finally {
                isLoadingRequests = false
                isRefreshing = false
            }
        }
    }

    fun submit(amountText: String, onSubmitted: (String) -> Unit = {}) {

        if (isBusy) return

        val digits = amountText.filter(Char::isDigit)
        if (digits.isEmpty()) {
            error = "Enter the limit amount you want."
            return
        }

        error = null
        isBusy = true

        viewModelScope.launch {
            when (val result = repository.requestUpgrade(BigDecimal(digits))) {
                is Outcome.Success -> {
                    loadRequests()
                    isBusy = false
                    onSubmitted(SUBMITTED_MESSAGE)
                    return@launch
                }

                is Outcome.Failure -> error = result.message
            }
            isBusy = false
        }
    }

    companion object {
        const val SUBMITTED_MESSAGE =
            "Limit increase request sent. Awaiting the branch manager decision."
    }
}

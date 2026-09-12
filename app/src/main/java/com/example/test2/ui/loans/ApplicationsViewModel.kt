package com.example.test2.ui.loans

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test2.core.LoanStatus
import com.example.test2.core.Outcome
import com.example.test2.data.dto.CustomerPlafondDto
import com.example.test2.data.dto.LoanApplicationDto
import com.example.test2.data.dto.PlafondRequestDto
import com.example.test2.data.local.SessionStore
import com.example.test2.data.repository.LoanRepository
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class ApplicationsViewModel(
    private val repository: LoanRepository,
    private val sessionStore: SessionStore,
) : ViewModel() {

    var applications by mutableStateOf<List<LoanApplicationDto>>(emptyList())
        private set

    var plafondRequests by mutableStateOf<List<PlafondRequestDto>>(emptyList())
        private set

    var plafond by mutableStateOf<CustomerPlafondDto?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var lastSyncedAt by mutableStateOf<Long?>(null)
        private set

    var showingCached by mutableStateOf(false)
        private set

    val active: List<LoanApplicationDto>
        get() = applications.filter { !LoanStatus.isRejected(it.status) }

    val closed: List<LoanApplicationDto>
        get() = applications.filter { LoanStatus.isClosed(it.status) }

    val disbursed: List<LoanApplicationDto>
        get() = applications.filter { it.status == LoanStatus.DISBURSED }

    init {
        viewModelScope.launch {
            repository.cachedApplications.collect { cached ->
                applications = cached.items
                cached.fetchedAt?.let { lastSyncedAt = it }
            }
        }

        viewModelScope.launch {
            repository.cachedUpgradeRequests.collect { cached ->
                plafondRequests = cached.items
                cached.fetchedAt?.let { lastSyncedAt = it }
            }
        }
    }

    fun refresh(userInitiated: Boolean = false) {
        if (isLoading) return

        isLoading = true
        if (userInitiated) isRefreshing = true
        error = null

        viewModelScope.launch {
            try {
                val session = sessionStore.sessionOnce()

                if (session == null) {
                    error = "Session not found. Please sign in again."
                    return@launch
                }

                val failed = withTimeout(REFRESH_TIMEOUT_MS) {
                    coroutineScope {
                        val applicationsCall =
                            async { repository.myApplications(session.customerId) }
                        val plafondCall = async { repository.myPlafond() }
                        val requestsCall = async { repository.myUpgradeRequests() }

                        var anyFailed = false

                        when (val result = applicationsCall.await()) {
                            is Outcome.Success -> Unit
                            is Outcome.Failure -> {
                                anyFailed = true
                                error = result.message
                            }
                        }

                        when (val result = plafondCall.await()) {
                            is Outcome.Success -> plafond = result.value
                            is Outcome.Failure -> {
                                anyFailed = true
                                if (error == null) error = result.message
                            }
                        }

                        when (val result = requestsCall.await()) {
                            is Outcome.Success -> Unit
                            is Outcome.Failure -> {
                                anyFailed = true
                                if (error == null) error = result.message
                            }
                        }

                        anyFailed
                    }
                }

                showingCached =
                    failed && (applications.isNotEmpty() || plafondRequests.isNotEmpty())
            } catch (e: TimeoutCancellationException) {
                error = "The server did not respond. Pull down to try again."
                showingCached = applications.isNotEmpty() || plafondRequests.isNotEmpty()
            } finally {
                isLoading = false
                isRefreshing = false
            }
        }
    }

    private companion object {
        const val REFRESH_TIMEOUT_MS = 20_000L
    }
}

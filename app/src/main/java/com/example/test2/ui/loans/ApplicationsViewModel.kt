package com.example.test2.ui.loans

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test2.core.LoanStatus
import com.example.test2.core.Outcome
import com.example.test2.data.local.SessionStore
import com.example.test2.data.dto.CustomerPlafondDto
import com.example.test2.data.dto.LoanApplicationDto
import com.example.test2.data.dto.PlafondRequestDto
import com.example.test2.data.repository.LoanRepository
import kotlinx.coroutines.launch

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

    var error by mutableStateOf<String?>(null)
        private set

    val active: List<LoanApplicationDto>
        get() = applications.filter { !LoanStatus.isRejected(it.status) }

    val closed: List<LoanApplicationDto>
        get() = applications.filter { LoanStatus.isClosed(it.status) }

    val disbursed: List<LoanApplicationDto>
        get() = applications.filter { it.status == LoanStatus.DISBURSED }

    fun refresh() {
        if (isLoading) return

        isLoading = true
        error = null

        viewModelScope.launch {
            val session = sessionStore.sessionOnce()

            if (session == null) {
                error = "Session not found. Please sign in again."
                isLoading = false
                return@launch
            }

            when (val result = repository.myApplications(session.customerId)) {
                is Outcome.Success -> applications = result.value
                is Outcome.Failure -> error = result.message
            }

            when (val result = repository.myPlafond()) {
                is Outcome.Success -> plafond = result.value
                is Outcome.Failure -> if (error == null) error = result.message
            }

            when (val result = repository.myUpgradeRequests()) {
                is Outcome.Success -> plafondRequests = result.value
                is Outcome.Failure -> if (error == null) error = result.message
            }

            isLoading = false
        }
    }
}

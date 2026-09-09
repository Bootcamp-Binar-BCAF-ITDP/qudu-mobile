package com.example.test2.ui.dashboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test2.core.Outcome
import com.example.test2.data.local.SessionStore
import com.example.test2.data.dto.CustomerPlafondDto
import com.example.test2.data.dto.LoanApplicationDto
import com.example.test2.data.repository.LoanRepository
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repository: LoanRepository,
    private val sessionStore: SessionStore,
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var plafond by mutableStateOf<CustomerPlafondDto?>(null)
        private set

    var applications by mutableStateOf<List<LoanApplicationDto>>(emptyList())
        private set

    var customerName by mutableStateOf("")
        private set

    val latestApplication: LoanApplicationDto? get() = applications.firstOrNull()

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

            customerName = session.fullName.ifBlank { session.email }

            when (val result = repository.myPlafond()) {
                is Outcome.Success -> plafond = result.value
                is Outcome.Failure -> error = result.message
            }

            when (val result = repository.myApplications(session.customerId)) {
                is Outcome.Success -> applications = result.value
                is Outcome.Failure -> if (error == null) error = result.message
            }

            isLoading = false
        }
    }
}

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

class PlafondViewModel(private val repository: LoanRepository) : ViewModel() {

    var isBusy by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var requests by mutableStateOf<List<PlafondRequestDto>>(emptyList())
        private set

    fun loadRequests() {
        viewModelScope.launch {
            when (val result = repository.myUpgradeRequests()) {
                is Outcome.Success -> requests = result.value
                is Outcome.Failure -> error = result.message
            }
        }
    }

    /**
     * @param onSubmitted called with the confirmation once the request lands.
     *        The screen leaves for Home on success, so the message has to
     *        travel with the caller - an inline notice on a screen nobody is
     *        looking at any more is not a confirmation.
     */
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

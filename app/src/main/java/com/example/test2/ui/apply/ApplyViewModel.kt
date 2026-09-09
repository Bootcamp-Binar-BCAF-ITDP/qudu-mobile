package com.example.test2.ui.apply

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test2.core.DocumentTypes
import com.example.test2.core.HTTP_OVER_PLAFOND
import com.example.test2.core.Outcome
import com.example.test2.data.local.SessionStore
import com.example.test2.data.dto.LoanApplicationCreateRequestDto
import com.example.test2.data.repository.LoanRepository
import com.example.test2.data.repository.ProfileRepository
import kotlinx.coroutines.launch
import java.math.BigDecimal

private const val HTTP_PROFILE_INCOMPLETE = 409

class ApplyViewModel(
    private val repository: LoanRepository,
    private val profileRepository: ProfileRepository,
    private val sessionStore: SessionStore,
) : ViewModel() {

    var isSubmitting by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var overPlafond by mutableStateOf(false)
        private set

   var profileIncomplete by mutableStateOf(false)
        private set

    var createdApplicationId by mutableStateOf<String?>(null)
        private set

    fun clearMessages() {
        error = null
        overPlafond = false
        profileIncomplete = false
    }

    fun submit(state: ApplyLoanState, onSubmitted: (String) -> Unit) {

        if (isSubmitting) return

        val validationError = validate(state)
        if (validationError != null) {
            error = validationError
            return
        }

        clearMessages()
        isSubmitting = true

        viewModelScope.launch {
            val session = sessionStore.sessionOnce()

            if (session == null) {
                error = "Session not found. Please sign in again."
                isSubmitting = false
                return@launch
            }

            // Every captured file goes up before the application exists. The
            // backend refuses to create one whose paperwork is incomplete, so a
            // failure here has to stop the submission rather than be reported
            // afterwards - which is what used to leave empty applications behind.
            val uploadError = uploadCapturedDocuments(state)
            if (uploadError != null) {
                error = uploadError
                isSubmitting = false
                return@launch
            }

            val request = LoanApplicationCreateRequestDto(
                customerId = session.customerId,
                requestedAmount = BigDecimal.valueOf(state.loanAmount),
                tenor = state.termMonths,
                purpose = state.purposeForSubmission.trim(),
                bank = state.bank.trim(),
                bankAccountNumber = state.bankAccountNumber.trim(),
                bankAccountName = state.bankAccountName.trim(),
                income = state.monthlyIncome.digitsAsAmount(),
            )

            when (val result = repository.createApplication(request)) {

                is Outcome.Failure -> {
                    error = result.message
                    overPlafond = result.code == HTTP_OVER_PLAFOND
                    profileIncomplete = result.code == HTTP_PROFILE_INCOMPLETE
                }

                is Outcome.Success -> {
                    val applicationId = result.value.applicationId

                    if (applicationId == null) {
                        error = "The server did not return an application number."
                    } else {
                        // Nothing to upload here any more: the backend snapshots
                        // the customer's documents onto the application as it
                        // creates it.
                        createdApplicationId = applicationId
                        onSubmitted(applicationId)
                    }
                }
            }

            isSubmitting = false
        }
    }

    /**
     * Sends every file captured in this flow to the customer document store.
     *
     * All five types live there now - identity papers and financial evidence
     * alike - which is what lets the backend refuse an application before it
     * exists. A replaced KTP and a fresh payslip take the same path.
     *
     * @return the first failure, or null when everything landed.
     */
    private suspend fun uploadCapturedDocuments(state: ApplyLoanState): String? {

        for ((documentType, uri) in state.documentUris) {
            val result = profileRepository.uploadDocument(documentType, uri)

            if (result is Outcome.Failure) {
                return "Could not upload the ${DocumentTypes.label(documentType)}: ${result.message}"
            }
        }

        return null
    }

    /**
     * Form fields only. Documents are gated by DocumentsStep, which can see the
     * profile and so knows whether a stored payslip is still fresh; repeating a
     * cruder check here would block submissions the server would have accepted.
     */
    private fun validate(state: ApplyLoanState): String? {

        return when {
            state.purpose.isBlank() -> "Please choose a loan purpose."
            state.bank.isBlank() -> "Bank name is required."
            state.bankAccountNumber.isBlank() -> "Account number is required."
            state.bankAccountName.isBlank() -> "Account holder name is required."
            state.monthlyIncome.digitsAsAmount() <= BigDecimal.ZERO ->
                "Monthly income is required."
            !state.termsAccepted -> "You must accept the terms and conditions."
            else -> null
        }
    }
}

private fun String.digitsAsAmount(): BigDecimal {
    val digits = filter(Char::isDigit)
    return if (digits.isEmpty()) BigDecimal.ZERO else BigDecimal(digits)
}

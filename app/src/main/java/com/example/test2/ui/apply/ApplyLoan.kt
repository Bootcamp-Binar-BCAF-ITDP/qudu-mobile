package com.example.test2.ui.apply

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.test2.core.LoanTier
import com.example.test2.core.PlafondTiers
import com.example.test2.core.ceilingAmount
import com.example.test2.core.floorAmount
import com.example.test2.core.monthlyInstalmentFor
import com.example.test2.core.tierFor
import com.example.test2.data.dto.CustomerProfileDto

@Stable
class ApplyLoanState {

    val documentUris = mutableStateMapOf<String, Uri>()

    var loanAmount by mutableStateOf(25_000_000L)
    var purpose by mutableStateOf("")

    var purposeDetail by mutableStateOf("")

    var termMonths by mutableStateOf(24)
    var monthlyIncome by mutableStateOf("")

    var creditLimit by mutableStateOf<Long?>(null)

    var bank by mutableStateOf("")
    var bankAccountNumber by mutableStateOf("")
    var bankAccountName by mutableStateOf("")

    var termsAccepted by mutableStateOf(false)

    var tiers by mutableStateOf(PlafondTiers.FALLBACK)

    var step by mutableStateOf(1)

    val tier: LoanTier?
        get() = tiers.tierFor(loanAmount)

    val annualRate: Double
        get() = tier?.annualRate ?: 0.0

    val adminFee: Long
        get() = tier?.adminFee ?: 0L

    val monthlyPayment: Long
        get() = monthlyInstalmentFor(loanAmount, termMonths, annualRate)

    val totalPayment: Long
        get() = monthlyPayment * termMonths

    val minAmount: Long
        get() = tiers.floorAmount()

    val maxAmount: Long
        get() = (creditLimit ?: tiers.ceilingAmount())
            .coerceIn(minAmount, tiers.ceilingAmount())

    fun updateLoanAmount(value: Long) {
        loanAmount = value.coerceAtMost(maxAmount)
        tier?.let { termMonths = it.clampTenor(termMonths) }
    }

    fun updateTermMonths(value: Int) {
        val ceiling = tier?.maxTenor ?: value
        termMonths = value.coerceAtMost(ceiling)
    }

    val purposeForSubmission: String
        get() = if (purpose == PURPOSE_OTHER && purposeDetail.isNotBlank()) {
            "$PURPOSE_OTHER - ${purposeDetail.trim()}"
        } else {
            purpose
        }

    val monthlyIncomeAmount: Long
        get() = monthlyIncome.filter(Char::isDigit).toLongOrNull() ?: 0L

    fun detailErrors(): LoanDetailErrors {
        val minTenor = tier?.minTenor
        return LoanDetailErrors(
            amount = when {
                loanAmount < minAmount ->
                    "The smallest loan is ${formatRupiah(minAmount)}."
                loanAmount > maxAmount ->
                    "This is above your limit of ${formatRupiah(maxAmount)}."
                else -> null
            },
            tenor = when {
                minTenor != null && termMonths < minTenor ->
                    "This plafond starts at $minTenor months."
                termMonths <= 0 -> "Choose a tenure."
                else -> null
            },
            purpose = if (purpose.isBlank()) "Choose what the loan is for." else null,
            purposeDetail = if (purpose == PURPOSE_OTHER && purposeDetail.isBlank()) {
                "Tell us what the loan is for."
            } else null,
            income = if (monthlyIncomeAmount <= 0L) "Enter your monthly income." else null,
            bank = if (bank.isBlank()) "Enter your bank." else null,
            accountNumber = if (bankAccountNumber.isBlank()) "Enter the account number." else null,
            accountName = if (bankAccountName.isBlank()) "Enter the account holder name." else null,
        )
    }

    fun isCaptured(documentType: String): Boolean = documentUris.containsKey(documentType)

    fun setDocumentUri(documentType: String, uri: Uri) {
        documentUris[documentType] = uri
    }

    fun reset() {
        documentUris.clear()
        loanAmount = 25_000_000L
        purpose = ""
        purposeDetail = ""
        termMonths = 24
        monthlyIncome = ""
        bank = ""
        bankAccountNumber = ""
        bankAccountName = ""
        termsAccepted = false
        step = 1
    }

    companion object {
        const val PURPOSE_OTHER = "Other"
    }
}

@Stable
class LoanDetailErrors(
    val amount: String? = null,
    val tenor: String? = null,
    val purpose: String? = null,
    val purposeDetail: String? = null,
    val income: String? = null,
    val bank: String? = null,
    val accountNumber: String? = null,
    val accountName: String? = null,
) {
    val hasAny: Boolean
        get() = listOf(
            amount, tenor, purpose, purposeDetail, income, bank, accountNumber, accountName,
        ).any { it != null }
}

@Composable
fun rememberApplyLoanState(): ApplyLoanState = remember { ApplyLoanState() }

@Composable
fun ApplyLoanFlow(
    state: ApplyLoanState,
    profile: CustomerProfileDto?,
    onExit: () -> Unit = {},
    onSubmit: (ApplyLoanState) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(profile) {
        state.creditLimit = (profile?.availableLimit ?: profile?.approvedLimit)?.toLong()
        state.updateLoanAmount(state.loanAmount)
    }

    fun canLeaveDetails(): Boolean = !state.detailErrors().hasAny

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBg)
    ) {
        StepIndicator(
            currentStep = state.step,
            onStepClick = { target ->
                when {
                    target <= state.step -> state.step = target

                    state.step == 2 && canLeaveDetails() -> state.step = target

                    else -> Unit
                }
            },
        )

        when (state.step) {
            1 -> DocumentsStep(
                state = state,
                profile = profile,
                onBack = onExit,
                onContinue = { state.step = 2 }
            )

            2 -> LoanDetailsStep(
                state = state,
                onBack = { state.step = 1 },
                onCancel = onExit,
                onContinue = { state.step = 3 }
            )

            else -> ReviewStep(
                state = state,
                profile = profile,
                onBack = { state.step = 2 },
                onEditDocuments = { state.step = 1 },
                onEditLoanDetails = { state.step = 2 },
                onSubmit = { onSubmit(state) }
            )
        }
    }
}

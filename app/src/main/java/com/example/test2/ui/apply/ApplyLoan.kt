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

/**
 * State for the three application steps.
 *
 * Personal details are gone from here: they live on the customer profile, are
 * shown read-only, and are never re-typed. What is left is this loan - how much,
 * for how long, paid where, and the two papers that describe it.
 */
@Stable
class ApplyLoanState {

    /**
     * Files captured in this flow, keyed by backend document type.
     *
     * All of them go to the customer document store on submit - see
     * ApplyViewModel.uploadCapturedDocuments. The backend then snapshots them
     * onto the application it creates, which is why nothing is uploaded after.
     */
    val documentUris = mutableStateMapOf<String, Uri>()

    // Step 2
    var loanAmount by mutableStateOf(25_000_000L)
    var purpose by mutableStateOf("")

    /** Free text, required only when [purpose] is [PURPOSE_OTHER]. */
    var purposeDetail by mutableStateOf("")

    var termMonths by mutableStateOf(24)
    // Blank, not a sample figure. A pre-filled income is a number the customer
    // never stated, on a form where the declared income is part of the credit
    // decision - and "required" means nothing if it arrives already answered.
    var monthlyIncome by mutableStateOf("")

    /**
     * The customer's own ceiling, pushed in from their profile.
     *
     * Null before the profile loads or for a guest, in which case only the tier
     * table constrains the amount. Once known it is the *binding* limit: the
     * backend refuses anything above the available credit line, so letting the
     * slider reach higher only manufactures a rejection.
     */
    var creditLimit by mutableStateOf<Long?>(null)

    // Step 2 - disbursement account. QuDu-be requires all three on create.
    var bank by mutableStateOf("")
    var bankAccountNumber by mutableStateOf("")
    var bankAccountName by mutableStateOf("")

    // Step 3
    var termsAccepted by mutableStateOf(false)

    /**
     * The plafond rate card, pushed in from the shared simulator ViewModel.
     *
     * Held here rather than fetched, because the wizard has no ViewModel of its
     * own that owns it and the two must agree: a customer who is quoted 13% by
     * the simulator and then 6.5% by the application form has been told two
     * different numbers about the same loan.
     */
    var tiers by mutableStateOf(PlafondTiers.FALLBACK)

    /**
     * Which of the three steps is showing.
     *
     * Part of the state rather than a local `remember` inside the flow, because
     * the flow now sits under the persistent tab shell: tapping History
     * mid-application and coming back has to land on the same step with the
     * same answers, not a blank form.
     */
    var step by mutableStateOf(1)

    /** The tier the requested amount falls into - it decides rate, tenor and fee. */
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

    /** Lowest amount any tier will lend. */
    val minAmount: Long
        get() = tiers.floorAmount()

    /**
     * The most this customer may ask for: their credit line, never above the
     * highest tier on offer, and never below [minAmount] - a slider whose start
     * exceeds its end throws.
     */
    val maxAmount: Long
        get() = (creditLimit ?: tiers.ceilingAmount())
            .coerceIn(minAmount, tiers.ceilingAmount())

    /**
     * Moves the amount and pulls the tenor back into whatever the new tier
     * allows. The backend rejects a tenor outside the tier's window, so letting
     * the slider express one only produces a submission that cannot succeed.
     *
     * The upper bound is enforced here rather than reported later: being unable
     * to type past your limit is kinder than being told afterwards that you did.
     * The lower bound is *not* clamped, because clamping it mid-typing turns a
     * half-entered "1" into a million.
     */
    fun updateLoanAmount(value: Long) {
        loanAmount = value.coerceAtMost(maxAmount)
        tier?.let { termMonths = it.clampTenor(termMonths) }
    }

    fun updateTermMonths(value: Int) {
        val ceiling = tier?.maxTenor ?: value
        termMonths = value.coerceAtMost(ceiling)
    }

    /**
     * What actually goes on the application.
     *
     * "Other" alone tells a reviewer nothing, and the typed explanation alone
     * loses the fact that it fell outside the offered categories - so both are
     * sent.
     */
    val purposeForSubmission: String
        get() = if (purpose == PURPOSE_OTHER && purposeDetail.isNotBlank()) {
            "$PURPOSE_OTHER - ${purposeDetail.trim()}"
        } else {
            purpose
        }

    /** Parsed monthly income, 0 when the field is blank or non-numeric. */
    val monthlyIncomeAmount: Long
        get() = monthlyIncome.filter(Char::isDigit).toLongOrNull() ?: 0L

    /**
     * Everything wrong with step 2 right now, one message per field.
     *
     * Computed rather than stored so it always describes the current answers -
     * a stored copy goes stale the moment a field is corrected, which is the
     * bug where an error message outlives the mistake.
     */
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

    /**
     * Wipe the draft. Called once a submission actually lands, so the next
     * application starts empty instead of inheriting the last one's amount,
     * account number and captured files.
     */
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
        /** The dropdown entry that opens the free-text box. */
        const val PURPOSE_OTHER = "Other"
    }
}

/**
 * Step 2's problems, one per field. Null means that field is fine.
 */
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
    // The customer's own ceiling. Available credit, not the granted limit: the
    // part already drawn down on a disbursed loan is not lendable again.
    LaunchedEffect(profile) {
        state.creditLimit = (profile?.availableLimit ?: profile?.approvedLimit)?.toLong()
        // Re-clamp: the draft may hold an amount from before the limit was known.
        state.updateLoanAmount(state.loanAmount)
    }

    // Step 2 must be complete before anything downstream of it may be reached,
    // whether the customer gets there with the button or by tapping the dot.
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
                    // Going back is always allowed - nothing downstream depends
                    // on a step you are retreating from.
                    target <= state.step -> state.step = target

                    // Jumping forward past step 2 is refused unless it is
                    // actually filled in; letting it through would land the
                    // customer on a Review page describing a loan that has no
                    // amount, account or purpose.
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

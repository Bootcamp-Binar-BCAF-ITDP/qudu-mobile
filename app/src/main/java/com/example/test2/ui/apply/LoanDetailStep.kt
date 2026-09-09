package com.example.test2.ui.apply

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test2.core.ceilingAmount
import com.example.test2.core.floorAmount
import kotlin.math.roundToInt

// Amount and tenor bounds are no longer constants here: they come from the
// plafond tier the requested amount falls into, so the form cannot describe a
// loan the bank does not sell. See ApplyLoanState.tier.

private val Purposes = listOf(
    "Business Expansion",
    "Education",
    "Home Renovation",
    "Medical Expenses",
    "Debt Consolidation",
    // The one entry that opens the free-text box - kept as the shared constant
    // so the dropdown and the validation cannot drift apart.
    ApplyLoanState.PURPOSE_OTHER,
)

@Composable
fun LoanDetailsStep(
    state: ApplyLoanState,
    onBack: () -> Unit,
    onCancel: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Errors appear only once the customer has tried to move on. Marking a form
    // red before it has been filled in is nagging, not helping.
    var showErrors by remember { mutableStateOf(false) }
    val errors = state.detailErrors()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp)
    ) {
        SectionCard(padding = 22) {
            Text(
                text = "Loan Details",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(Modifier.height(24.dp))

            AmountField(state, error = errors.amount.takeIf { showErrors })

            Spacer(Modifier.height(20.dp))
            TenureField(state, error = errors.tenor.takeIf { showErrors })

            // Purpose sits below tenure: amount and tenure are the two numbers
            // the simulation below depends on, so they are answered first.
            Spacer(Modifier.height(20.dp))
            FieldLabel("Loan Purpose", required = true)
            AppDropdownField(
                options = Purposes,
                selected = state.purpose.takeIf { it.isNotBlank() },
                onSelect = { state.purpose = it },
                placeholder = "Select a purpose",
                isError = showErrors && errors.purpose != null,
            )
            FieldError(errors.purpose.takeIf { showErrors })

            if (state.purpose == ApplyLoanState.PURPOSE_OTHER) {
                Spacer(Modifier.height(14.dp))
                FieldLabel("Please describe it", required = true)
                AppTextField(
                    value = state.purposeDetail,
                    onValueChange = { state.purposeDetail = it },
                    placeholder = "What will the loan be used for?",
                    isError = showErrors && errors.purposeDetail != null,
                )
                FieldError(errors.purposeDetail.takeIf { showErrors })
            }

            Spacer(Modifier.height(20.dp))
            FieldLabel("Monthly Income", required = true)
            AppTextField(
                value = state.monthlyIncome,
                onValueChange = { input ->
                    val digits = input.filter { it.isDigit() }
                    state.monthlyIncome =
                        if (digits.isEmpty()) "" else formatRupiah(digits.toLong(), withPrefix = false)
                },
                placeholder = "0",
                leadingText = "Rp",
                keyboardType = KeyboardType.Number,
                isError = showErrors && errors.income != null,
            )
            FieldError(errors.income.takeIf { showErrors })
        }

        // The simulation sits between the loan terms and the payout account, so
        // the customer sees what the instalment costs while the numbers that
        // produced it are still on screen - not after scrolling past the bank
        // details they have no reason to read first.
        Spacer(Modifier.height(20.dp))
        SimulationSummary(state)

        Spacer(Modifier.height(20.dp))
        SectionCard(padding = 22) {
            Text(
                text = "Payout Account",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Where the funds are sent once the loan is approved.",
                fontSize = 13.sp,
                color = TextSecondary,
            )

            Spacer(Modifier.height(18.dp))
            FieldLabel("Bank", required = true)
            AppTextField(
                value = state.bank,
                onValueChange = { state.bank = it },
                placeholder = "Example: BCA",
                isError = showErrors && errors.bank != null,
            )
            FieldError(errors.bank.takeIf { showErrors })

            Spacer(Modifier.height(20.dp))
            FieldLabel("Account Number", required = true)
            AppTextField(
                value = state.bankAccountNumber,
                onValueChange = { input -> state.bankAccountNumber = input.filter { it.isDigit() } },
                placeholder = "Account the funds are paid into",
                keyboardType = KeyboardType.Number,
                isError = showErrors && errors.accountNumber != null,
            )
            FieldError(errors.accountNumber.takeIf { showErrors })

            Spacer(Modifier.height(20.dp))
            FieldLabel("Account Holder Name", required = true)
            AppTextField(
                value = state.bankAccountName,
                onValueChange = { state.bankAccountName = it },
                placeholder = "As printed in your passbook",
                isError = showErrors && errors.accountName != null,
            )
            FieldError(errors.accountName.takeIf { showErrors })
        }

        if (showErrors && errors.hasAny) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Some required fields still need your attention. " +
                    "They are marked in red above.",
                fontSize = 13.sp,
                color = Danger,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DangerBg)
                    .padding(12.dp),
            )
        }

        Spacer(Modifier.height(28.dp))
        PrimaryButton(
            text = "Continue to Review",
            // Enabled even when incomplete, on purpose: a disabled button that
            // will not say why is the version of this that gets reported as a
            // bug. Pressing it names what is missing instead.
            onClick = {
                if (errors.hasAny) showErrors = true else onContinue()
            },
            trailingIcon = Icons.Filled.ArrowForward
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlineButton(
                text = "Back",
                onClick = onBack,
                modifier = Modifier.weight(1f)
            )
            OutlineButton(
                text = "Cancel",
                onClick = onCancel,
                contentColor = Danger,
                borderColor = Danger,
                background = DangerBg,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Amount as a slider *and* a typed field, kept on one value.
 *
 * The slider is for exploring, the field for the figure someone already has in
 * mind - 27,500,000 is unreachable by dragging a track that spans hundreds of
 * millions. Both go through [ApplyLoanState.updateLoanAmount], which is what
 * enforces the customer's credit limit on either route.
 */
@Composable
private fun AmountField(state: ApplyLoanState, error: String?) {

    val minAmount = state.minAmount
    val maxAmount = state.maxAmount

    FieldLabel("Loan Amount", required = true)
    AppTextField(
        value = if (state.loanAmount == 0L) "" else formatRupiah(state.loanAmount, withPrefix = false),
        onValueChange = { input ->
            val digits = input.filter(Char::isDigit)
            state.updateLoanAmount(digits.toLongOrNull() ?: 0L)
        },
        placeholder = "0",
        leadingText = "Rp",
        keyboardType = KeyboardType.Number,
        isError = error != null,
    )
    FieldError(error)

    // A slider needs room to move. When the customer's limit sits at or below
    // the smallest loan the range collapses to a point, and Material3 divides by
    // that width - so the field is left to stand on its own instead.
    if (maxAmount > minAmount) {
        Slider(
            // Coerced only for the track position: the typed value is left alone
            // so a half-entered number is not rewritten under their fingers.
            value = (state.loanAmount.coerceIn(minAmount, maxAmount) / 1_000_000L).toFloat(),
            onValueChange = { state.updateLoanAmount(it.roundToInt() * 1_000_000L) },
            valueRange = (minAmount / 1_000_000L).toFloat()..(maxAmount / 1_000_000L).toFloat(),
            colors = sliderColors()
        )
        RangeLabels(formatRupiah(minAmount), formatRupiah(maxAmount))
    }

    if (state.creditLimit != null) {
        Text(
            text = "Your available limit is ${formatRupiah(maxAmount)}.",
            fontSize = 12.sp,
            color = TextSecondary,
        )
    }

    state.tier?.let { tier ->
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Plafond " + tier.name + " (level " + tier.level + ") - " +
                tier.rateLabel + " p.a., " + tier.minTenor + "-" + tier.maxTenor +
                " months, admin " + formatRupiah(tier.adminFee),
            fontSize = 12.sp,
            color = TextSecondary,
        )
    }
}

@Composable
private fun TenureField(state: ApplyLoanState, error: String?) {

    val minTerm = state.tier?.minTenor ?: 1
    val maxTerm = state.tier?.maxTenor ?: 12

    FieldLabel("Tenure", required = true)
    AppTextField(
        value = if (state.termMonths == 0) "" else state.termMonths.toString(),
        onValueChange = { input ->
            val digits = input.filter(Char::isDigit).take(3)
            state.updateTermMonths(digits.toIntOrNull() ?: 0)
        },
        placeholder = "0",
        trailingText = "months",
        keyboardType = KeyboardType.Number,
        isError = error != null,
    )
    FieldError(error)

    // Same guard as the amount: a tier offering exactly one tenure would give
    // the slider a zero-width range.
    if (maxTerm > minTerm) {
        Slider(
            value = state.termMonths.coerceIn(minTerm, maxTerm).toFloat(),
            onValueChange = { state.updateTermMonths(it.roundToInt()) },
            valueRange = minTerm.toFloat()..maxTerm.toFloat(),
            colors = sliderColors()
        )
        RangeLabels("$minTerm months", "$maxTerm months")
    }
}


@Composable
private fun RangeLabels(start: String, end: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(start, fontSize = 12.sp, color = TextSecondary)
        Spacer(Modifier.weight(1f))
        Text(end, fontSize = 12.sp, color = TextSecondary, textAlign = TextAlign.End)
    }
}

@Composable
private fun sliderColors() = SliderDefaults.colors(
    thumbColor = Green,
    activeTrackColor = Green,
    inactiveTrackColor = TrackTodo
)

@Composable
private fun SimulationSummary(state: ApplyLoanState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(CardBg)
            .border(1.dp, CardBorder, CardShape)
    ) {
        // Soft green blob in the top-right corner, as in the mockup.
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 40.dp, y = (-60).dp)
                .size(200.dp)
                .clip(CircleShape)
                .background(Color(0xFFCFE6D6))
        )

        Column(Modifier.padding(20.dp)) {
            Text(
                text = "Instalment Simulation",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(Modifier.height(18.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Highlight)
                    .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Estimated Monthly Instalment",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = formatRupiah(state.monthlyPayment),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Green
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "/mo",
                        fontSize = 15.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            SummaryRow(
                label = "Interest (APR)",
                value = "${state.tier?.rateLabel ?: "-"} p.a.",
                leadingDot = true
            )
            HairLine()
            SummaryRow(label = "Principal", value = formatRupiah(state.loanAmount))
            HairLine()
            SummaryRow(label = "Admin Fee (one-off)", value = formatRupiah(state.adminFee))
            HairLine()
            SummaryRow(
                label = "Total Repayment",
                value = formatRupiah(state.totalPayment),
                bold = true
            )

            Spacer(Modifier.height(12.dp))
            Text(
                text = "Rates are estimates based on provided details. " +
                        "Final rates subject to credit approval.",
                fontSize = 12.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 1300)
@Composable
private fun LoanDetailsStepPreview() {
    Column(Modifier.background(ScreenBg)) {
        ApplyTopBar()
        StepIndicator(currentStep = 2)
        LoanDetailsStep(
            state = rememberApplyLoanState(),
            onBack = {},
            onCancel = {},
            onContinue = {}
        )
    }
}
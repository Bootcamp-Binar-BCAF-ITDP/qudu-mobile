package com.example.test2.ui.simulator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test2.core.LoanTier
import com.example.test2.ui.apply.AppTextField
import com.example.test2.ui.apply.CardBorder
import com.example.test2.ui.apply.Danger
import com.example.test2.ui.apply.FieldLabel
import com.example.test2.ui.apply.Green
import com.example.test2.ui.apply.Highlight
import com.example.test2.ui.apply.SectionCard
import com.example.test2.ui.apply.SummaryRow
import com.example.test2.ui.apply.TextMuted
import com.example.test2.ui.apply.TextPrimary
import com.example.test2.ui.apply.TextSecondary
import com.example.test2.ui.apply.TrackTodo
import com.example.test2.ui.apply.formatRupiah
import kotlin.math.roundToInt

private val Amber = com.example.test2.ui.theme.StatusAmber


private val AmberBg = com.example.test2.ui.theme.StatusAmberBg

@Composable
fun SimulatorPanel(
    viewModel: SimulatorViewModel,
    modifier: Modifier = Modifier,
    showTierRail: Boolean = true,
) {
    val tier = viewModel.tier
    val valid = viewModel.inputsValid

    Column(modifier = modifier.fillMaxWidth()) {

        AmountField(viewModel)

        Spacer(Modifier.height(16.dp))

        if (tier != null) {
            TierBadge(tier, showRail = showTierRail)
            Spacer(Modifier.height(16.dp))
        }

        TenorField(viewModel)

        Spacer(Modifier.height(18.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Highlight)
                .padding(16.dp),
        ) {
            Text("Estimated Monthly Instalment", fontSize = 13.sp, color = TextSecondary)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = if (valid) formatRupiah(viewModel.monthlyInstalment) else "-",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Green,
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "/mo",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        SummaryRow(
            label = "Interest rate",
            value = if (tier != null) "${tier.rateLabel} p.a." else "-",
            leadingDot = true,
        )
        SummaryRow(
            label = "Admin fee (one-off)",
            value = if (tier != null) formatRupiah(viewModel.adminFee) else "-",
        )
        SummaryRow(
            label = "Total repayment",
            value = if (valid) formatRupiah(viewModel.totalRepayment) else "-",
        )
        SummaryRow(
            label = "Total cost of credit",
            value = if (valid) formatRupiah(viewModel.totalCost) else "-",
            bold = true,
        )

        Spacer(Modifier.height(4.dp))
        Text(
            text = "An estimate only. Your final rate, tenure and fee follow the " +
                "plafond level approved for you.",
            fontSize = 12.sp,
            color = TextMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        if (!viewModel.live) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Showing the last published rates - we could not reach the " +
                    "server to refresh them.",
                fontSize = 12.sp,
                color = Amber,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(AmberBg)
                    .padding(10.dp),
            )
        }
    }
}

@Composable
private fun AmountField(viewModel: SimulatorViewModel) {

    val minAmount = viewModel.minAmount
    val maxAmount = viewModel.maxAmount
    val minMillions = (minAmount / 1_000_000L).coerceAtLeast(1L).toInt()
    val maxMillions = (maxAmount / 1_000_000L).toInt()

    FieldLabel("Loan Amount")
    AppTextField(
        value = if (viewModel.amount == 0L) "" else formatRupiah(viewModel.amount, withPrefix = false),
        onValueChange = { input ->
            val digits = input.filter(Char::isDigit).take(12)
            viewModel.updateAmount(digits.toLongOrNull() ?: 0L)
        },
        placeholder = "0",
        leadingText = "Rp",
        keyboardType = KeyboardType.Number,
        isError = !viewModel.amountInRange,
    )

    if (maxMillions > minMillions) {
        Spacer(Modifier.height(4.dp))
        Slider(
            value = (viewModel.amount.coerceIn(minAmount, maxAmount) / 1_000_000L).toFloat(),
            onValueChange = { viewModel.updateAmount(it.roundToInt() * 1_000_000L) },
            valueRange = minMillions.toFloat()..maxMillions.toFloat(),
            colors = simulatorSliderColors(),
        )
        RangeLabels(formatRupiah(minAmount), formatRupiah(maxAmount))
    }

    if (!viewModel.amountInRange) {
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Enter an amount between ${formatRupiah(minAmount)} and " +
                "${formatRupiah(maxAmount)}.",
            fontSize = 12.sp,
            color = Danger,
        )
    }
}

@Composable
private fun TenorField(viewModel: SimulatorViewModel) {

    val minTenor = viewModel.minTenor
    val maxTenor = viewModel.maxTenor

    FieldLabel("Tenure")
    AppTextField(
        value = if (viewModel.tenor == 0) "" else viewModel.tenor.toString(),
        onValueChange = { input ->
            val digits = input.filter(Char::isDigit).take(3)
            viewModel.updateTenor(digits.toIntOrNull() ?: 0)
        },
        placeholder = "0",
        trailingText = "months",
        keyboardType = KeyboardType.Number,
        isError = viewModel.amountInRange && !viewModel.tenorInRange,
    )

    if (maxTenor > minTenor) {
        Spacer(Modifier.height(4.dp))
        Slider(
            value = viewModel.tenor.coerceIn(minTenor, maxTenor).toFloat(),
            onValueChange = { viewModel.updateTenor(it.roundToInt()) },
            valueRange = minTenor.toFloat()..maxTenor.toFloat(),
            colors = simulatorSliderColors(),
        )
        RangeLabels("$minTenor months", "$maxTenor months")
    }

    if (viewModel.amountInRange && !viewModel.tenorInRange) {
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Choose a tenure between $minTenor and $maxTenor months.",
            fontSize = 12.sp,
            color = Danger,
        )
    }
}

@Composable
private fun TierBadge(tier: LoanTier, showRail: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Green.copy(alpha = 0.08f))
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = tier.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Green,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Level ${tier.level}",
                fontSize = 12.sp,
                color = TextSecondary,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "${tier.rateLabel} p.a.",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
        }

        if (showRail) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = "${formatRupiah(tier.minAmount)} - ${formatRupiah(tier.maxAmount)} · " +
                    "${tier.minTenor}-${tier.maxTenor} months · " +
                    "admin ${formatRupiah(tier.adminFee)}",
                fontSize = 12.sp,
                color = TextSecondary,
            )
        }
    }
}

@Composable
fun TierTable(tiers: List<LoanTier>, currentLevel: Int?, modifier: Modifier = Modifier) {
    SectionCard(modifier = modifier) {
        Text(
            text = "Plafond Levels",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "The bigger the loan, the lower the rate. Your level is set by " +
                "the limit approved for you.",
            fontSize = 13.sp,
            color = TextSecondary,
        )
        Spacer(Modifier.height(12.dp))

        tiers.forEachIndexed { index, tier ->
            if (index > 0) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(CardBorder.copy(alpha = 0.6f)),
                )
            }
            TierRow(tier, highlighted = tier.level == currentLevel)
        }
    }
}

@Composable
private fun TierRow(tier: LoanTier, highlighted: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (highlighted) Green.copy(alpha = 0.08f) else Color.Transparent)
            .padding(vertical = 10.dp, horizontal = if (highlighted) 10.dp else 0.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${tier.level}. ${tier.name}",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (highlighted) Green else TextPrimary,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "${tier.rateLabel} p.a.",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = "${formatRupiah(tier.minAmount)} - ${formatRupiah(tier.maxAmount)}",
            fontSize = 12.sp,
            color = TextSecondary,
        )
        Text(
            text = "${tier.minTenor}-${tier.maxTenor} months · admin ${formatRupiah(tier.adminFee)}",
            fontSize = 12.sp,
            color = TextMuted,
        )
    }
}

@Composable
private fun RangeLabels(start: String, end: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(start, fontSize = 12.sp, color = TextSecondary)
        Text(end, fontSize = 12.sp, color = TextSecondary, textAlign = TextAlign.End)
    }
}

@Composable
private fun simulatorSliderColors() = SliderDefaults.colors(
    thumbColor = Green,
    activeTrackColor = Green,
    inactiveTrackColor = TrackTodo,
)

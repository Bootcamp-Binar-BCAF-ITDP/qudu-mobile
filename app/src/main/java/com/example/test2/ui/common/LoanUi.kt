package com.example.test2.ui.common

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test2.core.LoanStatus
import com.example.test2.core.asRupiah
import com.example.test2.data.dto.LoanApplicationDto
import com.example.test2.ui.apply.CardBg
import com.example.test2.ui.apply.CardBorder
import com.example.test2.ui.apply.CardShape
import com.example.test2.ui.apply.Danger
import com.example.test2.ui.apply.DangerBg
import com.example.test2.ui.apply.Green
import com.example.test2.ui.apply.Highlight
import com.example.test2.ui.apply.TextMuted
import com.example.test2.ui.apply.TextPrimary
import com.example.test2.ui.apply.TextSecondary


private val ChipNeutralBg = com.example.test2.ui.theme.BrandHighlight
private val SuccessBg = com.example.test2.ui.theme.StatusSuccessBg

@Composable
fun StatusChip(status: String?) {

    val rejected = LoanStatus.isRejected(status)
    val settled = status == LoanStatus.DISBURSED

    val background = when {
        rejected -> DangerBg
        settled -> SuccessBg
        else -> ChipNeutralBg
    }
    val dot = when {
        rejected -> Danger
        else -> Green
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(background)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dot),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = LoanStatus.label(status),
            fontSize = 12.sp,
            lineHeight = 15.sp,
            color = if (rejected) Danger else TextPrimary,
        )
    }
}

@Composable
fun ApplicationCard(
    application: LoanApplicationDto,
    modifier: Modifier = Modifier,
    showTracker: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(CardBg)
            .border(1.dp, CardBorder, CardShape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(18.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = application.requestedAmount.asRupiah(),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = application.applicationId.orEmpty(),
                    fontSize = 13.sp,
                    color = TextMuted,
                )
            }
            StatusChip(application.status)
        }

        Spacer(Modifier.height(14.dp))
        Row {
            MetaColumn("Tenure", "${application.tenor ?: "-"} months", Modifier.weight(1f))
            MetaColumn("Purpose", application.purpose ?: "-", Modifier.weight(1.4f))
        }

        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.DateRange,
                contentDescription = null,
                tint = Green,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Submitted ${application.submissionDate ?: "-"}",
                fontSize = 13.sp,
                color = TextSecondary,
            )
        }

        rejectionReason(application)?.let { reason ->
            Spacer(Modifier.height(12.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DangerBg)
                    .padding(12.dp),
            ) {
                Text("Reason for rejection", fontSize = 12.sp, color = Danger)
                Spacer(Modifier.height(2.dp))
                Text(reason, fontSize = 14.sp, color = TextPrimary)
            }
        }

        if (application.status == LoanStatus.DISBURSED) {
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Highlight)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Disbursed to", fontSize = 13.sp, color = TextSecondary, modifier = Modifier.weight(1f))
                Text(
                    text = "${application.bank.orEmpty()} ${maskAccount(application.bankAccountNumber)}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                )
            }
        }

        if (showTracker) {
            Spacer(Modifier.height(16.dp))
            MiniTracker(application.status)
        }
    }
}

@Composable
private fun MetaColumn(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label, fontSize = 12.sp, color = TextMuted)
        Spacer(Modifier.height(2.dp))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
    }
}

@Composable
private fun MiniTracker(status: String?) {

    val reached = LoanStatus.trackerStep(status)
    val rejected = LoanStatus.isRejected(status)
    val labels = listOf("Submitted", "Review", "Decision")

    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            labels.indices.forEach { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            when {
                                index > reached -> CardBorder
                                rejected && index == reached -> Danger
                                else -> Green
                            },
                        ),
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Row {
            labels.forEachIndexed { index, label ->
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = if (index <= reached) TextSecondary else TextMuted,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
fun EmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(CardBg)
            .border(1.dp, CardBorder, CardShape)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(6.dp))
        Text(
            text = message,
            fontSize = 14.sp,
            color = TextSecondary,
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Green)
                    .clickable(onClick = onAction)
                    .padding(horizontal = 18.dp, vertical = 12.dp),
            ) {
                Text(actionLabel, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary,
        modifier = modifier,
    )
}

@Composable
fun ErrorText(message: String?, modifier: Modifier = Modifier) {
    if (message == null) return
    Text(
        text = message,
        fontSize = 13.sp,
        color = Danger,
        modifier = modifier,
    )
}

private fun rejectionReason(application: LoanApplicationDto): String? = when (application.status) {
    LoanStatus.REJECTED_BY_MARKETING -> application.review?.reviewNote
    LoanStatus.REJECTED_BY_BRANCH_MANAGER -> application.bmdecision?.decisionNote
    LoanStatus.REJECTED_BY_BACK_OFFICE -> application.disbursement?.decisionNote
    else -> null
}?.takeIf { it.isNotBlank() }

private fun maskAccount(accountNumber: String?): String {
    if (accountNumber.isNullOrBlank()) return "-"
    if (accountNumber.length <= 4) return "****"
    return "*".repeat(accountNumber.length - 4) + accountNumber.takeLast(4)
}

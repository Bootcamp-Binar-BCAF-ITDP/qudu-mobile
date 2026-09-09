package com.example.test2.ui.loans

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test2.core.LoanStatus
import com.example.test2.core.asRupiah
import com.example.test2.ui.apply.Green
import com.example.test2.ui.apply.Highlight
import com.example.test2.ui.apply.PrimaryButton
import com.example.test2.ui.apply.ScreenBg
import com.example.test2.ui.apply.SectionCard
import com.example.test2.ui.apply.TextPrimary
import com.example.test2.ui.apply.TextMuted
import com.example.test2.ui.apply.TextSecondary
import com.example.test2.ui.common.ApplicationCard
import com.example.test2.ui.common.EmptyState
import com.example.test2.ui.common.ErrorText
import com.example.test2.ui.common.SectionTitle

@Composable
fun LoansScreen(
    viewModel: ApplicationsViewModel,
    signedIn: Boolean,
    onApplyLoan: () -> Unit,
    onRequestUpgrade: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(signedIn) { if (signedIn) viewModel.refresh() }

    // No sign-in wall: the page renders normally for a visitor too, just empty.
    // The lists are empty because the ViewModel is never asked to load without a
    // session, not because anything failed.
    val active = viewModel.active
    val inReview = active.filter { it.status != LoanStatus.DISBURSED }
    val running = viewModel.disbursed

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(20.dp))

        Text(
            text = "My Loans",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Applications in progress and loans already disbursed.",
            fontSize = 15.sp,
            color = TextSecondary,
        )

        // Limit and "request an increase" are both account data, so the whole
        // card is absent for a visitor rather than showing an empty limit that
        // reads like an offer of nothing.
        if (signedIn) {
            Spacer(Modifier.height(18.dp))
            LimitCard(
                available = (viewModel.plafond?.availableLimit
                    ?: viewModel.plafond?.approvedLimit
                    ?: viewModel.plafond?.plafond?.maxAmount).asRupiah(),
                granted = viewModel.plafond?.approvedLimit.asRupiah(),
                used = viewModel.plafond?.usedLimit,
                level = viewModel.plafond?.plafond?.level,
                onRequestUpgrade = onRequestUpgrade,
            )

            ErrorText(viewModel.error, Modifier.padding(top = 14.dp))
        }

        Spacer(Modifier.height(20.dp))
        SectionTitle("In progress")
        Spacer(Modifier.height(10.dp))

        if (inReview.isEmpty()) {
            EmptyState(
                title = if (viewModel.isLoading) "Loading..." else "No applications in progress",
                // The signed-in copy promises stored documents, which a visitor
                // does not have - saying it to them would be a small lie.
                message = if (signedIn) {
                    "Apply any time - your identity documents are already on file."
                } else {
                    "Sign in to apply for a loan and follow it through every stage."
                },
                actionLabel = if (signedIn) "Apply for a Loan" else "Sign in to Apply",
                onAction = onApplyLoan,
            )
        } else {
            inReview.forEach { application ->
                ApplicationCard(
                    application = application,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
            }
        }

        if (running.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            SectionTitle("Disbursed")
            Spacer(Modifier.height(10.dp))
            running.forEach { application ->
                ApplicationCard(
                    application = application,
                    showTracker = false,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
            }
        }

        if (inReview.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            PrimaryButton(text = "Apply for a New Loan", onClick = onApplyLoan)
        }

        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun LimitCard(
    available: String,
    granted: String,
    used: java.math.BigDecimal?,
    level: Int?,
    onRequestUpgrade: () -> Unit,
) {
    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Available limit", fontSize = 14.sp, color = TextSecondary)
                Spacer(Modifier.height(4.dp))
                Text(available, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

                if ((used?.signum() ?: 0) > 0) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "of $granted · ${used.asRupiah()} in use",
                        fontSize = 13.sp,
                        color = TextMuted,
                    )
                }
            }
            Text(
                text = level?.let { "Level $it" } ?: "-",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Green,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Highlight)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
        Spacer(Modifier.height(14.dp))
        PrimaryButton(text = "Request a Limit Increase", onClick = onRequestUpgrade)
    }
}

package com.example.test2.ui.dashboard

import com.example.test2.core.DocumentTypes
import com.example.test2.core.LoanStatus
import com.example.test2.core.asRupiah
import com.example.test2.data.dto.CustomerPlafondDto
import com.example.test2.data.dto.LoanApplicationDto

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test2.ui.apply.OutlineButton
import com.example.test2.ui.apply.PrimaryButton
import com.example.test2.ui.apply.SectionCard
import com.example.test2.ui.simulator.SimulatorPanel
import com.example.test2.ui.simulator.SimulatorViewModel
import com.example.test2.ui.common.RefreshableScreen
import com.example.test2.ui.common.StatusChip
import com.example.test2.ui.common.UnreadBadge


private val Green = Color(0xFF0B6B2E)
private val ScreenBg = Color(0xFFF6F8FA)
private val CardBg = Color.White
private val CardBorder = Color(0xFFE6E8EB)
private val TextPrimary = Color(0xFF101828)
private val TextSecondary = Color(0xFF667085)
private val TextMuted = Color(0xFFB4BBC4)
private val TrackDone = Green
private val TrackTodo = Color(0xFFE0E3E7)
private val WarnBg = Color(0xFFFFF6E5)
private val Warn = Color(0xFFB25E02)

private val CardShape = RoundedCornerShape(16.dp)

@Composable
fun DashboardScreen(
    signedIn: Boolean = true,
    userName: String = "Alex",
    plafond: CustomerPlafondDto? = null,
    latestApplication: LoanApplicationDto? = null,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    profileComplete: Boolean = true,
    missingDocuments: List<String> = emptyList(),
    simulatorViewModel: SimulatorViewModel? = null,
    onApplyLoan: () -> Unit = {},
    onCompleteProfile: () -> Unit = {},
    onSeeLoans: () -> Unit = {},
    onBills: () -> Unit = {},
    onRequestUpgrade: () -> Unit = {},
    onLogin: () -> Unit = {},
    onRegister: () -> Unit = {},
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    modifier: Modifier = Modifier
) {
  RefreshableScreen(isRefreshing = isRefreshing, onRefresh = onRefresh, modifier = modifier) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {


        if (signedIn && !profileComplete) {
            Spacer(Modifier.height(20.dp))
            CompleteProfileCard(missing = missingDocuments, onClick = onCompleteProfile)
        }

        Spacer(Modifier.height(14.dp))

        if (signedIn) {
            Spacer(Modifier.height(14.dp))
            PlafondCard(plafond = plafond, onRequestUpgrade = onRequestUpgrade)
        }

        Spacer(Modifier.height(14.dp))

        ApplyLoanCard(onClick = onApplyLoan)

        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            TileCard(
                icon = Icons.Filled.List,
                label = "Status",
                onClick = onSeeLoans,
                modifier = Modifier.weight(1f)
            )
            TileCard(
                icon = Icons.Filled.DateRange,
                label = "Bills",
                onClick = onBills,
                modifier = Modifier.weight(1f)
            )
        }

        if (signedIn && errorMessage != null) {
            Spacer(Modifier.height(14.dp))
            Text(
                text = errorMessage,
                fontSize = 14.sp,
                color = Color(0xFFE03131)
            )
        }

        if (signedIn) {
            Spacer(Modifier.height(14.dp))
            CurrentApplicationCard(application = latestApplication, isLoading = isLoading)
        }


        if (!signedIn) {
            if (simulatorViewModel != null) {
                Spacer(Modifier.height(14.dp))
                SectionCard(padding = 22) {
                    Text(
                        text = "Loan Simulator",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                    )
                    Spacer(Modifier.height(16.dp))
                    SimulatorPanel(simulatorViewModel, showTierRail = false)
                }
            }
        }

        if (!signedIn) {
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlineButton(text = "Sign in", onClick = onLogin, modifier = Modifier.weight(1f))
                PrimaryButton(
                    text = "Create account",
                    onClick = onRegister,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Spacer(Modifier.height(28.dp))
    }
  }
}

@Composable
private fun CompleteProfileCard(missing: List<String>, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(WarnBg)
            .clickable(onClick = onClick)
            .padding(18.dp)
    ) {
        Text(
            text = "Complete your profile",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Warn
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = if (missing.isEmpty()) {
                "Upload your identity documents before applying for a loan."
            } else {
                "Not uploaded yet: " + missing.joinToString(", ") { DocumentTypes.label(it) } + "."
            },
            fontSize = 14.sp,
            color = TextPrimary
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Upload now →",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Warn
        )
    }
}

@Composable
private fun ApplyLoanCard(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(Green)
            .clickable(onClick = onClick)
            .padding(24.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.height(36.dp))
        Text(
            text = "Apply Loan",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Start a new application.",
            fontSize = 15.sp,
            color = Color.White.copy(alpha = 0.85f)
        )
    }
}

@Composable
private fun TileCard(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeCount: Long = 0
) {
    Box(
        modifier = modifier
            .height(160.dp)
            .clip(CardShape)
            .background(CardBg)
            .border(1.dp, CardBorder, CardShape)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Green,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = label,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }
        if (badgeCount > 0) {
            UnreadBadge(
                count = badgeCount,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            )
        }
    }
}

@Composable
private fun PlafondCard(plafond: CustomerPlafondDto?, onRequestUpgrade: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(CardBg)
            .border(1.dp, CardBorder, CardShape)
            .padding(20.dp)
    ) {
        val drawnDown = (plafond?.usedLimit?.signum() ?: 0) > 0

        Text(
            text = if (drawnDown) "Your remaining limit" else "Your limit",
            fontSize = 15.sp,
            color = TextSecondary
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = (plafond?.availableLimit ?: plafond?.approvedLimit
                ?: plafond?.plafond?.maxAmount).asRupiah(),
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(Modifier.height(4.dp))

        if (drawnDown) {
            Text(
                text = "Limit ${plafond?.approvedLimit.asRupiah()} · " +
                    "${plafond?.usedLimit.asRupiah()} in use",
                fontSize = 13.sp,
                color = TextMuted
            )
            Spacer(Modifier.height(2.dp))
        }

        Text(
            text = plafond?.plafond?.let { "Plafond level ${it.level} - ${it.description}" }
                ?: "Loading your plafond...",
            fontSize = 14.sp,
            color = TextSecondary
        )

        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Green)
                .clickable(onClick = onRequestUpgrade)
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Request a limit increase",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun CurrentApplicationCard(application: LoanApplicationDto?, isLoading: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(CardBg)
            .border(1.dp, CardBorder, CardShape)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = "Latest\nApplication",
                    fontSize = 24.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                StatusChip(application?.status)
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = when {
                    application != null ->
                        "${application.applicationId} - ${application.requestedAmount.asRupiah()}"
                    isLoading -> "Loading..."
                    else -> "You have not applied for a loan yet."
                },
                fontSize = 15.sp,
                color = TextSecondary
            )

            Spacer(Modifier.height(24.dp))
            ProgressTracker(status = application?.status)
            Spacer(Modifier.height(8.dp))
        }

        HairLine()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.DateRange,
                contentDescription = null,
                tint = Green,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "Submission date",
                fontSize = 15.sp,
                color = TextSecondary
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = application?.submissionDate ?: "-",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
    }
}

private enum class StepState { Done, Active, Todo }

@Composable
private fun ProgressTracker(status: String?) {

    val reached = LoanStatus.trackerStep(status)

    fun stateOf(index: Int): StepState = when {
        index < reached -> StepState.Done
        index == reached -> StepState.Active
        else -> StepState.Todo
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Step(label = "Submitted", state = stateOf(0))
        Connector(color = if (reached >= 1) TrackDone else TrackTodo)
        Step(label = "Review", state = stateOf(1))
        Connector(color = if (reached >= 2) TrackDone else TrackTodo)
        Step(label = "Decision", state = stateOf(2))
    }
}

@Composable
private fun RowScope.Connector(color: Color) {
    Box(
        modifier = Modifier
            .weight(1f)
            .padding(top = 16.dp, start = 4.dp, end = 4.dp)
            .height(2.dp)
            .background(color)
    )
}

@Composable
private fun Step(label: String, state: StepState) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        when (state) {
            StepState.Done -> Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Green),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            StepState.Active -> Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .border(2.dp, Green, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(Green)
                )
            }

            StepState.Todo -> Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .border(2.dp, TrackTodo, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (state == StepState.Active) FontWeight.SemiBold else FontWeight.Normal,
            color = when (state) {
                StepState.Todo -> TextMuted
                StepState.Active -> Green
                StepState.Done -> TextPrimary
            }
        )
    }
}

@Composable
private fun HairLine() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(CardBorder)
    )
}

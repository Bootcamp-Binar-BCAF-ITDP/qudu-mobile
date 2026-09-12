package com.example.test2.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test2.core.asRupiah
import com.example.test2.data.dto.PlafondRequestDto
import com.example.test2.ui.apply.CardBg
import com.example.test2.ui.apply.CardBorder
import com.example.test2.ui.apply.Danger
import com.example.test2.ui.apply.FieldShape
import com.example.test2.ui.apply.Green
import com.example.test2.ui.apply.ScreenBg
import com.example.test2.ui.apply.SectionCard
import com.example.test2.ui.apply.TextMuted
import com.example.test2.ui.apply.TextPrimary
import com.example.test2.ui.apply.TextSecondary
import com.example.test2.ui.common.ApplicationCard
import com.example.test2.ui.common.CachedDataNotice
import com.example.test2.ui.common.EmptyState
import com.example.test2.ui.common.ErrorText
import com.example.test2.ui.common.RefreshableScreen
import com.example.test2.ui.common.SignedOutCard
import com.example.test2.ui.loans.ApplicationsViewModel

private enum class HistoryFilter(val label: String) {
    Applications("Applications"),
    LimitRequests("Limit Increases"),
}

@Composable
fun HistoryScreen(
    viewModel: ApplicationsViewModel,
    signedIn: Boolean,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var filter by remember { mutableStateOf(HistoryFilter.Applications) }

    LaunchedEffect(signedIn) { if (signedIn) viewModel.refresh() }

    if (!signedIn) {
        SignedOutCard(
            title = "Sign in to see history",
            message = "Every application and limit increase you have made is kept ",
            onLogin = onLogin,
            modifier = modifier,
        )
        return
    }

  RefreshableScreen(
      isRefreshing = viewModel.isRefreshing,
      onRefresh = { viewModel.refresh(userInitiated = true) },
      modifier = modifier,
  ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(20.dp))


        FilterTabs(selected = filter, onSelect = { filter = it })

        CachedDataNotice(
            visible = viewModel.showingCached,
            fetchedAt = viewModel.lastSyncedAt,
            modifier = Modifier.padding(top = 14.dp),
        )
        if (!viewModel.showingCached) {
            ErrorText(viewModel.error, Modifier.padding(top = 14.dp))
        }

        Spacer(Modifier.height(16.dp))

        when (filter) {
            HistoryFilter.Applications -> {
                if (viewModel.applications.isEmpty()) {
                    EmptyState(
                        title = if (viewModel.isLoading) "Loading..." else "No history yet",
                        message = "Your loan applications will show up here.",
                    )
                } else {
                    viewModel.applications.forEach { application ->
                        ApplicationCard(
                            application = application,
                            showTracker = false,
                            modifier = Modifier.padding(bottom = 12.dp),
                        )
                    }
                }
            }

            HistoryFilter.LimitRequests -> {
                if (viewModel.plafondRequests.isEmpty()) {
                    EmptyState(
                        title = if (viewModel.isLoading) "Loading..." else "No limit requests yet",
                        message = "Limit increase requests and their decisions will show up here.",
                    )
                } else {
                    viewModel.plafondRequests.forEach { request ->
                        PlafondRequestCard(
                            request = request,
                            modifier = Modifier.padding(bottom = 12.dp),
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(28.dp))
    }
  }
}

@Composable
private fun FilterTabs(selected: HistoryFilter, onSelect: (HistoryFilter) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FieldShape)
            .background(CardBg)
            .padding(4.dp),
    ) {
        HistoryFilter.entries.forEach { option ->
            val isSelected = option == selected
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) Green else Color.Transparent)
                    .clickable { onSelect(option) }
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = option.label,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color.White else TextSecondary,
                )
            }
        }
    }
}

@Composable
private fun PlafondRequestCard(request: PlafondRequestDto, modifier: Modifier = Modifier) {

    val approved = request.status == "APPROVED"
    val rejected = request.status == "REJECTED"

    SectionCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = request.requestedAmount.asRupiah(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
                Spacer(Modifier.height(2.dp))
                Text(request.requestId.orEmpty(), fontSize = 13.sp, color = TextMuted)
            }
            Text(
                text = when (request.status) {
                    "APPROVED" -> "Approved"
                    "REJECTED" -> "Rejected"
                    "PENDING" -> "Awaiting decision"
                    else -> request.status.orEmpty()
                },
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = when {
                    approved -> Green
                    rejected -> Danger
                    else -> TextSecondary
                },
            )
        }

        Spacer(Modifier.height(12.dp))
        DetailLine("Level", buildString {
            append(request.previousLevel ?: "-")
            append(" → ")
            append(request.requestedLevel ?: "-")
        })
        if (approved) {
            DetailLine("Approved", request.approvedAmount.asRupiah())
        }
        DetailLine("Submitted", request.requestDate?.take(10) ?: "-")
        request.decisionDate?.let { DetailLine("Decided", it.take(10)) }
        request.reviewedBy?.takeIf { it.isNotBlank() }?.let { DetailLine("By", it) }
        request.notes?.takeIf { it.isNotBlank() }?.let { DetailLine("Notes", it) }
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(label, fontSize = 13.sp, color = TextSecondary, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(12.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            modifier = Modifier.weight(1.4f),
        )
    }
    Spacer(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(CardBorder.copy(alpha = 0.5f)),
    )
}

package com.example.test2.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.example.test2.data.dto.NotificationDto
import com.example.test2.ui.apply.CardBg
import com.example.test2.ui.apply.CardBorder
import com.example.test2.ui.apply.CardShape
import com.example.test2.ui.apply.Green
import com.example.test2.ui.apply.Highlight
import com.example.test2.ui.apply.OutlineButton
import com.example.test2.ui.apply.ScreenBg
import com.example.test2.ui.apply.TextMuted
import com.example.test2.ui.apply.TextPrimary
import com.example.test2.ui.apply.TextSecondary
import com.example.test2.ui.common.EmptyState
import com.example.test2.ui.common.ErrorText

@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) { viewModel.refresh() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(20.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Notifications",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (viewModel.unread > 0) {
                        "${viewModel.unread} unread"
                    } else {
                        "All caught up"
                    },
                    fontSize = 14.sp,
                    color = TextSecondary,
                )
            }
            if (viewModel.unread > 0) {
                Text(
                    text = "Mark all read",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Green,
                    modifier = Modifier.clickable { viewModel.markAllRead() },
                )
            }
        }

        ErrorText(viewModel.error, Modifier.padding(top = 14.dp))

        Spacer(Modifier.height(16.dp))

        if (viewModel.notifications.isEmpty()) {
            EmptyState(
                title = if (viewModel.isLoading) "Loading..." else "No notifications yet",
                message = "Decisions on your loan applications and limit requests appear here.",
            )
        } else {
            viewModel.notifications.forEach { notification ->
                NotificationCard(
                    notification = notification,
                    onClick = {
                        notification.notificationId
                            ?.takeIf { !notification.read }
                            ?.let(viewModel::markRead)
                    },
                    modifier = Modifier.padding(bottom = 12.dp),
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        OutlineButton(text = "Back", onClick = onBack, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun NotificationCard(
    notification: NotificationDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(if (notification.read) CardBg else Highlight)
            .border(1.dp, CardBorder, CardShape)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(8.dp)
                .clip(CircleShape)
                .background(if (notification.read) CardBorder else Green),
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = notification.title.orEmpty(),
                fontSize = 15.sp,
                fontWeight = if (notification.read) FontWeight.SemiBold else FontWeight.Bold,
                color = TextPrimary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = notification.body.orEmpty(),
                fontSize = 14.sp,
                lineHeight = 19.sp,
                color = TextSecondary,
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = notification.createdAt?.replace("T", " ")?.take(16) ?: "",
                    fontSize = 12.sp,
                    color = TextMuted,
                )
                notification.referenceId?.takeIf { it.isNotBlank() }?.let { reference ->
                    Spacer(Modifier.width(8.dp))
                    Text("·", fontSize = 12.sp, color = TextMuted)
                    Spacer(Modifier.width(8.dp))
                    Text(reference, fontSize = 12.sp, color = TextMuted)
                }
            }
        }
    }
}

package com.example.test2.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar
import java.util.Locale

private val Amber = com.example.test2.ui.theme.StatusAmber
private val AmberBg = com.example.test2.ui.theme.StatusAmberBg

@Composable
fun CachedDataNotice(
    visible: Boolean,
    fetchedAt: Long?,
    modifier: Modifier = Modifier,
) {
    if (!visible) return

    val stamp = fetchedAt?.let { formatTimestamp(it) }

    Text(
        text = if (stamp != null) {
            "Showing saved data from $stamp - we could not reach the server."
        } else {
            "Showing saved data - we could not reach the server."
        },
        fontSize = 12.sp,
        color = Amber,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AmberBg)
            .padding(10.dp),
    )
}

private fun formatTimestamp(epochMillis: Long): String {
    val calendar = Calendar.getInstance().apply { timeInMillis = epochMillis }

    val months = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
    )

    return String.format(
        Locale.US,
        "%d %s, %02d:%02d",
        calendar.get(Calendar.DAY_OF_MONTH),
        months[calendar.get(Calendar.MONTH)],
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
    )
}

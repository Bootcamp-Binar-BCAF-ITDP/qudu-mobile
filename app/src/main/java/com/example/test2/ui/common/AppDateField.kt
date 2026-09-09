package com.example.test2.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test2.ui.apply.FieldBg
import com.example.test2.ui.apply.FieldBorder
import com.example.test2.ui.apply.FieldShape
import com.example.test2.ui.apply.Green
import com.example.test2.ui.apply.TextMuted
import com.example.test2.ui.apply.TextPrimary
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

private val MONTHS = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December",
)

private const val EARLIEST_YEAR = 1930

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDateField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    futureAllowed: Boolean = false,
) {
    var showPicker by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(FieldShape)
            .background(FieldBg)
            .border(1.dp, FieldBorder, FieldShape)
            .clickable { showPicker = true }
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.DateRange,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = value.takeIf { it.isNotBlank() }?.let(::displayDate) ?: placeholder,
            fontSize = 15.sp,
            color = if (value.isBlank()) TextMuted else TextPrimary,
            modifier = Modifier.weight(1f),
        )
    }

    if (!showPicker) return

    val today = todayUtcMillis()

    val state = rememberDatePickerState(
        initialSelectedDateMillis = parseIsoToUtcMillis(value) ?: defaultBirthMillis(),
        yearRange = EARLIEST_YEAR..currentYear(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                futureAllowed || utcTimeMillis <= today

            override fun isSelectableYear(year: Int): Boolean =
                futureAllowed || year <= currentYear()
        },
    )

    DatePickerDialog(
        onDismissRequest = { showPicker = false },
        confirmButton = {
            TextButton(
                onClick = {
                    state.selectedDateMillis?.let { onValueChange(isoFromUtcMillis(it)) }
                    showPicker = false
                },
            ) {
                Text("Select", color = Green)
            }
        },
        dismissButton = {
            TextButton(onClick = { showPicker = false }) {
                Text("Cancel", color = TextMuted)
            }
        },
    ) {
        DatePicker(state = state)
    }
}

private fun displayDate(iso: String): String {
    val parts = iso.split("-")
    if (parts.size != 3) return iso

    val year = parts[0].toIntOrNull() ?: return iso
    val month = parts[1].toIntOrNull() ?: return iso
    val day = parts[2].toIntOrNull() ?: return iso

    if (month !in 1..12) return iso

    return "$day ${MONTHS[month - 1]} $year"
}

private fun utcCalendar(): Calendar =
    Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US)

private fun parseIsoToUtcMillis(iso: String): Long? {
    val parts = iso.split("-")
    if (parts.size != 3) return null

    val year = parts[0].toIntOrNull() ?: return null
    val month = parts[1].toIntOrNull() ?: return null
    val day = parts[2].toIntOrNull() ?: return null

    return utcCalendar().apply {
        clear()
        set(year, month - 1, day)
    }.timeInMillis
}

private fun isoFromUtcMillis(millis: Long): String {
    val calendar = utcCalendar().apply { timeInMillis = millis }

    return "%04d-%02d-%02d".format(
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH) + 1,
        calendar.get(Calendar.DAY_OF_MONTH),
    )
}

private fun todayUtcMillis(): Long = utcCalendar().apply {
    set(Calendar.HOUR_OF_DAY, 23)
    set(Calendar.MINUTE, 59)
    set(Calendar.SECOND, 59)
    set(Calendar.MILLISECOND, 999)
}.timeInMillis

private fun currentYear(): Int = utcCalendar().get(Calendar.YEAR)

private fun defaultBirthMillis(): Long = utcCalendar().apply {
    add(Calendar.YEAR, -25)
    set(Calendar.MONTH, Calendar.JANUARY)
    set(Calendar.DAY_OF_MONTH, 1)
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis

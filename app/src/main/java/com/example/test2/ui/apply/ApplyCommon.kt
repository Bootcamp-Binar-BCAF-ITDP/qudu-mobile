package com.example.test2.ui.apply

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test2.R
import kotlin.math.pow
import kotlin.math.roundToLong

/* ---------- Palette ---------- */

internal val Green = Color(0xFF0B6B2E)
internal val GreenBright = Color(0xFF1E9E4A)
internal val ScreenBg = Color(0xFFF6F8FA)
internal val CardBg = Color.White
internal val CardBorder = Color(0xFFE6E8EB)
internal val FieldBg = Color(0xFFF8F9FB)
internal val FieldBorder = Color(0xFFE3E6EA)
internal val TextPrimary = Color(0xFF101828)
internal val TextSecondary = Color(0xFF667085)
internal val TextMuted = Color(0xFF98A2B3)
internal val TrackTodo = Color(0xFFDCE7F1)
internal val StepTodoBg = Color(0xFFDCE7F1)
internal val Danger = Color(0xFFE03131)
internal val DangerBg = Color(0xFFFEF3F3)
internal val Highlight = Color(0xFFEEF3F8)

internal val CardShape = RoundedCornerShape(16.dp)
internal val FieldShape = RoundedCornerShape(10.dp)

/* ---------- Camera capture ---------- */
internal fun createCaptureImageUri(context: Context): Uri {
    val imagesDir = java.io.File(context.cacheDir, "images").apply { mkdirs() }
    val file = java.io.File(imagesDir, "selfie_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

/* ---------- Money + math helpers ---------- */

internal fun formatRupiah(amount: Long, withPrefix: Boolean = true): String {
    val digits = amount.toString()
    val sb = StringBuilder()
    digits.forEachIndexed { i, c ->
        if (i > 0 && (digits.length - i) % 3 == 0) sb.append('.')
        sb.append(c)
    }
    return if (withPrefix) "Rp$sb" else sb.toString()
}

internal fun monthlyInstallment(principal: Long, months: Int, apr: Double): Long {
    if (months <= 0) return 0
    val r = apr / 12.0
    if (r == 0.0) return principal / months
    val raw = principal * r / (1 - (1 + r).pow(-months.toDouble()))
    return (raw / 1000.0).roundToLong() * 1000
}

/* ---------- Chrome ---------- */
@Composable
internal fun ApplyTopBar(stepLabel: String? = null) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ScreenBg)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.logo_wordmark),
                contentDescription = "QuickDuit",
                modifier = Modifier.height(24.dp)
            )
            Spacer(Modifier.weight(1f))
            if (stepLabel != null) {
                Text(
                    text = stepLabel,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = "Notifications",
                    tint = TextPrimary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(16.dp))
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, TextPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Profile",
                        tint = TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
        HairLine()
    }
}

/**
 * @param onStepClick when given, the dots become a way back (and forward, if
 *        the flow allows it) rather than decoration. The flow decides which
 *        jumps are legal - this only reports the tap.
 */
@Composable
internal fun StepIndicator(currentStep: Int, onStepClick: ((Int) -> Unit)? = null) {
    val labels = listOf("Documents", "Details", "Review")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ScreenBg)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.Top
    ) {
        labels.forEachIndexed { index, label ->
            val number = index + 1
            if (index > 0) {
                Connector(color = if (number <= currentStep) Green else TrackTodo)
            }
            StepDot(
                number = number,
                label = label,
                currentStep = currentStep,
                onClick = onStepClick?.let { click -> { click(number) } },
            )
        }
    }
}

@Composable
private fun RowScope.Connector(color: Color) {
    Box(
        modifier = Modifier
            .weight(1f)
            .padding(top = 15.dp, start = 2.dp, end = 2.dp)
            .height(2.dp)
            .background(color)
    )
}

@Composable
private fun StepDot(
    number: Int,
    label: String,
    currentStep: Int,
    onClick: (() -> Unit)? = null,
) {
    val done = number < currentStep
    val active = number == currentStep

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = if (onClick == null) Modifier else Modifier.clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    when {
                        done -> Green
                        active -> CardBg
                        else -> StepTodoBg
                    }
                )
                .then(if (active) Modifier.border(2.dp, Green, CircleShape) else Modifier),
            contentAlignment = Alignment.Center
        ) {
            if (done) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Text(
                    text = number.toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (active) TextPrimary else TextSecondary
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (active || done) FontWeight.SemiBold else FontWeight.Normal,
            color = when {
                done -> Green
                active -> TextPrimary
                else -> TextSecondary
            }
        )
    }
}

/* ---------- Building blocks ---------- */
@Composable
internal fun SectionCard(
    modifier: Modifier = Modifier,
    padding: Int = 20,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(CardBg)
            .border(1.dp, CardBorder, CardShape)
            .padding(padding.dp),
        content = content
    )
}

@Composable
internal fun FieldLabel(text: String, required: Boolean = false) {
    Row(modifier = Modifier.padding(bottom = 6.dp)) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
        )
        // Marked before anything is submitted, not after it fails - the point is
        // to tell the customer what is mandatory while they are still filling it.
        if (required) {
            Text(text = " *", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Danger)
        }
    }
}

/** The one-line reason a field is refused. Renders nothing when [message] is null. */
@Composable
internal fun FieldError(message: String?) {
    if (message == null) return
    Text(
        text = message,
        fontSize = 12.sp,
        color = Danger,
        modifier = Modifier.padding(top = 6.dp),
    )
}

@Composable
internal fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector? = null,
    leadingText: String? = null,
    trailingIcon: ImageVector? = null,
    trailingText: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    isError: Boolean = false,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        textStyle = LocalTextStyle.current.copy(fontSize = 15.sp, color = TextPrimary),
        cursorBrush = SolidColor(Green),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier.fillMaxWidth(),
        decorationBox = { inner ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(FieldShape)
                    .background(FieldBg)
                    .border(if (isError) 1.5.dp else 1.dp, if (isError) Danger else FieldBorder, FieldShape)
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (leadingIcon != null) {
                    Icon(leadingIcon, null, tint = TextMuted, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                }
                if (leadingText != null) {
                    Text(leadingText, fontSize = 15.sp, color = TextSecondary)
                    Spacer(Modifier.width(8.dp))
                }
                Box(Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(placeholder, fontSize = 15.sp, color = TextMuted)
                    }
                    inner()
                }
                if (trailingText != null) {
                    Spacer(Modifier.width(8.dp))
                    Text(trailingText, fontSize = 15.sp, color = TextSecondary)
                }
                if (trailingIcon != null) {
                    Spacer(Modifier.width(10.dp))
                    Icon(trailingIcon, null, tint = TextMuted, modifier = Modifier.size(18.dp))
                }
            }
        }
    )
}

@Composable
internal fun <T> AppDropdownField(
    options: List<T>,
    selected: T?,
    onSelect: (T) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    isError: Boolean = false,
    label: (T) -> String = { it.toString() },
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(FieldShape)
                .background(FieldBg)
                .border(if (isError) 1.5.dp else 1.dp, if (isError) Danger else FieldBorder, FieldShape)
                .clickable { expanded = true }
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIcon != null) {
                Icon(leadingIcon, null, tint = TextMuted, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
            }
            Text(
                text = selected?.let(label) ?: placeholder,
                fontSize = 15.sp,
                color = if (selected == null) TextMuted else TextPrimary,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Filled.KeyboardArrowDown,
                null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(label(option)) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
internal fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Green,
    enabled: Boolean = true,
    trailingIcon: ImageVector? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(FieldShape)
            .background(if (enabled) color else color.copy(alpha = 0.4f))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
        if (trailingIcon != null) {
            Spacer(Modifier.width(8.dp))
            Icon(trailingIcon, null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
internal fun OutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentColor: Color = TextPrimary,
    borderColor: Color = FieldBorder,
    background: Color = CardBg
) {
    Box(
        modifier = modifier
            .clip(FieldShape)
            .background(background)
            .border(1.dp, borderColor, FieldShape)
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = contentColor)
    }
}

@Composable
internal fun HairLine(color: Color = CardBorder) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(color)
    )
}

@Composable
internal fun SummaryRow(
    label: String,
    value: String,
    valueColor: Color = TextPrimary,
    bold: Boolean = false,
    leadingDot: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            color = if (bold) TextPrimary else TextSecondary
        )
        Spacer(Modifier.weight(1f))
        if (leadingDot) {
            Box(
                Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(GreenBright)
            )
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}
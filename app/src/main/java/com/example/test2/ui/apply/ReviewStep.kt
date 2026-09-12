package com.example.test2.ui.apply

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test2.core.DocumentTypes
import com.example.test2.data.dto.CustomerProfileDto

@Composable
fun ReviewStep(
    state: ApplyLoanState,
    profile: CustomerProfileDto?,
    onBack: () -> Unit,
    onEditDocuments: () -> Unit,
    onEditLoanDetails: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "Review Your Application",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Make sure everything is correct before you submit.",
            fontSize = 15.sp,
            color = TextSecondary
        )

        Spacer(Modifier.height(20.dp))
        SectionCard {
            CardHeader(
                icon = Icons.Filled.Person,
                title = "Personal Details",
                onEdit = null,
            )
            DetailRow("Full Name", profile?.customerName)
            DetailRow("NIK (KTP)", profile?.nik)
            DetailRow("Email", profile?.email)
            DetailRow("Phone Number", profile?.phoneNumber)
            Text(
                text = "Taken from your profile. Change it on the Profile tab if needed.",
                fontSize = 12.sp,
                color = TextMuted,
            )
        }

        Spacer(Modifier.height(16.dp))
        SectionCard {
            CardHeader(
                icon = Icons.Filled.List,
                title = "Documents",
                onEdit = onEditDocuments
            )

            DocumentTypes.PROFILE.forEach { type ->
                val replaced = state.isCaptured(type)
                DocumentStatusRow(
                    label = DocumentTypes.label(type),
                    present = replaced || profile?.documentOf(type) != null,
                    note = when {
                        replaced -> "Replaced"
                        else -> "From profile"
                    },
                )
            }

            Spacer(Modifier.height(8.dp))

            DocumentTypes.APPLICATION.forEach { type ->
                DocumentStatusRow(
                    label = DocumentTypes.label(type),
                    present = state.isCaptured(type),
                    note = if (state.isCaptured(type)) "Ready to upload" else "Not selected",
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        SectionCard {
            CardHeader(
                icon = Icons.Filled.ShoppingCart,
                title = "Loan Details",
                onEdit = onEditLoanDetails
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Highlight)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Requested Amount",
                    fontSize = 15.sp,
                    color = TextSecondary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = formatRupiah(state.loanAmount),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Green
                )
            }

            Spacer(Modifier.height(14.dp))
            DetailRow("Tenure", "${state.termMonths} months")
            DetailRow("Interest", "${state.tier?.rateLabel ?: "-"} per year")
            DetailRow("Estimated Instalment", formatRupiah(state.monthlyPayment))
            DetailRow("Admin Fee", formatRupiah(state.adminFee))
            DetailRow("Purpose", state.purposeForSubmission)
            DetailRow("Payout Account", listOfNotNull(
                state.bank.takeIf { it.isNotBlank() },
                state.bankAccountNumber.takeIf { it.isNotBlank() },
            ).joinToString(" · ").ifBlank { null })
        }

        Spacer(Modifier.height(16.dp))
        ConsentCard(
            checked = state.termsAccepted,
            onToggle = { state.termsAccepted = !state.termsAccepted }
        )

        Spacer(Modifier.height(16.dp))
        PrimaryButton(
            text = "Submit Application",
            onClick = onSubmit,
            color = GreenBright,
            enabled = state.termsAccepted,
            trailingIcon = Icons.Filled.Send
        )

        Spacer(Modifier.height(12.dp))
        OutlineButton(
            text = "Back to Loan Details",
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun CardHeader(icon: ImageVector, title: String, onEdit: (() -> Unit)?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Green, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(10.dp))
        Text(
            text = title,
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        if (onEdit != null) {
            Text(
                text = "Edit",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Green,
                modifier = Modifier.clickable(onClick = onEdit)
            )
        }
    }
    Spacer(Modifier.height(12.dp))
    HairLine()
    Spacer(Modifier.height(14.dp))
}

@Composable
private fun DetailRow(label: String, value: String?) {
    Column(Modifier.padding(bottom = 14.dp)) {
        Text(label, fontSize = 13.sp, color = TextSecondary)
        Spacer(Modifier.height(2.dp))
        Text(
            text = value?.takeIf { it.isNotBlank() } ?: "-",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
        )
    }
}

@Composable
private fun DocumentStatusRow(label: String, present: Boolean, note: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (present) Highlight else Color.Transparent)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 15.sp, color = TextPrimary, modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = if (present) Green else TextMuted,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = note,
            fontSize = 13.sp,
            color = if (present) Green else TextMuted
        )
    }
}

@Composable
private fun ConsentCard(checked: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(CardBg)
            .border(1.dp, CardBorder, CardShape)
            .clickable(onClick = onToggle)
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(if (checked) Green else Color.White)
                .border(1.5.dp, if (checked) Green else FieldBorder, RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Icon(
                    Icons.Filled.Check,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = "I declare that the information I have given is true and complete, " +
                        "and I agree to the",
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = TextPrimary
            )
            Row {
                Text(
                    text = "Terms and Conditions",
                    fontSize = 14.sp,
                    color = Green,
                    modifier = Modifier.clickable { }
                )
                Text(" and ", fontSize = 14.sp, color = TextPrimary)
                Text(
                    text = "Privacy Policy",
                    fontSize = 14.sp,
                    color = Green,
                    modifier = Modifier.clickable { }
                )
            }
            Text(text = "Quick Duit.", fontSize = 14.sp, color = TextPrimary)
        }
    }
}

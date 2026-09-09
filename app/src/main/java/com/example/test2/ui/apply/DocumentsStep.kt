package com.example.test2.ui.apply

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test2.core.DocumentTypes
import com.example.test2.data.dto.CustomerProfileDto
import com.example.test2.ui.common.rememberDocumentCapture
import com.example.test2.ui.profile.DocumentRow

@Composable
fun DocumentsStep(
    state: ApplyLoanState,
    profile: CustomerProfileDto?,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val capture = rememberDocumentCapture { type, uri -> state.setDocumentUri(type, uri) }
    // A type is satisfied when it was captured in this flow, or is already on
    // file and still fresh. Financial evidence expires, so a stored payslip past
    // its window counts as missing here exactly as it does on the server.
    val missing = DocumentTypes.REQUIRED_FOR_SUBMISSION.filter { type ->
        !state.isCaptured(type) && profile?.needsUpload(type) != false
    }

    // Named only after the customer tries to continue - see the block near the
    // buttons. Listing what is absent before they have started is nagging.
    var showMissing by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp),
    ) {
        SectionCard(padding = 20) {
            Text(
                text = "Personal Details",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Taken from your profile.",
                fontSize = 14.sp,
                color = TextSecondary,
            )
            Spacer(Modifier.height(14.dp))

            IdentityLine("Name", profile?.customerName)
            IdentityLine("NIK", profile?.nik)
            IdentityLine("Email", profile?.email)
            IdentityLine("Phone", profile?.phoneNumber)
            IdentityLine("Occupation", profile?.occupation)
        }

        Spacer(Modifier.height(16.dp))
        SectionCard(padding = 20) {
            Text(
                text = "Identity Documents",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Already on your profile and reused automatically. " +
                    "Tap one only if you want to replace it.",
                fontSize = 14.sp,
                color = TextSecondary,
            )
            Spacer(Modifier.height(14.dp))

            DocumentTypes.PROFILE.forEachIndexed { index, type ->
                if (index > 0) Spacer(Modifier.height(10.dp))

                val stored = profile?.documentOf(type)
                val replacement = state.isCaptured(type)

                DocumentRow(
                    documentType = type,
                    fileName = if (replacement) "New file selected" else stored?.fileName,
                    uploadedAt = if (replacement) null else stored?.uploadedAt,
                    isUploading = false,
                    onCapture = capture,
                )
            }

            if (profile != null && !profile.profileComplete) {
                Spacer(Modifier.height(12.dp))
                Notice(
                    text = "Your profile documents are incomplete: " +
                        profile.missingDocuments.joinToString(", ") { DocumentTypes.label(it) } +
                        ". Upload them here before continuing.",
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        SectionCard(padding = 20) {
            Text(
                text = "Application Documents",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Payslips and passbooks stay valid for 30 days. " +
                    "After that they must be uploaded again before you apply.",
                fontSize = 14.sp,
                color = TextSecondary,
            )
            Spacer(Modifier.height(14.dp))

            DocumentTypes.APPLICATION.forEachIndexed { index, type ->
                if (index > 0) Spacer(Modifier.height(10.dp))
                DocumentRow(
                    documentType = type,
                    fileName = when {
                        state.isCaptured(type) -> "New file selected"
                        else -> profile?.documentOf(type)?.fileName
                    },
                    uploadedAt = if (state.isCaptured(type)) null
                        else profile?.documentOf(type)?.uploadedAt,
                    isUploading = false,
                    onCapture = capture,
                    isStale = !state.isCaptured(type) && profile?.isStale(type) == true,
                )
            }
        }

        // Named, not just counted. The button used to be disabled and say
        // "2 file(s) missing", which tells a customer they are stuck without
        // telling them which two.
        if (showMissing && missing.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Still needed: " +
                    missing.joinToString(", ") { DocumentTypes.label(it) } + ".",
                fontSize = 13.sp,
                color = Danger,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DangerBg)
                    .padding(12.dp),
            )
        }

        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlineButton(
                text = "Back",
                onClick = onBack,
                modifier = Modifier.weight(1f),
            )
            PrimaryButton(
                text = "Continue",
                onClick = {
                    if (missing.isEmpty()) onContinue() else showMissing = true
                },
                trailingIcon = Icons.Filled.ArrowForward,
                modifier = Modifier.weight(1.2f),
            )
        }
    }
}

@Composable
private fun IdentityLine(label: String, value: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(label, fontSize = 14.sp, color = TextSecondary, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(12.dp))
        Text(
            text = value?.takeIf { it.isNotBlank() } ?: "-",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            modifier = Modifier.weight(1.6f),
        )
    }
}

@Composable
private fun Notice(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        color = TextPrimary,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Highlight)
            .padding(12.dp),
    )
}

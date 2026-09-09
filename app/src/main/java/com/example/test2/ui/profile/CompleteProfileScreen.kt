package com.example.test2.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test2.core.DocumentTypes
import com.example.test2.ui.apply.Highlight
import com.example.test2.ui.apply.OutlineButton
import com.example.test2.ui.apply.PrimaryButton
import com.example.test2.ui.apply.ScreenBg
import com.example.test2.ui.apply.SectionCard
import com.example.test2.ui.apply.TextPrimary
import com.example.test2.ui.apply.TextSecondary
import com.example.test2.ui.auth.MessageArea
import com.example.test2.ui.common.rememberDocumentCapture

@Composable
fun CompleteProfileScreen(
    viewModel: ProfileViewModel,
    onDone: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val profile = viewModel.profile
    val capture = rememberDocumentCapture { type, uri -> viewModel.uploadDocument(type, uri) }

    LaunchedEffect(Unit) { viewModel.refresh() }

    LaunchedEffect(profile?.profileComplete) {
        if (profile?.profileComplete == true) onDone()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBg)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
    ) {
        Spacer(Modifier.height(24.dp))

        Text(
            text = "Complete Your Profile",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Upload your identity documents just once. Every later loan " +
                "application then only needs an amount and a payslip.",
            fontSize = 14.sp,
            color = TextSecondary,
        )

        Spacer(Modifier.height(20.dp))
        SectionCard {
            DocumentTypes.PROFILE.forEachIndexed { index, type ->
                if (index > 0) Spacer(Modifier.height(12.dp))
                val document = profile?.documentOf(type)
                DocumentRow(
                    documentType = type,
                    fileName = document?.fileName,
                    uploadedAt = document?.uploadedAt,
                    isUploading = viewModel.uploadingType == type,
                    onCapture = capture,
                )
            }

            MessageArea(error = viewModel.error, info = viewModel.info)

            Spacer(Modifier.height(20.dp))

            val remaining = viewModel.missingDocuments.size
            PrimaryButton(
                text = if (remaining == 0) "Done" else "$remaining document(s) left",
                enabled = remaining == 0,
                onClick = onDone,
            )
        }

        Spacer(Modifier.height(16.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Highlight)
                .padding(14.dp),
        ) {
            Text(
                text = "Why now?",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "These documents belong to your account, not to one application. " +
                    "Upload them once and every later application reuses them.",
                fontSize = 13.sp,
                color = TextSecondary,
            )
        }

        Spacer(Modifier.height(16.dp))
        OutlineButton(
            text = "Maybe later",
            onClick = onSkip,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(32.dp))
    }
}

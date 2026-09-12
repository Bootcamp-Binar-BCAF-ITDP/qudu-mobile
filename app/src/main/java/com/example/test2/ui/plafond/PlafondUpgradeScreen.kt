package com.example.test2.ui.plafond

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.test2.di.rememberViewModelFactory
import com.example.test2.ui.apply.*
import com.example.test2.core.DocumentTypes
import com.example.test2.ui.auth.MessageArea
import com.example.test2.ui.common.CachedDataNotice
import com.example.test2.ui.common.RefreshableScreen
import com.example.test2.ui.common.rememberDocumentCapture
import com.example.test2.ui.profile.DocumentRow
import com.example.test2.ui.profile.ProfileViewModel

@Composable
fun PlafondUpgradeScreen(
    profileViewModel: ProfileViewModel,
    onBack: () -> Unit,
    onSubmitted: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: PlafondViewModel = viewModel(factory = rememberViewModelFactory())

    var amount by remember { mutableStateOf("") }

    val profile = profileViewModel.profile
    val blocking = DocumentTypes.REQUIRED_FOR_SUBMISSION.filter { profile?.needsUpload(it) != false }
    val capture = rememberDocumentCapture { type, uri -> profileViewModel.uploadDocument(type, uri) }

    LaunchedEffect(Unit) {
        viewModel.loadRequests()
        profileViewModel.refresh()
    }

  RefreshableScreen(
      isRefreshing = viewModel.isRefreshing,
      onRefresh = {
          viewModel.loadRequests(userInitiated = true)
          profileViewModel.refresh(userInitiated = true)
      },
      modifier = modifier,
  ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
    ) {
        Spacer(Modifier.height(14.dp))

        Text(
            text = "Limit Increase",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
        )

        Spacer(Modifier.height(14.dp))

        SectionCard {
            FieldLabel("Amount you want")
            AppTextField(
                value = amount,
                onValueChange = { input ->
                    val digits = input.filter(Char::isDigit)
                    amount = if (digits.isEmpty()) {
                        ""
                    } else {
                        formatRupiah(digits.toLong(), withPrefix = false)
                    }
                },
                placeholder = "0",
                leadingText = "Rp",
                keyboardType = KeyboardType.Number,
            )

            MessageArea(error = viewModel.error, info = null)

            Spacer(Modifier.height(20.dp))

            PrimaryButton(
                text = when {
                    viewModel.isBusy -> "Sending..."
                    blocking.isNotEmpty() -> "${blocking.size} document(s) missing"
                    else -> "Request a limit increase"
                },
                enabled = !viewModel.isBusy && blocking.isEmpty(),
                onClick = {
                    viewModel.submit(amount) { message ->
                        amount = ""
                        onSubmitted(message)
                    }
                },
            )
        }

        Spacer(Modifier.height(16.dp))
        SectionCard {
            Text(
                text = "Supporting Documents",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "The branch manager reviews your request against these documents. " +
                    "Your payslip and passbook should be the most recent ones.",
                fontSize = 13.sp,
                color = TextSecondary,
            )
            Spacer(Modifier.height(14.dp))

            DocumentTypes.REQUIRED_FOR_SUBMISSION.forEachIndexed { index, type ->
                if (index > 0) Spacer(Modifier.height(10.dp))
                val document = profile?.documentOf(type)
                DocumentRow(
                    documentType = type,
                    fileName = document?.fileName,
                    uploadedAt = document?.uploadedAt,
                    isUploading = profileViewModel.uploadingType == type,
                    onCapture = capture,
                    isStale = profile?.isStale(type) == true,
                )
            }

            MessageArea(error = profileViewModel.error, info = profileViewModel.info)
        }

        if (viewModel.requests.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Request history",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            CachedDataNotice(
                visible = viewModel.showingCached,
                fetchedAt = viewModel.lastSyncedAt,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            for (request in viewModel.requests) {
                SectionCard(modifier = Modifier.padding(bottom = 12.dp)) {
                    Text(
                        text = request.requestId.orEmpty(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                    )
                    Spacer(Modifier.height(6.dp))
                    SummaryRow(
                        label = "Requested",
                        value = formatRupiah(
                            request.requestedAmount?.toLong() ?: 0L,
                        ),
                    )
                    SummaryRow(label = "Status", value = request.status.orEmpty())
                    request.notes?.takeIf { it.isNotBlank() }?.let {
                        SummaryRow(label = "Notes", value = it)
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        OutlineButton(text = "Back", onClick = onBack, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(32.dp))
    }
  }
}

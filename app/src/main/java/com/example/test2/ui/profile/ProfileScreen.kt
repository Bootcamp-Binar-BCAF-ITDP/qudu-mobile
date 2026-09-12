package com.example.test2.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test2.core.DocumentTypes
import com.example.test2.core.asRupiah
import com.example.test2.ui.apply.AppTextField
import com.example.test2.ui.apply.CardBg
import com.example.test2.ui.apply.CardBorder
import com.example.test2.ui.apply.CardShape
import com.example.test2.ui.apply.Danger
import com.example.test2.ui.apply.DangerBg
import com.example.test2.ui.apply.FieldLabel
import com.example.test2.ui.apply.Green
import com.example.test2.ui.apply.Highlight
import com.example.test2.ui.apply.OutlineButton
import com.example.test2.ui.apply.PrimaryButton
import com.example.test2.ui.apply.ScreenBg
import com.example.test2.ui.apply.SectionCard
import com.example.test2.ui.apply.TextMuted
import com.example.test2.ui.apply.TextPrimary
import com.example.test2.ui.apply.TextSecondary
import com.example.test2.ui.auth.MessageArea
import com.example.test2.ui.common.CachedDataNotice
import com.example.test2.ui.common.RefreshableScreen
import com.example.test2.ui.common.SectionTitle
import com.example.test2.ui.common.SignedOutCard
import com.example.test2.ui.common.rememberDocumentCapture

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    signedIn: Boolean,
    onLogout: () -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val profile = viewModel.profile
    val capture = rememberDocumentCapture { type, uri -> viewModel.uploadDocument(type, uri) }

    var editing by remember { mutableStateOf(false) }
    var phone by remember(profile?.phoneNumber) { mutableStateOf(profile?.phoneNumber.orEmpty()) }
    var address by remember(profile?.address) { mutableStateOf(profile?.address.orEmpty()) }
    var occupation by remember(profile?.occupation) { mutableStateOf(profile?.occupation.orEmpty()) }

    LaunchedEffect(signedIn) { if (signedIn) viewModel.refresh() }

    if (!signedIn) {
        SignedOutCard(
            title = "Sign in to see profile",
            message = "Your personal details, contact information and identity",
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

        CachedDataNotice(
            visible = viewModel.showingCached,
            fetchedAt = viewModel.lastSyncedAt,
            modifier = Modifier.padding(bottom = 14.dp),
        )

        IdentityHeader(
            name = profile?.customerName.orEmpty().ifBlank { "Customer" },
            email = profile?.email.orEmpty(),
        )

        if (profile != null && !profile.profileComplete) {
            Spacer(Modifier.height(16.dp))
            IncompleteBanner(missing = profile.missingDocuments)
        }

        Spacer(Modifier.height(16.dp))
        SectionCard {
            SectionTitle("Personal Details")
            Spacer(Modifier.height(14.dp))

            ReadOnlyRow("NIK", profile?.nik)
            ReadOnlyRow(
                "Place and date of birth", listOfNotNull(
                    profile?.birthPlace?.takeIf { it.isNotBlank() },
                    profile?.birthDate,
                ).joinToString(", ").ifBlank { null })
            ReadOnlyRow(
                "Gender", when (profile?.sex) {
                    "MALE" -> "Male"
                    "FEMALE" -> "Female"
                    else -> profile?.sex
                }
            )
            ReadOnlyRow("Citizenship", profile?.citizenship)

            Spacer(Modifier.height(6.dp))
            Text(
                text = "Contact customer service if change anything.",
                fontSize = 12.sp,
                color = TextMuted,
            )
        }

        SectionCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SectionTitle("Contact", Modifier.weight(1f))
                Text(
                    text = if (editing) "Cancel" else "Edit",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Green,
                    modifier = Modifier.clickable {
                        viewModel.clearMessages()
                        if (editing) {
                            phone = profile?.phoneNumber.orEmpty()
                            address = profile?.address.orEmpty()
                            occupation = profile?.occupation.orEmpty()
                        }
                        editing = !editing
                    },
                )
            }
            Spacer(Modifier.height(14.dp))

            if (editing) {
                FieldLabel("Phone Number")
                AppTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    placeholder = "08xxxxxxxxxx",
                    keyboardType = KeyboardType.Phone,
                )
                Spacer(Modifier.height(14.dp))

                FieldLabel("Address")
                AppTextField(
                    value = address,
                    onValueChange = { address = it },
                    placeholder = "Where you currently live",
                )
                Spacer(Modifier.height(14.dp))

                FieldLabel("Occupation")
                AppTextField(
                    value = occupation,
                    onValueChange = { occupation = it },
                    placeholder = "Example: Private employee",
                )

                Spacer(Modifier.height(18.dp))
                PrimaryButton(
                    text = if (viewModel.isSaving) "Saving..." else "Save",
                    enabled = !viewModel.isSaving,
                    onClick = {
                        viewModel.save(phone, address, occupation) { editing = false }
                    },
                )
            } else {
                ReadOnlyRow("Phone number", profile?.phoneNumber)
                ReadOnlyRow("Address", profile?.address)
                ReadOnlyRow("Occupation", profile?.occupation)
            }
        }

        Spacer(Modifier.height(16.dp))
        SectionCard {
            SectionTitle("Identity Documents")
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Tap one to replace document.",
                fontSize = 13.sp,
                color = TextSecondary,
            )
            Spacer(Modifier.height(14.dp))

            DocumentTypes.PROFILE.forEachIndexed { index, type ->
                if (index > 0) Spacer(Modifier.height(10.dp))
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
        }

        Spacer(Modifier.height(16.dp))
        SectionCard {
            val drawnDown = (profile?.usedLimit?.signum() ?: 0) > 0

            SectionTitle(if (drawnDown) "Your Remaining Limit" else "Your Limit")
            Spacer(Modifier.height(8.dp))
            Text(
                text = (profile?.availableLimit ?: profile?.approvedLimit
                ?: profile?.plafond?.maxAmount).asRupiah(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Spacer(Modifier.height(2.dp))

            if (drawnDown) {
                Text(
                    text = "Limit ${profile?.approvedLimit.asRupiah()} · " +
                            "${profile?.usedLimit.asRupiah()} in use on disbursed loans",
                    fontSize = 13.sp,
                    color = TextMuted,
                )
                Spacer(Modifier.height(2.dp))
            }

            Text(
                text = profile?.plafond?.let { "Plafond level ${it.level} - ${it.description}" }
                    ?: "Loading your plafond...",
                fontSize = 13.sp,
                color = TextSecondary,
            )
        }

        Spacer(Modifier.height(20.dp))
        OutlineButton(
            text = "Sign out",
            onClick = { viewModel.logout(onLogout) },
            contentColor = Danger,
            borderColor = Danger,
            background = DangerBg,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(28.dp))
    }
  }
}


@Composable
private fun IdentityHeader(name: String, email: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Green.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = Green,
                modifier = Modifier.size(28.dp),
            )
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            if (email.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(email, fontSize = 14.sp, color = TextSecondary)
            }
        }
    }
}

@Composable
private fun IncompleteBanner(missing: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Highlight)
            .padding(14.dp),
    ) {
        Text(
            text = "Profile incomplete",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Not uploaded yet: " + missing.joinToString(", ") { DocumentTypes.label(it) } +
                    ". You can file a new loan application once these are complete.",
            fontSize = 13.sp,
            color = TextSecondary,
        )
    }
}

@Composable
private fun ReadOnlyRow(label: String, value: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(label, fontSize = 13.sp, color = TextSecondary, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(12.dp))
        Text(
            text = value?.takeIf { it.isNotBlank() } ?: "-",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            modifier = Modifier.weight(1.4f),
        )
    }
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(CardBorder.copy(alpha = 0.6f)),
    )
}

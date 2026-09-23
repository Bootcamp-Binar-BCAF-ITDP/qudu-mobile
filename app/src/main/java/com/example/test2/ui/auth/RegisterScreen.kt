package com.example.test2.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test2.ui.apply.*
import com.example.test2.ui.common.AppDateField

enum class Citizenship(val code: String, val label: String) {
    WNI("WNI", "WNI - Indonesian citizen"),
    WNA("WNA", "WNA - Foreign national"),
}

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onRegistered: () -> Unit,
    onBackToLogin: () -> Unit,
    onEmailAlreadyRegistered: () -> Unit = onBackToLogin,
    modifier: Modifier = Modifier,
) {

    var form by remember { mutableStateOf(RegisterForm()) }
    var otpSent by remember { mutableStateOf(false) }
    var otp by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.loadBranches() }

    if (otpSent) {
        RegistrationOtpStep(
            email = form.email.trim(),
            otp = otp,
            onOtpChange = { otp = it },
            viewModel = viewModel,
            onVerify = { viewModel.register(form, otp, onRegistered) },
            onResend = {
                otp = ""
                viewModel.requestRegistrationOtp(
                    form = form,
                    onSent = {},
                    onAlreadyRegistered = onEmailAlreadyRegistered,
                )
            },
            onChangeEmail = {
                viewModel.clearMessages()
                otp = ""
                otpSent = false
            },
            modifier = modifier,
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBg)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
    ) {
        Spacer(Modifier.height(32.dp))

        Text(
            text = "Create Account",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
        )

        Text(
            text = "You start on plafond level 1 straight away.",
            fontSize = 14.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 6.dp, bottom = 20.dp),
        )

        SectionCard {
            FieldLabel("Nearest branch")
            AppDropdownField(
                options = viewModel.branches,
                selected = viewModel.branches.firstOrNull { it.branchId == form.branchId },
                onSelect = { form = form.copy(branchId = it.branchId) },
                placeholder = if (viewModel.branches.isEmpty()) {
                    "Branches unavailable — check your connection"
                } else {
                    "Choose the branch nearest to you"
                },
                label = { branch ->
                    listOfNotNull(branch.branchName, branch.location)
                        .filter { it.isNotBlank() }
                        .joinToString(" — ")
                        .ifBlank { "Branch ${branch.branchId}" }
                },
            )
            Text(
                text = "Your applications go to this branch's team.",
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 6.dp),
            )
            Spacer(Modifier.height(14.dp))

            LabelledField("Full Name", form.fullName, "As printed on your ID card") {
                form = form.copy(fullName = it)
            }
            LabelledField("Email", form.email, "name@email.com", KeyboardType.Email) {
                form = form.copy(email = it)
            }
            LabelledField(
                label = "Password",
                value = form.password,
                placeholder = "At least 6 characters",
                keyboardType = KeyboardType.Password,
                isPassword = true,
            ) { form = form.copy(password = it) }

            LabelledField("Phone Number", form.phoneNumber, "08xxxxxxxxxx", KeyboardType.Phone) {
                form = form.copy(phoneNumber = it)
            }
            LabelledField("NIK", form.nik, "16 digit", KeyboardType.Number) {
                form = form.copy(nik = it.filter(Char::isDigit).take(16))
            }
            LabelledField("Address", form.address, "Where you currently live") {
                form = form.copy(address = it)
            }
            LabelledField("Place of Birth", form.birthPlace, "City of birth") {
                form = form.copy(birthPlace = it)
            }

            FieldLabel("Date of Birth")
            AppDateField(
                value = form.birthDate,
                onValueChange = { form = form.copy(birthDate = it) },
                placeholder = "Select your date of birth",
            )
            Spacer(Modifier.height(14.dp))

            LabelledField("Occupation", form.occupation, "Example: Private employee") {
                form = form.copy(occupation = it)
            }

            FieldLabel("Citizenship")
            AppDropdownField(
                options = Citizenship.entries,
                selected = Citizenship.entries.firstOrNull { it.code == form.citizenship },
                onSelect = { form = form.copy(citizenship = it.code) },
                placeholder = "Select your citizenship",
                label = { it.label },
            )
            Spacer(Modifier.height(14.dp))

            FieldLabel("Gender")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SexOption("Male", "MALE", form.sex) { form = form.copy(sex = it) }
                SexOption("Female", "FEMALE", form.sex) { form = form.copy(sex = it) }
            }

            MessageArea(error = viewModel.error, info = viewModel.info)

            Spacer(Modifier.height(20.dp))

            PrimaryButton(
                text = if (viewModel.isBusy) "Sending code..." else "Register",
                enabled = !viewModel.isBusy,
                onClick = {
                    viewModel.requestRegistrationOtp(
                        form = form,
                        onSent = { otpSent = true },
                        onAlreadyRegistered = onEmailAlreadyRegistered,
                    )
                },
            )
        }

        Row(
            modifier = Modifier
                .padding(top = 20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Already have an account?", fontSize = 14.sp, color = TextSecondary)
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Sign in",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Green,
                modifier = Modifier.clickable {
                    viewModel.clearMessages()
                    onBackToLogin()
                },
            )
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun LabelledField(
    label: String,
    value: String,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    onValueChange: (String) -> Unit,
) {
    FieldLabel(label)
    AppTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder,
        keyboardType = keyboardType,
        isPassword = isPassword,
    )
    Spacer(Modifier.height(14.dp))
}

@Composable
private fun RowScope.SexOption(
    label: String,
    value: String,
    selected: String,
    onSelect: (String) -> Unit,
) {
    val isSelected = selected == value

    Box(
        modifier = Modifier
            .weight(1f)
            .background(if (isSelected) Highlight else FieldBg, FieldShape)
            .border(1.dp, if (isSelected) Green else FieldBorder, FieldShape)
            .clickable { onSelect(value) }
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) Green else TextSecondary,
        )
    }
}

@Composable
private fun RegistrationOtpStep(
    email: String,
    otp: String,
    onOtpChange: (String) -> Unit,
    viewModel: AuthViewModel,
    onVerify: () -> Unit,
    onResend: () -> Unit,
    onChangeEmail: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBg)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
    ) {
        Spacer(Modifier.height(20.dp))

        Text(
            text = "← Edit details",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary,
            modifier = Modifier.clickable(onClick = onChangeEmail),
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Verify Your Email",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Enter the $REGISTRATION_OTP_LENGTH-digit code we sent to $email " +
                "to finish creating your account.",
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = TextSecondary,
        )

        Spacer(Modifier.height(20.dp))

        SectionCard {
            FieldLabel("Verification Code")
            AppTextField(
                value = otp,
                onValueChange = { input ->
                    onOtpChange(input.filter(Char::isDigit).take(REGISTRATION_OTP_LENGTH))
                },
                placeholder = "$REGISTRATION_OTP_LENGTH digits from the email",
                keyboardType = KeyboardType.Number,
            )

            MessageArea(error = viewModel.error, info = viewModel.info)

            Spacer(Modifier.height(20.dp))
            PrimaryButton(
                text = if (viewModel.isBusy) "Verifying..." else "Verify & Register",
                enabled = !viewModel.isBusy,
                onClick = onVerify,
            )

            Spacer(Modifier.height(12.dp))
            OutlineButton(
                text = "Resend Code",
                onClick = onResend,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.height(16.dp))
        Text(
            text = "The code is valid for 15 minutes, can be used once, and is " +
                "disabled after several wrong attempts. Your account is not " +
                "created until this code is correct.",
            fontSize = 12.sp,
            lineHeight = 17.sp,
            color = TextMuted,
        )

        Spacer(Modifier.height(32.dp))
    }
}

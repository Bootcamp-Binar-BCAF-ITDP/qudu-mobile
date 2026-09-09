package com.example.test2.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.test2.di.rememberViewModelFactory
import com.example.test2.ui.apply.AppTextField
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

@Composable
fun ForgotPasswordScreen(
    onDone: () -> Unit,
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: AuthViewModel = viewModel(factory = rememberViewModelFactory())

    var codeSent by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBg)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
    ) {
        Spacer(Modifier.height(20.dp))

        Text(
            text = "← Sign in",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary,
            modifier = Modifier.clickable {
                viewModel.clearMessages()
                onBackToLogin()
            },
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Forgot Password",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = if (codeSent) {
                "Enter the $RESET_CODE_LENGTH-digit code we sent to $email, " +
                    "then choose your new password."
            } else {
                "Enter your account email. We will send a verification code " +
                    "so you can reset your password."
            },
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = TextSecondary,
        )

        Spacer(Modifier.height(20.dp))

        SectionCard {
            if (!codeSent) {
                FieldLabel("Email")
                AppTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "name@email.com",
                    keyboardType = KeyboardType.Email,
                )

                MessageArea(error = viewModel.error, info = viewModel.info)

                Spacer(Modifier.height(20.dp))
                PrimaryButton(
                    text = if (viewModel.isBusy) "Sending..." else "Send Code",
                    enabled = !viewModel.isBusy,
                    onClick = {
                        viewModel.requestPasswordReset(email) { codeSent = true }
                    },
                )
            } else {
                FieldLabel("Verification Code")
                AppTextField(
                    value = code,
                    onValueChange = { input ->
                        code = input.filter(Char::isDigit).take(RESET_CODE_LENGTH)
                    },
                    placeholder = "$RESET_CODE_LENGTH digits from the email",
                    keyboardType = KeyboardType.Number,
                )
                Spacer(Modifier.height(14.dp))

                FieldLabel("New Password")
                AppTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    placeholder = "At least $MIN_RESET_PASSWORD_LENGTH characters",
                    keyboardType = KeyboardType.Password,
                    isPassword = true,
                )
                Spacer(Modifier.height(14.dp))

                FieldLabel("Confirm Password")
                AppTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = "Repeat the new password",
                    keyboardType = KeyboardType.Password,
                    isPassword = true,
                )

                MessageArea(error = viewModel.error, info = viewModel.info)

                Spacer(Modifier.height(20.dp))
                PrimaryButton(
                    text = if (viewModel.isBusy) "Saving..." else "Change Password",
                    enabled = !viewModel.isBusy,
                    onClick = {
                        viewModel.resetPassword(
                            email = email,
                            code = code,
                            newPassword = newPassword,
                            confirmPassword = confirmPassword,
                            onSuccess = onDone,
                        )
                    },
                )

                Spacer(Modifier.height(12.dp))
                OutlineButton(
                    text = "Resend Code",
                    onClick = {
                        code = ""
                        viewModel.requestPasswordReset(email) { codeSent = true }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        if (codeSent) {
            Spacer(Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Highlight)
                    .padding(14.dp),
            ) {
                Text(
                    text = "The code is valid for 15 minutes",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "The code works once and is disabled after several " +
                        "wrong attempts. Never share it with anyone.",
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = TextSecondary,
                )
            }

            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Wrong email?", fontSize = 13.sp, color = TextMuted)
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Change it",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Green,
                    modifier = Modifier.clickable {
                        viewModel.clearMessages()
                        code = ""
                        codeSent = false
                    },
                )
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

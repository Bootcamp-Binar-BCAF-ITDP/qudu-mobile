package com.example.test2.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.test2.R
import com.example.test2.di.rememberViewModelFactory
import com.example.test2.ui.apply.*

@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    onGoToRegister: () -> Unit,
    onForgotPassword: () -> Unit = {},
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val viewModel: AuthViewModel = viewModel(factory = rememberViewModelFactory())

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBg)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(20.dp))

        Text(
            text = "← Home",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary,
            modifier = Modifier
                .align(Alignment.Start)
                .clickable {
                    viewModel.clearMessages()
                    onBack()
                },
        )

        Spacer(Modifier.height(24.dp))

        Image(
            painter = painterResource(R.drawable.logo_lockup),
            contentDescription = "QuickDuit",
            modifier = Modifier.height(96.dp),
        )

        Text(
            text = "Sign in to apply for and track your loans.",
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 28.dp),
        )

        SectionCard {
            FieldLabel("Email")
            AppTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "name@email.com",
                keyboardType = KeyboardType.Email,
            )

            Spacer(Modifier.height(16.dp))

            FieldLabel("Password")
            AppTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = "Your password",
                keyboardType = KeyboardType.Password,
                isPassword = true,
            )

            Spacer(Modifier.height(12.dp))
            Text(
                text = "Forgot password?",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Green,
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable {
                        viewModel.clearMessages()
                        onForgotPassword()
                    },
            )

            MessageArea(error = viewModel.error, info = viewModel.info)

            Spacer(Modifier.height(20.dp))

            PrimaryButton(
                text = if (viewModel.isBusy) "Signing in..." else "Sign in",
                enabled = !viewModel.isBusy,
                onClick = { viewModel.login(email, password, onLoggedIn) },
            )
        }

        Row(
            modifier = Modifier.padding(top = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("No account yet?", fontSize = 14.sp, color = TextSecondary)
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Create one",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Green,
                modifier = Modifier.clickable {
                    viewModel.clearMessages()
                    onGoToRegister()
                },
            )
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
internal fun MessageArea(error: String?, info: String?) {

    val message = error ?: info ?: return
    val isError = error != null

    Text(
        text = message,
        fontSize = 13.sp,
        color = if (isError) Danger else Green,
        modifier = Modifier
            .padding(top = 16.dp)
            .fillMaxWidth()
            .background(
                if (isError) DangerBg else Color(0xFFEAF7EF),
                FieldShape,
            )
            .padding(12.dp),
    )
}

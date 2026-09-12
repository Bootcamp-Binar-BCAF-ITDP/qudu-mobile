package com.example.test2.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.test2.ui.apply.ScreenBg

@Composable
fun SignedOutCard(
    title: String,
    message: String,
    onLogin: () -> Unit,
    modifier: Modifier = Modifier,
    actionLabel: String = "Sign in",
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(20.dp))

        EmptyState(
            title = title,
            message = message,
            actionLabel = actionLabel,
            onAction = onLogin,
        )

        Spacer(Modifier.height(28.dp))
    }
}

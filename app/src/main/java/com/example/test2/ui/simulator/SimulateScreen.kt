package com.example.test2.ui.simulator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test2.ui.apply.PrimaryButton
import com.example.test2.ui.common.RefreshableScreen
import com.example.test2.ui.apply.ScreenBg
import com.example.test2.ui.apply.SectionCard
import com.example.test2.ui.apply.TextPrimary
import com.example.test2.ui.apply.TextSecondary

@Composable
fun SimulateScreen(
    viewModel: SimulatorViewModel,
    signedIn: Boolean,
    onApplyLoan: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) { viewModel.load() }

  RefreshableScreen(
      isRefreshing = viewModel.isRefreshing,
      onRefresh = { viewModel.load(userInitiated = true) },
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


        SectionCard(padding = 22) {
            Text(
                text = "Loan Simulator",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )

            Spacer(Modifier.height(16.dp))

            SimulatorPanel(viewModel)
        }

        Spacer(Modifier.height(16.dp))
        TierTable(tiers = viewModel.tiers, currentLevel = viewModel.tier?.level)

        Spacer(Modifier.height(20.dp))

        if (!signedIn) {
            PrimaryButton(
                text = if (signedIn) "Apply for This Loan" else "Sign in to Apply",
                onClick = onApplyLoan,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (!signedIn) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Simulating is free and needs no account. You only sign in " +
                        "when you actually want to apply.",
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.height(32.dp))
    }
  }
}

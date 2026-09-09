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
import com.example.test2.ui.apply.ScreenBg
import com.example.test2.ui.apply.SectionCard
import com.example.test2.ui.apply.TextPrimary
import com.example.test2.ui.apply.TextSecondary

/**
 * The Simulate tab.
 *
 * Open to everyone, signed in or not: it quotes published product terms and
 * touches no customer data, and it is the one part of the app a prospect can
 * use before deciding to register. [onApplyLoan] is what needs the account,
 * and the shell sends a signed-out tap there to the auth screens.
 */
@Composable
fun SimulateScreen(
    viewModel: SimulatorViewModel,
    signedIn: Boolean,
    onApplyLoan: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) { viewModel.load() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(24.dp))

        Text(
            text = "Loan Simulator",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
        )
        Text(
            text = "Move the sliders to see what a loan would cost. Rate, tenure " +
                "and admin fee follow the plafond level your amount falls into.",
            fontSize = 14.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 6.dp, bottom = 20.dp),
        )

        SectionCard(padding = 22) {
            SimulatorPanel(viewModel)
        }

        Spacer(Modifier.height(16.dp))
        TierTable(tiers = viewModel.tiers, currentLevel = viewModel.tier?.level)

        Spacer(Modifier.height(20.dp))
        PrimaryButton(
            text = if (signedIn) "Apply for This Loan" else "Sign in to Apply",
            onClick = onApplyLoan,
            modifier = Modifier.fillMaxWidth(),
        )

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

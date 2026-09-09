package com.example.test2.ui.common

import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test2.ui.apply.Green
import com.example.test2.ui.apply.OutlineButton
import com.example.test2.ui.apply.PrimaryButton
import com.example.test2.ui.apply.ScreenBg
import com.example.test2.ui.apply.SectionCard
import com.example.test2.ui.apply.TextMuted
import com.example.test2.ui.apply.TextPrimary
import com.example.test2.ui.apply.TextSecondary

@Composable
fun SignInPrompt(
    title: String,
    message: String,
    points: List<String>,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Green.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                tint = Green,
                modifier = Modifier.size(26.dp),
            )
        }

        Spacer(Modifier.height(16.dp))
        Text(
            text = title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = message,
            fontSize = 15.sp,
            lineHeight = 21.sp,
            color = TextSecondary,
        )

        Spacer(Modifier.height(20.dp))
        SectionCard {
            Text(
                text = "What you can do once you are signed in",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Spacer(Modifier.height(12.dp))

            points.forEachIndexed { index, point ->
                if (index > 0) Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = Green,
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(16.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = point,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = TextSecondary,
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PrimaryButton(
                text = "Sign in",
                onClick = onLogin,
                modifier = Modifier.weight(1f),
            )
            OutlineButton(
                text = "Create account",
                onClick = onRegister,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(14.dp))
        Text(
            text = "Not ready to sign in? The loan simulator on the Home page is " +
                "still yours to use without an account.",
            fontSize = 12.sp,
            lineHeight = 17.sp,
            color = TextMuted,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(32.dp))
    }
}

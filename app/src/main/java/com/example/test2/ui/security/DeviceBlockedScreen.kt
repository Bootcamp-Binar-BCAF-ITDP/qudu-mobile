package com.example.test2.ui.security

import android.content.Intent
import android.provider.Settings
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.test2.BuildConfig
import com.example.test2.core.security.IntegrityStatus
import com.example.test2.ui.apply.CardBg
import com.example.test2.ui.apply.CardShape
import com.example.test2.ui.apply.Danger
import com.example.test2.ui.apply.DangerBg
import com.example.test2.ui.apply.Green
import com.example.test2.ui.apply.OutlineButton
import com.example.test2.ui.apply.PrimaryButton
import com.example.test2.ui.apply.ScreenBg
import com.example.test2.ui.apply.TextPrimary
import com.example.test2.ui.apply.TextSecondary

/**
 * The wall shown instead of the app when the device fails the integrity check.
 *
 * It is a `Dialog` that cannot be dismissed — no back press, no tap outside —
 * over a painted background, so nothing of the app behind it is readable.
 */
@Composable
fun DeviceBlockedScreen(
    status: IntegrityStatus,
    onRecheck: () -> Unit,
    onExit: () -> Unit,
    onContinueAnyway: (() -> Unit)? = null,
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg),
    )

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CardShape)
                .background(CardBg)
                .padding(24.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(DangerBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    tint = Danger,
                    modifier = Modifier.size(26.dp),
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "This device is not secure",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "QuickDuit handles loan agreements and personal documents, " +
                        "so it cannot run while the device is in this state.",
                fontSize = 14.sp,
                color = TextSecondary,
            )

            Spacer(Modifier.height(16.dp))

            if (status.rooted) {
                Reason(
                    title = "Root access detected",
                    detail = "This phone appears to be rooted. Use a device without " +
                            "root to continue.",
                )
            }

            if (status.developerOptions) {
                Reason(
                    title = "Developer options are on",
                    detail = "Open Settings and turn Developer options off, then come " +
                            "back and tap Check again.",
                )
            }

            if (status.usbDebugging) {
                Reason(
                    title = "USB debugging is on",
                    detail = "Turn USB debugging off in Developer options.",
                )
            }

            Spacer(Modifier.height(20.dp))

            if (status.developerOptions || status.usbDebugging) {
                PrimaryButton(
                    text = "Open developer settings",
                    onClick = {
                        // ACTION_APPLICATION_DEVELOPMENT_SETTINGS is missing on some
                        // OEM ROMs; the general settings screen is always there.
                        val opened = runCatching {
                            context.startActivity(
                                Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }.isSuccess

                        if (!opened) {
                            runCatching {
                                context.startActivity(
                                    Intent(Settings.ACTION_SETTINGS)
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(10.dp))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlineButton(
                    text = "Check again",
                    onClick = onRecheck,
                    contentColor = Green,
                    borderColor = Green,
                    background = CardBg,
                    modifier = Modifier.weight(1f),
                )

                OutlineButton(
                    text = "Close app",
                    onClick = onExit,
                    contentColor = Danger,
                    borderColor = Danger,
                    background = DangerBg,
                    modifier = Modifier.weight(1f),
                )
            }

            // Debug builds keep a way through: a developer phone always has
            // developer options on, and this wall would otherwise make the app
            // impossible to work on. Release builds never get this button.
            if (BuildConfig.DEBUG && onContinueAnyway != null) {
                Spacer(Modifier.height(10.dp))

                OutlineButton(
                    text = "Continue anyway (debug build)",
                    onClick = onContinueAnyway,
                    contentColor = TextSecondary,
                    borderColor = TextSecondary,
                    background = CardBg,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun Reason(title: String, detail: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DangerBg)
            .padding(12.dp),
    ) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(Danger),
        )

        Spacer(Modifier.width(10.dp))

        Column {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(Modifier.height(2.dp))
            Text(detail, fontSize = 13.sp, color = TextSecondary)
        }
    }

    Spacer(Modifier.height(10.dp))
}

package com.example.test2.ui.common

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.test2.core.UploadRules
import com.example.test2.data.ImageCompressor
import com.example.test2.ui.apply.CardBg
import com.example.test2.ui.apply.Danger
import com.example.test2.ui.apply.Green
import com.example.test2.ui.apply.Highlight
import com.example.test2.ui.apply.OutlineButton
import com.example.test2.ui.apply.PrimaryButton
import com.example.test2.ui.apply.TextMuted
import com.example.test2.ui.apply.TextPrimary
import com.example.test2.ui.apply.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun PhotoPreviewDialog(
    uri: Uri,
    title: String,
    onConfirm: () -> Unit,
    onRetake: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current

    val bitmap: Bitmap? by produceState<Bitmap?>(initialValue = null, uri) {
        value = withContext(Dispatchers.IO) {
            ImageCompressor.decodeUpright(
                contentResolver = context.contentResolver,
                uri = uri,
                maxDimension = UploadRules.MAX_IMAGE_DIMENSION,
            )
        }
    }

    val decoding = bitmap == null

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp))
                .background(CardBg)
                .padding(20.dp),
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Check the photo before you send it. Your face should be " +
                    "clear, sharp and fully in frame.",
                fontSize = 13.sp,
                color = TextSecondary,
            )

            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Highlight),
                contentAlignment = Alignment.Center,
            ) {
                val shot = bitmap
                when {
                    shot != null -> Image(
                        bitmap = shot.asImageBitmap(),
                        contentDescription = "The photo you just took",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize(),
                    )

                    else -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Green)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Preparing preview...",
                            fontSize = 13.sp,
                            color = TextSecondary,
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            PrimaryButton(
                text = "Use This Photo",
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlineButton(
                    text = "Retake",
                    onClick = onRetake,
                    modifier = Modifier.weight(1f),
                )
                OutlineButton(
                    text = "Cancel",
                    onClick = onDismiss,
                    contentColor = Danger,
                    borderColor = Danger,
                    modifier = Modifier.weight(1f),
                )
            }

            if (!decoding) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "The photo is resized before it is sent.",
                    fontSize = 12.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

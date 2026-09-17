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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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

private sealed interface PreviewContent {
    data object Loading : PreviewContent
    data class Picture(val bitmap: Bitmap) : PreviewContent
    data object NotAnImage : PreviewContent
}

@Composable
fun PhotoPreviewDialog(
    uri: Uri,
    title: String,
    onConfirm: () -> Unit,
    onRetake: () -> Unit,
    onDismiss: () -> Unit,
    fromCamera: Boolean = true,
    isSelfie: Boolean = false,
    fileName: String? = null,
) {
    val context = LocalContext.current

    val content: PreviewContent by produceState<PreviewContent>(PreviewContent.Loading, uri) {
        val decoded = withContext(Dispatchers.IO) {
            try {
                ImageCompressor.decodeUpright(
                    contentResolver = context.contentResolver,
                    uri = uri,
                    maxDimension = UploadRules.MAX_IMAGE_DIMENSION,
                )
            } catch (e: Exception) {
                null
            } catch (e: OutOfMemoryError) {
                null
            }
        }

        value = if (decoded == null) PreviewContent.NotAnImage else PreviewContent.Picture(decoded)
    }

    val guidance = when {
        isSelfie -> "Check the photo before you send it. Your face should be clear, " +
            "sharp and fully in frame."

        fromCamera -> "Check the photo before you send it. Every corner of the document " +
            "should be readable."

        else -> "Check the file before you send it."
    }

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
                text = guidance,
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
                when (val shown = content) {
                    is PreviewContent.Picture -> Image(
                        bitmap = shown.bitmap.asImageBitmap(),
                        contentDescription = "Preview of $title",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize(),
                    )

                    PreviewContent.Loading -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Green)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Preparing preview...",
                            fontSize = 13.sp,
                            color = TextSecondary,
                        )
                    }

                    PreviewContent.NotAnImage -> Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.List,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(44.dp),
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = fileName ?: "The selected file",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "This file cannot be shown here, but it will be sent as it is.",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            PrimaryButton(
                text = if (content is PreviewContent.Picture) "Use This Photo" else "Use This File",
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlineButton(
                    text = if (fromCamera) "Retake" else "Choose Another",
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

            if (content is PreviewContent.Picture) {
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

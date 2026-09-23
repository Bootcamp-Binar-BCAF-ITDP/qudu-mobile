package com.example.test2.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test2.core.DocumentTypes
import com.example.test2.ui.apply.FieldBg
import com.example.test2.ui.apply.FieldBorder
import com.example.test2.ui.apply.FieldShape
import com.example.test2.ui.apply.Green
import com.example.test2.ui.apply.TextMuted
import com.example.test2.ui.apply.TextPrimary
import com.example.test2.ui.apply.TextSecondary
import androidx.compose.ui.graphics.Color

private val Amber = com.example.test2.ui.theme.StatusAmber

@Composable
fun DocumentRow(
    documentType: String,
    fileName: String?,
    uploadedAt: String?,
    isUploading: Boolean,
    onCapture: (String) -> Unit,
    modifier: Modifier = Modifier,
    isStale: Boolean = false,
) {
    val uploaded = fileName != null && !isStale

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(FieldShape)
            .background(FieldBg)
            .border(1.dp, if (uploaded) Green else if (isStale) Amber else FieldBorder, FieldShape)
            .clickable(enabled = !isUploading) { onCapture(documentType) }
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(
                    when {
                        uploaded -> Green.copy(alpha = 0.12f)
                        isStale -> Amber.copy(alpha = 0.14f)
                        else -> FieldBorder.copy(alpha = 0.4f)
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            when {
                isUploading -> CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    color = Green,
                    modifier = Modifier.size(18.dp),
                )

                uploaded -> Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = Green,
                    modifier = Modifier.size(18.dp),
                )

                else -> Icon(
                    imageVector = documentType.icon(),
                    contentDescription = null,
                    tint = if (isStale) Amber else TextMuted,
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(
                text = DocumentTypes.label(documentType),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = when {
                    isUploading -> "Uploading..."
                    isStale -> "Needs updating" +
                        (uploadedAt?.take(10)?.let { " - last uploaded $it" } ?: "")
                    uploaded -> "Stored" + (uploadedAt?.take(10)?.let { " - $it" } ?: "")
                    else -> DocumentTypes.hint(documentType)
                },
                fontSize = 13.sp,
                color = when {
                    uploaded -> Green
                    isStale -> Amber
                    else -> TextSecondary
                },
            )
        }

        if (!isUploading) {
            Spacer(Modifier.width(10.dp))
            if (uploaded) {
                Text(
                    text = "Replace",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Green,
                )
            } else if (isStale) {
                Text(
                    text = "Update",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Amber,
                )
            } else {
                Icon(Icons.Filled.Add, null, tint = TextMuted, modifier = Modifier.size(20.dp))
            }
        }
    }
}

private fun String.icon(): ImageVector = when (this) {
    DocumentTypes.KTP -> Icons.Filled.AccountBox
    DocumentTypes.KK -> Icons.Filled.Person
    DocumentTypes.SELFIE -> Icons.Filled.Face
    DocumentTypes.SLIP_GAJI -> Icons.Filled.List
    else -> Icons.Filled.Build
}

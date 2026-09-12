package com.example.test2.ui.common

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.test2.core.DocumentTypes
import com.example.test2.ui.apply.createCaptureImageUri

@Composable
fun rememberDocumentCapture(onCaptured: (documentType: String, uri: Uri) -> Unit): (String) -> Unit {

    val context = LocalContext.current

    var pendingType by remember { mutableStateOf<String?>(null) }
    var pendingCaptureUri by remember { mutableStateOf<Uri?>(null) }

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        val type = pendingType
        pendingType = null
        if (uri != null && type != null) onCaptured(type, uri)
    }

    var previewType by remember { mutableStateOf<String?>(null) }
    var previewUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { success ->
        val uri = pendingCaptureUri
        val type = pendingType
        pendingCaptureUri = null
        pendingType = null
        if (success && uri != null && type != null) {
            previewType = type
            previewUri = uri
        }
    }

    fun launchCamera() {
        val uri = createCaptureImageUri(context)
        pendingCaptureUri = uri
        cameraLauncher.launch(uri)
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            launchCamera()
        } else {
            pendingType = null
        }
    }

    val confirmingType = previewType
    val confirmingUri = previewUri

    if (confirmingType != null && confirmingUri != null) {
        PhotoPreviewDialog(
            uri = confirmingUri,
            title = DocumentTypes.label(confirmingType),
            onConfirm = {
                previewType = null
                previewUri = null
                onCaptured(confirmingType, confirmingUri)
            },
            onRetake = {
                previewUri = null
                previewType = null
                pendingType = confirmingType
                launchCamera()
            },
            onDismiss = {
                previewType = null
                previewUri = null
            },
        )
    }

    return remember(context) {
        { documentType: String ->
            pendingType = documentType

            if (DocumentTypes.isCameraCapture(documentType)) {
                val granted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA,
                ) == PackageManager.PERMISSION_GRANTED

                if (granted) launchCamera() else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            } else {
                pickerLauncher.launch("*/*")
            }
        }
    }
}

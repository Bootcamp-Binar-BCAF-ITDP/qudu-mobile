package com.example.test2.data.repository

import android.content.ContentResolver
import android.net.Uri
import com.example.test2.core.DocumentTypes
import com.example.test2.core.Outcome
import com.example.test2.core.UploadRules
import com.example.test2.data.ImageCompressor
import com.example.test2.data.dto.ApiEnvelope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

internal fun <T> Outcome<ApiEnvelope<T>>.unwrapEnvelope(): Outcome<T> = when (this) {
    is Outcome.Failure -> this
    is Outcome.Success -> value.data
        ?.let { Outcome.Success(it) }
        ?: Outcome.Failure(value.message ?: "The server returned an empty response.")
}

internal suspend fun filePartFrom(
    contentResolver: ContentResolver,
    uri: Uri,
    documentType: String,
): Outcome<MultipartBody.Part> = withContext(Dispatchers.IO) {

    val sourceType = try {
        contentResolver.getType(uri)
    } catch (e: Exception) {
        null
    }

    val label = DocumentTypes.label(documentType)

    val looksLikeImage = sourceType == null || sourceType.startsWith("image/")

    val compressed = if (looksLikeImage) {
        try {
            ImageCompressor.compressToJpeg(
                contentResolver = contentResolver,
                uri = uri,
                targetBytes = UploadRules.TARGET_IMAGE_BYTES,
                maxDimension = UploadRules.MAX_IMAGE_DIMENSION,
            )
        } catch (e: OutOfMemoryError) {
            null
        } catch (e: Exception) {
            null
        }
    } else {
        null
    }

    if (compressed != null) {
        return@withContext Outcome.Success(
            MultipartBody.Part.createFormData(
                "file",
                "$documentType.jpg",
                compressed.toRequestBody("image/jpeg".toMediaTypeOrNull()),
            )
        )
    }

    rawPart(contentResolver, uri, documentType, sourceType, label)
}

private fun rawPart(
    contentResolver: ContentResolver,
    uri: Uri,
    documentType: String,
    contentType: String?,
    label: String,
): Outcome<MultipartBody.Part> {

    val bytes = try {
        contentResolver.openInputStream(uri)?.use { it.readBytes() }
    } catch (e: Exception) {
        null
    } ?: return Outcome.Failure(unreadableFileMessage(documentType))

    if (bytes.size > UploadRules.MAX_FILE_BYTES) {
        return Outcome.Failure(
            "The $label file is ${bytes.size / 1024} KB, over the " +
                "${UploadRules.MAX_FILE_BYTES / (1024 * 1024)} MB limit. Please pick a smaller file."
        )
    }

    return Outcome.Success(
        MultipartBody.Part.createFormData(
            "file",
            "$documentType.${extensionFor(contentType)}",
            bytes.toRequestBody(contentType?.toMediaTypeOrNull()),
        )
    )
}

private fun extensionFor(contentType: String?): String = when (contentType?.lowercase()) {
    "image/jpeg", "image/jpg" -> "jpg"
    "image/png" -> "png"
    "image/webp" -> "webp"
    "image/heic" -> "heic"
    "application/pdf" -> "pdf"
    else -> "bin"
}

internal fun unreadableFileMessage(documentType: String): String =
    "The ${DocumentTypes.label(documentType)} file could not be read. Please pick it again."

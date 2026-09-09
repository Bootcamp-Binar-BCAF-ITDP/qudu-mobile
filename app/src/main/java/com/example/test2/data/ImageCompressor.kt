package com.example.test2.data

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri

import java.io.ByteArrayOutputStream


internal object ImageCompressor {

    private const val MIN_QUALITY = 40
    private const val START_QUALITY = 90
    private const val QUALITY_STEP = 10

    fun compressToJpeg(
        contentResolver: ContentResolver,
        uri: Uri,
        targetBytes: Long,
        maxDimension: Int,
    ): ByteArray? {

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }

        val boundsStream = contentResolver.openInputStream(uri) ?: return null
        boundsStream.use { BitmapFactory.decodeStream(it, null, bounds) }

        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSizeFor(bounds.outWidth, bounds.outHeight, maxDimension)
        }

        val decoded = contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: return null

        val upright = applyExifRotation(contentResolver, uri, decoded)
        val scaled = scaleWithin(upright, maxDimension)

        var quality = START_QUALITY
        var bytes = scaled.toJpeg(quality)

        while (bytes.size > targetBytes && quality > MIN_QUALITY) {
            quality -= QUALITY_STEP
            bytes = scaled.toJpeg(quality)
        }

        return bytes
    }

    private fun sampleSizeFor(width: Int, height: Int, maxDimension: Int): Int {
        var sample = 1
        var longest = maxOf(width, height)

        while (longest / 2 >= maxDimension) {
            longest /= 2
            sample *= 2
        }

        return sample
    }

    private fun scaleWithin(bitmap: Bitmap, maxDimension: Int): Bitmap {

        val longest = maxOf(bitmap.width, bitmap.height)
        if (longest <= maxDimension) return bitmap

        val ratio = maxDimension.toFloat() / longest

        return Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * ratio).toInt().coerceAtLeast(1),
            (bitmap.height * ratio).toInt().coerceAtLeast(1),
            true,
        )
    }

    private fun applyExifRotation(
        contentResolver: ContentResolver,
        uri: Uri,
        bitmap: Bitmap,
    ): Bitmap {

        val orientation = try {
            contentResolver.openInputStream(uri)?.use { stream ->
                ExifInterface(stream).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL,
                )
            } ?: ExifInterface.ORIENTATION_NORMAL
        } catch (e: Exception) {
            ExifInterface.ORIENTATION_NORMAL
        }

        val matrix = Matrix()

        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            else -> return bitmap
        }

        return try {
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: OutOfMemoryError) {
            bitmap
        }
    }

    private fun Bitmap.toJpeg(quality: Int): ByteArray =
        ByteArrayOutputStream().use { out ->
            compress(Bitmap.CompressFormat.JPEG, quality, out)
            out.toByteArray()
        }
}

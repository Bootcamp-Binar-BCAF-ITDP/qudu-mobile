package com.example.test2.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.test2.core.DocumentTypes
import com.example.test2.core.Outcome
import com.example.test2.core.UploadRules
import com.example.test2.data.repository.filePartFrom
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream
import kotlin.random.Random

/**
 * Runs the real compressor against a real photo on a real device, because the
 * bug this guards was invisible to reading: `openInputStream(uri)?.use { decode }
 * ?: return null` looks correct, and returns null every time when the decoder is
 * in inJustDecodeBounds mode. Nothing short of executing it would have caught
 * that - BitmapFactory is a framework class, so a JVM unit test cannot.
 */
@RunWith(AndroidJUnit4::class)
class ImageCompressorTest {

    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun compressesLargePhotoWithinTheUploadBudget() {
        val uri = writeSamplePhoto("compressor_budget.jpg", width = 2000, height = 1500)

        val bytes = ImageCompressor.compressToJpeg(
            contentResolver = context.contentResolver,
            uri = uri,
            targetBytes = UploadRules.TARGET_IMAGE_BYTES,
            maxDimension = UploadRules.MAX_IMAGE_DIMENSION,
        )

        assertNotNull("compressToJpeg returned null for a valid JPEG", bytes)

        assertTrue(
            "compressed to ${bytes!!.size} bytes, over the ${UploadRules.TARGET_IMAGE_BYTES} budget",
            bytes.size <= UploadRules.TARGET_IMAGE_BYTES,
        )

        val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        assertNotNull("compressor produced bytes that do not decode as an image", decoded)

        assertTrue(
            "longest edge ${maxOf(decoded.width, decoded.height)} exceeds " +
                UploadRules.MAX_IMAGE_DIMENSION,
            maxOf(decoded.width, decoded.height) <= UploadRules.MAX_IMAGE_DIMENSION,
        )
    }

    /** A photo already smaller than the cap must survive without being upscaled. */
    @Test
    fun leavesSmallPhotoWithinItsOriginalDimensions() {
        val uri = writeSamplePhoto("compressor_small.jpg", width = 800, height = 600)

        val bytes = ImageCompressor.compressToJpeg(
            contentResolver = context.contentResolver,
            uri = uri,
            targetBytes = UploadRules.TARGET_IMAGE_BYTES,
            maxDimension = UploadRules.MAX_IMAGE_DIMENSION,
        )

        assertNotNull(bytes)

        val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes!!.size)
        assertEquals(800, decoded.width)
        assertEquals(600, decoded.height)
    }

    /** The integration point that actually broke: the selfie upload path. */
    @Test
    fun buildsAMultipartPartForACapturedSelfie() = runBlocking {
        val uri = writeSamplePhoto("compressor_selfie.jpg", width = 2000, height = 1500)

        val outcome = filePartFrom(
            contentResolver = context.contentResolver,
            uri = uri,
            documentType = DocumentTypes.SELFIE,
        )

        assertTrue(
            "expected a part, got: " + (outcome as? Outcome.Failure)?.message,
            outcome is Outcome.Success,
        )

        val part = (outcome as Outcome.Success).value
        assertTrue("part must carry bytes", part.body.contentLength() > 0)
    }

    /**
     * Writes a JPEG through the same FileProvider path the camera capture uses,
     * so the test reads it back exactly the way the app does.
     */
    private fun writeSamplePhoto(name: String, width: Int, height: Int): Uri {

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        // Noise, not a flat fill: a solid colour compresses to a few kilobytes
        // and would pass the budget assertion without the resizing ever mattering.
        val random = Random(42)
        val row = IntArray(width)
        for (y in 0 until height) {
            for (x in 0 until width) {
                row[x] = 0xFF000000.toInt() or random.nextInt(0xFFFFFF)
            }
            bitmap.setPixels(row, 0, width, 0, y, width, 1)
        }

        val dir = File(context.cacheDir, "images").apply { mkdirs() }
        val file = File(dir, name)

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
        }
        bitmap.recycle()

        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
}

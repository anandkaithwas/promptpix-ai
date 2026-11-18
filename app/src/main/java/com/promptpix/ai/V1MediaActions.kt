package com.promptpix.ai

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import java.io.OutputStream

/**
 * V1 output actions. Call only with a genuine Bitmap returned by the inference engine.
 * Original input is never modified.
 */
object V1MediaActions {
    fun saveGeneratedImage(context: Context, bitmap: Bitmap, displayName: String = "AuraStudio_${System.currentTimeMillis()}.jpg"): Uri {
        require(!bitmap.isRecycled) { "Cannot save a recycled bitmap." }
        if (Build.VERSION.SDK_INT < 29) {
            throw UnsupportedOperationException("Android 8–9 save flow requires the legacy runtime storage-permission UI before writing to the shared gallery.")
        }
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Aura Studio")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: throw IllegalStateException("Could not create gallery entry.")
        try {
            val output: OutputStream = resolver.openOutputStream(uri)
                ?: throw IllegalStateException("Could not open gallery output.")
            output.use { stream ->
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)) {
                    throw IllegalStateException("Image encoding failed.")
                }
            }
            val complete = ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }
            resolver.update(uri, complete, null, null)
            return uri
        } catch (error: Exception) {
            resolver.delete(uri, null, null)
            throw error
        }
    }

    fun shareGeneratedImage(context: Context, uri: Uri) {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = android.content.ClipData.newUri(context.contentResolver, "Aura Studio image", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, "Share your image"))
    }
}

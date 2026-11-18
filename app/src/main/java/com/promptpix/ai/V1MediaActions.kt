package com.promptpix.ai

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.OutputStream

/** V1 output actions. Call only with a genuine Bitmap returned by local inference. */
object V1MediaActions {
    fun saveGeneratedImage(
        context: Context,
        bitmap: Bitmap,
        displayName: String = "AuraStudio_${System.currentTimeMillis()}.jpg"
    ): Uri {
        require(!bitmap.isRecycled) { "Cannot save a recycled bitmap." }
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }

        val collection = if (Build.VERSION.SDK_INT >= 29) {
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Aura Studio")
            values.put(MediaStore.Images.Media.IS_PENDING, 1)
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        } else {
            if (context.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                throw SecurityException("Grant Photos and media storage permission before saving on Android 8–9.")
            }
            val directory = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "Aura Studio"
            )
            if (!directory.exists() && !directory.mkdirs()) {
                error("Could not create Aura Studio gallery folder.")
            }
            val destination = File(directory, displayName)
            values.put(MediaStore.Images.Media.DATA, destination.absolutePath)
            values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val uri = resolver.insert(collection, values)
            ?: throw IllegalStateException("Could not create gallery entry.")
        try {
            val output: OutputStream = resolver.openOutputStream(uri)
                ?: throw IllegalStateException("Could not open gallery output.")
            output.use { stream ->
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)) {
                    throw IllegalStateException("Image encoding failed.")
                }
            }
            if (Build.VERSION.SDK_INT >= 29) {
                val complete = ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }
                resolver.update(uri, complete, null, null)
            }
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

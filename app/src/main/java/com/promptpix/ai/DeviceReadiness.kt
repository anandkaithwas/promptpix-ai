package com.promptpix.ai

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.ByteArrayOutputStream
import kotlin.system.measureTimeMillis

private data class DeviceReport(
    val androidApi: Int,
    val totalRamMb: Long,
    val availableRamMb: Long,
    val lowRamDevice: Boolean,
    val abi: String,
    val cpuCores: Int
)

private fun readDeviceReport(context: Context): DeviceReport {
    val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memory = ActivityManager.MemoryInfo()
    manager.getMemoryInfo(memory)
    return DeviceReport(
        androidApi = Build.VERSION.SDK_INT,
        totalRamMb = memory.totalMem / (1024L * 1024L),
        availableRamMb = memory.availMem / (1024L * 1024L),
        lowRamDevice = manager.isLowRamDevice,
        abi = Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown",
        cpuCores = Runtime.getRuntime().availableProcessors()
    )
}

/** Decode a user photo into a bounded bitmap for a future low-memory inference engine. */
private fun decodePreviewForInference(context: Context, uri: Uri, maxEdge: Int = 512): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    var sample = 1
    while (bounds.outWidth / (sample * 2) >= maxEdge && bounds.outHeight / (sample * 2) >= maxEdge) {
        sample *= 2
    }
    val options = BitmapFactory.Options().apply {
        inSampleSize = sample
        inPreferredConfig = Bitmap.Config.RGB_565
    }
    val decoded = context.contentResolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it, null, options)
    } ?: return null
    val longest = maxOf(decoded.width, decoded.height)
    if (longest <= maxEdge) return decoded
    val scale = maxEdge.toFloat() / longest.toFloat()
    val resized = Bitmap.createScaledBitmap(
        decoded,
        (decoded.width * scale).toInt().coerceAtLeast(1),
        (decoded.height * scale).toInt().coerceAtLeast(1),
        true
    )
    if (resized !== decoded) decoded.recycle()
    return resized
}

@Composable
internal fun DeviceReadinessCard(context: Context, photoUri: Uri?, cameraBitmap: Bitmap?) {
    var report by remember { mutableStateOf(readDeviceReport(context)) }
    var benchmarkText by remember { mutableStateOf("Balanced profile: photo preprocessing target is 512 px. Full diffusion inference has not yet been integrated.") }

    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF252033))
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text("ON-DEVICE AI READINESS", color = Color(0xFFC7A8FF), fontSize = 11.sp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Android API", color = Color.White, fontSize = 12.sp)
                Text("${report.androidApi} (${if (report.androidApi >= 26) "Supported" else "Unsupported"})", color = Color(0xFFBFB4D0), fontSize = 12.sp)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total / available RAM", color = Color.White, fontSize = 12.sp)
                Text("${report.totalRamMb / 1024} GB / ${report.availableRamMb} MB", color = Color(0xFFBFB4D0), fontSize = 12.sp)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("CPU cores / ABI", color = Color.White, fontSize = 12.sp)
                Text("${report.cpuCores} / ${report.abi}", color = Color(0xFFBFB4D0), fontSize = 11.sp)
            }
            Text(
                if (report.lowRamDevice) "Android classifies this as a low-RAM device." else "Android does not classify this as a low-RAM device.",
                color = Color(0xFFFFD18A), fontSize = 11.sp
            )
            OutlinedButton(onClick = {
                report = readDeviceReport(context)
                val uri = photoUri
                if (uri == null) {
                    benchmarkText = "Select a gallery photo to run the 512 px memory-safe preprocessing test."
                } else {
                    var output: Bitmap? = null
                    var failure: String? = null
                    val elapsed = measureTimeMillis {
                        try {
                            output = decodePreviewForInference(context, uri, 512)
                        } catch (e: Exception) {
                            failure = e.message ?: "Unable to decode image"
                        }
                    }
                    val bitmap = output
                    benchmarkText = when {
                        failure != null -> "Photo preprocessing failed: $failure"
                        bitmap == null -> "Photo could not be decoded."
                        else -> {
                            val result = "${bitmap.width}×${bitmap.height} RGB_565 prepared in ${elapsed} ms. This tests image preprocessing only, not AI inference."
                            bitmap.recycle()
                            result
                        }
                    }
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Run low-memory photo test")
            }
            Text(benchmarkText, color = Color(0xFFBFB4D0), fontSize = 11.sp)
        }
    }
}

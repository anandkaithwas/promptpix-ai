package com.promptpix.ai

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.StatFs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

/**
 * Storage- and data-conscious model installer.
 *
 * Models are never bundled or downloaded automatically. Call install() only after
 * the user explicitly selects a model and accepts its size/licence disclosure.
 * Downloads are restricted to an unmetered connection (normally Wi-Fi).
 */
data class LeanModelSpec(
    val id: String,
    val displayName: String,
    val downloadUrl: String,
    val expectedBytes: Long,
    val sha256: String,
    val licenceName: String,
    val licenceUrl: String
) {
    init {
        require(id.matches(Regex("[a-z0-9_-]{1,64}")))
        require(downloadUrl.startsWith("https://")) { "Model downloads must use HTTPS." }
        require(expectedBytes > 0)
        require(sha256.matches(Regex("[a-fA-F0-9]{64}"))) {
            "A pinned SHA-256 checksum is required before a model can be installed."
        }
        require(licenceName.isNotBlank() && licenceUrl.startsWith("https://"))
    }
}

data class LeanModelInstallResult(val file: File, val bytesWritten: Long, val sha256: String)

class LeanModelPackStore(context: Context) {
    private val appContext = context.applicationContext
    private val modelDirectory = File(appContext.filesDir, "model-packs")

    fun installedModel(spec: LeanModelSpec): File? {
        val target = File(modelDirectory, "${spec.id}.tflite")
        if (!target.isFile || target.length() != spec.expectedBytes) return null
        return target.takeIf { sha256(it).equals(spec.sha256, ignoreCase = true) }
    }

    fun installedBytes(): Long = modelDirectory.listFiles()
        ?.filter { it.isFile }
        ?.sumOf { it.length() } ?: 0L

    fun availableBytes(): Long = StatFs(appContext.filesDir.absolutePath).availableBytes

    fun remove(spec: LeanModelSpec): Boolean {
        val target = File(modelDirectory, "${spec.id}.tflite")
        val partial = File(modelDirectory, "${spec.id}.download")
        val removedTarget = !target.exists() || target.delete()
        val removedPartial = !partial.exists() || partial.delete()
        return removedTarget && removedPartial
    }

    /**
     * Explicit, single-model download. Wi-Fi/unmetered only; validates exact size
     * and SHA-256 before atomically making the model visible to inference.
     */
    suspend fun install(
        spec: LeanModelSpec,
        onProgress: (downloaded: Long, total: Long) -> Unit = { _, _ -> }
    ): LeanModelInstallResult = withContext(Dispatchers.IO) {
        requireUnmeteredNetwork()
        require(availableBytes() >= spec.expectedBytes + MIN_FREE_SPACE_BYTES) {
            "Not enough free storage. Keep at least 16 MB free beyond the model size."
        }
        installedModel(spec)?.let {
            return@withContext LeanModelInstallResult(it, it.length(), spec.sha256.lowercase())
        }

        if (!modelDirectory.exists() && !modelDirectory.mkdirs()) {
            error("Could not create private model storage.")
        }
        val partial = File(modelDirectory, "${spec.id}.download")
        val target = File(modelDirectory, "${spec.id}.tflite")
        partial.delete()

        val connection = (URL(spec.downloadUrl).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15_000
            readTimeout = 30_000
            instanceFollowRedirects = true
            requestMethod = "GET"
        }
        try {
            connection.connect()
            check(connection.responseCode in 200..299) {
                "Model download failed (HTTP ${connection.responseCode})."
            }
            val declaredLength = connection.contentLengthLong
            check(declaredLength <= 0 || declaredLength == spec.expectedBytes) {
                "Model size does not match the published manifest."
            }

            var written = 0L
            connection.inputStream.use { input ->
                partial.outputStream().buffered().use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    while (true) {
                        currentCoroutineContext().ensureActive()
                        val count = input.read(buffer)
                        if (count < 0) break
                        written += count
                        check(written <= spec.expectedBytes) { "Downloaded file exceeds expected size." }
                        output.write(buffer, 0, count)
                        onProgress(written, spec.expectedBytes)
                    }
                }
            }
            check(written == spec.expectedBytes) { "Downloaded model is incomplete." }
            val actualHash = sha256(partial)
            check(actualHash.equals(spec.sha256, ignoreCase = true)) {
                "Model checksum mismatch. The downloaded file was rejected."
            }
            if (target.exists() && !target.delete()) error("Could not replace the old model.")
            check(partial.renameTo(target)) { "Could not finalise model installation." }
            LeanModelInstallResult(target, written, actualHash)
        } catch (error: Throwable) {
            partial.delete()
            throw error
        } finally {
            connection.disconnect()
        }
    }

    private fun requireUnmeteredNetwork() {
        val manager = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = manager.activeNetwork ?: error("Connect to Wi-Fi before downloading a model.")
        val capabilities = manager.getNetworkCapabilities(network)
            ?: error("Network status is unavailable.")
        check(capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            "No internet connection is available."
        }
        check(capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)) {
            "To save mobile data, model downloads are Wi-Fi only."
        }
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().buffered().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val MIN_FREE_SPACE_BYTES = 16L * 1024L * 1024L
    }
}

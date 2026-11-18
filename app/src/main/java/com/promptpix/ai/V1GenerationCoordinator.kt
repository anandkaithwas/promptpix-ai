package com.promptpix.ai

import android.graphics.Bitmap
import android.net.Uri
import kotlinx.coroutines.CancellationException

sealed interface GenerationUiState {
    data object Idle : GenerationUiState
    data class Preparing(val message: String = "Preparing image…") : GenerationUiState
    data class Running(val progress: Float?, val message: String) : GenerationUiState
    data class Success(val result: GenerationResult) : GenerationUiState
    data class Failed(val message: String, val modelMissing: Boolean = false) : GenerationUiState
}

/**
 * One orchestration point for Generate, Try Again and HD Enhance.
 * UI observes state; only actual engine callbacks can report determinate progress.
 */
class V1GenerationCoordinator(
    private val imageEngine: LocalImageGenerationEngine,
    private val upscaler: LocalSuperResolutionEngine
) {
    var state: GenerationUiState = GenerationUiState.Idle
        private set

    private var lastRequest: GenerationRequest? = null

    suspend fun generate(request: GenerationRequest): GenerationUiState {
        lastRequest = request
        return run(request)
    }

    suspend fun shuffle(): GenerationUiState {
        val previous = lastRequest
            ?: return GenerationUiState.Failed("Generate an image before using Try Again.")
        val next = previous.copy(seed = V1Shuffle.nextSeed(previous.seed).seed)
        lastRequest = next
        return run(next)
    }

    suspend fun enhanceHd(source: Bitmap): Bitmap {
        state = GenerationUiState.Preparing("Preparing HD enhancement…")
        return try {
            upscaler.upscale(SuperResolutionRequest(source, scale = 2))
        } catch (cancelled: CancellationException) {
            state = GenerationUiState.Idle
            throw cancelled
        } catch (error: Exception) {
            state = GenerationUiState.Failed(
                error.message ?: "HD enhancement failed.",
                error is ModelNotConfiguredException
            )
            throw error
        }
    }

    private suspend fun run(request: GenerationRequest): GenerationUiState {
        state = GenerationUiState.Preparing()
        return try {
            val result = imageEngine.generate(request) { raw ->
                // A callback of NaN or a negative value must not corrupt the UI.
                val progress = raw.takeIf { it.isFinite() }?.coerceIn(0f, 1f)
                state = GenerationUiState.Running(progress, "Generating image…")
            }
            require(!result.image.isRecycled) { "Inference returned an invalid image." }
            state = GenerationUiState.Success(result)
            state
        } catch (cancelled: CancellationException) {
            state = GenerationUiState.Idle
            throw cancelled
        } catch (error: Exception) {
            state = GenerationUiState.Failed(
                error.message ?: "Image generation failed.",
                error is ModelNotConfiguredException
            )
            state
        }
    }
}

/** Memory-bounded URI decode utility; never modifies or recycles the original source. */
object V1ImageInput {
    fun decodeForInference(context: android.content.Context, uri: Uri, maxEdge: Int): Bitmap {
        require(maxEdge >= 128) { "Inference edge must be at least 128 pixels." }
        val bounds = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            android.graphics.BitmapFactory.decodeStream(it, null, bounds)
        } ?: throw IllegalArgumentException("Cannot open selected photo.")
        require(bounds.outWidth > 0 && bounds.outHeight > 0) { "Selected file is not a decodable image." }

        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= maxEdge) sample *= 2
        val options = android.graphics.BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val decoded = context.contentResolver.openInputStream(uri)?.use {
            android.graphics.BitmapFactory.decodeStream(it, null, options)
        } ?: throw IllegalArgumentException("Photo decoding failed.")
        val longest = maxOf(decoded.width, decoded.height)
        if (longest <= maxEdge) return decoded
        val scale = maxEdge.toFloat() / longest
        val resized = Bitmap.createScaledBitmap(
            decoded,
            (decoded.width * scale).toInt().coerceAtLeast(1),
            (decoded.height * scale).toInt().coerceAtLeast(1),
            true
        )
        if (resized !== decoded) decoded.recycle()
        return resized
    }
}

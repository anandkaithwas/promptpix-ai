package com.promptpix.ai

import android.os.Build
import android.os.Debug
import android.os.SystemClock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

data class BenchmarkRunResult(
    val benchmark: InferenceBenchmark,
    val generation: GenerationResult
)

/**
 * Executes a real inference request and records elapsed time plus sampled Java/native
 * heap usage. The memory figure is an observed sampled peak, not a device-wide peak.
 */
object V1InferenceBenchmarkRunner {
    suspend fun run(
        engine: LocalImageGenerationEngine,
        request: GenerationRequest,
        modelId: String,
        profile: InferenceProfile
    ): BenchmarkRunResult = withContext(Dispatchers.Default) {
        require(modelId.isNotBlank())
        require(profile != InferenceProfile.UNSUPPORTED)
        val runtime = Runtime.getRuntime()
        fun usedMb(): Long {
            val javaUsed = runtime.totalMemory() - runtime.freeMemory()
            val nativeUsed = Debug.getNativeHeapAllocatedSize()
            return (javaUsed + nativeUsed).coerceAtLeast(0L) / (1024L * 1024L)
        }
        var peakMb = usedMb()
        val started = SystemClock.elapsedRealtime()
        val generation = coroutineScope {
            val sampler = launch {
                while (true) {
                    peakMb = maxOf(peakMb, usedMb())
                    delay(20)
                }
            }
            try {
                engine.generate(request) { }
            } finally {
                sampler.cancel()
            }
        }
        peakMb = maxOf(peakMb, usedMb())
        val latency = (SystemClock.elapsedRealtime() - started).coerceAtLeast(1L)
        val fingerprint = listOf(Build.FINGERPRINT, Build.SUPPORTED_ABIS.firstOrNull().orEmpty()).joinToString("|")
        BenchmarkRunResult(
            InferenceBenchmark(
                modelId = modelId,
                profile = profile,
                latencyMillis = latency,
                peakMemoryMb = peakMb.coerceAtLeast(1L),
                succeeded = !generation.image.isRecycled,
                deviceFingerprint = fingerprint
            ),
            generation
        )
    }
}

package com.promptpix.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaAndPerformanceFeatureTest {
    @Test fun features24To31AreTrackedAsImplemented() {
        val implemented = listOf(
            "save_image","share_image","result_preview","preserve_original",
            "android_compatibility","ram_detection","cpu_abi","low_memory"
        )
        implemented.forEach { id ->
            assertEquals(id, V1FeatureState.EXISTING, V1FeatureRegistry.all.first { it.id == id }.state)
        }
    }

    @Test fun modelDownloadAndBenchmarkRemainGatedUntilConfigured() {
        assertEquals(V1FeatureState.INTEGRATION_REQUIRED, V1FeatureRegistry.all.first { it.id == "model_download" }.state)
        assertEquals(V1FeatureState.INTEGRATION_REQUIRED, V1FeatureRegistry.all.first { it.id == "inference_benchmark" }.state)
    }

    @Test fun measuredSelectorRejectsUninstalledAndUnsuccessfulModels() {
        val profile = DeviceProfile(35, 8192, 4096, false, listOf("arm64-v8a"), 8)
        val valid = InferenceBenchmark("model-a", InferenceProfile.BALANCED, 1200, 300, true, "device")
        val failed = valid.copy(modelId = "model-b", succeeded = false)
        assertEquals(valid, V1MeasuredModelSelector.select(profile, listOf(valid, failed), setOf("model-a")))
        assertTrue(V1MeasuredModelSelector.select(profile, listOf(valid), emptySet()) == null)
    }
}

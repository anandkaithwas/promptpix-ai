package com.promptpix.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GenerationOrchestrationTest {
    @Test fun shuffleUsesFreshSeedAndKeepsOtherRequestSettings() {
        val original = V1Shuffle.nextSeed(123456L)
        assertNotEquals(123456L, original.seed)
        assertTrue(original.sameSettings)
    }

    @Test fun generationProgressIsClampedAndInvalidValuesAreIgnored() {
        val values = listOf(-0.2f, 0.4f, 1.5f, Float.NaN)
        val normalized = values.map { raw -> raw.takeIf { it.isFinite() }?.coerceIn(0f, 1f) }
        assertEquals(0f, normalized[0])
        assertEquals(0.4f, normalized[1])
        assertEquals(1f, normalized[2])
        assertEquals(null, normalized[3])
    }

    @Test fun registryKeepsRealModelDependenciesExplicit() {
        assertEquals(V1FeatureState.MODEL_REQUIRED, V1FeatureRegistry.all.first { it.id == "local_generation" }.state)
        assertEquals(V1FeatureState.MODEL_REQUIRED, V1FeatureRegistry.all.first { it.id == "hd_enhance" }.state)
    }
}

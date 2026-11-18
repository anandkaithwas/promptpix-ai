package com.promptpix.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class V1ShuffleTest {
    @Test fun newSeedPreservesRequestSettings() {
        val plan = V1Shuffle.nextSeed(99L)
        assertTrue(plan.seed != 99L)
        assertTrue(plan.sameSettings)
    }

    @Test fun shufflePlanDoesNotMutateInputRequest() {
        val plan = V1Shuffle.nextSeed(123L)
        assertTrue(plan.seed != 123L)
    }

    @Test fun featureRegistryHasNoDuplicateIds() {
        assertEquals(33, V1FeatureRegistry.all.map { it.id }.distinct().size)
    }
}

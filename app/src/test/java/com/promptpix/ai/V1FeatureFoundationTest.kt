package com.promptpix.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class V1FeatureFoundationTest {
    @Test fun registryContainsExactly33Features() {
        assertEquals(33, V1FeatureRegistry.all.size)
        assertEquals(33, V1FeatureRegistry.all.map { it.id }.distinct().size)
    }

    @Test fun intensityIsBoundedAndMonotonic() {
        val values = (0..100 step 10).map {
            CreativeSelection(emptyList(), it).normalizedDenoisingStrength()
        }
        assertTrue(values.zipWithNext().all { (a, b) -> b >= a })
        assertTrue(values.first() >= 0f && values.last() <= 1f)
    }

    @Test fun rouletteDoesNotSelectDuplicateCategories() {
        val selected = StyleRoulette.spin(emptyList(), maxStyles = 3)
        assertTrue(selected.isEmpty())
    }

    @Test fun shuffleChangesSeed() {
        val next = V1Shuffle.nextSeed(1234L)
        assertTrue(next.seed != 1234L)
        assertTrue(next.sameSettings)
    }

    @Test fun promptInterpreterKeepsUserRequest() {
        val bundle = V1PromptInterpreter.interpret("Make it cinematic", emptyList())
        assertEquals("Make it cinematic", bundle.userRequest)
        assertTrue(bundle.combined.contains("Make it cinematic"))
    }
}

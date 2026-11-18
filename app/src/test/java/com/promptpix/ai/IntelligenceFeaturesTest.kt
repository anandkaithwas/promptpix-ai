package com.promptpix.ai

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.ui.graphics.Color
import org.junit.Assert.*
import org.junit.Test

class IntelligenceFeaturesTest {
    private fun style(id: String, title: String, category: String, instruction: String) =
        Effect(id, title, category, Icons.Rounded.Palette, listOf(Color.Black, Color.White), "TEST", "/$id", instruction)

    @Test fun promptInterpreterClassifiesPortraitAndBackgroundRequests() {
        assertEquals(PromptIntent.PORTRAIT, PromptIntentInterpreter.classify("Create a cinematic portrait"))
        assertEquals(PromptIntent.BACKGROUND, PromptIntentInterpreter.classify("Replace the background with a beach"))
        assertEquals(PromptIntent.GENERAL, PromptIntentInterpreter.classify("Make it better"))
    }

    @Test fun styleRecommendationsAreRankedAndIgnoreInvalidConfidence() {
        val portrait = style("p", "Portrait Glow", "Photography", "Soft portrait light")
        val landscape = style("l", "Mountain Vista", "Nature", "Landscape atmosphere")
        val ranked = V1StyleRecommender.recommend(
            listOf(SubjectLabel("person", .9f), SubjectLabel("portrait", .8f), SubjectLabel("bad", 1.5f)),
            listOf(landscape, portrait)
        )
        assertEquals(listOf("p"), ranked.map { it.id })
    }

    @Test fun userDescriptionProvidesContextSuggestionsWithoutClaimingMl() {
        val styles = listOf(style("p", "Portrait Glow", "Photography", "Soft portrait light"))
        val result = V1ContextSuggestionService(UnconfiguredOfflineImageLabeler())
            .suggestFromUserDescription("portrait, face", styles)
        assertEquals("user-description", result.source)
        assertTrue(result.recommendations.isNotEmpty())
    }

    @Test fun hardwareSetupDisablesUnsupportedApiAndLowRam() {
        val profile = DeviceProfile(25, 2048, 1700, false, listOf("arm64-v8a"), 8)
        val plan = V1HardwareSetup.plan(profile)
        assertEquals(InferenceProfile.UNSUPPORTED, plan.inferenceProfile)
        assertEquals(0, plan.maxInputEdge)
    }

    @Test fun adaptiveSelectionRequiresInstalledSuccessfulMeasuredModel() {
        val profile = DeviceProfile(35, 8192, 4000, false, listOf("arm64-v8a"), 8)
        val candidate = InferenceBenchmark("lite-style", InferenceProfile.BALANCED, 120, 700, true, "device-a")
        assertNull(V1MeasuredModelSelector.select(profile, listOf(candidate), emptySet()))
        assertEquals(candidate, V1MeasuredModelSelector.select(profile, listOf(candidate), setOf("lite-style")))
        val failed = candidate.copy(succeeded = false)
        assertNull(V1MeasuredModelSelector.select(profile, listOf(failed), setOf("lite-style")))
    }
}

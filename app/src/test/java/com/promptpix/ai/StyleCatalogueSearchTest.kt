package com.promptpix.ai

import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StyleCatalogueSearchTest {
    private val style = Effect(
        id = "golden_hour_portrait",
        title = "Golden Hour Portrait",
        category = "Photography & Image Quality",
        icon = Icons.Rounded.AutoAwesome,
        colors = listOf(Color.Black, Color.White),
        sample = "PHOTO",
        command = "/golden_hour_portrait",
        instruction = "Warm sunset light with natural skin tones and soft shadows."
    )

    @Test fun blankSearchMatchesEverything() {
        assertTrue(StyleCatalogueSearch.matches(style, "  "))
    }

    @Test fun searchesNameCategoryInstructionAndId() {
        assertTrue(StyleCatalogueSearch.matches(style, "golden hour"))
        assertTrue(StyleCatalogueSearch.matches(style, "photography"))
        assertTrue(StyleCatalogueSearch.matches(style, "natural skin"))
        assertTrue(StyleCatalogueSearch.matches(style, "golden_hour_portrait"))
    }

    @Test fun searchIsCaseInsensitiveAndTrimsWhitespace() {
        assertTrue(StyleCatalogueSearch.matches(style, "  GOLDEN HOUR  "))
    }

    @Test fun unrelatedSearchDoesNotMatch() {
        assertFalse(StyleCatalogueSearch.matches(style, "underwater"))
    }
}

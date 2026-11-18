package com.promptpix.ai

import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class StyleCatalogueValidatorTest {
    private fun style(id: String, title: String) = Effect(
        id = id,
        title = title,
        category = "Photography",
        icon = Icons.Rounded.AutoAwesome,
        colors = listOf(Color.Black, Color.White),
        sample = "PHOTOGRAPHY",
        command = "/$id",
        instruction = "Apply $title while preserving the source subject."
    )

    @Test fun acceptsCompleteUnique275StyleCatalogue() {
        val styles = (1..275).map { style("style_$it", "Style $it") }
        assertEquals(275, StyleCatalogueValidator.validate(styles).size)
    }

    @Test fun rejectsIncompleteCatalogue() {
        assertThrows(IllegalArgumentException::class.java) {
            StyleCatalogueValidator.validate(listOf(style("one", "One")))
        }
    }

    @Test fun rejectsDuplicateIds() {
        val styles = (1..275).map { style(if (it == 275) "style_1" else "style_$it", "Style $it") }
        assertThrows(IllegalArgumentException::class.java) {
            StyleCatalogueValidator.validate(styles)
        }
    }

    @Test fun rejectsMissingInstructions() {
        val styles = (1..275).map { index ->
            style("style_$index", "Style $index").let {
                if (index == 1) it.copy(instruction = " ") else it
            }
        }
        assertThrows(IllegalArgumentException::class.java) {
            StyleCatalogueValidator.validate(styles)
        }
    }
}

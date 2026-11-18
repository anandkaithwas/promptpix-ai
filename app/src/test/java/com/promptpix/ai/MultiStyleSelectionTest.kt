package com.promptpix.ai

import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class MultiStyleSelectionTest {
    private fun style(id: String, title: String = "Shared title") = Effect(
        id = id,
        title = title,
        category = "Photography",
        icon = Icons.Rounded.AutoAwesome,
        colors = listOf(Color.Black, Color.White),
        sample = "PHOTO",
        command = "/$id",
        instruction = "Instruction for $id."
    )

    private val catalogue = listOf(
        style("style_a"), style("style_b"), style("style_c"),
        style("style_d"), style("style_e", "Another title")
    )

    @Test fun toggleUsesStableIdsEvenWhenDisplayNamesMatch() {
        val selected = MultiStyleSelection.toggle(emptyList(), "style_a", catalogue)
        val second = MultiStyleSelection.toggle(selected, "style_b", catalogue)
        assertEquals(listOf("style_a", "style_b"), second)
        assertEquals(listOf("style_a", "style_b"), MultiStyleSelection.resolve(second, catalogue).map { it.id })
    }

    @Test fun togglingSelectedStyleRemovesOnlyThatId() {
        assertEquals(
            listOf("style_b"),
            MultiStyleSelection.toggle(listOf("style_a", "style_b"), "style_a", catalogue)
        )
    }

    @Test fun enforcesFourStyleLimit() {
        val selected = listOf("style_a", "style_b", "style_c", "style_d")
        assertThrows(IllegalArgumentException::class.java) {
            MultiStyleSelection.toggle(selected, "style_e", catalogue)
        }
    }

    @Test fun resolvePreservesUserSelectionOrder() {
        val resolved = MultiStyleSelection.resolve(listOf("style_c", "style_a"), catalogue)
        assertEquals(listOf("style_c", "style_a"), resolved.map { it.id })
    }

    @Test fun rejectsUnknownIds() {
        assertThrows(IllegalArgumentException::class.java) {
            MultiStyleSelection.toggle(emptyList(), "missing", catalogue)
        }
        assertThrows(IllegalArgumentException::class.java) {
            MultiStyleSelection.resolve(listOf("missing"), catalogue)
        }
    }

    @Test fun sanitizeRemovesDuplicatesAndStaleIds() {
        assertEquals(
            listOf("style_a", "style_b"),
            MultiStyleSelection.sanitize(listOf("style_a", "style_a", "stale", "style_b"), catalogue)
        )
    }
}

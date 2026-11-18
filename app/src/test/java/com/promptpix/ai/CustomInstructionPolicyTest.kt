package com.promptpix.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CustomInstructionPolicyTest {
    @Test fun trimsValidInstruction() {
        val result = CustomInstructionPolicy.validate("  Make it cinematic  ")
        assertEquals(InstructionValidation.Valid("Make it cinematic"), result)
    }

    @Test fun rejectsBlankInstruction() {
        assertTrue(CustomInstructionPolicy.validate("   ") is InstructionValidation.Invalid)
    }

    @Test fun rejectsInstructionOverLimit() {
        assertTrue(CustomInstructionPolicy.validate("x".repeat(CustomInstructionPolicy.MAX_LENGTH + 1)) is InstructionValidation.Invalid)
    }

    @Test fun acceptsInstructionAtLimit() {
        assertTrue(CustomInstructionPolicy.validate("x".repeat(CustomInstructionPolicy.MAX_LENGTH)) is InstructionValidation.Valid)
    }

    @Test fun interpreterCombinesCustomTextAndSelectedStyles() {
        val style = Effect("cinema", "Cinematic", "Photography",
            androidx.compose.material.icons.Icons.Rounded.Palette,
            listOf(androidx.compose.ui.graphics.Color.Black, androidx.compose.ui.graphics.Color.White),
            "PHOTO", "/cinema", "Use cinematic lighting and film colour.")
        val prompt = V1PromptInterpreter.interpret("  Keep my face unchanged  ", listOf(style))
        assertTrue(prompt.userRequest == "Keep my face unchanged")
        assertTrue(prompt.positivePrompt.contains("Keep my face unchanged"))
        assertTrue(prompt.positivePrompt.contains("Use cinematic lighting"))
        assertTrue(prompt.preservationPrompt.contains("identity"))
        assertTrue(prompt.combined.isNotBlank())
    }
}

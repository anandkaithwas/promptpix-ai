package com.promptpix.ai

import java.io.File

/**
 * Validates the documented MNN Sana img2img directory layout before any native
 * runtime is loaded. This is an integrity/layout gate, not proof that weights
 * are compatible with a particular MNN build.
 */
object SanaModelPackValidator {
    private val requiredFiles = listOf(
        "connector.mnn",
        "projector.mnn",
        "transformer.mnn",
        "vae_encoder.mnn",
        "vae_decoder.mnn"
    )

    data class Validation(val valid: Boolean, val missing: List<String>, val llmModelFiles: Int)

    fun validate(root: File): Validation {
        val missing = mutableListOf<String>()
        if (!root.isDirectory) return Validation(false, listOf("."), 0)

        requiredFiles.forEach { relative ->
            val file = File(root, relative)
            if (!file.isFile || file.length() <= 0L) missing += relative
        }

        val llm = File(root, "llm")
        val llmFiles = if (llm.isDirectory) {
            llm.walkTopDown().count { it.isFile && it.length() > 0L }
        } else 0
        if (llmFiles == 0) missing += "llm/(non-empty model files)"

        return Validation(missing.isEmpty(), missing, llmFiles)
    }

    fun requireValid(root: File) {
        val result = validate(root)
        require(result.valid) {
            "Sana img2img model pack is incomplete. Missing: ${result.missing.joinToString()}"
        }
    }
}

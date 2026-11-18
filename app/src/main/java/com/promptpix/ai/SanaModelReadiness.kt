package com.promptpix.ai

import java.io.File

/**
 * Fail-closed readiness check for the optional MNN Sana image-to-image package.
 *
 * A directory is not considered installable merely because it contains a few model
 * files. Sana img2img requires both the diffusion graph and its image encoder plus
 * the prompt-conditioning LLM. This validator deliberately does not invent download
 * URLs, sizes, hashes, or licence metadata.
 */
data class SanaPackageReadiness(
    val ready: Boolean,
    val missing: List<String>,
    val explanation: String
)

object SanaModelPackageValidator {
    private val requiredRootFiles = listOf(
        "connector.mnn",
        "projector.mnn",
        "transformer.mnn",
        "vae_decoder.mnn",
        "vae_encoder.mnn"
    )

    fun validate(modelDirectory: File): SanaPackageReadiness {
        if (!modelDirectory.exists() || !modelDirectory.isDirectory) {
            return SanaPackageReadiness(
                ready = false,
                missing = listOf(modelDirectory.absolutePath),
                explanation = "The Sana model package is not installed."
            )
        }

        val missing = requiredRootFiles.filter { name ->
            val file = File(modelDirectory, name)
            !file.isFile || file.length() <= 0L
        }.toMutableList()

        val llmDirectory = File(modelDirectory, "llm")
        if (!llmDirectory.isDirectory || llmDirectory.listFiles()?.none { it.isFile && it.length() > 0L } != false) {
            missing += "llm/ (prompt-conditioning model assets)"
        }

        return if (missing.isEmpty()) {
            SanaPackageReadiness(
                ready = true,
                missing = emptyList(),
                explanation = "Required Sana img2img package components are present. Runtime load and inference still need validation."
            )
        } else {
            SanaPackageReadiness(
                ready = false,
                missing = missing,
                explanation = "Sana image-to-image is unavailable until every required model component is installed and verified."
            )
        }
    }
}

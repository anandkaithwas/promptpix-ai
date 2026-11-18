package com.promptpix.ai

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files

class SanaModelPackageValidatorTest {
    @Test fun missingPackageFailsClosed() {
        val root = Files.createTempDirectory("sana-missing").toFile()
        try {
            val result = SanaModelPackageValidator.validate(File(root, "not-installed"))
            assertFalse(result.ready)
            assertTrue(result.missing.isNotEmpty())
        } finally {
            root.deleteRecursively()
        }
    }

    @Test fun missingImageEncoderIsReported() {
        val root = Files.createTempDirectory("sana-incomplete").toFile()
        try {
            listOf("connector.mnn", "projector.mnn", "transformer.mnn", "vae_decoder.mnn").forEach {
                File(root, it).writeBytes(byteArrayOf(1))
            }
            File(root, "llm").apply { mkdirs(); resolve("prompt.mnn").writeBytes(byteArrayOf(1)) }
            val result = SanaModelPackageValidator.validate(root)
            assertFalse(result.ready)
            assertTrue(result.missing.contains("vae_encoder.mnn"))
        } finally {
            root.deleteRecursively()
        }
    }

    @Test fun completeFileLayoutPassesStructuralCheckOnly() {
        val root = Files.createTempDirectory("sana-complete-layout").toFile()
        try {
            listOf("connector.mnn", "projector.mnn", "transformer.mnn", "vae_decoder.mnn", "vae_encoder.mnn").forEach {
                File(root, it).writeBytes(byteArrayOf(1))
            }
            File(root, "llm").apply { mkdirs(); resolve("prompt.mnn").writeBytes(byteArrayOf(1)) }
            val result = SanaModelPackageValidator.validate(root)
            assertTrue(result.ready)
            // Structural presence is not proof that these placeholder bytes are valid model weights.
            assertTrue(result.explanation.contains("still need validation"))
        } finally {
            root.deleteRecursively()
        }
    }
}

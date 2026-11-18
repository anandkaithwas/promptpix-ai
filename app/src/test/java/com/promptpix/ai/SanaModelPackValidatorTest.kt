package com.promptpix.ai

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SanaModelPackValidatorTest {
    @Test fun rejectsMissingSanaImageEncoder() {
        val root = createPack(includeEncoder = false)
        try {
            val result = SanaModelPackValidator.validate(root)
            assertFalse(result.valid)
            assertTrue(result.missing.contains("vae_encoder.mnn"))
        } finally { root.deleteRecursively() }
    }

    @Test fun acceptsDocumentedImg2ImgLayout() {
        val root = createPack(includeEncoder = true)
        try {
            val result = SanaModelPackValidator.validate(root)
            assertTrue(result.valid)
            assertEquals(1, result.llmModelFiles)
        } finally { root.deleteRecursively() }
    }

    private fun createPack(includeEncoder: Boolean): File {
        val root = createTempDir(prefix = "sana-pack-test")
        listOf("connector.mnn", "projector.mnn", "transformer.mnn", "vae_decoder.mnn").forEach {
            File(root, it).writeText("test")
        }
        if (includeEncoder) File(root, "vae_encoder.mnn").writeText("test")
        File(root, "llm").mkdirs()
        File(root, "llm/embeddings.mnn").writeText("test")
        return root
    }
}

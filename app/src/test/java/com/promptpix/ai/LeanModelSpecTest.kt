package com.promptpix.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class LeanModelSpecTest {
    private val valid = LeanModelSpec(
        id = "style_candy",
        displayName = "Candy style",
        downloadUrl = "https://example.org/style_candy.tflite",
        expectedBytes = 3_500_000,
        sha256 = "a".repeat(64),
        licenceName = "BSD-3-Clause",
        licenceUrl = "https://opensource.org/license/bsd-3-clause/"
    )

    @Test fun acceptsPinnedHttpsModelWithLicence() {
        assertEquals("style_candy", valid.id)
        assertEquals(3_500_000L, valid.expectedBytes)
    }

    @Test fun rejectsHttpModelUrl() {
        assertThrows(IllegalArgumentException::class.java) {
            valid.copy(downloadUrl = "http://example.org/model.tflite")
        }
    }

    @Test fun rejectsMissingChecksum() {
        assertThrows(IllegalArgumentException::class.java) {
            valid.copy(sha256 = "")
        }
    }

    @Test fun rejectsUnlicensedModel() {
        assertThrows(IllegalArgumentException::class.java) {
            valid.copy(licenceName = "")
        }
    }
}

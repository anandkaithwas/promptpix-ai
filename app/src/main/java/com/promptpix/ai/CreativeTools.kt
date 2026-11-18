package com.promptpix.ai

import kotlin.random.Random

/**
 * Local, deterministic creative helpers. These prepare user intent and generation
 * settings; they do not claim to perform image inference.
 */
data class PromptEnhancement(
    val positivePrompt: String,
    val preservationInstruction: String,
    val qualityInstruction: String
)

object PromptEnhancer {
    private val identityTerms = listOf(
        "keep my face", "preserve my face", "face unchanged",
        "keep identity", "preserve identity", "don't change my face",
        "do not change my face"
    )

    fun enhance(userText: String, selectedStyleInstructions: List<String>): PromptEnhancement {
        val request = userText.trim()
        val preserveIdentity = identityTerms.any { request.contains(it, ignoreCase = true) }
        val styleText = selectedStyleInstructions
            .map(String::trim)
            .filter(String::isNotEmpty)
            .distinct()
            .joinToString(". ")

        val prompt = buildList {
            if (request.isNotEmpty()) add("User's requested edit: $request")
            if (styleText.isNotEmpty()) add("Selected visual style instructions: $styleText")
            add("Create a coherent image-to-image transformation based on the supplied source photograph.")
            add("Keep composition and subject details consistent unless the user explicitly requests a change.")
        }.joinToString("\n")

        return PromptEnhancement(
            positivePrompt = prompt,
            preservationInstruction = if (preserveIdentity) {
                "Prioritise preservation of the person's identity and recognisable facial features."
            } else {
                "Preserve important source-photo details unless the requested edit requires changing them."
            },
            qualityInstruction = "Use coherent lighting, natural textures, clean edges and sharp subject detail; avoid unintended artifacts."
        )
    }
}

data class CreativeSelection(
    val styleIds: List<String>,
    /** 0..100: 0 is subtle, 100 is strong transformation. */
    val intensity: Int
) {
    fun normalizedDenoisingStrength(): Float =
        (0.20f + (intensity.coerceIn(0, 100) / 100f) * 0.65f)
            .coerceIn(0.20f, 0.85f)
}

object StyleRoulette {
    /**
     * Picks at most one style per category to reduce conflicting combinations.
     * The caller supplies the existing, already-loaded catalogue.
     */
    fun spin(styles: List<Effect>, random: Random = Random.Default, maxStyles: Int = 3): List<Effect> {
        if (styles.isEmpty() || maxStyles <= 0) return emptyList()
        return styles
            .groupBy { it.category }
            .values
            .mapNotNull { categoryStyles -> categoryStyles.randomOrNull(random) }
            .shuffled(random)
            .take(maxStyles)
    }
}

/**
 * Conservative subject suggestions. This is deliberately a deterministic fallback,
 * not a claim of ML image recognition. Replace/augment with a verified offline
 * image-labeling model before calling it automatic photo understanding.
 */
object StyleSuggestions {
    fun forSubject(subject: String, styles: List<Effect>, limit: Int = 8): List<Effect> {
        val query = subject.lowercase()
        val terms = when {
            query.contains("person") || query.contains("portrait") || query.contains("face") ->
                listOf("portrait", "photography", "lighting", "camera", "cinematic")
            query.contains("pet") || query.contains("animal") || query.contains("dog") || query.contains("cat") ->
                listOf("nature", "photography", "lighting", "art")
            query.contains("landscape") || query.contains("nature") || query.contains("outdoor") ->
                listOf("nature", "weather", "photography", "lighting")
            query.contains("building") || query.contains("architecture") ->
                listOf("architecture", "photography", "lighting", "art")
            else -> listOf("photography", "art", "lighting")
        }
        return styles
            .filter { style -> terms.any { term -> style.category.contains(term, ignoreCase = true) || style.title.contains(term, ignoreCase = true) } }
            .distinctBy { it.id }
            .take(limit.coerceAtLeast(1))
    }
}


/**
 * Selection state is keyed by stable catalogue IDs, never display names.
 * IDs are kept unique and in user-selected order for predictable prompt assembly.
 */
object MultiStyleSelection {
    const val MAX_STYLES = 4

    fun toggle(currentIds: List<String>, styleId: String, availableStyles: List<Effect>): List<String> {
        require(availableStyles.any { it.id == styleId }) { "Selected style is not in the loaded catalogue." }
        val normalized = currentIds.distinct().filter { id -> availableStyles.any { it.id == id } }
        return if (styleId in normalized) {
            normalized.filterNot { it == styleId }
        } else {
            require(normalized.size < MAX_STYLES) { "You can combine up to $MAX_STYLES styles in one edit." }
            normalized + styleId
        }
    }

    fun resolve(selectedIds: List<String>, availableStyles: List<Effect>): List<Effect> {
        val byId = availableStyles.associateBy { it.id }
        val ids = selectedIds.distinct()
        require(ids.size <= MAX_STYLES) { "You can combine up to $MAX_STYLES styles in one edit." }
        require(ids.all { it in byId }) { "One or more selected styles are no longer available." }
        return ids.map { byId.getValue(it) }
    }

    fun sanitize(selectedIds: List<String>, availableStyles: List<Effect>): List<String> {
        val validIds = availableStyles.mapTo(hashSetOf()) { it.id }
        return selectedIds.distinct().filter { it in validIds }.take(MAX_STYLES)
    }
}

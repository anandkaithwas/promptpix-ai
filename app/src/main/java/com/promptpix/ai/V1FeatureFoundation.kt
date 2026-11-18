package com.promptpix.ai

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import java.io.File
import java.util.UUID
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * V1.0 feature inventory. Every entry has an implementation seam and an explicit
 * state so the UI/product does not confuse scaffolding with a working AI capability.
 */
enum class V1FeatureState { EXISTING, SCAFFOLDED, MODEL_REQUIRED, INTEGRATION_REQUIRED }

data class V1Feature(
    val id: String,
    val title: String,
    val group: String,
    val description: String,
    val state: V1FeatureState
)

object V1FeatureRegistry {
    val all = listOf(
        V1Feature("photo_gallery","Photo Gallery","Core","Select a source photo from Android's photo picker.",V1FeatureState.EXISTING),
        V1Feature("camera","Camera","Core","Capture a source photo using the Android camera contract.",V1FeatureState.EXISTING),
        V1Feature("photo_preview","Photo Preview","Core","Preview the selected source photo.",V1FeatureState.EXISTING),
        V1Feature("style_catalogue","Style Catalogue","Core","Browse the bundled visual transformation catalogue.",V1FeatureState.EXISTING),
        V1Feature("style_categories","Style Categories","Core","Filter styles by category.",V1FeatureState.EXISTING),
        V1Feature("style_search","Style Search","Core","Search style names and categories.",V1FeatureState.EXISTING),
        V1Feature("multi_style","Multi-Style Selection","Core","Combine selected compatible style instructions.",V1FeatureState.EXISTING),
        V1Feature("custom_instructions","Custom Instructions","Core","Describe requested changes in natural language.",V1FeatureState.EXISTING),
        V1Feature("direct_generate","Direct Generate","Core","Start generation from the main action without a mandatory review screen.",V1FeatureState.INTEGRATION_REQUIRED),
        V1Feature("prompt_interpreter","AI Prompt Interpreter","Core","Combine selected style instructions, custom instructions and preservation guidance.",V1FeatureState.EXISTING),
        V1Feature("prompt_enhancer","Automatic Prompt Enhancer","AI Intelligence","Enrich a short request with explicit scene, lighting, preservation and quality guidance.",V1FeatureState.EXISTING),
        V1Feature("context_suggestions","Context-Aware Suggestions","AI Intelligence","Use on-device image labels to infer image subject.",V1FeatureState.MODEL_REQUIRED),
        V1Feature("style_recommendations","Smart Style Recommendations","AI Intelligence","Rank catalogue styles against supplied or detected subject labels.",V1FeatureState.EXISTING),
        V1Feature("hardware_setup","Auto-Hardware Setup","AI Intelligence","Inspect runtime memory, ABI and CPU and produce a conservative execution plan.",V1FeatureState.EXISTING),
        V1Feature("adaptive_model","Adaptive Model Selection","AI Intelligence","Select only compatible model profiles backed by measured device benchmarks.",V1FeatureState.INTEGRATION_REQUIRED),
        V1Feature("local_generation","Local AI Generation","Generation","Run genuine image-to-image inference on the device.",V1FeatureState.MODEL_REQUIRED),
        V1Feature("generation_progress","Generation Progress","Generation","Report real inference progress from engine callbacks.",V1FeatureState.INTEGRATION_REQUIRED),
        V1Feature("shuffle_look","Shuffle Look","Generation","Repeat the same edit with a new random seed.",V1FeatureState.SCAFFOLDED),
        V1Feature("style_roulette","Style Roulette","Generation","Choose a non-conflicting combination of styles.",V1FeatureState.SCAFFOLDED),
        V1Feature("intensity","Intensity Slider","Generation","Map Subtle–Wild intensity to model denoising strength.",V1FeatureState.SCAFFOLDED),
        V1Feature("hd_enhance","HD Enhance","Generation","Upscale a real generated image with a local super-resolution model.",V1FeatureState.MODEL_REQUIRED),
        V1Feature("before_after","Before/After Comparison","Generation","Compare original and generated images with a draggable divider.",V1FeatureState.SCAFFOLDED),
        V1Feature("contact_sheet","Multiple Style Preview","Generation","Show a 2×2 grid of genuine generated variations on capable devices.",V1FeatureState.INTEGRATION_REQUIRED),
        V1Feature("save_image","Save Generated Image","Image Management","Save a generated image to the user's media collection.",V1FeatureState.SCAFFOLDED),
        V1Feature("share_image","Share Generated Image","Image Management","Share a generated image through Android Sharesheet.",V1FeatureState.SCAFFOLDED),
        V1Feature("result_preview","Result Preview","Image Management","Display the actual generated image returned by the engine.",V1FeatureState.INTEGRATION_REQUIRED),
        V1Feature("preserve_original","Original Preservation","Image Management","Keep the source image immutable and save outputs separately.",V1FeatureState.SCAFFOLDED),
        V1Feature("android_compatibility","Android Compatibility","Device & Performance","Declare and enforce Android API 26 as the minimum OS.",V1FeatureState.EXISTING),
        V1Feature("ram_detection","RAM Detection","Device & Performance","Read total and available device memory.",V1FeatureState.EXISTING),
        V1Feature("cpu_abi","CPU/ABI Detection","Device & Performance","Inspect supported ABI and processor count.",V1FeatureState.EXISTING),
        V1Feature("low_memory","Low-Memory Processing","Device & Performance","Decode and resize photos using bounded memory.",V1FeatureState.EXISTING),
        V1Feature("model_download","AI Model Download","Device & Performance","Download optional model assets with size and licence disclosure.",V1FeatureState.INTEGRATION_REQUIRED),
        V1Feature("inference_benchmark","Inference Benchmarking","Device & Performance","Measure actual model latency and memory before enabling profiles.",V1FeatureState.MODEL_REQUIRED)
    )

    init { check(all.size == 33) { "V1.0 feature registry must contain exactly 33 entries." } }
}

data class PromptBundle(
    val userRequest: String,
    val positivePrompt: String,
    val preservationPrompt: String,
    val qualityPrompt: String
) {
    val combined: String get() = listOf(userRequest, positivePrompt, preservationPrompt, qualityPrompt)
        .filter(String::isNotBlank).joinToString("\n")
}

object V1PromptInterpreter {
    fun interpret(request: String, selectedStyles: List<Effect>): PromptBundle {
        val enhanced = PromptEnhancer.enhance(request, selectedStyles.map { it.instruction })
        return PromptBundle(
            userRequest = request.trim(),
            positivePrompt = enhanced.positivePrompt,
            preservationPrompt = enhanced.preservationInstruction,
            qualityPrompt = enhanced.qualityInstruction
        )
    }
}

data class SubjectLabel(val label: String, val confidence: Float)
fun interface OfflineImageLabeler {
    suspend fun classify(bitmap: Bitmap): List<SubjectLabel>
}

/** Replace this adapter with a verified offline TFLite/MediaPipe model. */
class UnconfiguredOfflineImageLabeler : OfflineImageLabeler {
    override suspend fun classify(bitmap: Bitmap): List<SubjectLabel> =
        throw ModelNotConfiguredException("Offline image-labeling model has not been configured.")
}

class ModelNotConfiguredException(message: String) : IllegalStateException(message)

object V1StyleRecommender {
    fun recommend(labels: List<SubjectLabel>, styles: List<Effect>, limit: Int = 8): List<Effect> {
        val terms = labels.sortedByDescending { it.confidence }.flatMap { label ->
            when (label.label.lowercase()) {
                "person", "face", "portrait" -> listOf("portrait","photography","lighting","camera","cinematic")
                "dog", "cat", "animal", "pet" -> listOf("nature","photography","lighting","art")
                "landscape", "mountain", "tree", "nature" -> listOf("nature","weather","photography","lighting")
                "building", "architecture" -> listOf("architecture","photography","lighting","art")
                else -> listOf(label.label.lowercase())
            }
        }.distinct()
        return styles.filter { style ->
            terms.any { term ->
                style.category.contains(term, true) || style.title.contains(term, true) ||
                    style.instruction.contains(term, true)
            }
        }.distinctBy { it.id }.take(limit.coerceAtLeast(1))
    }
}

data class DeviceProfile(
    val apiLevel: Int,
    val totalRamMb: Long,
    val availableRamMb: Long,
    val isLowRamDevice: Boolean,
    val supportedAbis: List<String>,
    val cpuCores: Int
)

object V1DeviceManager {
    fun inspect(context: Context): DeviceProfile {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memory = ActivityManager.MemoryInfo().also(manager::getMemoryInfo)
        return DeviceProfile(
            apiLevel = Build.VERSION.SDK_INT,
            totalRamMb = memory.totalMem / 1_048_576L,
            availableRamMb = memory.availMem / 1_048_576L,
            isLowRamDevice = manager.isLowRamDevice,
            supportedAbis = Build.SUPPORTED_ABIS.toList(),
            cpuCores = Runtime.getRuntime().availableProcessors()
        )
    }
}

enum class InferenceProfile { UNSUPPORTED, LOW_MEMORY, BALANCED, HIGH_CAPABILITY }
object V1AdaptiveModelSelector {
    /** Conservative preflight only; real model enablement requires measured inference benchmarks. */
    fun suggest(profile: DeviceProfile): InferenceProfile = when {
        profile.apiLevel < 26 -> InferenceProfile.UNSUPPORTED
        profile.isLowRamDevice || profile.availableRamMb < 900 -> InferenceProfile.UNSUPPORTED
        profile.availableRamMb < 1_500 -> InferenceProfile.LOW_MEMORY
        profile.availableRamMb < 3_000 -> InferenceProfile.BALANCED
        else -> InferenceProfile.HIGH_CAPABILITY
    }
}

data class GenerationRequest(
    val source: Uri,
    val prompt: PromptBundle,
    val selectedStyleIds: List<String>,
    val intensity: Int,
    val seed: Long,
    val requestedWidth: Int,
    val requestedHeight: Int
) {
    val denoisingStrength: Float
        get() = CreativeSelection(selectedStyleIds, intensity).normalizedDenoisingStrength()
}

data class GenerationResult(
    val image: Bitmap,
    val seed: Long,
    val width: Int,
    val height: Int
)

interface LocalImageGenerationEngine {
    suspend fun generate(request: GenerationRequest, onProgress: (Float) -> Unit): GenerationResult
}

/** Deliberately fails closed until a real local inference engine is installed. */
class UnconfiguredLocalImageGenerationEngine : LocalImageGenerationEngine {
    override suspend fun generate(request: GenerationRequest, onProgress: (Float) -> Unit): GenerationResult {
        throw ModelNotConfiguredException("No local image-to-image model/runtime is configured yet.")
    }
}

data class ShufflePlan(val seed: Long, val sameSettings: Boolean = true)
object V1Shuffle {
    fun nextSeed(previousSeed: Long): ShufflePlan {
        var next = Random.nextLong()
        while (next == previousSeed) next = Random.nextLong()
        return ShufflePlan(next)
    }
}

data class SuperResolutionRequest(val source: Bitmap, val scale: Int = 2)
fun interface LocalSuperResolutionEngine {
    suspend fun upscale(request: SuperResolutionRequest): Bitmap
}
class UnconfiguredSuperResolutionEngine : LocalSuperResolutionEngine {
    override suspend fun upscale(request: SuperResolutionRequest): Bitmap =
        throw ModelNotConfiguredException("No offline super-resolution model is configured.")
}

data class ModelAsset(val id: String, val displayName: String, val downloadUrl: String, val bytes: Long, val licenceUrl: String, val sha256: String)
interface ModelAssetInstaller {
    suspend fun install(asset: ModelAsset, destination: File, onProgress: (Float) -> Unit)
}

/** Basic output metadata. Source and output are separate references by design. */
data class SavedEdit(val id: String = UUID.randomUUID().toString(), val sourceUri: Uri, val outputUri: Uri, val createdAtMillis: Long, val prompt: String, val seed: Long)


/** Runtime integrity gate for the bundled catalogue; malformed data fails clearly at startup. */
object StyleCatalogueValidator {
    const val EXPECTED_STYLE_COUNT = 275

    fun validate(styles: List<Effect>): List<Effect> {
        require(styles.size == EXPECTED_STYLE_COUNT) {
            "Style catalogue is incomplete: expected $EXPECTED_STYLE_COUNT entries, found ${styles.size}."
        }
        require(styles.all { it.id.isNotBlank() && it.title.isNotBlank() && it.category.isNotBlank() && it.instruction.isNotBlank() }) {
            "Style catalogue contains an entry with missing ID, name, category or instruction."
        }
        require(styles.map { it.id }.distinct().size == styles.size) {
            "Style catalogue contains duplicate style IDs."
        }
        require(styles.map { it.title }.distinct().size == styles.size) {
            "Style catalogue contains duplicate style names."
        }
        require(styles.map { it.category }.distinct().size >= 2) {
            "Style catalogue must contain multiple categories."
        }
        return styles
    }
}


/** Case-insensitive search across all user-relevant style metadata. */
object StyleCatalogueSearch {
    fun matches(style: Effect, query: String): Boolean {
        val term = query.trim()
        if (term.isEmpty()) return true
        return style.title.contains(term, ignoreCase = true) ||
            style.category.contains(term, ignoreCase = true) ||
            style.instruction.contains(term, ignoreCase = true) ||
            style.id.contains(term, ignoreCase = true)
    }
}


/** Hardware-aware execution advice. This is preflight, not a performance guarantee. */
data class HardwareExecutionPlan(
    val profile: DeviceProfile,
    val inferenceProfile: InferenceProfile,
    val maxInputEdge: Int,
    val allowGpuDelegate: Boolean,
    val explanation: String
)

object V1HardwareSetup {
    fun plan(profile: DeviceProfile): HardwareExecutionPlan {
        val suggested = V1AdaptiveModelSelector.suggest(profile)
        val maxEdge = when (suggested) {
            InferenceProfile.UNSUPPORTED -> 0
            InferenceProfile.LOW_MEMORY -> 256
            InferenceProfile.BALANCED -> 384
            InferenceProfile.HIGH_CAPABILITY -> 512
        }
        val abiSupportsArm = profile.supportedAbis.any { it.startsWith("arm64-v8a") || it.startsWith("armeabi-v7a") }
        val gpuEligible = suggested != InferenceProfile.UNSUPPORTED && abiSupportsArm
        val explanation = when {
            suggested == InferenceProfile.UNSUPPORTED -> "Insufficient available memory or unsupported Android version; inference disabled."
            profile.availableRamMb < 1500 -> "Low-memory mode: small input and conservative processing."
            else -> "Hardware preflight complete. Runtime benchmarking is still required before selecting an optimized delegate."
        }
        return HardwareExecutionPlan(profile, suggested, maxEdge, gpuEligible, explanation)
    }
}

data class InferenceBenchmark(
    val modelId: String,
    val profile: InferenceProfile,
    val latencyMillis: Long,
    val peakMemoryMb: Long,
    val succeeded: Boolean,
    val deviceFingerprint: String
)

object V1MeasuredModelSelector {
    fun select(
        profile: DeviceProfile,
        benchmarks: List<InferenceBenchmark>,
        installedModelIds: Set<String>
    ): InferenceBenchmark? {
        val hardware = V1HardwareSetup.plan(profile)
        if (hardware.inferenceProfile == InferenceProfile.UNSUPPORTED) return null
        return benchmarks.asSequence()
            .filter { it.succeeded && it.modelId in installedModelIds }
            .filter { it.deviceFingerprint.isNotBlank() && it.latencyMillis > 0 && it.peakMemoryMb > 0 }
            .filter { it.peakMemoryMb < profile.availableRamMb * 0.65 }
            .filter { it.profile.ordinal <= hardware.inferenceProfile.ordinal && it.profile != InferenceProfile.UNSUPPORTED }
            .minByOrNull { it.latencyMillis }
    }
}

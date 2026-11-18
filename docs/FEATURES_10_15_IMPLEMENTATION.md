# Aura Studio Features 10–15 Implementation

## 10. AI Prompt Interpreter
- Added intent classification for portrait, landscape, product, style-transfer, lighting, background and general edits.
- PromptBundle carries the classified intent alongside the assembled prompt, preservation and quality instructions.
- This is a deterministic local interpreter, not a hosted LLM.

## 11. Automatic Prompt Enhancer
- Expanded deterministic prompt enrichment with intent-sensitive guidance for portraits, landscapes, products and background edits.
- Adds preservation, composition, lighting, perspective and artifact-avoidance constraints.
- No external API or upload is used.

## 12. Context-Aware Suggestions
- Added a service interface that can call an offline image-labeling model and rank styles from its labels.
- The model adapter intentionally fails closed because no verified labeling model is installed.
- The screen currently supports user-described photo context and labels recommendations as description-based; it does not claim automatic image recognition.

## 13. Smart Style Recommendations
- Recommendations now score style metadata against subject labels, weight by label confidence, filter invalid confidence values, rank by score and deduplicate by style ID.
- Suggestions can be applied directly from the UI subject-description field.

## 14. Auto-Hardware Setup
- Reads Android API, total/available RAM, low-RAM flag, supported ABIs and CPU core count.
- Produces a conservative execution plan with input-edge limit and GPU-delegate eligibility guidance.
- GPU eligibility is a preflight hint only; no GPU delegate is enabled without runtime testing.

## 15. Adaptive Model Selection
- Added benchmark record and selector requiring a successful measured run, installed model ID, positive latency/memory values, matching device fingerprint and memory headroom.
- No benchmark data is fabricated or persisted yet. Selection returns null until a compatible measured benchmark is supplied.

## Validation
- Unit tests cover intent classification, confidence-weighted recommendations, description-based suggestions, hardware rejection and benchmark-gated selection.
- CI must pass on the latest feature-branch commit before these changes are considered build-verified.
- Offline image-labeling model, actual inference runtime and per-device benchmark capture remain separate integration work.

# Aura Studio — High-Feasibility Feature Rollout

This document records the approved scope for the next product increment. Existing app behavior, the 275+ style catalogue, Android API 26 minimum, privacy commitments and direct Generate flow must be preserved.

## First-release implementation scaffold

Initial Kotlin foundations for all 33 V1.0 feature entries are tracked in:
- `app/src/main/java/com/promptpix/ai/V1FeatureFoundation.kt` — 33-entry feature registry, prompt interpreter, local image-labeler contract, style recommender, device profile, adaptive profile selector, generation/upscaling interfaces, shuffle plan and model-asset contract.
- `app/src/main/java/com/promptpix/ai/V1FeatureUi.kt` — creative controls, prompt preview, style roulette action, before/after comparison and feature status panel.
- `app/src/test/java/com/promptpix/ai/V1FeatureFoundationTest.kt` — initial unit tests.
- Existing `MainActivity.kt` now hosts the creative controls and V1 feature status panel.

These are initial scaffolds. Inference, image recognition and upscaling adapters deliberately fail closed until real local model/runtime implementations are installed. The feature registry makes all 33 entries visible with honest implementation states.

## Approved immediate scope

| Feature | Planned implementation | Current status / gate |
|---|---|---|
| Automatic Prompt Enhancer | Deterministic on-device prompt interpreter that combines the user's words, selected style instructions, preservation guidance and quality constraints. A small local language model can be evaluated later. | Helper logic added; wire into Generate only after img2img engine is real. |
| Context-Aware Smart Suggestions | On-device image labeling, then rank existing styles by detected subject. | Conservative subject-based fallback helper added; true automatic labeling requires a bundled/offline model and validation. |
| Auto-Hardware Setup | Use DeviceReadiness diagnostics to select model, resolution and backend from measured RAM/ABI/runtime capability. | Existing diagnostics retained; automated model selection remains gated on actual model/runtime benchmarks. |
| One-Tap HD Enhance | Optional lightweight super-resolution model, separately downloadable with licence and size disclosure. | Architecture planned; no upscaler weights or inference implementation yet. |
| Try Again / Shuffle Look | Re-run the same edit with a fresh seed and unchanged source/style/intensity. | Requires a functioning img2img engine and seed support. Never show a fake result. |
| Smart Style Roulette | Select a small combination of styles, at most one per category, to reduce conflicts. | Roulette selection helper added; UI wiring pending. |
| Intensity Slider | Map a simple Subtle–Wild value to model img2img denoising strength. | Mapping helper added; UI wiring and engine support pending. |
| Before/After Compare | Draggable curtain comparison using original and actual generated output. | Requires a real generated bitmap/URI; UI wiring pending. |

## Product and engineering rules

1. No cloud upload, paid API, account, subscription or advertisement is required.
2. Prompt enhancement and style selection must run locally.
3. Prompt enhancement must not invent user-requested scene changes. It may add quality and preservation constraints.
4. Intensity must affect actual img2img denoising strength, not a cosmetic overlay.
5. Shuffle must preserve the source image, selected styles, prompt and intensity while changing only the random seed.
6. Automatic hardware selection must use measured capability and safe fallbacks; RAM marketing tiers alone are insufficient.
7. HD Enhance must be optional and must not overwrite the original or generated source.
8. Smart suggestions must be based on a verified on-device labeler. Until then, any manual subject-based suggestion helper must be labelled as such.
9. Before/after comparison must compare the original with a real model output.
10. No progress animation or UI may imply generation succeeded before an inference result exists.

## Future development backlog — approved features

These five features are explicitly accepted for future development. They are part of the product roadmap, but are not part of the current high-feasibility implementation increment. Keep their architecture seams in mind; do not add non-functional controls or claim completion before the required models and workflows are validated.

### 1. Magic Brush / Selective Inpainting
- Let users paint a mask over the region they want to change.
- Provide brush size, erase-mask, undo/redo and clear-mask controls.
- Convert the painted overlay into a model-resolution mask with correct coordinate mapping.
- Run genuine masked inpainting so unmasked regions are preserved as closely as the model permits.
- Include mask feathering and edge-aware compositing to reduce visible seams.
- Dependencies: image-to-image/inpainting model with mask input, segmentation or mask utilities, Compose drawing overlay, memory-safe bitmap pipeline.
- Acceptance: mask aligns with the displayed image after zoom/crop; undo/redo works; only masked area is submitted for modification; original remains untouched; low-memory failure is handled without data loss.

### 2. Face Protect / Background-Only Mode
- Detect faces locally and generate a protection mask.
- Allow background-only transformation while retaining the original face pixels where possible.
- Provide a user-visible mask preview and manual correction because automatic detection can fail.
- Composite protected pixels back after generation; do not promise perfect identity preservation.
- Dependencies: offline face detection/segmentation, mask refinement, image compositing, inference pipeline that supports masks or region preservation.
- Acceptance: no network required; face mask can be inspected/corrected; protected source pixels are preserved by compositing; multiple faces and detection failures are handled; no identity or accuracy guarantee is implied.

### 3. Generation Time-Lapse Export
- Capture genuine intermediate generation states only when the selected inference runtime exposes them.
- Encode a short Before → Processing → Final clip or GIF using Android-supported media APIs.
- Make capture optional and bounded to avoid excessive memory/storage use.
- If intermediate states are unavailable, offer a clearly labelled simple before/after transition instead; never fabricate denoising frames.
- Dependencies: inference callbacks/intermediate tensors or decoded frames, bounded frame sampling, MediaCodec or a vetted encoder, storage permission-safe export.
- Acceptance: exported media contains real captured states; cancellation and insufficient-storage conditions are handled; output dimensions, frame rate and file size are bounded.

### 4. Non-Destructive Edit History
- Store each generated result separately with source reference, prompt, style IDs, intensity, seed, model/version and timestamp.
- Allow users to reopen a prior edit, adjust settings and regenerate.
- Preserve originals; use app-private storage and explicit export/share actions.
- Provide deletion and storage-management controls; avoid retaining unnecessary face/photo data.
- Dependencies: Room database, app-private media storage, migration strategy, generation metadata schema.
- Acceptance: history survives process restart; records can be reopened and regenerated when the required model is available; deleting a history item removes its managed files safely; original user photos are not overwritten.

### 5. Offline Style Recipes (QR and JSON Sharing)
- Serialize style IDs, compatible settings and optional user-authored prompt text into a versioned recipe format.
- Export/import as JSON and QR code without requiring a server.
- Validate schema version, payload size, known style IDs and incompatible combinations on import.
- Treat imported text as untrusted user data; never execute code or download a model automatically.
- Dependencies: versioned Kotlin serialization schema, QR encoder/decoder, Android Sharesheet and import flow.
- Acceptance: recipe round-trips offline; malformed/oversized payloads are rejected safely; unknown style IDs are reported and skipped or mapped explicitly; import never changes the user's current edit until they confirm.

### Future-backlog sequencing
1. Non-Destructive Edit History and Offline Style Recipes (low coupling to model internals).
2. Face Protect / Background-Only Mode (after validated local face/segmentation models).
3. Magic Brush / Selective Inpainting (after mask-conditioned inference is proven).
4. Generation Time-Lapse Export (after runtime exposes genuine intermediate states).

The order may change after model/runtime benchmarks, but all five remain approved roadmap items.

## Other deferred capabilities

Batch editing, background removal and personal style library remain later milestones.

## Acceptance criteria

- Existing photo picker, camera input, style search/filter/multi-select and catalogue continue to work.
- Prompt enhancer output is deterministic for the same inputs and stays on-device.
- Roulette returns no more than one style per category.
- Intensity maps monotonically from 0..100 to a bounded engine strength; final bounds must be tuned using model tests.
- Shuffle uses a fresh seed without changing other generation settings.
- Automatic subject detection is not marked complete until an offline model works without network access.
- HD Enhance is not enabled until a compatible model passes device benchmarks.
- Before/after is enabled only after actual img2img output exists.
- Android CI builds the debug APK successfully; device tests cover API 26 and representative low-memory hardware.

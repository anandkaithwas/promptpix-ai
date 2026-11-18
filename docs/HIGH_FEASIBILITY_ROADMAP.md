# Aura Studio — High-Feasibility Feature Rollout

This document records the approved scope for the next product increment. Existing app behavior, the 275+ style catalogue, Android API 26 minimum, privacy commitments and direct Generate flow must be preserved.

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

## Deferred capabilities

Magic Brush/inpainting, Face Protect/background-only editing, generation time-lapse, non-destructive edit history, offline QR/JSON style recipes, batch editing, background removal and personal style library remain later milestones.

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

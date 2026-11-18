# Aura Studio Features 16–23

## 16. Local AI Generation
- The app's generation coordinator now invokes the LocalImageGenerationEngine contract and displays only a real Bitmap returned by it.
- The configured implementation remains UnconfiguredLocalImageGenerationEngine and fails closed. No verified compatible image-to-image model/runtime is available in this repository yet. This feature is not complete.

## 17. Generation Progress
- Coordinator publishes Preparing, Running, Success and Failed state updates to the UI.
- Determinate progress is sourced only from the inference engine callback and clamped to [0, 1]. Invalid/NaN values are ignored.
- No synthetic percentage or timer-based progress is generated.

## 18. Shuffle Look
- Try Again reruns the last request with a fresh seed while preserving prompt, selected styles and intensity.

## 19. Style Roulette
- Existing roulette action selects a bounded, non-duplicated set of catalogue styles and updates selected IDs.

## 20. Intensity Slider
- Slider maps 0–100 to the request's normalized denoising strength and is passed to the generation engine request.

## 21. HD Enhance
- HD action calls LocalSuperResolutionEngine on the generated bitmap.
- Current adapter is unconfigured and fails closed. No fake upscaled result is returned.

## 22. Before/After
- Added original-image decode for comparison and a draggable divider UI.
- Original is decoded independently and not modified by generation.

## 23. Multiple Style Preview
- Added sequential 2×2 generation orchestration, using four distinct seeds and the real engine.
- Results are shown only if all four real engine runs succeed.
- Current engine gate means no contact sheet can be produced until local inference is configured.

## Verification
- Added unit tests for progress normalization, seed freshness and model-required registry states.
- GitHub Actions is running unit tests and debug APK build. Device validation remains outstanding.

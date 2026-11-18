# Aura Studio — Features 8 and 9

## Feature 8: Custom Instructions

- Main screen accepts natural-language edit instructions.
- Input is capped at 1,000 characters and trimmed before prompt assembly.
- Blank custom text is allowed when one or more catalogue styles are selected.
- Blank text with no selected style is rejected by the Generate action.
- `V1PromptInterpreter` combines the user request, selected style instructions, preservation guidance and quality guidance.
- Tests cover trimming, blank input, maximum length and style/custom prompt composition.

## Feature 9: Direct Generate

- The main Generate action builds a `GenerationRequest` and calls `V1GenerationCoordinator.generate` directly; there is no mandatory prompt-review screen.
- Source photo URI, stable style IDs, intensity, random seed and requested dimensions are passed to the coordinator.
- UI observes preparing/running/success/failure states and displays only a bitmap returned by the engine.
- The current configured engine is intentionally `UnconfiguredLocalImageGenerationEngine`. It fails closed with a model-not-configured message.
- Therefore, the direct action and orchestration path are connected, but actual on-device image generation is **not complete** until a verified local inference engine and compatible model are integrated and device-tested.
- No fake progress, placeholder output or cloud upload is used.

## Verification

GitHub Actions is running unit tests and the debug build on the feature branch. Device-level validation remains outstanding.

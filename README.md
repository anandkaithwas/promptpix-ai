# Aura Studio

Offline-first Android creative photo editor. Browse visual styles or describe an edit in everyday language.

## Product experience
1. Select a photo from the gallery or capture one with the camera.
2. Choose a visual effect or describe the desired change in natural language.
3. Aura Studio interprets the request and displays a plain-language Edit Plan.
4. The user reviews the plan and can confirm, revise the request, or cancel.
5. Image generation starts only after explicit confirmation.
6. Show real animated progress, then the generated result with before/after comparison.
7. Save the generated image or share it through Android's native Sharesheet.

## Confirmation-first natural-language editing
The app must never silently transform a photo immediately after interpreting free text. Before generation, show an Edit Plan summarizing:
- Main requested change
- Subject and identity preservation requirements
- Background/environment changes
- Lighting, colour, mood and visual style
- Composition and framing changes
- Elements explicitly requested to remain unchanged
- Ambiguities or conflicting instructions

Required actions:
- **Confirm & generate** — approves the plan and starts image-to-image inference.
- **Edit request** — returns to the description field with original wording retained.
- **Cancel** — exits without generating.

If instructions are materially ambiguous or contradictory, ask a focused clarification before enabling generation. Never claim success until a real transformed image exists.

## Intended local pipeline
Photo + natural-language request -> on-device instruction interpreter -> structured edit plan -> user review and confirmation -> local image-to-image model -> generated output -> save/share.

## Current implementation status
The prototype includes gallery/camera selection, visual effect cards, a natural-language description field and a share-action scaffold. The instruction interpreter, Edit Plan review/confirmation screen, image-to-image engine and generated-image export are not yet integrated. Sharing must remain unavailable until a real generated output URI exists. Never present the original photo as an edited result.

## Product constraints
- No paid API, subscription, ads or account requirement.
- Prefer on-device inference for privacy and offline operation.
- Benchmark model quality, speed, RAM, storage and thermal behaviour on the target device.
- Verify model and runtime licences before distributing model weights.
- Preserve the original photo; edits create a separate output.

# Aura Studio — Product Specification

**Status:** Product direction approved; implementation in progress  
**Platform:** Android  
**Core promise:** Turn a user's photo and creative intent into an edited image without exposing prompt syntax or requiring paid cloud services.

## 1. Product principles
- Visual-first effect discovery; never expose technical prompt strings.
- Natural-language editing in ordinary user language.
- Confirmation-first: explain the system's interpretation and obtain approval before generation.
- Honest feedback: never simulate generation or show the source as a completed edit.
- Privacy-first, with on-device processing preferred.
- Free to use: no paid API, subscription or advertising planned.

## 2. Primary user journey
1. Select an image from Android Photo Picker or capture one with the camera.
2. Choose a catalogue effect or enter a natural-language editing request.
3. Interpret the request in the context of the selected image.
4. Display an Edit Plan before running the image model.
5. User confirms, edits the request, or cancels.
6. Only after confirmation, run image-to-image inference and show animated progress.
7. Present the generated image with before/after comparison, save and share options.

## 3. Edit Plan review screen
Translate the request into a readable, editable plan containing:
- **Main change:** central transformation requested.
- **Subject preservation:** face, identity, pose, clothing, product details or other requested features to preserve.
- **Background/environment:** what changes and what remains.
- **Lighting/colour:** tone, palette, contrast and light direction.
- **Composition/style:** framing, depth of field and artistic or photographic treatment.
- **Keep unchanged:** explicit constraints extracted from the request.
- **Clarification needed:** unresolved ambiguity or conflicting instructions.

### Required actions
- **Confirm & generate:** approve the displayed plan and begin generation.
- **Edit request:** return to text input with the original wording retained.
- **Cancel:** abandon this edit without running inference.

Generation must not start merely because interpretation has finished. It starts only after an explicit user confirmation.

### Ambiguity handling
If instructions are materially ambiguous, contradictory, or likely to alter an important feature unintentionally, ask a concise clarification before enabling confirmation. Do not silently invent major changes. Show plain-language instructions, not internal model prompts or raw structured data.

## 4. Technical architecture
### A — Input
- Android Photo Picker and camera capture.
- Decode/resize a working copy to the model's input limits.
- Preserve the original file unchanged.

### B — Instruction understanding
- On-device language model or deterministic local parser converts natural language into a structured edit plan.
- Plan contains requested edits, preservation constraints, style, composition, ambiguity flags and model parameters.
- Internal prompt templates remain hidden from users.

### C — Human confirmation
- Render the structured plan in the Edit Plan screen.
- Allow confirm, revise and cancel.
- Persist the approved plan for the generation job.
- No inference job may be enqueued before explicit confirmation.

### D — Image generation
- Run a compatible local image-to-image model.
- Show real progress stages and cancellation where supported.
- Handle out-of-memory, unsupported input, missing model and generation failure.
- Never show a placeholder or original image as a successful generated result.

### E — Output
- Save generated output as a new image through Android MediaStore.
- Provide before/after comparison.
- Share the generated image using Android ACTION_SEND and a content URI with read permission via native Sharesheet.
- Keep save/share disabled until a valid generated output exists.

## 5. Non-functional requirements
- Offline-first after required model assets are installed.
- No account or paid inference endpoint.
- Disclose clearly if any optional cloud-processing path is introduced.
- Evaluate model licence, download size, runtime compatibility, RAM, latency, quality, thermals and battery.
- Protect user images and avoid unnecessary copies.
- Fail gracefully on devices unable to run the selected model.

## 6. Acceptance criteria
1. Select a gallery image or capture a photo.
2. Enter a free-form natural-language editing request.
3. See a readable interpretation/Edit Plan before image generation.
4. Edit the original request and review an updated plan.
5. Cancel without triggering generation.
6. Generation begins only after tapping **Confirm & generate**.
7. Progress UI reflects a real inference job.
8. Success appears only when an actual transformed image is available.
9. Save writes the generated image, not the source.
10. Share opens Android's native share chooser with the generated image attached.
11. Raw prompt strings and internal model instructions never appear in the user-facing UI.

## 7. Current status and next milestone
The app remains a UI prototype. Gallery/camera selection, effect catalogue, natural-language text input and a share-action scaffold exist. The local instruction interpreter, Edit Plan review/confirmation flow, image generation engine and actual output export are not complete. Next milestone: implement the confirmation-first instruction pipeline, then integrate and benchmark the local image-generation model.

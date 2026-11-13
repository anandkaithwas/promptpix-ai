# Aura Studio — Product Specification

**Product type:** Free Android AI image-to-image editor  
**Product status:** Early prototype; core AI engine not integrated  
**Positioning:** A creative transformation tool, not a beauty camera or selfie-filter app.

## 1. Product vision

A user uploads or captures their own photograph, selects one or more visual commands from a catalogue of at least 250 categorized styles, writes a custom natural-language instruction, or combines both. Aura Studio understands the intended changes and uses an image-to-image AI model to transform the user's actual photo.

The catalogue is not a set of static previews or cosmetic camera filters. Each catalogue item represents an editing instruction/style that is passed to the image transformation engine.

## 2. Main user journey

1. **Input photo:** Select an image from gallery or capture with camera.
2. **Choose editing method:** The user can:
   - browse and select a command/style from the 250+ catalogue;
   - type a custom description in a free-text box;
   - combine catalogue selection(s) with custom instructions.
3. **Interpret:** AI interprets the selected catalogue commands and/or custom description in the context of the uploaded photo.
4. **Edit Plan:** Show a plain-language summary of exactly what the AI intends to change and what it intends to preserve.
5. **Confirm:** User chooses Confirm & generate, Edit request, or Cancel. No image generation starts before explicit confirmation.
6. **Generate:** Run real image-to-image inference on the user's photo. Show animated progress tied to actual processing stages.
7. **Result:** Display the actual transformed image, support before/after comparison, then save or share it.

## 3. Catalogue: 250+ categorized commands

### Catalogue UX
- Provide a searchable, scrollable, categorized catalogue with at least 250 commands/styles.
- Each item is a visual icon tile with a short title and a concise explanation.
- Icons are semantic visual cues (e.g. camera angle, lighting, weather, lens, fantasy, material, art style); they are not beauty-camera controls.
- Selecting an item adds its instruction to the current edit request.
- User can inspect selected items, remove them, and combine compatible commands.
- Show warnings or ask for clarification when selected commands conflict.
- Do not expose raw slash commands, prompt templates or technical model instructions in the normal UI.
- Do not make sample thumbnails the primary product experience. The user wants to choose an icon representing a style/action and have that style applied to their own uploaded image by the AI model.

### Initial catalogue taxonomy
Use the user's supplied reference catalogue as the seed taxonomy, including:
- Photography, camera angles, perspectives and framing
- Weather, time and atmosphere
- Lighting and depth
- Motion, style and aesthetic
- Fantasy, sci-fi and epic visuals
- Nature, space, places and adventure
- Art and drawing styles
- Buildings and interiors
- Toys and miniatures
- Materials and textures
- Effects and special effects
- Design and branding

The two reference posters contain overlapping and inconsistent numbering/names. Normalize and deduplicate entries into a curated catalogue of at least 250 distinct, useful transformations; do not blindly copy numbering errors or duplicate concepts.

## 4. Custom natural-language editing

A prominent description box must always be available after photo selection. The user can write requests such as:
- “Replace the background with a snowy Swiss mountain village, keep the person unchanged.”
- “Turn this into a cinematic night portrait with soft rim lighting.”
- “Make this look like a professional 35mm film photograph.”

The custom description is a first-class editing path, not an optional secondary feature. The system must understand the photo plus the user's description, not just match keywords.

Catalogue and custom text can be used independently or together. The AI should combine compatible instructions and identify contradictions before generation.

## 5. Confirmation-first Edit Plan

Before generation, show a readable plan covering:
- Main requested transformation
- Subject and identity preservation constraints
- Background/environment changes
- Lighting, colour and mood
- Camera perspective, framing and composition
- Chosen catalogue styles/commands
- Custom instruction summary
- Any ambiguity or conflicting instructions

Actions:
- **Confirm & generate**
- **Edit request**
- **Remove/change selected styles**
- **Cancel**

No inference job may start until the user explicitly confirms. Preserve the original wording and selected catalogue choices when returning to edit.

## 6. Image-to-image AI backend

Required pipeline:
Photo + selected catalogue instruction(s) + custom description -> multimodal instruction understanding -> normalized structured edit specification -> user review and confirmation -> image-to-image model conditioned on the input photo and edit specification -> generated image -> save/share.

Important:
- The image model must receive the actual selected photo as its input image.
- A text-only prompt generator is not an image editor.
- Static examples, CSS/Compose overlays, colour matrices, LUTs, gradients or canned sample photos cannot be represented as AI-generated results.
- Preserve identity/details when requested, to the extent supported by the chosen model.
- Use masks/inpainting or control mechanisms where supported for localized edits.
- Report genuine inference progress and errors.
- Keep source and output separate.

## 7. Free and offline requirements

- No paid API, subscription, ads or mandatory account.
- Prioritize on-device inference for privacy and offline operation.
- Evaluate open-weight image-to-image models and Android-compatible runtimes for licence, commercial-use terms, file size, memory, speed, quality, device support, thermal load and battery.
- Evaluate the target OPPO device before committing to a model. Do not assume the phone can run a large diffusion model simply because it is Android.
- If full local inference is too demanding, assess a smaller model, quantization, staged download or optional user-controlled community/self-hosted runtime. Do not silently introduce a paid cloud API.
- Download model weights only after clear user consent and show storage requirements.

## 8. Output

- Show actual generated result and before/after comparison.
- Save as a new image through Android MediaStore.
- Share the generated image through Android native Sharesheet.
- Save/share controls stay disabled until a valid generated output exists.

## 9. Current implementation status

Implemented prototype elements:
- Gallery/camera input
- 20 starter catalogue metadata entries
- Custom natural-language text field
- Basic keyword-based Edit Plan preview
- Initial app branding

Not implemented:
- Complete 250+ curated catalogue and icon grid
- Catalogue selection composition with custom text
- Genuine multimodal instruction understanding
- Confirmation screen wired to a real generation job
- Image-to-image model/runtime
- Actual generated output, progress, save and share pipeline

## 10. Acceptance criteria

1. User can upload or capture a photo.
2. User can search/browse at least 250 categorized icon commands.
3. User can select an icon command and apply it to their own photo through image-to-image AI.
4. User can enter a custom description and have the AI interpret it in the context of their photo.
5. User can combine catalogue styles and custom text.
6. User sees a plain-language Edit Plan and can confirm, revise or cancel.
7. No image generation occurs before explicit confirmation.
8. The actual input photo is passed to the image-to-image model.
9. UI reports genuine generation progress and never fakes a result.
10. User can save/share only a real generated output.
11. Core product remains free, with no paid API dependency.

## 11. Immediate development priorities

1. Correct the app information architecture around **photo first -> 250+ icon catalogue and/or custom description -> AI interpretation -> confirmation -> image-to-image generation**.
2. Build a normalized, deduplicated catalogue data file of 250+ commands with categories, labels, icons and internal instruction templates.
3. Implement icon catalogue browsing, search, multi-select, selected-style chips and custom-description composition.
4. Build the Edit Plan to consume both catalogue selections and custom text.
5. In parallel, benchmark a genuinely free on-device image-to-image model on the target handset before promising offline generation.

# Aura Studio

Aura Studio is a free, Android-first AI image-to-image editing app. It is **not** a beauty-camera app, selfie filter app, or collection of static sample thumbnails.

## Core experience

The user starts with their own photo, then chooses how to describe the transformation:

1. **Choose from the 250+ style/action catalogue.** Browse searchable, categorized visual command tiles. Each tile has a recognizable icon, name and short plain-language meaning. The user can combine compatible catalogue choices where supported.
2. **Write a custom instruction.** A free-text description box lets the user explain any desired edit in their own words. The AI understands the request in the context of the uploaded photo.
3. **Combine both.** A catalogue style can be selected and refined with the user's own description.

The selected catalogue command or custom instruction is sent through the same image-editing pipeline. The app must transform the user's actual photo using image-to-image inference. Static example images, decorative thumbnails, overlays, or ordinary colour filters are not substitutes for AI image generation.

## Confirmation-first flow

Photo -> choose one or more catalogue styles and/or enter custom description -> AI interprets the request -> show a plain-language Edit Plan -> user confirms / revises / cancels -> image-to-image generation -> actual output -> save/share.

The Edit Plan must describe the requested changes, subject/identity preservation, background, lighting, colour, composition, style and any ambiguity. Generation starts only after explicit confirmation.

## Catalogue requirements

- Target at least 250 useful commands/styles, organized by category and searchable.
- Use icon tiles with concise names and descriptions; icons should communicate the visual concept, not act as beauty-camera controls.
- Catalogue selection is an instruction/template for image-to-image transformation, not a local cosmetic filter.
- Keep internal prompt strings hidden from end users.
- Support a custom natural-language description at all times.
- Allow catalogue + custom instruction composition with conflict detection.

## Honest implementation status

Current Android prototype has gallery/camera input, 20 starter effect metadata entries, custom text input and a basic keyword-based Edit Plan preview. It does **not** yet implement the 250-item catalogue, genuine AI instruction understanding, image-to-image inference, real generation progress, or completed image export. The current 20 entries are starter catalogue data, not the final scope. Do not describe the app as generating images until the model actually runs and returns an edited image.

## Free/offline goal

No paid API, subscription, advertisements or account requirement. Prefer local model inference, but validate model licence, runtime compatibility, model size, RAM, latency, thermals, battery and output quality on target devices. If a device cannot run the model, clearly explain the limitation rather than silently using a paid endpoint or pretending to generate.

## Output

Preserve the source image. Save generated results as new files and share only a real generated output using Android's native Sharesheet.

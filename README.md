# Aura Studio

Aura Studio is a free Android AI image-to-image editor. It is not a beauty-camera app or a static filter gallery.

## Minimum device target
- Android 14 (API 34) or newer.
- 64-bit Android device.
- 3 GB physical RAM minimum; 4 GB or more recommended.
- 64 GB device storage target. The app cannot reliably verify the device's advertised total storage capacity, so it checks available free space before model downloads.
- Keep at least 3 GB free storage before downloading an optional image model; actual model size will be shown before download.

## Important AI hardware limitation
The minimum app compatibility target is not a guarantee that every generative model can run locally. Diffusion models can require multiple gigabytes of RAM for weights and intermediate tensors. Aura Studio must benchmark a small, quantized image-to-image model on representative 3 GB devices before claiming local generation support. Devices that cannot run a model locally must receive a clear capability message; the app must not fake results or silently use paid cloud APIs.

## Main experience
1. Select a photo from gallery or camera.
2. Select one or more of 275+ visual transformation styles.
3. Optionally enter a natural-language editing description.
4. Tap **Generate**. There is no separate review button or mandatory review screen.
5. The engine internally interprets the style instructions and custom description.
6. Run genuine image-to-image inference using the selected photo as the source.
7. Show real progress and the actual generated result.
8. Save the result or share it using Android's Sharesheet.

## Catalogue
- Searchable and categorized style catalogue stored at `app/src/main/assets/catalogue/styles.json`.
- Each style has a unique ID, category, display name, semantic icon and hidden editing instruction.
- Multi-select is supported; incompatible instructions must be resolved by the interpreter.
- Do not expose raw prompt strings in the user interface.

## Zero-cost and privacy requirements
- No paid API, subscription, advertisement, or mandatory account.
- Prefer on-device inference and offline operation.
- Any optional cloud fallback must be genuinely free, explicitly disclosed, opt-in, and never required for core app use.
- Preserve the original photo; generated output is saved separately.

## Current implementation status
- Android minimum SDK 34 and target SDK 36 configured.
- Gallery/camera input, catalogue, search, category filters, multi-selection and custom description are implemented.
- Actual image-to-image model and inference runtime are not yet integrated.
- Real generation progress, output saving and output sharing remain to be implemented and tested.

## Development sequence
1. Establish the Android 14 / 3 GB / 64 GB compatibility baseline.
2. Add device diagnostics and memory-safe image preprocessing.
3. Benchmark candidate small/quantized image-to-image models and runtimes.
4. Implement a working inference proof of concept.
5. Integrate it behind the single Generate action.
6. Add actual progress, result comparison, save and share.

Do not claim that Aura Studio generates images until an actual model returns a transformed image.
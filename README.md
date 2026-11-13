# Aura Studio

Aura Studio is a free Android AI image-to-image editor. It is not a beauty-camera app or a static filter gallery.

## Minimum app compatibility
- Android 8.0 Oreo (API 26) or newer.
- 3 GB physical RAM minimum device class; 4 GB or more recommended for general app responsiveness.
- 64 GB device storage target. The app cannot reliably verify advertised total device storage; it checks available free space before optional model downloads.
- Keep at least 3 GB free storage for a future optional model package, but the actual model size and storage requirement must be displayed before download.
- The app interface must remain usable on low-memory devices; large source photos should be decoded and resized safely.

## Critical AI hardware limitation
Android 8 / 3 GB RAM is the minimum compatibility target for the app shell, not a guarantee that on-device diffusion generation will work. Model execution depends on available RAM after Android and other apps reserve memory, CPU/GPU/NPU support, model quantization, and intermediate tensor memory. The app must detect capability and benchmark before enabling a local model. Never promise that every 3 GB device can generate images.

## AI execution strategy
- Primary goal: free, on-device image-to-image inference where device capability permits.
- Benchmark small and quantized models at low resolutions (start at 256x256 and 384x384; test 512x512 only where stable).
- For devices that fail the local inference capability check, clearly explain that local generation is unsupported on that device. Do not silently send photos to a cloud service.
- Optional cloud inference may only be added if genuinely free, explicitly opt-in, privacy-disclosed, and not required for core app use.
- Do not bundle multi-gigabyte model weights inside the base APK. Offer separately downloadable models, show size and license, and require user consent.

## Main experience
1. Select a photo from gallery or camera.
2. Select one or more of 275+ visual transformation styles.
3. Optionally enter a natural-language editing description.
4. Tap **Generate**. There is no separate review button or mandatory review screen.
5. The engine internally interprets style instructions and custom description.
6. Run genuine image-to-image inference using the selected photo as source.
7. Show real progress and the actual generated result.
8. Save the result or share it using Android Sharesheet.

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
- Android minSdk 26 (Android 8.0) and targetSdk 36.
- Gallery/camera input, catalogue, search, category filters, multi-selection and custom description are implemented.
- Actual image-to-image model and inference runtime are not yet integrated.
- Real generation progress, output saving and output sharing remain to be implemented and tested.

## Development sequence
1. Confirm Android 8 compatibility of the app shell and dependencies.
2. Add device diagnostics and memory-safe image preprocessing.
3. Benchmark candidate small/quantized image-to-image models and runtimes on 3 GB RAM devices.
4. Implement a working inference proof of concept.
5. Integrate it behind the single Generate action.
6. Add actual progress, result comparison, save and share.

Do not claim that Aura Studio generates images until an actual model returns a transformed image.
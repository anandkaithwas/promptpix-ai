# Aura Studio — Product Specification

## Minimum supported hardware and OS
- Minimum Android version: Android 8.0 Oreo (API 26).
- Minimum device class: 3 GB physical RAM.
- Storage target: devices with 64 GB internal storage; show actual free space before downloading model assets.
- UI compatibility and AI inference capability are separate. The app shell must work on API 26 devices, but local image generation is enabled only after runtime capability checks and model benchmarks.
- 3 GB total RAM does not guarantee diffusion inference; memory available to the app, GPU support, model quantization and working resolution determine feasibility.

## Product promise
Aura Studio is a free Android AI image-to-image editor. Users select a photo, choose one or more of 275+ visual transformation styles, enter optional natural-language instructions, and tap Generate. The app must transform the user's actual photo. It is not a beauty-camera app or static filter gallery.

## Main user flow
1. Choose a photo from gallery or camera.
2. Select one or more visual styles from the searchable, categorized icon catalogue.
3. Optionally describe additional changes in natural language.
4. Tap Generate directly. There is no separate review button or mandatory review screen.
5. The AI pipeline internally interprets selected styles and custom description.
6. Run image-to-image inference on the selected photo.
7. Show truthful animated progress, then the actual generated result.
8. Allow before/after comparison, save and Android share.

## AI execution requirements
- Prefer fully free, on-device inference and offline operation.
- Benchmark small/quantized image-to-image models at low resolutions first; do not assume SDXL/large diffusion models run on 3 GB devices.
- Add device capability and available-memory checks before offering local generation.
- Do not bundle huge model weights in the base APK; offer separately downloadable model assets with size and licence disclosure.
- Optional cloud fallback must be genuinely free, opt-in and privacy-disclosed; never upload photos silently.
- No paid APIs, subscriptions, ads, or mandatory accounts.
- Never fake output or report success without a real model result.

## Current implementation status
- Android minSdk 26 (Android 8.0), targetSdk 36.
- Photo input, 275-style catalogue, search, filters, multi-selection, custom description and direct Generate UI: implemented.
- Actual image-to-image inference engine: NOT integrated.
- Real generation progress, output save and share: NOT complete.

## Balanced low-end inference profile
- Device baseline: Android 8.0/API 26, 3 GB RAM class, 64 GB storage class.
- Balanced means start with 256x256 image-to-image, measure memory and latency, then test 384x384; 512x512 is conditional on measured headroom.
- Use bounded image decoding and RGB_565 preprocessing to limit peak bitmap memory.
- The app includes device diagnostics (API level, total/available RAM, ABI, CPU cores and Android low-RAM classification) plus a photo preprocessing benchmark.
- The preprocessing test is not an AI generation benchmark. The real diffusion model/runtime remains unintegrated and must not be represented as complete.
- Do not enable inference on a 3 GB phone solely because it meets the marketing RAM minimum; require a measured safe available-memory threshold and recover gracefully from out-of-memory errors.
- Benchmark the CPU/GPU MNN route first on MediaTek/Mali hardware; Snapdragon QNN/NPU acceleration is device-specific and cannot be assumed.

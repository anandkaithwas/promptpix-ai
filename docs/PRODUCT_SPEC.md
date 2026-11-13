# Aura Studio — Product & Technical Specification

## Minimum supported device
- Android 14 (API 34): `minSdk = 34`.
- Target Android 16 (API 36) for current Play submission requirements.
- 64-bit ARM Android device.
- 3 GB physical RAM baseline; 4 GB+ recommended.
- 64 GB total device storage target; require sufficient free space before downloading models. Total marketed storage cannot be reliably queried on Android.
- Keep model files separate from the APK and allow deletion/re-download.

## Critical feasibility constraint
App compatibility and AI model compatibility are separate. A device with 3 GB RAM can run the app UI, catalogue, image selection and lightweight preprocessing, but this does not guarantee that a diffusion model can run locally. Published mobile Stable Diffusion benchmarks on high-end devices report roughly 2.1 GB for model weights plus intermediate tensors; that leaves insufficient headroom on many 3 GB phones. Benchmark candidate quantized/distilled image-to-image models on actual low-memory devices before selecting an engine. Never claim universal local inference at 3 GB.

## User flow
Photo -> choose one or more catalogue styles -> optional custom description -> Generate -> internal request interpretation -> actual image-to-image inference -> actual output -> save/share.

## UX requirements
- One primary action labelled Generate; no Review button or mandatory review screen.
- Style selections are visibly acknowledged and removable.
- Hidden style instructions; users see style names and visual examples/icons, not prompt text.
- The custom description and selected styles feed the same edit request.
- Honest generation status and error states; never show a fake result.

## Resource management for 3 GB RAM devices
- Decode source photos to a bounded working resolution before inference; never hold original full-resolution and multiple full-size intermediate bitmaps simultaneously.
- Use `ImageDecoder`/BitmapFactory sampling or equivalent bounds-aware decode.
- Prefer model quantization and low-memory runtime paths; unload model/session when app is backgrounded or memory pressure is reported.
- Use `ActivityManager.MemoryInfo` and `StatFs` to inspect RAM/free storage; avoid assuming advertised storage capacity can be detected.
- Make model downloads optional, resumable, integrity-checked and user-initiated; display model size and required free space.
- Start with 512x512 or lower proof-of-concept output only if the chosen model/runtime supports it; test thermal throttling and realistic latency.

## AI engine
- No paid API, subscription, ad, or mandatory account.
- Prefer on-device inference. A cloud fallback is allowed only if truly free, opt-in, transparent about photo upload/privacy and not essential to core functionality.
- Candidate model/runtime must be assessed for image-to-image conditioning (not text-to-image only), Android ABI, CPU/GPU/NPU support, RAM peak, disk footprint, latency, thermal behaviour, licence and redistribution terms.
- Model compatibility is determined per device; do not promise every model runs on the 3 GB baseline.

## Current status
- Gallery/camera, catalogue, search, category filters, multi-selection, custom description and direct Generate UI are present.
- Actual image-to-image inference is not connected.
- Model manager, device diagnostics, real progress, result saving and sharing remain future implementation work.

## Acceptance criteria
1. App installs on Android 14+ arm64 devices.
2. UI/catalogue/photo selection remains stable on 3 GB RAM devices.
3. Model download requires adequate free space and can be cancelled/resumed.
4. The selected source photo is passed to a genuine image-to-image model.
5. Generation either returns a real transformed image or a clear capability/error message.
6. No paid dependency or fake completion state.
7. Real generated image can be saved and shared.
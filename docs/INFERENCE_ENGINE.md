# Aura Studio inference engine prototype

## Selected runtime

The first runtime candidate is **Alibaba MNN Diffusion** (Apache-2.0), built for Android ARM64. Its upstream Android build script uses Android native API 21, so the native library's API floor is compatible with Aura Studio's API 26 application floor. This must still be verified on an API 26 device.

Upstream references:
- https://github.com/alibaba/MNN
- https://github.com/alibaba/MNN/blob/master/transformers/diffusion/README.md
- https://github.com/alibaba/MNN/wiki/diffusion

## Balanced profile

MNN documents diffusion memory modes as 0=low-memory, 1=speed-first, and 2=balanced. Aura Studio will start with mode 2, but will benchmark mode 0 as a fallback on devices with low available memory.

Initial test matrix:
- Input photo: 288x512 preprocessing output from the OPPO F27 Pro+ benchmark.
- Model: Stable Diffusion 1.5 converted to MNN format, subject to checkpoint licence review.
- Resolution: start at 256x256; then test 384x384. 512x512 only after stable peak-memory measurements.
- Backends: CPU first; OpenCL GPU second where supported; automatic CPU fallback.
- Metrics: cold model load time, warm-up time, generation latency, peak app memory, output dimensions, crash/OOM rate, thermal throttling and battery drain.

## Android compatibility

- App minimum: Android 8.0 / API 26.
- Native ABI target: arm64-v8a initially.
- MNN upstream Android native build script sets Android native API level 21.
- API 26 compatibility must be validated on a real Android 8 device before release.

## Model delivery and licences

Model weights are not bundled in the APK or native-runtime CI artifact. They will be downloaded on demand after the user sees the model size, licence, storage requirement and Wi-Fi/mobile-data choice. A model is not "free for any use" merely because its runtime is open source; each checkpoint's licence and usage restrictions must be checked independently.

## Important capability gap

MNN's documented Stable Diffusion demo currently documents text-to-image invocation. Aura Studio requires image-to-image. The first runtime build is therefore an engine packaging/ABI milestone, not proof that Generate works. Before connecting the UI, implement or identify a supported SD1.5 img2img pipeline (including VAE encode, latent noise injection/denoising strength, UNet denoising and VAE decode), then add a native smoke test using an actual input photo.

Sana's documented MNN img2img path is not selected for the 3 GB baseline because its model stack includes a language model and transformer components that need separate memory feasibility testing.

## Acceptance gates

1. Native MNN libraries build and load on arm64.
2. Runtime can initialize on API 26+.
3. Actual image-to-image produces a changed image from a supplied source photo.
4. Identity/content preservation and style adherence are reviewed.
5. 3 GB-class device benchmark records peak memory and generation time.
6. If the device cannot safely run inference, the app explains why and offers no fake result.

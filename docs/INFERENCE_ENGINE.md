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

## 16B decision: MNN Sana img2img

The MNN Diffusion README documents a genuine Sana img2img mode. Its documented resource tree requires:
- `llm/` containing the Qwen3-0.6B model files (including embeddings/blocks/lm assets)
- `connector.mnn`
- `projector.mnn`
- `transformer.mnn`
- `vae_decoder.mnn`
- `vae_encoder.mnn` (required for img2img)

The documented demo invocation accepts `img2img`, a prompt, an input image path, output path, width/height (multiples of 32), steps, seed and CFG settings. The app now has `SanaModelPackValidator`, which fails closed when the documented required files or non-empty LLM directory are missing. This checks directory completeness only; it does not validate model provenance, tensor compatibility, or successful inference.

Upstream reference: https://github.com/alibaba/MNN/blob/master/transformers/diffusion/README.md

### Remaining integration work (not yet complete)

The current Aura Studio MNN workflow builds native MNN libraries but does not yet build and package an Aura-specific JNI bridge that exposes SanaSession to Kotlin. The documented upstream Android app contains a Sana JNI/session implementation that can guide this integration, but it must be adapted, built against the exact MNN revision, and packaged as an optional arm64 runtime module. Model weights are not yet selected or downloadable; do not enable 16B in the UI until the model licence, artifact URLs, exact sizes, SHA-256 hashes and device memory requirements are verified.

Sana's Qwen3-0.6B + DiT stack may exceed the practical memory budget of a 3 GB-class device. It is an experimental optional engine, not a default OPPO F27 Pro+ capability. Start at 512x512 only after a measured memory preflight; if the device cannot sustain it, report unsupported rather than falling back to fake output.

### 16A / 16B separation

16A remains the lightweight style-transfer path. 16B is the prompt-conditioned Sana img2img path. They must have separate model packs, runtime checks and measured capability profiles.

## Acceptance gates

1. Native MNN libraries build and load on arm64.
2. Runtime can initialize on API 26+.
3. Actual image-to-image produces a changed image from a supplied source photo.
4. Identity/content preservation and style adherence are reviewed.
5. 3 GB-class device benchmark records peak memory and generation time.
6. If the device cannot safely run inference, the app explains why and offers no fake result.

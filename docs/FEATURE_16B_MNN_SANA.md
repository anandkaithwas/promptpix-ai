# Feature 16B — MNN Sana image-to-image integration

## Decision

Use Alibaba MNN Diffusion's Sana path as the first 16B candidate. Do not use the Stable Diffusion 1.5 `Diffusion::run(prompt, imagePath, ...)` overload for 16B: the current upstream implementation creates random latent noise and writes a text-to-image result. The current MNN Stable Diffusion implementation explicitly reports that its unified embedding API does not support img2img.

Sana exposes a real image-conditioned entry point: `Diffusion::run(input_embeds, "img2img", inputImagePath, outputImagePath, width, height, steps, seed, use_cfg, cfg_scale, callback)`. The upstream Android sample uses a `SanaSession` JNI wrapper and passes an image path to this entry point.

## Runtime and model requirements

The Sana model directory needs the diffusion graph components:
- `connector.mnn`
- `projector.mnn`
- `transformer.mnn`
- `vae_decoder.mnn`
- `vae_encoder.mnn` (required for img2img)

It also needs a complete `llm/` prompt-conditioning model package. Upstream Sana Android code constructs `SanaLlm` from that directory before it runs diffusion. The exact set of LLM files and tokenizer assets must come from the selected upstream model export/package manifest; do not assume that an arbitrary non-empty file is sufficient.

## Implemented in Aura Studio

`SanaModelPackageValidator` performs a fail-closed structural readiness check for the core Sana files, image encoder and LLM directory. Unit tests cover a missing package, a missing image encoder, and the distinction between structural presence and validated weights.

This is intentionally only a structural gate. A directory containing arbitrary non-empty files must not be treated as a valid model. Before enabling inference, the installer must validate a signed/pinned manifest, exact file sizes and SHA-256 hashes, model licence acceptance, free storage and successful native model load.

## Remaining work before 16B can be marked operational

1. Select a specific Sana checkpoint and verify its model licence and redistribution/download terms.
2. Obtain the exact upstream export manifest, file names, sizes and SHA-256 hashes. No download URL is enabled until these are pinned.
3. Build MNN Diffusion plus an Aura JNI bridge for `SanaSession`. Package all required native shared libraries for `arm64-v8a` and handle 16 KB page-size requirements.
4. Add a Kotlin runtime adapter implementing `LocalImageGenerationEngine`, passing the source photo path, output path, prompt, seed, dimensions and progress callbacks to JNI.
5. Serialize inference, run on `Dispatchers.IO`, support cancellation safely, release native sessions on every exit path, and map output back to a bitmap.
6. Add fixed-input dry-run and real img2img tests on the target phone. Benchmark memory, latency, thermal behaviour and output integrity.
7. Gate the feature by actual model load and benchmark evidence. If the device cannot run the selected package safely, keep 16B unavailable rather than silently switching to cloud.

## Device caveat

Sana is not a small four-file style-transfer model. It has a prompt-conditioning LLM as well as diffusion and VAE components. No performance or compatibility claim is made for the OPPO F27 Pro+ until the exact model package is loaded and tested on that device.

## References

- MNN diffusion guide: https://github.com/alibaba/MNN/blob/master/transformers/diffusion/README.md
- MNN diffusion API: https://github.com/alibaba/MNN/blob/master/transformers/diffusion/engine/include/diffusion/diffusion.hpp
- Upstream Android Sana JNI/session example: https://github.com/alibaba/MNN/tree/master/apps/Android/MnnLlmChat/app/src/main/cpp
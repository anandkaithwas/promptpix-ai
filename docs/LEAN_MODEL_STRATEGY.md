# Lean offline model strategy

## Product decision

Aura Studio uses a small, on-demand model-pack strategy rather than bundling a large generative model in the APK.

- No model weights are bundled in the base APK.
- No model downloads happen at first launch or in the background.
- A user chooses one style pack at a time and sees its exact size and licence before accepting.
- Model downloads are restricted to unmetered networks (normally Wi-Fi); mobile-data downloads are rejected.
- Models are stored in app-private storage, not the public Downloads folder.
- A model can be removed from the app to reclaim storage.
- Installation requires a pinned SHA-256 checksum and exact expected byte count.
- The original photo is processed locally; there is no cloud fallback.

## First candidate: Fast Neural Style Transfer

The LiteRT community Fast Neural Style models are a plausible first pack: four FP16 style graphs (Candy, Mosaic, Rain Princess and Udnie), approximately 3.5 MB each, with a BSD-3-Clause upstream licence. The published ~9 ms figure is from a Pixel 8a at 256×256 and is not a performance promise for the OPPO F27 Pro+.

Reference: https://huggingface.co/litert-community/Fast-Neural-Style-LiteRT

The candidate repository README documents NCHW RGB float input at 256×256 with values in the 0–255 range. Android integration must verify this contract against exact pinned model files before enabling the pack.

## Why model specs are not yet shipped as active downloads

The downloader intentionally refuses a model unless its manifest includes:
- HTTPS download URL
- exact expected file size
- 64-character SHA-256 checksum
- licence name and HTTPS licence URL

The candidate model's size and licence are discoverable, but a verified immutable file checksum and a device-tested runtime artifact have not yet been pinned in this branch. Do not replace the checksum requirement with an unverified value.

## Implementation

LeanModelPackStore.kt provides explicit caller-triggered download only, unmetered-network enforcement, app-private storage, bounded download size, exact byte-count validation, SHA-256 verification before atomic installation, partial-file cleanup, installed-model validation and removal. It reserves 16 MB beyond the model size.

LeanModelSpecTest.kt checks manifest validation. Android CI runs unit tests before assembling the debug APK.

## Next acceptance gates

1. Pin model artifact URLs, sizes, licences and SHA-256 values from immutable source revisions.
2. Add the LiteRT runtime dependency and an adapter that preprocesses to the model's exact tensor contract.
3. Run CPU and GPU paths on the OPPO F27 Pro+; compare output quality and correctness.
4. Enable GPU only if output is correct and performance is better; retain CPU fallback.
5. Test repeated inference, memory pressure, thermal behaviour and uninstall/reinstall.
6. Only then surface the models as installable in the production UI.

## Storage target

Initial candidate pack: one selected style model, roughly 3.5 MB plus small app-private metadata. Installing all four is not automatic. The user can keep one style installed and remove it before installing another.

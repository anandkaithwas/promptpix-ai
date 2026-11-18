# Aura Studio Features 24–33

## 24 Save Generated Image
- Save action writes the genuine returned Bitmap to Pictures/Aura Studio using MediaStore.
- Android 10+ uses scoped storage and pending-write finalization. Android 8–9 requests legacy WRITE_EXTERNAL_STORAGE permission at the point of use.
- Failed writes remove the incomplete MediaStore entry.

## 25 Share Generated Image
- Share action uses Android Sharesheet with a content URI and temporary read grant.
- Sharing is enabled only after a generated output has been saved to a shareable URI.

## 26 Result Preview
- The UI renders the actual Bitmap returned by the generation coordinator, with a fit-scaled preview.
- No placeholder or sample image is presented as a generated result.

## 27 Original Preservation
- Generation consumes the source URI through a read-only decode path. Save writes a separate output item; no code path overwrites the source.
- Before/after comparison decodes the original separately.

## 28–31 Device Compatibility and Performance
- minSdk remains 26.
- Runtime device profile reports Android API, total/available RAM, low-RAM status, CPU cores and supported ABIs.
- Inference image decode is bounded to a requested maximum edge.
- Hardware plan chooses conservative input dimensions and does not assert GPU support without runtime validation.

## 32 Model Download
- Existing LeanModelPackStore enforces explicit invocation, HTTPS, pinned byte size, SHA-256, licence metadata, unmetered Wi-Fi, private app storage and removal.
- Added UI disclosure; download remains disabled because no verified model manifest/tensor contract is configured. No placeholder model is offered.

## 33 Inference Benchmarking
- Added V1InferenceBenchmarkRunner, which runs the real inference engine, measures elapsed time and samples Java/native heap usage during execution.
- The reported memory is sampled process heap usage, not total device peak memory; this limitation is explicit in code.
- Results are tied to device fingerprint and passed through V1MeasuredModelSelector, which requires installed model IDs and successful measurements.
- Benchmarking cannot produce results while the local engine is unconfigured.

## Verification limits
- Unit tests cover feature registry states and measured model gating.
- GitHub Actions must complete before build success can be claimed.
- OPPO F27 Pro+ 5G physical-device tests remain required, including permission flow, saving/sharing, real inference latency, memory and thermal behavior.

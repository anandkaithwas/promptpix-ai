# On-device inference feasibility

Target: OPPO F27 Pro+ 5G, MediaTek Dimensity 7050 and Mali GPU. Verify exact variant and available RAM on the handset.

Do not target Qualcomm QNN/NPU acceleration: this is a MediaTek device. Evaluate CPU/GPU inference first.

Candidate: https://github.com/xororz/local-dream . Upstream describes SD 1.5 CPU/GPU inference for many recent Android devices; NPU acceleration is Snapdragon-specific. Treat it as an evaluation candidate, not a drop-in SDK.

Benchmark 256, 384 and 512 square img2img. Record model load time, generation latency, peak memory, temperature, battery drain, crash rate, portrait identity preservation and prompt adherence. Repeat after sustained use to detect thermal throttling.

Never fake progress. Use real engine callbacks; otherwise show an indeterminate animation and truthful stage labels. Preserve the source photo and save outputs separately. Verify each model and dependency licence before distribution.
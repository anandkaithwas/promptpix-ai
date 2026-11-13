# PromptPix AI

Free, offline-first Android photo editor. Initial MVP scaffold and device feasibility track.

## Scope
- Android Kotlin + Jetpack Compose
- Gallery and camera input
- Categorised prompt catalogue (starter set, not final 1,000)
- Local prompt compilation
- Separate image export
- No paid APIs, ads or subscriptions

**AI image-to-image inference is not integrated yet.** The app must not fake an edited preview or fake progress. The first milestone is to validate a real local SD 1.5 img2img engine on the target handset.

## Device target
OPPO F27 Pro+ 5G / MediaTek Dimensity 7050 / Mali GPU. Qualcomm QNN/NPU acceleration is not applicable. Evaluate CPU/GPU inference and benchmark latency, memory, heat and battery on the actual phone.

Candidate for evaluation: https://github.com/xororz/local-dream . Its upstream documentation describes SD 1.5 CPU/GPU inference and Snapdragon-specific NPU acceleration. It is not embedded in this app yet.

Open in Android Studio with JDK 17 and Android SDK 35. GitHub Actions builds a debug APK.

Model weights have separate licences; verify redistribution and commercial-use terms individually before bundling.
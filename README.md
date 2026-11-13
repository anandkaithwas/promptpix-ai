# Promptora

An offline-first Android creative photo-effects app. Browse visual effect styles, choose a photo from gallery or camera, and save AI-transformed results when the local inference engine is integrated.

## Current prototype
- Colorful Promptora branding and launcher icon
- Gallery and camera input
- Visual effect cards with categories and sample-style swatches
- Prompt text hidden from the user-facing interface
- Honest status: current effect tiles are illustrative samples; real AI transformation and export are not yet integrated

## Build
Open in Android Studio with JDK 17 and Android SDK 35. GitHub Actions builds a debug APK.

## Privacy and cost
The intended architecture is local/offline inference with no paid API, account or subscription. Model download/storage and redistribution licences must be handled separately.
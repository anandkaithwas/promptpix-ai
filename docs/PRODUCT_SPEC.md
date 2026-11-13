# Aura Studio — Product Specification

## Product promise
Aura Studio is a free Android AI image-to-image editor. Users select a photo, choose one or more of 275+ visual transformation styles, enter optional natural-language instructions, and tap **Generate**. The app must transform the user's actual photo. It is not a beauty-camera app or static filter gallery.

## Main user flow
1. Choose a photo from gallery or camera.
2. Select one or more visual styles from the searchable, categorized icon catalogue.
3. Optionally describe additional changes in natural language.
4. Tap **Generate** directly. There is no separate review button or mandatory review screen.
5. The AI pipeline internally interprets the selected styles and custom description.
6. Run image-to-image inference on the selected photo.
7. Show truthful animated progress, then the actual generated result.
8. Allow before/after comparison, save and Android share.

## Catalogue
- Maintain at least 250 curated styles in `app/src/main/assets/catalogue/styles.json`.
- Each style has a unique id, category, visible name, semantic icon and hidden editing instruction.
- Users can search, filter, multi-select and remove selected styles.
- Selecting a style adds it to the pending edit; it does not claim to have transformed the image.
- Do not expose raw prompt strings.

## Custom instructions
- Keep a free-text description box available.
- Custom text and selected catalogue styles are combined into one request.
- The interpreter resolves compatible instructions and detects conflicts internally; do not force a user confirmation step.

## Generate action
- There must be one primary action labelled **Generate**, not Review, Review my edit, or Confirm & generate.
- It is enabled only when a source photo and at least one style or non-empty custom instruction exist.
- Tapping Generate should start actual image-to-image inference once the engine is integrated.
- Until a real model is integrated, the app must clearly say generation is unavailable; never fake an edited image or show a success state.
- No secondary review button or mandatory review screen.

## AI image engine
Pipeline: source photo + selected style instructions + optional custom description -> instruction interpretation -> image-to-image model conditioned on the actual source photo -> generated image.
- Evaluate fully free/open-weight models and Android-compatible runtimes.
- Prioritize on-device inference and offline use.
- Validate model licence, RAM, storage, latency, thermal and battery use on target devices.
- No paid API, subscription, advertisements, mandatory account or hidden cloud dependency.
- Report real inference progress/errors only.
- Preserve original image and save generated output separately.

## Current implementation status
- Gallery/camera photo input: implemented.
- 275-style catalogue data: implemented.
- Search/category filtering/multi-selection: implemented.
- Custom description field: implemented.
- Direct Generate button UI: implemented; currently reports that the AI engine is not connected.
- Actual image-to-image model: NOT integrated.
- Real generation, progress, result save/share: NOT complete.

## Acceptance criteria
1. No Review button or review screen.
2. One primary Generate action.
3. Style selection is visibly acknowledged.
4. Generate combines selected styles and custom description.
5. Generate runs a real image-to-image model on the selected photo.
6. No fake output or false completion message.
7. Actual output can be saved and shared.
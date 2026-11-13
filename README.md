# Aura Studio

Offline-first Android creative photo editor. Users select a photo, choose a visual effect or describe an edit in natural language, review the interpreted Edit Plan, then explicitly confirm before generation.

## Implemented prototype flow
- Gallery and camera selection
- Visual effect catalogue
- Natural-language editing field
- Edit Plan review screen with main change, lighting, style, background and preservation sections
- Confirm & generate, Edit request and Cancel actions

## Important implementation status
The current Edit Plan interpreter is a simple local keyword-based prototype, not a trained language model. Confirming the plan currently records approval but does not generate an image. The image-to-image engine, genuine AI instruction understanding, real progress reporting, generated-image save and functional sharing remain to be integrated. Do not represent the prototype interpretation as full AI understanding or a generated result.

## Intended pipeline
Photo + natural-language request -> local instruction interpreter -> readable Edit Plan -> explicit user confirmation -> local image-to-image model -> generated output -> save/share.

No paid API, ads or subscription are planned. Model performance and licences must be verified before bundling.
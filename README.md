# Aura Studio

Offline-first Android creative photo editor. Browse visual styles or describe an edit in everyday language.

Current prototype includes gallery/camera selection, visual effect cards, a natural-language edit description field and a share-action scaffold. The language interpreter and image-to-image AI engine are not yet integrated. Sharing is intentionally disabled until a real generated output URI exists.

Planned local pipeline: selected photo + natural-language instruction -> on-device instruction interpreter -> structured edit parameters -> local image-to-image model -> saved output -> Android Sharesheet.

No paid API, ads or subscription are planned. Performance and model licensing must be validated before distributing model weights.
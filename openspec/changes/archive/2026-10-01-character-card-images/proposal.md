**Status:** accepted, locally validated and archived.

## Why

Character browsing needs a reviewed card and independent portrait states before catalogue requests and screen state are introduced. Establish the selected dark visual language and a single image loader without coupling reusable visual foundations to character data.

## What Changes

- Add the dark Material 3 theme and the spacing, shape and typography tokens used by the card.
- Add the Home-owned character card, status treatment and noninteractive loading skeleton.
- Configure a shared Coil loader at the app boundary with bounded library-managed caches and constraint-sized image requests.
- Provide standard Compose previews; the production shell remains a shell until C04.
- Verify builds/static checks in C03; exercise card selection and image states through the real Home screen in C04. Visual regression checks remain later work.

## Capabilities

### New Capabilities

- `character-card-images`: usable character cards with independent portrait loading and safe data placeholders.

### Modified Capabilities

None.

## Impact

`:core:designsystem`, `:feature:home`, `:app`, dependency catalogue and development instructions. No catalogue API client, ViewModel, navigation, repository or custom image cache is introduced.

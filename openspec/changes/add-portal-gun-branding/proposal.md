**Status:** Implemented, locally validated and accepted by the user on 2026-10-01 with authorization to commit and push; not yet archived. This independent visual adjustment does not accept or advance C08/C09.

## Why

The launcher and native launch screen currently use the generic Android icon. The user selected the generated blue portal gun with green accents.

## What Changes

- Install that artwork as the adaptive launcher icon, with sufficient room for launcher masks.
- Show the same identity on the native launch screen over the existing charcoal palette.
- Preserve native startup timing and existing navigation; add no destination or dependency.

## Capabilities

### New Capabilities

- `app-branding`: launcher identity and native startup appearance.

### Modified Capabilities

None.

## Impact

App manifest, drawable/mipmap resources and versioned platform themes. No Kotlin or product behavior changes.

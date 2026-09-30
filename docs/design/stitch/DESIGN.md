---
name: Rick And Morty Characters Native Android System
colors:
  surface: '#121316'
  surface-dim: '#121316'
  surface-bright: '#38393c'
  surface-container-lowest: '#0d0e11'
  surface-container-low: '#1b1b1f'
  surface-container: '#1f1f23'
  surface-container-high: '#292a2d'
  surface-container-highest: '#343538'
  on-surface: '#e3e2e6'
  on-surface-variant: '#c2c7ce'
  inverse-surface: '#e3e2e6'
  inverse-on-surface: '#2f3034'
  outline: '#8c9198'
  outline-variant: '#42474d'
  surface-tint: '#a6caf0'
  primary: '#a6caf0'
  on-primary: '#043352'
  primary-container: '#7a9ec2'
  on-primary-container: '#083554'
  inverse-primary: '#3d6183'
  secondary: '#b9c8de'
  on-secondary: '#233143'
  secondary-container: '#39485a'
  on-secondary-container: '#a7b6cc'
  tertiary: '#4de082'
  on-tertiary: '#003919'
  tertiary-container: '#00b25b'
  on-tertiary-container: '#003b1a'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#cee5ff'
  primary-fixed-dim: '#a6caf0'
  on-primary-fixed: '#001d33'
  on-primary-fixed-variant: '#234a69'
  secondary-fixed: '#d4e4fa'
  secondary-fixed-dim: '#b9c8de'
  on-secondary-fixed: '#0d1c2d'
  on-secondary-fixed-variant: '#39485a'
  tertiary-fixed: '#6dfe9c'
  tertiary-fixed-dim: '#4de082'
  on-tertiary-fixed: '#00210c'
  on-tertiary-fixed-variant: '#005227'
  background: '#121316'
  on-background: '#e3e2e6'
  surface-variant: '#343538'
typography:
  display-lg:
    fontFamily: Roboto Flex
    fontSize: 3.5rem
    fontWeight: '400'
    lineHeight: 4rem
    letterSpacing: -0.015em
  headline-lg:
    fontFamily: Roboto Flex
    fontSize: 2rem
    fontWeight: '600'
    lineHeight: 2.5rem
    letterSpacing: 0em
  headline-md:
    fontFamily: Roboto Flex
    fontSize: 1.75rem
    fontWeight: '600'
    lineHeight: 2.25rem
    letterSpacing: 0em
  headline-sm:
    fontFamily: Roboto Flex
    fontSize: 1.5rem
    fontWeight: '600'
    lineHeight: 2rem
    letterSpacing: 0em
  title-lg:
    fontFamily: Roboto Flex
    fontSize: 1.375rem
    fontWeight: '600'
    lineHeight: 1.75rem
    letterSpacing: 0em
  title-md:
    fontFamily: Roboto Flex
    fontSize: 1rem
    fontWeight: '500'
    lineHeight: 1.5rem
    letterSpacing: 0.01em
  title-sm:
    fontFamily: Roboto Flex
    fontSize: 0.875rem
    fontWeight: '500'
    lineHeight: 1.25rem
    letterSpacing: 0.01em
  body-lg:
    fontFamily: Roboto Flex
    fontSize: 1rem
    fontWeight: '400'
    lineHeight: 1.5rem
    letterSpacing: 0.02em
  body-md:
    fontFamily: Roboto Flex
    fontSize: 0.875rem
    fontWeight: '400'
    lineHeight: 1.25rem
    letterSpacing: 0.015em
  body-sm:
    fontFamily: Roboto Flex
    fontSize: 0.75rem
    fontWeight: '400'
    lineHeight: 1rem
    letterSpacing: 0.02em
  label-lg:
    fontFamily: Roboto Flex
    fontSize: 0.875rem
    fontWeight: '500'
    lineHeight: 1.25rem
    letterSpacing: 0.01em
  label-md:
    fontFamily: Roboto Flex
    fontSize: 0.75rem
    fontWeight: '500'
    lineHeight: 1rem
    letterSpacing: 0.03em
  label-sm:
    fontFamily: Roboto Flex
    fontSize: 0.6875rem
    fontWeight: '600'
    lineHeight: 0.875rem
    letterSpacing: 0.04em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1rem
  margin: 1rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2rem
---

## Brand & Style

This design system establishes a high-performance, polished native Android experience built with Jetpack Compose. Tailored for deep media exploration, it couples Material 3 foundational architecture with a restrained, dark visual style. Rather than leaning on over-saturated sci-fi tropes or neon glows, the interface acts as a cinematic viewfinder: quiet, deep obsidian surfaces step back to let character assets, status signatures, and episodic data command attention.

The personality balances utilitarian Android conventions with refined catalog elegance. The tactile feel is snappy, predictable, and distinctly native, relying on structural tonal elevation, balanced typography, and deliberate feedback states. It provides long-session comfort through dark ambient surfaces, high-contrast legibility, and zero visual clutter.

## Colors

The palette is engineered as an OLED-optimized, accessible Material 3 tonal hierarchy. Light reflection is simulated not through physical dropshadows, but via calibrated steps of charcoal-slate luminance.

- **Background & Base Surfaces**: Root canvas rests at `#121316`. The surface stack climbs progressively: `Surface Container Low` (`#1A1C20`), `Surface Container` (`#22252A`), `Surface Container High` (`#2C3037`), and `Surface Container Highest` (`#333842`).
- **Primary Accent**: Muted slate-blue (`#7A9EC2`) ensures functional focus without causing chromatic fatigue. Paired with `On-Primary` (`#0E1B26`), `Primary Container` (`#253545`), and `On-Primary Container` (`#CDE5FE`).
- **Semantic Status Signals**:
  - **Alive**: `#4ADE80` (Accent), `#143823` (Container), `#86EFAC` (Text & Glyph).
  - **Dead**: `#F87171` (Accent), `#3E181A` (Container), `#FCA5A5` (Text & Glyph).
  - **Unknown**: `#94A3B8` (Accent), `#272F38` (Container), `#CBD5E1` (Text & Glyph).
- **Text & Outlines**: Primary copy uses `#F0F2F5` (90%+ luminance contrast), secondary metadata uses `#9CA3AF`, and structural separation uses `#363B44`.

## Typography

Typography relies entirely on the mechanical balance of `Roboto Flex`. Its variable weight adjustments allow optical stability across dense lists and large media heroes.

- **Display & Headline Roles**: Reserved for character names and primary screen titles. Clean weights (`600` SemiBold) guarantee legibility against dark imagery without bleeding.
- **Title Roles**: Used within cards, app bars, and modal sheets. Tight letter-spacing keeps multi-line titles clean.
- **Body Roles**: Standard reading rhythm for episode summaries, dimension descriptions, and character origins. Set with open line heights to prevent visual fatigue on OLED screens.
- **Label Roles**: Purpose-built for badges, status indicators, and Compose chips. Enhanced tracking (`letter-spacing`) ensures full legibility at compact physical sizes.

## Layout & Spacing

Layout geometry follows an 8dp baseline grid with a 4dp micro-step for compact metadata rows. Screen composition maps cleanly to standard Android window size classes:

- **Compact (<600dp)**: Single column or 2-column adaptive `LazyVerticalGrid`. Outer margin is set to `16dp` (`1rem`) with a `16dp` gutter between character cards.
- **Medium (600dp - 839dp)**: 3-column to 4-column dynamic grid. Outer margins scale to `24dp` (`1.5rem`), transitioning top-level navigation to a standard `NavigationRail`.
- **Expanded (840dp+)**: Multi-pane split screen (List on left, character detail canvas on right). Max-width limits on content prevent line lengths from exceeding accessible thresholds.

Inner component padding preserves consistent density: `space-xs` (4dp) for icon-to-label gaps, `space-sm` (8dp) for badge and chip internal padding, and `space-md` (16dp) for card content margins.

## Elevation & Depth

Depth in this system is communicated via pure tonal surface layering rather than diffuse blur shadows. In dark themes, drop shadows are virtually invisible against deep backgrounds; tonal layering provides predictable optical separation without dirtying the palette.

- **Level 0 (Canvas)**: Background `#121316` for root scaffolds and list viewports.
- **Level 1 (Structural Containers)**: Surface Container Low `#1A1C20` for persistent bottom bars and app header backgrounds.
- **Level 2 (Raised Content)**: Surface Container `#22252A` for character cards, dialogs, and menu popups.
- **Level 3 (Interactive Floating Elements)**: Surface Container High `#2C3037` for active filters and state sheets.
- **Level 4 (Embedded Active Input)**: Surface Container Highest `#333842` for search fields and focused text inputs.

Subtle hairline borders using `#363B44` at 1dp width provide razor-sharp demarcation where cards sit directly adjacent to full-bleed character imagery.

## Shapes

The design system implements balanced geometric rounding calibrated for thumb reach and modern Android device corners. 

- **Cards & Sheets**: Set to `16dp` (`1rem` / `rounded-lg`). Matches modern hardware bezel curvatures and softens character thumbnails.
- **Chips & Status Tags**: Set to `8dp` (`0.5rem` / standard radius). Retains a structural, compact pill form that doesn't waste horizontal space.
- **Search Bar & Inputs**: Set to a `28dp` full-stadium pill (`rounded-xl` context), creating a persistent, accessible anchor for character queries at the top of list viewports.
- **Micro-Indicators**: Circular radii for status dots and avatar placeholders.

## Components

### Character Cards
- **Container**: `Surface Container` (`#22252A`), 16dp rounded corner radius, optional 1dp border (`#363B44`).
- **Image**: Aspect ratio 1:1 or 4:3 with subtle bottom gradient scrim (`#22252A` at 0% to 100% alpha) to ground name typography.
- **Content**: Name styled in `title-lg` (`#F0F2F5`), secondary species/gender metadata in `body-sm` (`#9CA3AF`).
- **Interaction**: Material 3 ripple using slate-blue tint at 12% alpha.

### Status Chips & Badges
- **Structure**: 8dp corner radius, horizontal padding of 8dp, vertical padding of 4dp. Contains a 6dp vector dot indicator followed by `label-sm` text.
- **Alive**: `#143823` background, `#86EFAC` text, dot filled with `#4ADE80`.
- **Dead**: `#3E181A` background, `#FCA5A5` text, dot filled with `#F87171`.
- **Unknown**: `#272F38` background, `#CBD5E1` text, dot filled with `#94A3B8`.

### Search Input Field
- **Surface**: `Surface Container Highest` (`#333842`), 56dp height, 28dp pill shape.
- **Leading Icon**: Search glyph in `#9CA3AF`.
- **Trailing Action**: Voice/filter glyph or clear button.
- **Text**: Placeholder in `body-md` (`#9CA3AF`), active text in `#F0F2F5`. Zero baseline indicator line; rely strictly on surface containment.

### Buttons & Interactive Controls
- **Primary Filled Button**: `#7A9EC2` background, `#0E1B26` label (`label-lg`), 20dp corner radius.
- **Tonal / Secondary Button**: `#253545` container with `#CDE5FE` label.
- **Filter Chips**: Inactive states sit on `#22252A` with `#9CA3AF` label. Selected states transition to `#2C3037` with `#CDE5FE` text and a primary-colored check icon.
- **Selection Controls**: Checkboxes and radio targets use `#7A9EC2` active fills with `#0E1B26` icons; unselected outlines render in `#363B44`.

### Lists & Navigation
- **Navigation Bar**: Surface Container Low (`#1A1C20`) with 80dp standard height. Active indicators use an elongated pill in `#253545` with `#CDE5FE` icon tints.
- **List Dividers**: Avoid full-width divider rules; favor structural vertical margins (12dp) or 1dp inset lines using `#363B44`.
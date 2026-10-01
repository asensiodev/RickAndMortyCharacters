# UI/UX Definition

Status: the selected Stitch screenshots and HTML have been reviewed. The visual references are sufficient to begin implementation, with the bounded corrections and stale-loading-screenshot note below. No further Stitch generation is planned. Native behavior, contrast and touch-target validation remain pending.

[PRD](PRD.md) owns scope and priorities. This document owns screen content, components, interactions and visual references. [DEVELOPMENT](DEVELOPMENT.md) owns the process. OpenSpec implementation changes will reference the relevant sections.

## Design sequence

1. Use the selected content and state references in `docs/design/stitch/`.
2. Apply the handoff decisions below when creating the shared theme and components.
3. Implement the documented states in small reviewed changes; no further Stitch prompts are required.
4. Verify home → detail → back, loading, errors, scrolling and motion in previews and on a device as the corresponding features are built.

Designing the pair together keeps its visual language consistent; implementation still proceeds through small, independently reviewed changes. Choose final tokens in screen context rather than building a speculative component library first.

## Selected visual reference

The reviewed export is stored in [design/stitch](design/stitch/). It contains one content screen for each destination and the loading/error/empty references below. Their HTML files are beside the PNGs. Use the rendered dark surfaces, soft blue accents, typography, square portraits and rounded cards as the visual direction.

| Reference | Screenshot and source |
|---|---|
| Home content | [Screenshot](design/stitch/home/content/screen.png), [HTML](design/stitch/home/content/code.html) |
| Home loading | [Screenshot](design/stitch/home/loading/screen.png), [HTML](design/stitch/home/loading/code.html) |
| Home API error | [Screenshot](design/stitch/home/error/screen.png), [HTML](design/stitch/home/error/code.html) |
| Home no matches | [Screenshot](design/stitch/home/no-matches/screen.png), [HTML](design/stitch/home/no-matches/code.html) |
| Home empty catalogue | [Screenshot](design/stitch/home/empty/screen.png), [HTML](design/stitch/home/empty/code.html) |
| Detail content | [Screenshot](design/stitch/details/content/screen.png), [HTML](design/stitch/details/content/code.html) |
| Detail loading | [Screenshot](design/stitch/details/loading/screen.png), [HTML](design/stitch/details/loading/code.html) |
| Detail API error | [Screenshot](design/stitch/details/error/screen.png), [HTML](design/stitch/details/error/code.html) |

The [exported design system](design/stitch/DESIGN.md) and HTML are generated reference material. Their prose and tokens disagree in places, and generated suggestions do not expand the PRD. Reconcile actual screen colors into the native Material 3 theme; web dimensions are not verified dp/sp values. The PNGs remain visual evidence if external HTML assets change or become unavailable.

| Rendered/exported role | Candidate HEX; native mapping and contrast pending |
|---|---|
| Background | `#121316` |
| Card surface | `#1F1F23` |
| Search/raised surface | `#343538` |
| Primary accent / container | `#A6CAF0` / `#7A9EC2` |
| Text on primary / container | `#043352` / `#083554` |
| Main / secondary text | `#E3E2E6` / `#C2C7CE` |

The Toxic Rick detail sample was checked against [the character API](https://rickandmortyapi.com/api/character/361/): ID 361, Dead, Humanoid, Male, Type “Rick's toxic side”, Origin “Detoxifier”, location “Earth (Replacement Dimension)” and one episode reference. Show only its labelled count, without fetching episode titles. Sample catalogue totals are illustrative; implementation uses `info.count` for the active query.

### Implementation handoff

The references are sufficient to begin implementation. Apply these bounded adjustments in Compose:

- The Home-loading PNG is an outdated snapshot and still shows a counter/tune icon; its HTML removes both. Keep its skeleton geometry, reuse the content search/chips and follow the documented counter visibility contract. The corrected HTML has been inspected, not rendered as a replacement screenshot.
- Detail loading adds an unsupported Share glyph. Omit it; Back remains the only screen-level action. Preserve the content layout's Back position and portrait bounds across states.
- The selected Alive chip has dark text on a dark surface in no matches. Reuse one selected treatment across every state and verify native contrast. Keep suggested-name pills readable and give them the required interactive bounds.

Use a consistent outlined error icon and Retry treatment across Home and Detail. Keep the reviewed fixed name suggestions in no matches. The floating counter intentionally overlays the grid; end padding must allow final cards and pagination Retry to scroll clear of it. Static exports do not verify keyboard behavior, larger text, parallax or motion. Check those behaviors during the corresponding implementation changes. No further Stitch generation is queued.

## Data available to the UI

| Data | Planned use |
|---|---|
| `id` | Card identity and navigation |
| `image`, `name`, `status` | Primary card content and detail identity |
| `species` | Secondary card text if readable; detail information |
| `gender`, optional `type` | Detail facts |
| `origin.name`, `location.name` | Origin and last known location |
| `episode` references | Episode count; episode names need additional data |
| Pagination metadata, including `info.count` | Incremental loading, completion and total matches for the current query |

Name and status filters can be combined remotely. Images are square, 300 × 300 pixels; there is no separate cinematic backdrop. `created` is a database timestamp, not a birth date. Ratings, biographies and image galleries are not supplied by the character schema. [Official API documentation](https://rickandmortyapi.com/documentation), checked 2026-09-30.

## Shared screen structure

Use portrait Android layouts, English text and Material 3 foundations. Neither screen has a standard top app bar. Home is the only top-level destination, so there is no bottom navigation bar. Search and detail are parts of the browsing journey, not separate tabs.

There are two destinations: Home and Character detail. Search, filter selection, loading, empty results, request errors and Retry update the current destination in place; they never push a route or open a modal. Selecting a character opens detail; empty/error feedback never does. The app provides Back on detail only. Android system Back follows normal navigation and dismisses an open keyboard before leaving the current destination.

System status/navigation bars and their safe areas remain respected. Home has no app title, heading row or toolbar: search is the first app component below the status-bar safe area, followed by the chips and grid. Detail has an always-available floating Back button with a readable surface and at least a 48dp target, including while loading or displaying errors.

## Home: content and components

| Component | Responsibility |
|---|---|
| Search field | At the top of the content: “Search characters”, search icon and clear action when text exists. |
| Status chips | Directly below search: All, Alive, Dead, Unknown. Single-choice; All initially selected. Allow horizontal scrolling if the row cannot fit; never clip or hide access to a chip. |
| Character grid | Below the controls; two columns at ordinary phone widths, adapting to text size and width. |
| Character card | Square image, prominent wrapping name, text status with a dot/icon, and optional secondary species. The entire card opens detail. |
| Card skeleton | A loading variant with the same image and text regions, no invented character data and no navigation action. |
| Image placeholder/fallback | Contained within a real card's image area; independent of its text and click action. |
| Results feedback | Empty/error messaging with contextual retry; pagination feedback remains below loaded cards. |
| Suggested searches | Rick, Morty, Beth and Summer shortcuts below the no-match message; submit a name search on the same Home. |
| Floating progress pill | Read-only loaded/total count for the active query, with clearance above the system navigation area. |

Keep the search field and chips visible above the results in every home state. Only the results area changes. Empty/error content must remain scrollable and its actions reachable with the keyboard open or large text. Prefer one grid column when two readable cards do not fit. Do not add a “Characters” heading or reserve its former vertical space.

Put card text on a solid surface; do not depend on the image for text contrast. Allow up to two visible name lines with ellipsis at ordinary text sizes; retain the full name in semantics and in detail. Expand/adapt card height and columns for larger text. Detail names and factual values wrap without ellipsis. The species subtitle occupies one line with ellipsis when necessary; preserve its full text in semantics and detail. Keep the species and status badge on the same row, reserving the badge width before assigning the remaining width to species. A status badge is informational rather than another filter control. Cards belong in home; generic tokens and shared primitives belong in the design system.

### Loading inside cards

Distinguish data loading from image loading:

- Before character data arrives, show a small visible grid of card skeletons. Keep search and chips available; do not invent tappable characters or cover the screen with a spinner.
- Once data exists, show the real name/status immediately. Only the image region keeps its placeholder until that request finishes, then fades to the image. Image failure replaces that region with a neutral fallback; the card remains usable.
- Loading another page preserves every loaded card and uses a small footer indicator. An append failure exposes a footer retry. Do not turn loaded cards back into skeletons.

A subtle pulse or shimmer can make skeletons readable as loading. Keep a static equivalent for disabled motion, stop off-screen animation and avoid delaying fast results to display an effect. Loading placeholders do not repeat artificial character semantics to accessibility services.

### Floating loaded/total counter

Use the selected pill's subdued rounded surface and small blue dot, floating at bottom center above the system gesture/navigation safe area. It is informative, not a button or bottom navigation bar. Use “Loaded {loadedCount} of {totalCount} characters”.

`loadedCount` is the number of real characters available in the current paginated result, not the number currently visible, placeholders or a scroll position. `totalCount` comes from `info.count` for the same name/status query. The API paginates results and includes totals with the active response; no extra count request is needed. A complete first page can contain 20 loaded characters even if only four cards fit on screen. [API pagination and filtering](https://rickandmortyapi.com/documentation/#info-and-pagination).

Show the pill only when the current result has loaded characters and a known total. Reset both values with a changed query; hide it during initial/replacement loading, empty results and initial/query errors. Keep it for append loading/error and at the end of the catalogue. It complements the footer spinner/Retry, without replacing either.

Reserve enough bottom scroll padding for the final cards and footer Retry to clear the pill. Hide it while the keyboard or a connectivity snackbar is visible, avoiding overlays competing for the same space. Large text must remain readable without clipping or blocking actions. Use actual dynamic values in implementation; numeric mockup examples are illustrative.

### Home interactions and states

Typing updates the query after a short debounce; the keyboard Search action applies the current input immediately without duplicating an equivalent pending/requested query. A changed name or status starts a new result set at the beginning; previous-query cards must not masquerade as current matches. Clearing the input preserves status; All preserves the name. Selecting an already-selected chip does nothing.

The input stays mounted and keeps its text, selection and focus across loading, empty and error results. Home does not automatically open the keyboard on entry. Search on the keyboard dismisses it after applying the query; dismissing it otherwise changes no filters. Opening detail dismisses the keyboard. Returning restores query, status and scroll without automatically reopening it.

No matches from a name, a status or both displays feedback below the unchanged controls on Home. There is no Back, Go home, Retry or separate empty-results destination. Users recover by editing/clearing the name, selecting another status or choosing a suggested search.

| State | Visible response |
|---|---|
| Initial or new-query loading | Card skeletons below the current controls; editing remains available |
| Content | Real cards, each with independent image loading/failure |
| No matches from name/status | “No characters found” / “Try another name or status.” and Rick/Morty/Beth/Summer search shortcuts below the visible controls; no navigation or Retry |
| Empty unfiltered catalogue | “No characters available.” In the same results area, with search and chips visible |
| Initial/query error | “Couldn't load characters” and “Retry” inside the results area; preserve visible search/chips and their values |
| Append loading/error | Footer feedback or “Couldn't load more characters” with “Retry”; retain cards |
| End reached | Retain cards, remove the loading footer and stop page requests; a confirmed out-of-range append response also ends pagination |

Suggested searches appear only in the no-match state, not during loading, request errors or an empty unfiltered catalogue. Keep the existing compact pill style with readable text, at least 48dp interactive targets and wrapping when needed. They are action buttons, distinct from the single-choice status chips; no new icons are needed.

Selecting Rick, Morty, Beth or Summer updates the visible name field, retains the current status and submits immediately through the same path as keyboard Search. Dismiss the keyboard, reset pagination/scroll/counter and suppress duplicate equivalent requests as for normal submission. Continue on Home with the normal loading/content/empty/error states. The names are fixed UI suggestions; no recommendation endpoint, search history or result prefetch is introduced. A suggestion can still return no matches for the retained status.

Retry acts in the failed area: home results, the pagination footer or detail content. Show progress there and prevent duplicate retries while that operation is pending. For pagination, keep the error and loading footer at the same measured height so progress replaces Retry in place; a repeated failure restores Retry without requiring another scroll. It never clears filters, jumps to a new destination or restarts pagination unnecessarily.

The API returns 404 for multiple situations. Map by request context and the recognized response contract, as documented in [ARCHITECTURE](ARCHITECTURE.md#api-outcomes-and-visible-states). Never make every 404 a generic empty screen or a Back action.

## Detail: hierarchy and components

Implement the selected detail composition with the following information hierarchy.

| Region | Content and intended behaviour |
|---|---|
| Hero and Back | Prominent character portrait, bounded height, controlled crop and floating Back button. A subtle theme gradient can frame the image. |
| Identity | Character name as the main heading, status badge and species. Place immediately below the image or on a reliably contrasted lower hero surface. |
| Character facts | Gender and Type when meaningful. Species stays in identity; avoid redundant display. |
| Locations | Clearly labelled Origin and Last known location, allowing long names to wrap. |
| Appearances | A visible “Episode appearances” label with a single count value/badge; no episode names or links to another screen. |

Use the selected grouped fact rows for gender, optional type, locations and the episode count. Preserve their labels and information priority. These regions are read-only; do not make them appear to navigate when no destination exists.

The hero uses the existing character portrait, not an assumed second image. Prefer a contained or approximately square presentation over an oversized panoramic crop. A mild scroll-linked parallax moves the image more slowly than the content; constrain movement to its clipped bounds and keep the Back control steady. The title scrolls naturally with the content and does not become a collapsing top app bar. Provide a static motion alternative. Inspect image sharpness on a device before accepting the final hero size.

An empty `type` omits that row. Unknown supplied values display “Unknown”; do not infer missing facts. Count the available episode references without fetching every episode. Long names and locations must not overlap the image or controls. Review the top, middle and end of the scroll: the fixed Back control must remain readable and every fact reachable, with sufficient system insets and scroll clearance.

| State | Visible response |
|---|---|
| Detail data loading | Back remains available; skeleton hero/identity/facts preserve the intended structure |
| Data ready, image pending/failed | Show the facts immediately; keep loading/fallback local to the hero image |
| Request failure | “Couldn't load character”, “Retry” and Back |
| Character unavailable | “Character not found” and Back; no endless retry for a confirmed missing ID |
| Content | Scrollable information and Back; normal return preserves browsing context |

## Connectivity feedback — Should

Show an app-level snackbar reading “No internet connection” with a dismiss control and normal timeout, above the system navigation/keyboard insets. Notify once per observed disconnected period while the app is visible, including initial confirmed disconnection. An unresolved initial connectivity state produces no warning. Recomposition, repeated callbacks and home/detail navigation must not replay it during the same app session. Do not queue warnings while the app is in the background; reconcile current connectivity on return and discard obsolete warnings. Dismiss an outstanding warning when connectivity returns; do not force a reload.

Keep loaded content and screen-level Retry available. A persistent home/detail error explains a failed request even after the snackbar disappears. Position the snackbar so a visible Retry action remains reachable, including with the keyboard open. Server errors can happen with a working connection; the snackbar is only a connectivity hint, not an error diagnosis. This Should variant does not promise offline browsing. See [ARCHITECTURE](ARCHITECTURE.md#connectivity-awareness--should) for ownership.

## Visual language and motion

Aim for a contemporary Android product aesthetic: clear typographic hierarchy, deliberate spacing, subtly separated dark surfaces, consistent shapes and restrained motion. Character images remain the main visual focus. The selected reference uses restrained blue accents, recorded above. Preserve that palette and the original character artwork colours across state variants. Final theme roles and contrast remain subject to verification. Light/dark support keeps its Should priority.

Both screens share colour roles, type scale, spacing, radii, status treatment and icon style. Use readable contrast, text-based status, meaningful grouped semantics and at least 48dp interactive targets. Review a narrow portrait layout and increased text size. This establishes component foundations, not a completed accessibility audit.

| Interaction | Motion intent |
|---|---|
| Card skeleton | Restrained local pulse/shimmer; no full-screen blocking overlay |
| Image appears | Brief fade with stable image bounds, not replayed on every recomposition |
| Chip selection | Contained feedback with an unambiguous static selected state |
| Result replacement | Evaluate in C11 after screen integration; avoid grid-wide replays on every keystroke |
| Detail scroll | Subtle bounded portrait parallax with a static alternative |
| Navigation | Coherent forward/back transition; shared image transition remains optional |

Static mockups need motion annotations and do not validate performance. Prefer small native Compose animations and stop unnecessary off-screen work. [Android animation guidance](https://developer.android.com/develop/ui/compose/animation/choose-api).

### From the selected design to the app theme

The exported design file supplies conflicting HEX values and roles between its prose, front matter and actual screen uses, as recorded above. Use the preferred rendered appearance as the visual reference, inspect actual HTML token use and reconcile one palette before translating it into Material 3 colour roles in `:core:designsystem`, with matching foreground/background pairs such as `primary`/`onPrimary` and `surface`/`onSurface`. Keep character-status colours explicit instead of tying Alive/Dead/Unknown to the brand accent. [Material 3 theming in Compose](https://developer.android.com/develop/ui/compose/designsystems/material3).

Review contrast on real text, controls and states; adjust tones when necessary. The generated palette is a design proposal, not verified theme code. Keep the accepted token values with the selected design reference here until the implementation becomes authoritative. If a light theme is selected later, derive its tonal roles separately rather than reusing dark-theme HEX values unchanged.

## Review and handoff

Review both loaded screens together for consistency and data fidelity. Then refine home and detail states separately using their tables above, including image failure, Unknown status, empty Type, long values and larger text. Finally check the return journey, scroll behaviour and motion. If the Should monitor is selected for implementation, review its snackbar alongside both content and persistent error states.

The 2026-09-30 documentation audit checked all six public documents and the prompts against these journeys. It clarified in-place empty/error states, contextual API outcomes, keyboard/focus behaviour, narrow layouts, retries and snackbar placement. Three live API probes informed the outcome mapping; this is not a runtime UI test.

The selected references and implementation adjustments are recorded above. Visual planning is sufficient to begin implementation. C04 verifies first-page Home states, portrait feedback, system-safe final-row clearance, narrow/large-text rendering and Retry on API 37. C05 verifies Detail states and production navigation on API 37, with native review of the live portrait, fixed Back, wrapping facts and disabled-motion alternative. C06 verifies real pagination and retained append errors/Retry, loaded/total visibility, keyboard hiding and later-page Back retention on API 37. Native review confirms live recovery from 240 to 280 loaded characters, safe footer clearance and a readable single-column layout at font scale 1.5. C07 implements persistent search, clear/keyboard submission and fixed no-match shortcuts; C08 implements persistent compact status chips and combined queries, locally verified on API 37; accepted for publication. Automated screenshot regression and measured contrast/performance audits remain separate verification work.

## Component and icon inventory

Status: visual inventory from the reviewed content exports and agreed state contracts, checked 2026-09-30. Selected content and Home/Detail state references have been reviewed with the handoff adjustments above; pagination and image samples can be resolved directly in Compose. Names below identify design pieces, not implemented Kotlin classes or a requirement for one class/file per row. Earlier sections own behavior; this table records the pieces and their assets without creating another specification. C03 implements the Home card, local portrait feedback and skeleton plus shared theme/loading primitives; standard previews support visual review; C03 is accepted and archived. C04 adds the first-page Home screen and state/interaction tests. C05 implements Detail states, grouped facts, fixed Back and bounded/static portrait motion; C06 implements pagination feedback and the floating counter; C07 adds the search field, clear action and no-match suggestions; C08 implements persistent compact status chips and combined queries, locally verified on API 37; accepted for publication.

| Component | Where and required variants | Icons or visual marks | Evidence/status |
|---|---|---|---|
| Search field | Home; empty, entered text, focused | `search`; `close` for Clear | Exported HTML omits mic/tune actions; reuse the content field across states |
| Status chip group | Home; All/Alive/Dead/Unknown, selected/unselected | Selected surface and semantics; status dots | Native chips size to their text/indicator with consistent padding, solid surfaces, a 40dp minimum visible height and 12dp corners; selection uses a blue-grey surface and checkmark; scroll when needed for narrow widths or large text; native touch targets remain at least 48dp |
| Character card | Home; real name, square portrait, species and status | No extra action icon; uses status treatment below | Content reviewed; whole card opens detail |
| Status treatment | Cards and detail identity; Alive/Dead/Unknown | Text plus colored dot; detail Dead also uses `skull` | Present; verify other status variants without adding new icons by assumption |
| Portrait region | Card and detail; loading, loaded, failed | API image; neutral placeholder/fallback | Content reviewed; resolve a neutral local fallback in Compose |
| Card skeleton | Home before data arrives | Placeholder shapes; no character text or decorative icon | Geometry reviewed; exported HTML corrects visibility, but the loading PNG is stale |
| Floating loaded/total pill | Home content/append/end | Small blue dot; no action icon | Content reviewed; preserves the floating overlay |
| Pagination feedback | Home grid footer; loading and retry | Native progress indicator; text Retry, no icon required | Specified; resolve in Compose without requiring further mockups |
| Empty/error feedback | Home results and detail content where applicable | Message; contextual Retry; `travel_explore` for empty results, `error_outline` for errors | Home and Detail error compositions reviewed; unify the outlined error treatment |
| Suggested-search buttons | Home no matches; Rick/Morty/Beth/Summer | Text-only action pills; no icons or selected state | Selected visual reference accepted; reuse name submission and retain status |
| Floating Back control | Detail, including loading/error | `arrow_back` | Present; 48dp interactive target in native implementation |
| Detail hero and identity | Portrait, name, status/species, optional ID eyebrow | `person` for species; `bubble_chart` beside the current ID; status treatment above | Content reviewed; ID glyph is decorative |
| Character facts group and row | Detail; label/value, optional Type, wrapping values | Field mapping below | Content and “Episode appearances” label/count verified against the character API |
| Detail skeleton | Detail before data arrives; Back retained | Placeholder shapes | Selected composition reviewed; omit Share and preserve content geometry |
| Connectivity snackbar | App-level; Should | Dismiss action; reuse `close` if rendered as an icon button | Resolve through native component review if implemented |

### Detail fact icons

These Material Symbols names come from the exported HTML, not guessed Compose property names. The icon repeats the adjacent label rather than carrying a separate action.

| Field label | Exported symbol | Note |
|---|---|---|
| Type | `fingerprint` | Omit the entire row for empty Type |
| Gender | `wc` | Current visual reference; decorative generic pictogram, not an inference about the value |
| Origin | `public` | Read-only; no navigation arrow |
| Last known location | `location_on` | Read-only; permit multiline values |
| Episode appearances | `movie` | One count from the character's episode references; no episode names |

### Native asset and component conventions

- Keep the current Material Symbols Outlined visual family. Obtain only the selected glyphs as Android vector drawables and render them with Compose Material 3 `Icon`; do not copy the web icon font, screenshot the glyphs or assume every symbol exists in `Icons.Outlined`. The current Android guidance recommends downloaded Material Symbols XML assets over the older Material Icons artifact. [Android icon guidance](https://developer.android.com/develop/ui/compose/graphics/images/material), [Material Symbols formats and license](https://developers.google.com/fonts/docs/material_symbols).
- Store selected reusable icon assets and visual tokens in `:core:designsystem`. Keep character/query decisions in their feature; a neutral badge or labelled-row primitive can be shared when there is actual reuse. Keep small dots as drawn shapes. This inventory does not introduce another module or a mandatory component wrapper for every row.
- Give Back, Clear and any icon-only Dismiss action localized accessible labels. Icons beside equivalent visible labels and status dots are decorative; avoid duplicate announcements. Loading uses progress semantics. Status remains understandable from text, and actionable controls meet the existing touch-target requirement.
- Android owns the clock, Wi-Fi/battery indicators and gesture navigation bar. They are mockup context, not assets to reproduce inside Compose. Filter/settings, bookmark/share and the removed lore badge remain absent.
- Resolve remaining state icons during native component review; additional exports are optional. Reconcile icon weight/size, typography, surface roles and spacing with the chosen appearance during implementation, then check previews and a device. This inventory does not claim verified contrast or pixel-perfect native rendering.

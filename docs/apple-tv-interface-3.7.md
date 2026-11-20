# Apple TV interface redesign - CleanYourTV 3.7

Status: implementation branch `feature/apple-tv-interface-3.7`  
Baseline: `sync/cleanyourtv-3.6.0-custom` (3.6.0)  
Design target: Apple TV / tvOS visual language. This is an independent implementation inspired by the interaction and visual principles of tvOS, not an Apple product or an official port.

## Product direction

The goal is not to recolour the existing launcher. CleanYourTV 3.7 should read as one coherent, cinematic television interface:

1. Content is the main visual object. The Hero / Top Shelf owns the upper visual field and changes with the focused app.
2. App icons are compact, recognisable, consistent objects. Their identity must not depend on every installed app providing a 16:9 banner.
3. Focus is spatial. Scale, elevation, a restrained light response and a short motion transition work together; a permanent heavy outline is not the primary focus signal.
4. The Dock is a distinct floating navigation surface. Its glass material must remain subtle enough for wallpaper and content to show through.
5. Controls, quick panels and settings share one material, typography, corner and focus language.
6. Motion is quick and composed. Rapid D-pad repeat must not queue animations or make focus lag behind the remote.
7. Performance is part of the design. No continuous full-screen blur, per-frame bitmap filtering or unnecessary hardware layers.

## Constraints

- Keep the 3.6.0 baseline branch unchanged.
- All implementation commits on the feature branch must start with `[Apple TV]`.
- Preserve the established RecyclerView / GridLayoutManager architecture, stable IDs, app ordering, package discovery, TileRefresh, MemoryTrimPolicy and lifecycle cleanup unless a measured regression requires a change.
- Preserve remote navigation, row-edge behaviour, Dock movement, accessibility labels and return-from-app focus restoration.
- Target the known device class: Android 11 / API 30 and Mali-G71. Do not rely on Android 12 `RenderEffect` APIs.
- The existing local theme system remains the delivery mechanism for wallpapers, artwork and visual settings. No network service is introduced into the launcher.

## Audit findings from 3.6.0

### Existing building blocks to retain

- `MainActivity` already coordinates the Hero shelf, cinematic shelf, Ambient glow, focus highlight and focus springs.
- `AppAdapter` already supports app banners, icon-only tiles, cached accent colour sampling, stable item IDs and focus animations.
- `GlassView` already renders a cached hardware-layer highlight with a translucent fill, specular gradient, rim and accent glow.
- `DockDecoration` already draws the Dock band and calculates its bounds from visible Dock cards.
- `HeroArt` already makes a small cached colour backdrop from app artwork.
- `WallpaperPlayer` already decodes wallpaper rotations off the main thread after the initial image and releases resources under memory pressure.
- `Config` already supports a theme overlay from local `config.json`, so visual tuning does not require a new settings backend.

### Gaps to address

- The app tile is currently based on a 180 x 101 dp 16:9 banner. `uniform_tiles` switches to a centred icon, but does not establish a new icon-card geometry or a true content-led tile system.
- `HeroArt` is intentionally only 48 x 27 pixels. It produces ambient colour, not a readable featured-content image.
- Focus is currently distributed between tile scale, optional lift/Z, banner scale, name animation and a separate spring-driven glass slab. These signals need one consistent timing and visual hierarchy.
- `GlassView` is a translucent focus slab, not a background-blurring glass material. It should remain a focus treatment, not be reused as a universal overlay.
- `DockDecoration` draws a gradient/tinted band; it does not perform a live background blur.
- The quick panel's `glass_panel.xml` is a mostly opaque dark gradient. Launcher settings use a different flat style and green focus state.
- The header clock/date and service pills are visually prominent and compete with the Hero area.

## Target visual system

### 1. App tiles

- Introduce a coherent icon-led presentation that tolerates apps with banners, square icons, adaptive icons and missing artwork.
- Avoid cropping important logos or text. Keep an explicit fallback path.
- Use consistent card geometry, corner radius, icon scale and image clipping.
- Keep unfocused labels quiet or hidden; reveal the selected app name in the Hero area or a single focused label.
- Do not force an untested geometry across every column count. Recalculate cell width, height, focus overscan and name clearance together.

### 2. Focus 2.0

- One source of truth for focused scale and motion timings.
- Initial target scale range: 1.08-1.10. Validate by screenshot and D-pad traces before locking the value.
- Use restrained elevation and a soft accent response derived from the already-cached app colour.
- Avoid a thick permanent border. The edge, light and scale should form one composed signal.
- Keep the focus surface aligned to the card during both spring movement and RecyclerView scrolling.
- Rapid repeat must retarget the current spring rather than queue transitions.
- Reduced-motion mode must remain supported and remove bounce/fades without changing navigation semantics.

### 3. Hero / Top Shelf 2.0

- Separate actual featured artwork from the tiny colour/ambient artwork.
- Support optional local, theme-provided per-app Hero artwork with a deterministic key/fallback rule.
- Show app identity and optional metadata with a readable gradient/scrim.
- Crossfade artwork and update text without blank frames.
- Preserve the existing low-cost HeroArt colour treatment as fallback when no large artwork is configured.
- Do not claim to show catalog recommendations or trailers unless a supported local/provider source actually exists.

### 4. Dock 2.0

- Treat the Dock as a floating navigation plane, visually separate from the app grid.
- Use consistent inset, spacing, corner radii, edge light and translucent fill.
- Preserve existing app order, empty slots, move/reorder behaviour and focus navigation.
- Avoid a heavy solid slab. Wallpaper/content should remain visible through a subtle material where contrast permits.
- Confirm the Dock's edges and focus overscan at 1080p and other supported resolutions.

### 5. Liquid Glass material family

- Build lightweight material primitives from translucent fills, restrained highlights, thin edge strokes and static/prepared textures.
- Use materials for functional surfaces: Dock, top controls, menus, quick settings and dialogs.
- Keep app artwork itself clear and saturated.
- Do not imply that Android 11 provides native tvOS blur. Use a prepared or low-resolution backdrop treatment where a real blur is needed.
- Avoid a continuously redrawn full-screen offscreen layer.

### 6. Header, panels and settings

- Reduce the header's visual weight so it supports rather than competes with Hero.
- Use consistent type scale, line height, icon size, corner radius and focus treatment.
- Bring quick settings, Inputs, app/module panels, prompts and launcher settings into one design system.
- Keep text readable from viewing distance and maintain focus visibility for remote operation.

## Implementation order

1. App tile geometry and artwork fallback.
2. Focus 2.0 and focus/scroll alignment.
3. Hero / Top Shelf 2.0 with local artwork support.
4. Dock 2.0.
5. Shared lightweight glass materials.
6. Header, quick panels, dialogs and launcher settings.
7. Motion polish, accessibility and device regression tests.

## Validation checklist

- [ ] Build release APK with the repository's Android SDK / Gradle configuration.
- [ ] Run relevant unit and repository tests.
- [ ] Verify every existing theme still parses; missing optional artwork must fall back cleanly.
- [ ] Verify 4-7 column layouts and row-below-Dock variants.
- [ ] Verify focus at every row edge, Dock end, Library entry and top controls.
- [ ] Verify fast D-pad repeat and long focus movement.
- [ ] Verify app launch and return preserve focus and scroll position.
- [ ] Compare cold launch, warm Home return, D-pad jank and memory against 3.6.0 on Tanix W2 Ultra.
- [ ] Capture a real-device screenshot before claiming visual parity or adding a published theme screenshot.

## Current implementation note

This document records the design direction and constraints. A checklist item is not complete until its code and validation evidence are present in this branch.

## Implementation progress

### First slice - Apple TV tile and focus mode

Implemented on the feature branch:

- Added the opt-in `apple_tv_tiles` theme key. Its default is false, so legacy themes retain their 16:9 tile geometry.
- Apple TV mode uses square icon-led app cards and automatically enables the existing uniform icon treatment.
- Increased the tile corner radius and aligned the focus outline, glass highlight and Dock band with the new shape.
- Reduced focus zoom to 1.09, softened the vertical lift/elevation and disabled the extra banner zoom in this mode.
- Added a lighter focus-glass sheen and fewer glow rings.
- In cinematic mode, use the real landscape app banner as a full-size Hero background where available. The tile itself still displays the app icon; icon-only apps use the existing softened HeroArt fallback.
- Clear Hero drawable wrappers together with the existing bitmap cache during reload and memory-pressure release.
- Add a lighter floating Dock material with a thin bright edge and a static top sheen; legacy Dock drawing remains unchanged when Apple TV mode is off.
- Preserved the original 3.6.0 geometry and motion paths when the flag is off.

### Preview theme overlay

The initial mode can be enabled without changing the built-in defaults by using the companion `apple-tv-preview-config.json` as a theme's `config.json`. It combines the new square tile mode with the existing cinematic shelf and glass Dock. The Hero image in this first slice is still the existing low-resolution colour treatment; full-resolution local Hero artwork is a later implementation slice.

Not yet complete: the full-content Hero artwork provider, Dock material polish, header/panel restyle and on-device validation. No screenshot or performance claim is made until checked on the Tanix.

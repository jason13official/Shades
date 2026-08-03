# Shades — Project Notes

Internal dev notes: current state, references, findings, and next steps. Not user-facing docs.

## Current State

Multiloader mod (`common`/`fabric`/`neoforge`, target `26.1.2`) adding pairs of "shades" that apply a
screen/vision effect while worn in the head slot. Items so far:

- **`basic_shades`** — bespoke tint/darken/vignette shader (`shades_visor.fsh`)
- **`creeper_shades` / `invert_shades` / `spider_shades` / `blur_shades`** — vanilla-derived
  `post_effect` chains, reusing vanilla's own `.fsh`/`.vsh` by reference
- **`night_vision_shades` / `thermal_shades` / `matrix_shades`** — bespoke custom shaders
- **`receipt_shades` / `halftone_shades` / `lego_shades` / `fluted_glass_shades`** — bespoke
  shaders adapted from real-world post-processing techniques (pixelation/cell patterns, SDFs,
  derivative-based lighting)
- **`prism_shades`** — cycles through *all* of the above via a keybind (`G`, rebindable, category
  "Shades"), plus a HUD label showing the current selection
- **`plasma_shades`** — the odd one out: **not** a `post_effect`/`PostChain` item at all. Uses a
  genuine custom `RenderPipeline` (`ShadesRenderPipelines.PLASMA`) so its shader gets live
  `GameTime`, unlike every other item here. Renders two ways: the actual worn lens model
  (visible to other players, third person) and a camera-anchored translucent quad (the
  "cheat" for a full-screen look, since `post_effect` structurally can't animate — see Findings).
  `prism_shades` can also cycle *into* Plasma mode (`ShadesClient.isPlasmaSelected`).

Every item: no `Equippable` asset (so vanilla's `HumanoidArmorLayer` renders nothing), a custom
3D visor model (`ShadesVisorModel`/`ShadesVisorLayer`, lens + temple arms grafted onto the
vanilla head bone), and vanilla's decorative-head-item fallback suppressed via
`CustomHeadLayerMixin`. See Architecture below for how these fit together.

## References (priority)

**Decompilation** — `D:\_decomp\26.1.2` is the primary/authoritative reference for this project's
actual version. `D:\_decomp\1.21.1` also exists alongside it but wasn't needed here — this
project's APIs (renderer, shaders, equipment) have diverged enough from 1.21.1 that it's not a
reliable analog; always verify against `26.1.2` specifically. Decomp has three variants —
`minecraft/`, `minecraft_fabric/`, `minecraft_neoforge/` — the plain `minecraft/` one was
sufficient for everything so far.

**Sibling projects** (same author, same `26.1.2` version — read before reinventing something):
- `D:\github\jason13official\Adaptive-Armor` — cosmetic render-layer pattern
  (`AdaptationCosmeticsLayer`, one `AdaptationCosmetic` per effect), the
  `PlayerRendererMixin`-extends-real-superclass idiom for calling protected `addLayer` from a
  mixin outside the target's package, and `planning/custom-model-rendering.md` (worth reading —
  covers the "standalone gated overlay" vs "parented bone attachment" tradeoff for cosmetic
  geometry, box UV layout notes, and confirms overlay-copy-pose is sufficient — no child-bone
  mixin ever needed in practice).
- `D:\github\jason13official\Thaumatic` — **the direct template for everything render-pipeline
  related in this mod.** `ThaumaticRenderPipelines.java` (custom `RenderPipeline` + `RenderType`
  construction), `ThaumaticClient.java`'s `doHudOverlay` (HUD layer pattern, copied directly for
  the Prism overlay), `ShatterStarManager`/`FXShatterStar` (camera-relative billboard quad
  submission via `submitCustomGeometry` — copied directly for `ShadesPlasmaEffect`), and its
  `common/src/main/resources/{thaumatic.accesswidener, META-INF/accesstransformer.cfg}` (the
  access-widening setup this project's `shades.accesswidener`/`accesstransformer.cfg` mirror).
  Also shows the exact NeoForge event wiring: `RegisterRenderPipelinesEvent` (mod bus),
  `SubmitCustomGeometryEvent`/`RenderGuiEvent.Post` (`NeoForge.EVENT_BUS`, not mod bus).

**External reading:**
- The pasted "Guide to vanilla shaders" (2021, pre-1.17-core-shaders era) — conceptually useful
  for the targets/passes/programs mental model, but **structurally outdated** for `26.1.2`:
  `shaders/post/*.json` → `post_effect/*.json`, the separate `shaders/program/*.json` layer is
  gone (programs are inline in each pass), GLSL 110 → 330, uniform blocks are now `std140` UBOs.
  Don't copy its JSON shapes verbatim — verify against decomp every time.
- "Post-Processing Shaders as a Creative Medium" (Maxime, Feb 2025) — source for the pixelation/
  cell-pattern technique (`receipt_vision.fsh`, `halftone_vision.fsh`), the Lego brick lighting
  trick (`lego_vision.fsh`), and the fluted-glass derivative/normal trick
  (`fluted_glass_vision.fsh`). Also describes an ASCII-atlas effect and dynamic mouse-trail
  pixelation that weren't ported — see Ideas below.
- [thebookofshaders.com](https://thebookofshaders.com) (examples + editor) — general GLSL
  reference; the direct inspiration for `plasma_vision`'s sum-of-sines recipe and for chasing
  down whether this engine has a real `u_time` equivalent at all (it does, for core shaders —
  see Findings).

**Key vanilla source files worth knowing by name** (all under `D:\_decomp\26.1.2\minecraft`):
`net/minecraft/client/renderer/{GameRenderer,LevelRenderer,ShaderManager,PostChain,PostPass,
PostChainConfig,RenderPipelines}.java`, `net/minecraft/client/renderer/entity/{LivingEntityRenderer,
HumanoidMobRenderer}.java`, `.../entity/layers/{HumanoidArmorLayer,CustomHeadLayer}.java`,
`net/minecraft/client/model/geom/ModelPart.java`, `net/minecraft/client/{KeyMapping,Camera}.java`,
`assets/minecraft/shaders/include/*.glsl`, `assets/minecraft/post_effect/*.json`.

## Key Findings

- **`state.headEquipment` is not a plain copy of the worn item.**
  `HumanoidMobRenderer.getEquipmentIfRenderable` returns `ItemStack.EMPTY` unless
  `HumanoidArmorLayer.shouldRender` is true — i.e. unless the item has an `Equippable` asset.
  Since our items deliberately have none, that field is permanently empty for us. We carry the
  real item ourselves via a mixin-added interface (`ShadesRenderStateExtension`, populated in
  `AvatarRendererMixin#extractRenderState`) instead of relying on it.
- **Vanilla's "no armor asset → render flat item icon on head" fallback** (in
  `LivingEntityRenderer#extractRenderState`, feeding `CustomHeadLayer`) fires for exactly the
  same reason our items have no asset. Suppressed via `CustomHeadLayerMixin` cancelling
  `CustomHeadLayer#submit` for our items.
- **`post_effect` shaders never get live time.** `PostPass` always builds its `RenderPipeline`
  from `POST_PROCESSING_SNIPPET` alone, never combined with `GLOBALS_SNIPPET` — confirmed by
  reading `PostPass.createPass`. No JSON field exists to substitute a different pipeline. Core
  shaders *do* get live `GameTime` via the `Globals` UBO (`globals.glsl`,
  `GlobalSettingsUniform#update`, called every frame in `GameRenderer.render()`) — it loops 0..1
  once per in-game day, not once per real second, but it's genuinely live. This is *why*
  `plasma_shades` needed a whole separate custom-`RenderPipeline` code path instead of another
  `post_effect` JSON.
- **`RenderPipelines.GLOBALS_SNIPPET`/`MATRICES_FOG_SNIPPET` and `RenderType.create(String,
  RenderSetup)` are package-private in vanilla.** Building any custom pipeline needs an access
  widener (Fabric) + access transformer (NeoForge) — both build scripts already had the
  conditional wiring (`if (file.exists()) ...`) from the project template, just needed the actual
  `shades.accesswidener`/`META-INF/accesstransformer.cfg` files (see Thaumatic reference above).
- **`ModelPart`-baked geometry can feed into a reduced-vertex-format custom pipeline.**
  `ModelPart.Cube#compile` always calls the full `addVertex(pos, color, uv, overlay, light,
  normal)` overload, but that's a default method that just chains the individual `setColor`/
  `setUv`/`setOverlay`/`setLight`/`setNormal` calls — a `POSITION_TEX_COLOR`-only buffer simply
  has no packed slot for the ones it doesn't need. This is how `plasma_shades`'s lens can use the
  *same* `ShadesVisorModel` geometry as every other item while still running a bespoke pipeline.
- **`Camera#getNearPlane(fov)`** hands back the camera's near-clip-plane corners as
  camera-relative offset vectors, already correct for the player's actual FOV/aspect ratio. Used
  to size `ShadesPlasmaEffect`'s cheat quad so it fills the screen identically in every camera
  mode, instead of guessing a fixed size/distance from the player's eyes.
- **HotSwap only replaces method bodies.** Any field/method/class-shape change (new fields, new
  mixin, new interface) needs a genuine JVM restart, not just IntelliJ's "Reload Changed
  Classes." Several debugging sessions here were derailed by testing against a stale process.
- **Prism cycle index is networked via `ModComponents.PRISM_CYCLE_INDEX`, not a local field.**
  Pressing the cycle key sends `CyclePrismC2SPacket` (empty payload) to the server; the server
  validates `prism_shades` is still worn, then advances the component directly on the real
  equipped `ItemStack`. No explicit resync packet needed either direction: `LivingEntity`'s own
  per-tick equipment diffing (`detectEquipmentUpdates`, comparing via `ItemStack.matches`)
  broadcasts the change to *other* tracking players automatically since a changed component makes
  the stack no longer match; the *wearer's own* client picks it up via the normal
  `AbstractContainerMenu#broadcastChanges` slot-diff sync every tick, since the mutation happens
  in place on the actual instance in the player's inventory. `ShadesClient.c2s` is the generic
  client→server packet acceptor (wired per loader in `Shades{Fabric,ClientNeoForge}`), mirroring
  the `s2c`/`c2s` pattern from the Hermetica sibling project.

## Architecture (quick map)

- `ModItems` — registers every shades item (`Equippable`, no asset, `stacksTo(1)`)
- `ModComponents` — data components (currently: `PRISM_CYCLE_INDEX`, persistent + networked)
- `ShadesClient` — client-side brain: per-item `post_effect` id lookup, Prism cycle
  keybind (sends `CyclePrismC2SPacket` on press; the index itself lives on the item, not a local
  field), `isPlasmaSelected`, HUD overlay. Also hosts `c2s`, the generic client→server packet
  acceptor wired per loader.
- `CyclePrismC2SPacket` — serverbound, empty payload; server advances
  `ModComponents.PRISM_CYCLE_INDEX` on the wearer's actual equipped `ItemStack`
- `GameRendererMixin` → `ShadesClient.doGameRender` — runs the `PostChain` for whichever item is
  worn (skipped entirely for Plasma)
- `AvatarRendererMixin` — adds `ShadesVisorLayer`; also populates `ShadesRenderStateExtension`
  each frame (the real head-slot item, bypassing the `headEquipment` gating bug)
- `AvatarRenderStateMixin` / `ShadesRenderStateExtension` — the mixin-added-interface pair
  carrying that real item on the render state
- `ShadesVisorLayer` / `ShadesVisorModel` — the worn lens+arms model, textured normally for most
  items, or rendered through `ShadesRenderPipelines.plasma()` when `isPlasmaSelected`
- `CustomHeadLayerMixin` — suppresses vanilla's flat-icon fallback for our items
- `ShadesRenderPipelines` / `ShadesPlasmaEffect` — the one item using a genuine custom
  `RenderPipeline` + `SubmitCustomGeometryEvent`/`LevelRenderEvents.COLLECT_SUBMITS` instead of
  the `post_effect` system

## Ideas (not yet started, surfaced during this work)

- **Split `ShadesVisorModel` lens vs. temple-arm rendering** for Plasma specifically, so the arms
  keep a normal opaque material instead of also shimmering — currently one `submitModel` call
  colors the whole thing. Cheap: two calls, two `RenderType`s.
- **Extend custom-pipeline items to other tracked players**, not just the local player —
  `ShadesPlasmaEffect` is explicitly local-player-only right now (its camera-quad wouldn't make
  sense otherwise, but *other* players' Plasma lenses already work today via `ShadesVisorLayer`
  regardless).
- **ASCII-art shader** (from the "Creative Medium" article) — needs an external character-atlas
  texture, a bigger asset-pipeline lift than the pure-procedural shaders here; deferred earlier
  for that reason.
- **Reinterpret the article's mouse-trail pixelation** using something Minecraft actually has —
  e.g. look-direction angular velocity or player movement speed as the driving input instead of
  cursor position.
- **Shift+cycle-key to go backward** through the Prism list, and/or a chat command to jump
  straight to a named effect instead of only cycling forward.

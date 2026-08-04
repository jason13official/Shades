package io.github.jason13official.shades;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import io.github.jason13official.shades.impl.client.ShadesLiveVision;
import io.github.jason13official.shades.impl.client.ShadesRenderPipelines;
import io.github.jason13official.shades.impl.common.registry.ModComponents;
import io.github.jason13official.shades.impl.common.registry.ModItems;
import io.github.jason13official.shades.impl.network.CyclePrismC2SPacket;
import io.github.jason13official.shades.platform.Services;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.MoonPhase;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;

public class ShadesClient {

  public static final Identifier SHADES_VISOR_POST_EFFECT = Shades.identifier("shades_visor");
  public static final Identifier CREEPER_SHADES_POST_EFFECT = Shades.identifier("creeper_shades");
  public static final Identifier INVERT_SHADES_POST_EFFECT = Shades.identifier("invert_shades");
  public static final Identifier SPIDER_SHADES_POST_EFFECT = Shades.identifier("spider_shades");
  public static final Identifier BLUR_SHADES_POST_EFFECT = Shades.identifier("blur_shades");
  public static final Identifier NIGHT_VISION_SHADES_POST_EFFECT = Shades.identifier("night_vision_shades");
  public static final Identifier THERMAL_SHADES_POST_EFFECT = Shades.identifier("thermal_shades");
  public static final Identifier RECEIPT_SHADES_POST_EFFECT = Shades.identifier("receipt_shades");
  public static final Identifier HALFTONE_SHADES_POST_EFFECT = Shades.identifier("halftone_shades");
  public static final Identifier LEGO_SHADES_POST_EFFECT = Shades.identifier("lego_shades");
  public static final Identifier FLUTED_GLASS_SHADES_POST_EFFECT = Shades.identifier("fluted_glass_shades");
  public static final Identifier CHROMATIC_SHADES_POST_EFFECT = Shades.identifier("chromatic_shades");
  public static final Identifier XRAY_SHADES_POST_EFFECT = Shades.identifier("xray_shades");
  public static final Identifier FISHEYE_SHADES_POST_EFFECT = Shades.identifier("fisheye_shades");
  public static final Identifier NEON_SHADES_POST_EFFECT = Shades.identifier("neon_shades");
  public static final Identifier KALEIDOSCOPE_SHADES_POST_EFFECT = Shades.identifier("kaleidoscope_shades");
  public static final Identifier PREDATOR_SHADES_POST_EFFECT = Shades.identifier("predator_shades");
  public static final Identifier FRACTAL_SHADES_POST_EFFECT = Shades.identifier("fractal_shades");

  /// sentinel -> plasma_shades has no real post_effect JSON; doGameRender skips it entirely, the
  /// visual comes from ShadesVisorLayer's lens model + ShadesPlasmaEffect's camera quad instead
  public static final Identifier PLASMA_SHADES_MARKER = Shades.identifier("plasma_shades");

  /// sentinels for items needing live GameTime (post_effect JSON can't provide it) -> doGameRender
  /// routes these to ShadesLiveVision's hand-rolled pass instead of PostChain
  public static final Identifier STATIC_SHADES_MARKER = Shades.identifier("static_shades");
  public static final Identifier SONAR_SHADES_MARKER = Shades.identifier("sonar_shades");
  public static final Identifier GLITCH_SHADES_MARKER = Shades.identifier("glitch_shades");
  public static final Identifier RAIN_SHADES_MARKER = Shades.identifier("rain_shades");
  public static final Identifier CURSOR_SHADES_MARKER = Shades.identifier("cursor_shades");
  public static final Identifier VERTIGO_SHADES_MARKER = Shades.identifier("vertigo_shades");
  public static final Identifier ANIMATED_GLASS_SHADES_MARKER = Shades.identifier("animated_glass_shades");
  public static final Identifier MOLTEN_GLASS_SHADES_MARKER = Shades.identifier("molten_glass_shades");
  public static final Identifier FIRE_SHADES_MARKER = Shades.identifier("fire_shades");
  public static final Identifier GRID_SHADES_MARKER = Shades.identifier("grid_shades");
  public static final Identifier ORB_SHADES_MARKER = Shades.identifier("orb_shades");
  public static final Identifier WAVEFORM_SHADES_MARKER = Shades.identifier("waveform_shades");
  public static final Identifier FLUID_SHADES_MARKER = Shades.identifier("fluid_shades");
  public static final Identifier COPPER_SHADES_MARKER = Shades.identifier("copper_shades");

  /// converted from a real post_effect (green tint + scanlines only, no live GameTime) once
  /// falling code glyphs needed live time to animate
  public static final Identifier MATRIX_SHADES_MARKER = Shades.identifier("matrix_shades");
  public static final Identifier MIRAGE_SHADES_MARKER = Shades.identifier("mirage_shades");
  public static final Identifier LENS_SHADES_MARKER = Shades.identifier("lens_shades");
  public static final Identifier VORTEX_SHADES_MARKER = Shades.identifier("vortex_shades");
  public static final Identifier WISP_SHADES_MARKER = Shades.identifier("wisp_shades");
  public static final Identifier AURORA_SHADES_MARKER = Shades.identifier("aurora_shades");
  public static final Identifier COSMIC_SHADES_MARKER = Shades.identifier("cosmic_shades");
  public static final Identifier VOXEL_SHADES_MARKER = Shades.identifier("voxel_shades");
  public static final Identifier PULSE_SHADES_MARKER = Shades.identifier("pulse_shades");
  public static final Identifier BIOME_SHADES_MARKER = Shades.identifier("biome_shades");
  public static final Identifier RADAR_SHADES_MARKER = Shades.identifier("radar_shades");
  public static final Identifier GRAVITY_SHADES_MARKER = Shades.identifier("gravity_shades");
  public static final Identifier LUNAR_SHADES_MARKER = Shades.identifier("lunar_shades");

  /// one entry per prism_shades cycle position, in order; `item` is `null` only for the index-0
  /// "off" state. A single list instead of two parallel ones, so the id/displayName pairing can't
  /// desync by index the way the old two-list version could
  private record Effect(Item item, Identifier id, String displayName) {}

  /// lazy, same reason as postEffectsByItem below: ModItems' fields aren't set yet at
  /// ShadesClient's own class-init time, only once registration has actually run
  private static List<Effect> effects;

  private static List<Effect> effects() {

    if (effects == null) {
      effects = List.of(
          new Effect(null, null, "Off"),
          new Effect(ModItems.BASIC_SHADES, SHADES_VISOR_POST_EFFECT, "Basic"),
          new Effect(ModItems.CREEPER_SHADES, CREEPER_SHADES_POST_EFFECT, "Creeper"),
          new Effect(ModItems.INVERT_SHADES, INVERT_SHADES_POST_EFFECT, "Negative"),
          new Effect(ModItems.SPIDER_SHADES, SPIDER_SHADES_POST_EFFECT, "Spider"),
          new Effect(ModItems.BLUR_SHADES, BLUR_SHADES_POST_EFFECT, "Blurry"),
          new Effect(ModItems.NIGHT_VISION_SHADES, NIGHT_VISION_SHADES_POST_EFFECT, "Night Vision"),
          new Effect(ModItems.THERMAL_SHADES, THERMAL_SHADES_POST_EFFECT, "Thermal"),
          new Effect(ModItems.MATRIX_SHADES, MATRIX_SHADES_MARKER, "Matrix"),
          new Effect(ModItems.RECEIPT_SHADES, RECEIPT_SHADES_POST_EFFECT, "Receipt"),
          new Effect(ModItems.HALFTONE_SHADES, HALFTONE_SHADES_POST_EFFECT, "Halftone"),
          new Effect(ModItems.LEGO_SHADES, LEGO_SHADES_POST_EFFECT, "Lego"),
          new Effect(ModItems.FLUTED_GLASS_SHADES, FLUTED_GLASS_SHADES_POST_EFFECT, "Fluted Glass"),
          new Effect(ModItems.CHROMATIC_SHADES, CHROMATIC_SHADES_POST_EFFECT, "Chromatic"),
          new Effect(ModItems.XRAY_SHADES, XRAY_SHADES_POST_EFFECT, "X-Ray"),
          new Effect(ModItems.FISHEYE_SHADES, FISHEYE_SHADES_POST_EFFECT, "Fisheye"),
          new Effect(ModItems.NEON_SHADES, NEON_SHADES_POST_EFFECT, "Neon"),
          new Effect(ModItems.KALEIDOSCOPE_SHADES, KALEIDOSCOPE_SHADES_POST_EFFECT, "Kaleidoscope"),
          new Effect(ModItems.STATIC_SHADES, STATIC_SHADES_MARKER, "Static"),
          new Effect(ModItems.SONAR_SHADES, SONAR_SHADES_MARKER, "Sonar"),
          new Effect(ModItems.GLITCH_SHADES, GLITCH_SHADES_MARKER, "Glitch"),
          new Effect(ModItems.RAIN_SHADES, RAIN_SHADES_MARKER, "Rain"),
          new Effect(ModItems.CURSOR_SHADES, CURSOR_SHADES_MARKER, "Cursor"),
          new Effect(ModItems.VERTIGO_SHADES, VERTIGO_SHADES_MARKER, "Vertigo"),
          new Effect(ModItems.PREDATOR_SHADES, PREDATOR_SHADES_POST_EFFECT, "Predator"),
          new Effect(ModItems.FRACTAL_SHADES, FRACTAL_SHADES_POST_EFFECT, "Fractal"),
          new Effect(ModItems.ANIMATED_GLASS_SHADES, ANIMATED_GLASS_SHADES_MARKER, "Animated Glass"),
          new Effect(ModItems.MOLTEN_GLASS_SHADES, MOLTEN_GLASS_SHADES_MARKER, "Molten Glass"),
          new Effect(ModItems.FIRE_SHADES, FIRE_SHADES_MARKER, "Fire"),
          new Effect(ModItems.GRID_SHADES, GRID_SHADES_MARKER, "Grid"),
          new Effect(ModItems.ORB_SHADES, ORB_SHADES_MARKER, "Orb"),
          new Effect(ModItems.WAVEFORM_SHADES, WAVEFORM_SHADES_MARKER, "Waveform"),
          new Effect(ModItems.FLUID_SHADES, FLUID_SHADES_MARKER, "Fluid"),
          new Effect(ModItems.COPPER_SHADES, COPPER_SHADES_MARKER, "Copper"),
          new Effect(ModItems.MIRAGE_SHADES, MIRAGE_SHADES_MARKER, "Mirage"),
          new Effect(ModItems.LENS_SHADES, LENS_SHADES_MARKER, "Lens"),
          new Effect(ModItems.VORTEX_SHADES, VORTEX_SHADES_MARKER, "Vortex"),
          new Effect(ModItems.WISP_SHADES, WISP_SHADES_MARKER, "Wisp"),
          new Effect(ModItems.AURORA_SHADES, AURORA_SHADES_MARKER, "Aurora"),
          new Effect(ModItems.COSMIC_SHADES, COSMIC_SHADES_MARKER, "Cosmic"),
          new Effect(ModItems.VOXEL_SHADES, VOXEL_SHADES_MARKER, "Voxel"),
          new Effect(ModItems.PULSE_SHADES, PULSE_SHADES_MARKER, "Pulse"),
          new Effect(ModItems.BIOME_SHADES, BIOME_SHADES_MARKER, "Biome"),
          new Effect(ModItems.RADAR_SHADES, RADAR_SHADES_MARKER, "Radar"),
          new Effect(ModItems.GRAVITY_SHADES, GRAVITY_SHADES_MARKER, "Gravity"),
          new Effect(ModItems.LUNAR_SHADES, LUNAR_SHADES_MARKER, "Lunar"),
          new Effect(ModItems.PLASMA_SHADES, PLASMA_SHADES_MARKER, "Plasma"));
    }

    return effects;
  }

  private static final KeyMapping.Category SHADES_KEY_CATEGORY = KeyMapping.Category.register(Shades.identifier("shades"));

  public static final KeyMapping CYCLE_PRISM_KEY =
      new KeyMapping("key.shades.cycle_prism", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, SHADES_KEY_CATEGORY);

  /// general purpose client-to-server packet acceptor, wired up per loader
  public static Consumer<CustomPacketPayload> c2s;

  /// which vanilla-derived post-processing chain plays for which pair of glasses; filled in once
  /// ModItems is populated, since the fields aren't set yet at class-init time
  private static Map<Item, Identifier> postEffectsByItem;

  public static void init() {
  }

  /// derived from effects() -> every item except the "off" sentinel (null item) and plasma_shades,
  /// which routes to the live shader lens/quad instead of a post_effect chain
  private static Map<Item, Identifier> postEffectsByItem() {

    if (postEffectsByItem == null) {
      postEffectsByItem = effects().stream()
          .filter(effect -> effect.item() != null && effect.item() != ModItems.PLASMA_SHADES)
          .collect(Collectors.toMap(Effect::item, Effect::id));
    }

    return postEffectsByItem;
  }

  /// real head slot if it holds a shades item, else whatever accessory slot (Trinkets/Curios, if
  /// installed) currently holds one -> shared by doGameRender, doHudOverlay, and
  /// AvatarRendererMixin's render-state capture, so wearing shades as an accessory works
  /// identically to the real head slot everywhere the mod cares which item is "worn"
  public static ItemStack resolveWornShades(LivingEntity entity) {

    ItemStack headStack = entity.getItemBySlot(EquipmentSlot.HEAD);
    if (ModItems.idOf(headStack.getItem()) != null) {
      return headStack;
    }

    return Services.PLATFORM.getAccessoryShadesItem(entity);
  }

  public static void doGameRender(CrossFrameResourcePool resourcePool) {

    Minecraft mc = Minecraft.getInstance();
    LocalPlayer player = mc.player;
    if (player == null) {
      return;
    }

    ItemStack headStack = resolveWornShades(player);
    Item headItem = headStack.getItem();

    Identifier postEffectId;
    if (headItem == ModItems.PRISM_SHADES) {

      while (CYCLE_PRISM_KEY.consumeClick()) {
        if (c2s != null) {
          c2s.accept(new CyclePrismC2SPacket(mc.hasShiftDown()));
        }
      }

      int index = headStack.getOrDefault(ModComponents.PRISM_CYCLE_INDEX, 0);
      postEffectId = effects().get(index).id();
    } else {
      postEffectId = postEffectsByItem().get(headItem);
    }

    if (postEffectId == null || postEffectId.equals(PLASMA_SHADES_MARKER)) {
      return;
    }

    LiveEffect liveEffect = liveEffects().get(postEffectId);
    if (liveEffect != null) {
      ShadesLiveVision.process(resourcePool, liveEffect.pipeline(), liveEffect.depth().get(), liveEffect.extraUniforms(), liveEffect.feedback().get());
      return;
    }

    PostChain postChain = mc.getShaderManager().getPostChain(postEffectId, LevelTargetBundle.MAIN_TARGETS);
    if (postChain != null) {
      postChain.process(mc.getMainRenderTarget(), resourcePool);
    }
  }

  /// one entry per item needing live GameTime (see the *_SHADES_MARKER sentinels above). `depth`,
  /// `extraUniforms`, and `feedback` are suppliers so an effect can opt into real depth, a custom
  /// per-frame uniform, or a ping-pong trail; `() -> null` / `null` where it doesn't need one
  private record LiveEffect(RenderPipeline pipeline, Supplier<GpuTextureView> depth, Function<RenderPass, GpuBuffer> extraUniforms,
      Supplier<RenderTarget> feedback) {}

  private static Map<Identifier, LiveEffect> liveEffects;

  private static Map<Identifier, LiveEffect> liveEffects() {

    if (liveEffects == null) {
      liveEffects = Map.ofEntries(
          Map.entry(STATIC_SHADES_MARKER, new LiveEffect(ShadesRenderPipelines.STATIC_TV, () -> null, null, () -> null)),
          Map.entry(SONAR_SHADES_MARKER, new LiveEffect(ShadesRenderPipelines.SONAR, ShadesClient::getWorldDepthCapture, ShadesClient::buildSonarCameraRayUniform, () -> null)),
          Map.entry(GLITCH_SHADES_MARKER, new LiveEffect(ShadesRenderPipelines.GLITCH, () -> null, null, () -> null)),
          Map.entry(RAIN_SHADES_MARKER, new LiveEffect(ShadesRenderPipelines.RAIN, () -> null, ShadesClient::buildRainUniform, () -> null)),
          Map.entry(CURSOR_SHADES_MARKER, new LiveEffect(ShadesRenderPipelines.CURSOR, () -> null, ShadesClient::buildCursorUniform, () -> null)),
          Map.entry(VERTIGO_SHADES_MARKER, new LiveEffect(ShadesRenderPipelines.VERTIGO, () -> null, ShadesClient::buildMotionUniform, () -> null)),
          Map.entry(ANIMATED_GLASS_SHADES_MARKER, new LiveEffect(ShadesRenderPipelines.ANIMATED_GLASS, () -> null, null, () -> null)),
          Map.entry(MOLTEN_GLASS_SHADES_MARKER, new LiveEffect(ShadesRenderPipelines.MOLTEN_GLASS, () -> null, ShadesClient::buildGlassUniform, () -> null)),
          Map.entry(FIRE_SHADES_MARKER, new LiveEffect(ShadesRenderPipelines.FIRE, () -> null, ShadesClient::buildFireUniform, () -> null)),
          Map.entry(GRID_SHADES_MARKER, new LiveEffect(ShadesRenderPipelines.GRID, ShadesClient::getWorldDepthCapture, ShadesClient::buildGridRayUniform, () -> null)),
          Map.entry(ORB_SHADES_MARKER, new LiveEffect(ShadesRenderPipelines.ORB, () -> null, ShadesClient::buildOrbUniform, () -> null)),
          Map.entry(WAVEFORM_SHADES_MARKER, new LiveEffect(ShadesRenderPipelines.WAVEFORM, ShadesClient::getWorldDepthCapture, ShadesClient::buildWaveformRayUniform, ShadesClient::getWaveformFeedback)),
          Map.entry(FLUID_SHADES_MARKER, new LiveEffect(ShadesRenderPipelines.FLUID, ShadesClient::getWorldDepthCapture, ShadesClient::buildFluidRayUniform, () -> null)),
          Map.entry(COPPER_SHADES_MARKER, new LiveEffect(ShadesRenderPipelines.COPPER, () -> null, ShadesClient::buildCopperUniform, () -> null)),
          Map.entry(MATRIX_SHADES_MARKER, new LiveEffect(ShadesRenderPipelines.MATRIX, () -> null, ShadesClient::buildMatrixUniform, () -> null)),
          Map.entry(MIRAGE_SHADES_MARKER, new LiveEffect(ShadesRenderPipelines.MIRAGE, () -> null, ShadesClient::buildMirageUniform, () -> null)),
          Map.entry(LENS_SHADES_MARKER, new LiveEffect(ShadesRenderPipelines.LENS, () -> null, ShadesClient::buildLensUniform, () -> null)),
          Map.entry(VORTEX_SHADES_MARKER, new LiveEffect(ShadesRenderPipelines.VORTEX, () -> null, null, () -> null)),
          Map.entry(WISP_SHADES_MARKER, new LiveEffect(ShadesRenderPipelines.WISP, () -> null, ShadesClient::buildWispUniform, () -> null)),
          Map.entry(AURORA_SHADES_MARKER, new LiveEffect(ShadesRenderPipelines.AURORA, () -> null, null, () -> null)),
          Map.entry(COSMIC_SHADES_MARKER, new LiveEffect(ShadesRenderPipelines.COSMIC, () -> null, ShadesClient::buildCosmicUniform, () -> null)),
          Map.entry(VOXEL_SHADES_MARKER, new LiveEffect(ShadesRenderPipelines.VOXEL, ShadesClient::getWorldDepthCapture, ShadesClient::buildVoxelRayUniform, () -> null)),
          Map.entry(PULSE_SHADES_MARKER, new LiveEffect(ShadesRenderPipelines.PULSE, () -> null, ShadesClient::buildPulseUniform, () -> null)),
          Map.entry(BIOME_SHADES_MARKER, new LiveEffect(ShadesRenderPipelines.BIOME, () -> null, ShadesClient::buildBiomeUniform, () -> null)),
          Map.entry(RADAR_SHADES_MARKER, new LiveEffect(ShadesRenderPipelines.RADAR, () -> null, ShadesClient::buildRadarUniform, () -> null)),
          Map.entry(GRAVITY_SHADES_MARKER, new LiveEffect(ShadesRenderPipelines.GRAVITY, () -> null, ShadesClient::buildGravityUniform, () -> null)),
          Map.entry(LUNAR_SHADES_MARKER, new LiveEffect(ShadesRenderPipelines.LUNAR, () -> null, ShadesClient::buildLunarUniform, () -> null)));
    }

    return liveEffects;
  }

  /// persistent copy of the real depth buffer, refreshed by GameRendererMixin's
  /// shades$captureWorldDepth right before the item-in-hand render clears the real one. Runs every
  /// frame regardless of worn item, so sonar/grid/waveform/fluid/voxel_shades all just read this
  private static RenderTarget worldDepthCapture;

  public static void captureWorldDepth() {

    Minecraft mc = Minecraft.getInstance();
    RenderTarget mainTarget = mc.getMainRenderTarget();
    if (!mainTarget.useDepth) {
      return;
    }

    if (worldDepthCapture == null || worldDepthCapture.width != mainTarget.width || worldDepthCapture.height != mainTarget.height) {
      if (worldDepthCapture != null) {
        worldDepthCapture.destroyBuffers();
      }
      worldDepthCapture = new TextureTarget(null, mainTarget.width, mainTarget.height, true);
    }

    RenderSystem.getDevice().createCommandEncoder().copyTextureToTexture(
        mainTarget.getDepthTexture(), worldDepthCapture.getDepthTexture(), 0, 0, 0, 0, 0, mainTarget.width, mainTarget.height);
  }

  private static GpuTextureView getWorldDepthCapture() {
    return worldDepthCapture != null ? worldDepthCapture.getDepthTextureView() : null;
  }

  /// persistent color-only feedback buffer holding waveform_shades' previous frame, for its
  /// fading trail
  private static RenderTarget waveformFeedback;

  private static RenderTarget getWaveformFeedback() {
    waveformFeedback = ensureFeedbackTarget(waveformFeedback);
    return waveformFeedback;
  }

  /// (re)allocates a persistent color-only target sized to the main target, cleared to opaque
  /// black on (re)creation ->  unlike worldDepthCapture, this one gets *read* before anything is
  /// ever copied into it (the very first frame an item wearing it is worn), so it can't rely on a
  /// same-frame write-before-read like captureWorldDepth does; it needs an explicit clear instead
  private static RenderTarget ensureFeedbackTarget(RenderTarget existing) {

    Minecraft mc = Minecraft.getInstance();
    RenderTarget mainTarget = mc.getMainRenderTarget();

    if (existing == null || existing.width != mainTarget.width || existing.height != mainTarget.height) {
      if (existing != null) {
        existing.destroyBuffers();
      }
      RenderTarget feedback = new TextureTarget(null, mainTarget.width, mainTarget.height, false);
      RenderSystem.getDevice().createCommandEncoder().clearColorTexture(feedback.getColorTexture(), 0xFF000000);
      return feedback;
    }

    return existing;
  }

  /// world position the sonar ping currently expands from -> re-anchored once per ping cycle
  /// instead of sliding with the player every frame
  private static Vec3 sonarPingOrigin;
  private static long sonarPingCycle = -1;

  /// pushes a combined inverse-projection*view matrix + camera position + ping origin, so
  /// sonar.fsh can reconstruct real world-space position per pixel (same `worldPos()` technique
  /// orbital_railgun's strike.fsh uses) and measure real distance from a fixed point in the world
  private static GpuBuffer buildSonarCameraRayUniform(RenderPass renderPass) {

    Minecraft mc = Minecraft.getInstance();
    LocalPlayer player = mc.player;
    CameraRenderState cameraState = mc.gameRenderer.getGameRenderState().levelRenderState.cameraRenderState;

    Matrix4f inverseTransform = new Matrix4f(cameraState.projectionMatrix).mul(cameraState.viewRotationMatrix).invert();
    Vec3 cameraPos = cameraState.pos;

    // ~8.3 real seconds per full sweep (matches sonar.fsh's `t * 0.06` phase) -> re-anchor the
    // origin only when a new cycle starts, so the ring stays fixed in the world for its sweep
    // instead of tracking the player's live position every frame
    long cycle = player != null ? (long) Math.floor((player.level().getGameTime() % 24000) * 0.006) : 0;
    if (sonarPingOrigin == null || cycle != sonarPingCycle) {
      sonarPingOrigin = player != null ? player.getEyePosition() : cameraPos;
      sonarPingCycle = cycle;
    }

    try (MemoryStack stack = MemoryStack.stackPush()) {
      Std140Builder builder = Std140Builder.onStack(stack, 96)
          .putMat4f(inverseTransform)
          .putVec3((float) cameraPos.x, (float) cameraPos.y, (float) cameraPos.z)
          .putVec3((float) sonarPingOrigin.x, (float) sonarPingOrigin.y, (float) sonarPingOrigin.z);

      GpuBuffer buffer = RenderSystem.getDevice().createBuffer(() -> "shades:sonar_camera_ray", GpuBuffer.USAGE_UNIFORM, builder.get());
      renderPass.setUniform("CameraRay", buffer);
      return buffer;
    }
  }

  /// pushes real cursor position (normalized 0..1, texCoord-space) + whether a screen is
  /// currently open, so cursor.fsh can ripple from the actual mouse position
  private static GpuBuffer buildCursorUniform(RenderPass renderPass) {

    Minecraft mc = Minecraft.getInstance();
    boolean screenOpen = mc.screen != null;
    Window window = mc.getWindow();
    float u = (float) (mc.mouseHandler.xpos() / window.getWidth());
    float v = 1.0f - (float) (mc.mouseHandler.ypos() / window.getHeight());

    try (MemoryStack stack = MemoryStack.stackPush()) {
      Std140Builder builder = Std140Builder.onStack(stack, 32)
          .putFloat(screenOpen ? 1.0f : 0.0f)
          .putVec2(u, v);

      GpuBuffer buffer = RenderSystem.getDevice().createBuffer(() -> "shades:cursor_config", GpuBuffer.USAGE_UNIFORM, builder.get());
      renderPass.setUniform("CursorConfig", buffer);
      return buffer;
    }
  }

  /// smoothed 0..1 factors driving vertigo.fsh; raw speed/turn are noisy per-frame, so each is
  /// eased toward its target every frame via a plain exponential filter instead of applied directly
  private static float smoothedSpeed = 0.0f;
  private static float smoothedTurn = 0.0f;
  private static float previousYaw = Float.NaN;

  /// pushes smoothed horizontal-speed and yaw-turn-rate factors, so vertigo.fsh can drive a
  /// zoom blur + swirl off the player's real movement instead of GameTime
  private static GpuBuffer buildMotionUniform(RenderPass renderPass) {

    Minecraft mc = Minecraft.getInstance();
    LocalPlayer player = mc.player;

    float rawSpeed = 0.0f;
    float rawTurn = 0.0f;
    if (player != null) {
      Vec3 motion = player.getDeltaMovement();
      rawSpeed = (float) Math.sqrt(motion.x * motion.x + motion.z * motion.z);

      float yaw = player.getYRot();
      if (!Float.isNaN(previousYaw)) {
        rawTurn = Math.abs(Mth.wrapDegrees(yaw - previousYaw));
      }
      previousYaw = yaw;
    }

    smoothedSpeed += (rawSpeed - smoothedSpeed) * 0.15f;
    smoothedTurn += (rawTurn - smoothedTurn) * 0.25f;

    // normalize against roughly sprint-speed/fast-turn baselines, clamp to 0..1
    float speedFactor = Math.min(smoothedSpeed / 0.35f, 1.0f);
    float turnFactor = Math.min(smoothedTurn / 15.0f, 1.0f);

    try (MemoryStack stack = MemoryStack.stackPush()) {
      Std140Builder builder = Std140Builder.onStack(stack, 16)
          .putFloat(speedFactor)
          .putFloat(turnFactor);

      GpuBuffer buffer = RenderSystem.getDevice().createBuffer(() -> "shades:vertigo_motion_config", GpuBuffer.USAGE_UNIFORM, builder.get());
      renderPass.setUniform("MotionConfig", buffer);
      return buffer;
    }
  }

  /// smoothed yaw/pitch delta driving molten_glass.fsh's blob sway; kept as separate fields from
  /// vertigo's so switching items mid-cycle can't jump-start one effect's smoothing from the other's
  private static float smoothedSwayYaw = 0.0f;
  private static float smoothedSwayPitch = 0.0f;
  private static float previousGlassYaw = Float.NaN;
  private static float previousGlassPitch = Float.NaN;

  /// pushes the real window aspect ratio plus a smoothed screen-space sway offset opposite the
  /// camera's current turn direction -> the blobs lag behind like they have real inertia, then
  /// drift back to center as the turn eases off
  private static GpuBuffer buildGlassUniform(RenderPass renderPass) {

    Minecraft mc = Minecraft.getInstance();
    Window window = mc.getWindow();
    float aspect = (float) window.getWidth() / (float) window.getHeight();

    LocalPlayer player = mc.player;
    float rawYawDelta = 0.0f;
    float rawPitchDelta = 0.0f;
    if (player != null) {
      float yaw = player.getYRot();
      float pitch = player.getXRot();
      if (!Float.isNaN(previousGlassYaw)) {
        rawYawDelta = Mth.wrapDegrees(yaw - previousGlassYaw);
        rawPitchDelta = pitch - previousGlassPitch;
      }
      previousGlassYaw = yaw;
      previousGlassPitch = pitch;
    }

    smoothedSwayYaw += (rawYawDelta - smoothedSwayYaw) * 0.2f;
    smoothedSwayPitch += (rawPitchDelta - smoothedSwayPitch) * 0.2f;

    // negated so the blobs drift opposite the swing direction, clamped so a fast flick can't
    // fling them off-screen
    float swayX = Mth.clamp(-smoothedSwayYaw * 0.01f, -0.08f, 0.08f);
    float swayY = Mth.clamp(-smoothedSwayPitch * 0.01f, -0.08f, 0.08f);

    try (MemoryStack stack = MemoryStack.stackPush()) {
      Std140Builder builder = Std140Builder.onStack(stack, 16)
          .putFloat(aspect)
          .putFloat(swayX)
          .putFloat(swayY);

      GpuBuffer buffer = RenderSystem.getDevice().createBuffer(() -> "shades:glass_config", GpuBuffer.USAGE_UNIFORM, builder.get());
      renderPass.setUniform("GlassConfig", buffer);
      return buffer;
    }
  }

  /// pushes the real window aspect ratio, so fire.fsh's virtual-space sampling isn't stretched on
  /// a non-square window ->  no sway/other state needed, unlike molten_glass's GlassConfig
  private static GpuBuffer buildFireUniform(RenderPass renderPass) {

    Window window = Minecraft.getInstance().getWindow();
    float aspect = (float) window.getWidth() / (float) window.getHeight();

    try (MemoryStack stack = MemoryStack.stackPush()) {
      Std140Builder builder = Std140Builder.onStack(stack, 16)
          .putFloat(aspect);

      GpuBuffer buffer = RenderSystem.getDevice().createBuffer(() -> "shades:fire_config", GpuBuffer.USAGE_UNIFORM, builder.get());
      renderPass.setUniform("FireConfig", buffer);
      return buffer;
    }
  }

  /// pushes a combined inverse-projection*view matrix + camera position, so grid.fsh can
  /// reconstruct real world-space position per pixel and grid-snap by real block columns; every
  /// column bounces on its own hashed phase instead of sweeping from a fixed point
  private static GpuBuffer buildGridRayUniform(RenderPass renderPass) {
    return buildWorldRayUniform(renderPass, "GridRay", "shades:grid_ray");
  }

  /// pushes both the inverse-projection*view matrix (worldPos(), screen->world) and the
  /// non-inverted one (world->screen), so waveform.fsh can forward-project the scan altitude's
  /// crossing plane to find its real screen row; without it the flash stayed pinned to row 0.5
  private static GpuBuffer buildWaveformRayUniform(RenderPass renderPass) {

    CameraRenderState cameraState = Minecraft.getInstance().gameRenderer.getGameRenderState().levelRenderState.cameraRenderState;

    Matrix4f projView = new Matrix4f(cameraState.projectionMatrix).mul(cameraState.viewRotationMatrix);
    Matrix4f inverseTransform = new Matrix4f(projView).invert();
    Vec3 cameraPos = cameraState.pos;

    try (MemoryStack stack = MemoryStack.stackPush()) {
      Std140Builder builder = Std140Builder.onStack(stack, 144)
          .putMat4f(inverseTransform)
          .putMat4f(projView)
          .putVec3((float) cameraPos.x, (float) cameraPos.y, (float) cameraPos.z);

      GpuBuffer buffer = RenderSystem.getDevice().createBuffer(() -> "shades:waveform_ray", GpuBuffer.USAGE_UNIFORM, builder.get());
      renderPass.setUniform("WaveformRay", buffer);
      return buffer;
    }
  }

  /// pushes a combined inverse-projection*view matrix + camera position, so fluid.fsh can sample
  /// its ripple field at real world XZ position instead of a fixed screen-space pattern
  private static GpuBuffer buildFluidRayUniform(RenderPass renderPass) {
    return buildWorldRayUniform(renderPass, "FluidRay", "shades:fluid_ray");
  }

  /// shared by the three world-position-reconstruction uniform blocks above; each just needs a
  /// different uniform/buffer name
  private static GpuBuffer buildWorldRayUniform(RenderPass renderPass, String uniformName, String bufferLabel) {

    CameraRenderState cameraState = Minecraft.getInstance().gameRenderer.getGameRenderState().levelRenderState.cameraRenderState;

    Matrix4f inverseTransform = new Matrix4f(cameraState.projectionMatrix).mul(cameraState.viewRotationMatrix).invert();
    Vec3 cameraPos = cameraState.pos;

    try (MemoryStack stack = MemoryStack.stackPush()) {
      Std140Builder builder = Std140Builder.onStack(stack, 80)
          .putMat4f(inverseTransform)
          .putVec3((float) cameraPos.x, (float) cameraPos.y, (float) cameraPos.z);

      GpuBuffer buffer = RenderSystem.getDevice().createBuffer(() -> bufferLabel, GpuBuffer.USAGE_UNIFORM, builder.get());
      renderPass.setUniform(uniformName, buffer);
      return buffer;
    }
  }

  /// pushes the real window aspect ratio, so orb.fsh's near-plane projection isn't stretched on a
  /// non-square window
  private static GpuBuffer buildOrbUniform(RenderPass renderPass) {
    return buildAspectOnlyUniform(renderPass, "OrbConfig", "shades:orb_config");
  }

  /// pushes the real window aspect ratio, so copper.fsh's light-direction calc isn't skewed on a
  /// non-square window
  private static GpuBuffer buildCopperUniform(RenderPass renderPass) {
    return buildAspectOnlyUniform(renderPass, "CopperConfig", "shades:copper_config");
  }

  /// pushes the real window aspect ratio, so rain.fsh's droplet noise field isn't stretched on a
  /// non-square window
  private static GpuBuffer buildRainUniform(RenderPass renderPass) {
    return buildAspectOnlyUniform(renderPass, "RainConfig", "shades:rain_config");
  }

  /// pushes the real window aspect ratio, so matrix.fsh's falling-glyph grid isn't stretched on a
  /// non-square window
  private static GpuBuffer buildMatrixUniform(RenderPass renderPass) {
    return buildAspectOnlyUniform(renderPass, "MatrixConfig", "shades:matrix_config");
  }

  /// pushes the real window aspect ratio, so mirage.fsh's plasma-warmth field isn't stretched on
  /// a non-square window
  private static GpuBuffer buildMirageUniform(RenderPass renderPass) {
    return buildAspectOnlyUniform(renderPass, "MirageConfig", "shades:mirage_config");
  }

  /// pushes the real window aspect ratio, so lens.fsh's blob field isn't stretched on a
  /// non-square window
  private static GpuBuffer buildLensUniform(RenderPass renderPass) {
    return buildAspectOnlyUniform(renderPass, "LensConfig", "shades:lens_config");
  }

  /// pushes the real window aspect ratio, so wisp.fsh's point positions aren't stretched on a
  /// non-square window
  private static GpuBuffer buildWispUniform(RenderPass renderPass) {
    return buildAspectOnlyUniform(renderPass, "WispConfig", "shades:wisp_config");
  }

  /// pushes the real window aspect ratio, so cosmic.fsh's strand field isn't stretched on a
  /// non-square window
  private static GpuBuffer buildCosmicUniform(RenderPass renderPass) {
    return buildAspectOnlyUniform(renderPass, "CosmicConfig", "shades:cosmic_config");
  }

  /// pushes the inverse-projection*view matrix + camera position (so voxel.fsh can reconstruct
  /// real world position and distance, same worldPos() technique grid.fsh/fluid.fsh use) plus the
  /// real window aspect ratio for its camera ray direction. Aspect is placed before the vec3 in
  /// the push order to match std140's scalar-before-vector packing
  private static GpuBuffer buildVoxelRayUniform(RenderPass renderPass) {

    CameraRenderState cameraState = Minecraft.getInstance().gameRenderer.getGameRenderState().levelRenderState.cameraRenderState;

    Matrix4f inverseTransform = new Matrix4f(cameraState.projectionMatrix).mul(cameraState.viewRotationMatrix).invert();
    Vec3 cameraPos = cameraState.pos;
    Window window = Minecraft.getInstance().getWindow();
    float aspect = (float) window.getWidth() / (float) window.getHeight();

    try (MemoryStack stack = MemoryStack.stackPush()) {
      Std140Builder builder = Std140Builder.onStack(stack, 96)
          .putMat4f(inverseTransform)
          .putFloat(aspect)
          .putVec3((float) cameraPos.x, (float) cameraPos.y, (float) cameraPos.z);

      GpuBuffer buffer = RenderSystem.getDevice().createBuffer(() -> "shades:voxel_ray", GpuBuffer.USAGE_UNIFORM, builder.get());
      renderPass.setUniform("VoxelRay", buffer);
      return buffer;
    }
  }

  /// accumulated heartbeat phase for pulse.fsh; advanced every frame by a rate that climbs as
  /// real player health drops, using wall-clock delta rather than GameTime so the beat keeps a
  /// consistent real-world tempo regardless of game speed
  private static float pulsePhase = 0.0f;
  private static long pulseLastNanos = -1L;

  /// pushes the real window aspect ratio, current health fraction, and the accumulated beat
  /// phase, so pulse.fsh's double-thump vignette tracks the player's real health and beats
  /// faster the lower it gets
  private static GpuBuffer buildPulseUniform(RenderPass renderPass) {

    Minecraft mc = Minecraft.getInstance();
    LocalPlayer player = mc.player;
    float healthFactor = player != null ? Mth.clamp(player.getHealth() / player.getMaxHealth(), 0.0f, 1.0f) : 1.0f;

    long now = System.nanoTime();
    float dt = pulseLastNanos < 0 ? 0.0f : (now - pulseLastNanos) / 1_000_000_000.0f;
    pulseLastNanos = now;

    float beatsPerSecond = Mth.lerp(1.0f - healthFactor, 1.1f, 3.2f);
    pulsePhase += dt * beatsPerSecond * (2.0f * (float) Math.PI);

    Window window = mc.getWindow();
    float aspect = (float) window.getWidth() / (float) window.getHeight();

    try (MemoryStack stack = MemoryStack.stackPush()) {
      Std140Builder builder = Std140Builder.onStack(stack, 16)
          .putFloat(aspect)
          .putFloat(healthFactor)
          .putFloat(pulsePhase);

      GpuBuffer buffer = RenderSystem.getDevice().createBuffer(() -> "shades:pulse_config", GpuBuffer.USAGE_UNIFORM, builder.get());
      renderPass.setUniform("PulseConfig", buffer);
      return buffer;
    }
  }

  /// pushes the real window aspect ratio plus the real biome-blended grass/foliage tint at the
  /// player's position (the same smoothed per-biome color vanilla grass/leaves render with, via
  /// BiomeColors -> a real biome identity signal, unlike fog/sky color which is mostly
  /// weather/dimension-driven), so biome.fsh can tint the whole view like a colored lens
  private static GpuBuffer buildBiomeUniform(RenderPass renderPass) {

    Minecraft mc = Minecraft.getInstance();
    LocalPlayer player = mc.player;

    int groundTint = 0;
    if (player != null && player.level() instanceof ClientLevel level) {
      BlockPos pos = BlockPos.containing(player.position());
      int grassColor = BiomeColors.getAverageGrassColor(level, pos);
      int foliageColor = BiomeColors.getAverageFoliageColor(level, pos);
      groundTint = ARGB.average(grassColor, foliageColor);
    }

    Window window = mc.getWindow();
    float aspect = (float) window.getWidth() / (float) window.getHeight();

    try (MemoryStack stack = MemoryStack.stackPush()) {
      Std140Builder builder = Std140Builder.onStack(stack, 32)
          .putFloat(aspect)
          .putVec3(((groundTint >> 16) & 0xFF) / 255.0f, ((groundTint >> 8) & 0xFF) / 255.0f, (groundTint & 0xFF) / 255.0f);

      GpuBuffer buffer = RenderSystem.getDevice().createBuffer(() -> "shades:biome_config", GpuBuffer.USAGE_UNIFORM, builder.get());
      renderPass.setUniform("BiomeConfig", buffer);
      return buffer;
    }
  }

  /// how far radar_shades looks for a target; entities further than this never light up the blip
  private static final double RADAR_RANGE = 48.0;

  /// pushes the real window aspect ratio plus the distance/bearing of the nearest real entity
  /// (excluding the player), found by scanning ClientLevel's actual render entity list, so
  /// radar.fsh's blip always points at something genuinely in the world
  private static GpuBuffer buildRadarUniform(RenderPass renderPass) {

    Minecraft mc = Minecraft.getInstance();
    LocalPlayer player = mc.player;

    double bestDistance = Double.MAX_VALUE;
    double bestAngle = 0.0;
    boolean found = false;

    if (player != null && player.level() instanceof ClientLevel level) {
      for (Entity entity : level.entitiesForRendering()) {
        if (entity == player) {
          continue;
        }

        double distance = entity.position().distanceTo(player.position());
        if (distance < bestDistance && distance <= RADAR_RANGE) {
          bestDistance = distance;
          double dx = entity.getX() - player.getX();
          double dz = entity.getZ() - player.getZ();
          double bearing = Math.atan2(dx, dz) + Math.toRadians(player.getYRot());
          bestAngle = Mth.wrapDegrees(Math.toDegrees(bearing)) * Mth.DEG_TO_RAD + Math.PI / 2.0;
          found = true;
        }
      }
    }

    Window window = mc.getWindow();
    float aspect = (float) window.getWidth() / (float) window.getHeight();

    try (MemoryStack stack = MemoryStack.stackPush()) {
      Std140Builder builder = Std140Builder.onStack(stack, 16)
          .putFloat(aspect)
          .putFloat(found ? (float) bestDistance : 999.0f)
          .putFloat((float) bestAngle)
          .putFloat(found ? 1.0f : 0.0f);

      GpuBuffer buffer = RenderSystem.getDevice().createBuffer(() -> "shades:radar_config", GpuBuffer.USAGE_UNIFORM, builder.get());
      renderPass.setUniform("RadarConfig", buffer);
      return buffer;
    }
  }

  /// smoothed downward-speed factor and the wall-clock timestamp of the last real landing,
  /// tracked across frames so gravity.fsh's shockwave ring can expand from a genuine landing
  /// event instead of a looping animation
  private static float smoothedFall = 0.0f;
  private static boolean previouslyOnGround = true;
  private static long lastLandingNanos = Long.MIN_VALUE;

  /// pushes the real window aspect ratio, smoothed fall-speed factor, and seconds since the last
  /// real landing, so gravity.fsh can stretch the view while falling and ring out on impact
  private static GpuBuffer buildGravityUniform(RenderPass renderPass) {

    Minecraft mc = Minecraft.getInstance();
    LocalPlayer player = mc.player;

    float rawFall = 0.0f;
    if (player != null) {
      rawFall = (float) Math.max(-player.getDeltaMovement().y, 0.0);

      boolean onGround = player.onGround();
      if (onGround && !previouslyOnGround && smoothedFall > 0.3f) {
        lastLandingNanos = System.nanoTime();
      }
      previouslyOnGround = onGround;
    }

    smoothedFall += (rawFall - smoothedFall) * 0.3f;
    float fallFactor = Mth.clamp(smoothedFall / 1.2f, 0.0f, 1.0f);

    float shockAge = lastLandingNanos == Long.MIN_VALUE ? 999.0f : (System.nanoTime() - lastLandingNanos) / 1_000_000_000.0f;

    Window window = mc.getWindow();
    float aspect = (float) window.getWidth() / (float) window.getHeight();

    try (MemoryStack stack = MemoryStack.stackPush()) {
      Std140Builder builder = Std140Builder.onStack(stack, 16)
          .putFloat(aspect)
          .putFloat(fallFactor)
          .putFloat(shockAge);

      GpuBuffer buffer = RenderSystem.getDevice().createBuffer(() -> "shades:gravity_config", GpuBuffer.USAGE_UNIFORM, builder.get());
      renderPass.setUniform("GravityConfig", buffer);
      return buffer;
    }
  }

  /// pushes the real window aspect ratio, the real current moon phase (via
  /// MoonPhase.index()/7, 0 full .. 1 new), and real sky darkness (via Level.getSkyDarken()/15,
  /// 0 day .. 1 night), so lunar.fsh's vignette/starfield track the world's actual night state
  private static GpuBuffer buildLunarUniform(RenderPass renderPass) {

    Minecraft mc = Minecraft.getInstance();
    LocalPlayer player = mc.player;

    float phaseFraction = 0.0f;
    float nightFactor = 0.0f;
    if (player != null && player.level() instanceof ClientLevel level) {
      MoonPhase phase = level.environmentAttributes().getDimensionValue(EnvironmentAttributes.MOON_PHASE);
      phaseFraction = phase.index() / 7.0f;
      nightFactor = level.getSkyDarken() / 15.0f;
    }

    Window window = mc.getWindow();
    float aspect = (float) window.getWidth() / (float) window.getHeight();

    try (MemoryStack stack = MemoryStack.stackPush()) {
      Std140Builder builder = Std140Builder.onStack(stack, 16)
          .putFloat(aspect)
          .putFloat(phaseFraction)
          .putFloat(nightFactor);

      GpuBuffer buffer = RenderSystem.getDevice().createBuffer(() -> "shades:lunar_config", GpuBuffer.USAGE_UNIFORM, builder.get());
      renderPass.setUniform("LunarConfig", buffer);
      return buffer;
    }
  }

  /// shared by the eight aspect-ratio-only uniform blocks above; each just needs a different
  /// uniform/buffer name
  private static GpuBuffer buildAspectOnlyUniform(RenderPass renderPass, String uniformName, String bufferLabel) {

    Window window = Minecraft.getInstance().getWindow();
    float aspect = (float) window.getWidth() / (float) window.getHeight();

    try (MemoryStack stack = MemoryStack.stackPush()) {
      Std140Builder builder = Std140Builder.onStack(stack, 16)
          .putFloat(aspect);

      GpuBuffer buffer = RenderSystem.getDevice().createBuffer(() -> bufferLabel, GpuBuffer.USAGE_UNIFORM, builder.get());
      renderPass.setUniform(uniformName, buffer);
      return buffer;
    }
  }

  /// true when plasma_shades is worn directly, or prism_shades is worn and currently cycled to
  /// the plasma option. Takes the real head-slot stack rather than just the Item, since the cycle
  /// position lives on the stack's own PRISM_CYCLE_INDEX component
  public static boolean isPlasmaSelected(ItemStack headStack) {

    Item headItem = headStack.getItem();
    if (headItem == ModItems.PLASMA_SHADES) {
      return true;
    }

    if (headItem != ModItems.PRISM_SHADES) {
      return false;
    }

    int index = headStack.getOrDefault(ModComponents.PRISM_CYCLE_INDEX, 0);
    return PLASMA_SHADES_MARKER.equals(effects().get(index).id());
  }

  /// shows the current prism_shades effect name while worn, e.g. "Prism: Thermal"
  public static void doHudOverlay(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {

    Minecraft mc = Minecraft.getInstance();
    if (mc.options.hideGui || mc.player == null) {
      return;
    }

    ItemStack headStack = resolveWornShades(mc.player);
    if (headStack.getItem() != ModItems.PRISM_SHADES) {
      return;
    }

    int index = headStack.getOrDefault(ModComponents.PRISM_CYCLE_INDEX, 0);
    String effectName = effects().get(index).displayName();
    graphics.text(mc.font, "Prism: " + effectName, 5, 5, 0xAAFFFFFF);
  }

  public static int prismCycleSize() {
    return effects().size();
  }
}

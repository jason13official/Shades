package io.github.jason13official.shades;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
  public static final Identifier MATRIX_SHADES_POST_EFFECT = Shades.identifier("matrix_shades");
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

  /// every effect prism_shades can cycle through; `null` at index 0 is the "off" state
  private static final List<Identifier> PRISM_CYCLE = Arrays.asList(
      null,
      SHADES_VISOR_POST_EFFECT,
      CREEPER_SHADES_POST_EFFECT,
      INVERT_SHADES_POST_EFFECT,
      SPIDER_SHADES_POST_EFFECT,
      BLUR_SHADES_POST_EFFECT,
      NIGHT_VISION_SHADES_POST_EFFECT,
      THERMAL_SHADES_POST_EFFECT,
      MATRIX_SHADES_POST_EFFECT,
      RECEIPT_SHADES_POST_EFFECT,
      HALFTONE_SHADES_POST_EFFECT,
      LEGO_SHADES_POST_EFFECT,
      FLUTED_GLASS_SHADES_POST_EFFECT,
      CHROMATIC_SHADES_POST_EFFECT,
      XRAY_SHADES_POST_EFFECT,
      FISHEYE_SHADES_POST_EFFECT,
      NEON_SHADES_POST_EFFECT,
      KALEIDOSCOPE_SHADES_POST_EFFECT,
      STATIC_SHADES_MARKER,
      SONAR_SHADES_MARKER,
      GLITCH_SHADES_MARKER,
      RAIN_SHADES_MARKER,
      CURSOR_SHADES_MARKER,
      VERTIGO_SHADES_MARKER,
      PREDATOR_SHADES_POST_EFFECT,
      FRACTAL_SHADES_POST_EFFECT,
      ANIMATED_GLASS_SHADES_MARKER,
      MOLTEN_GLASS_SHADES_MARKER,
      PLASMA_SHADES_MARKER);

  /// display names for PRISM_CYCLE, same order/indices -> shown by doHudOverlay
  private static final List<String> PRISM_NAMES = Arrays.asList(
      "Off", "Basic", "Creeper", "Negative", "Spider", "Blurry", "Night Vision", "Thermal", "Matrix",
      "Receipt", "Halftone", "Lego", "Fluted Glass", "Chromatic", "X-Ray", "Fisheye", "Neon", "Kaleidoscope",
      "Static", "Sonar", "Glitch", "Rain", "Cursor", "Vertigo", "Predator", "Fractal",
      "Animated Glass", "Molten Glass", "Plasma");

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

  private static Map<Item, Identifier> postEffectsByItem() {

    if (postEffectsByItem == null) {
      postEffectsByItem = Map.ofEntries(
          Map.entry(ModItems.BASIC_SHADES, SHADES_VISOR_POST_EFFECT),
          Map.entry(ModItems.CREEPER_SHADES, CREEPER_SHADES_POST_EFFECT),
          Map.entry(ModItems.INVERT_SHADES, INVERT_SHADES_POST_EFFECT),
          Map.entry(ModItems.SPIDER_SHADES, SPIDER_SHADES_POST_EFFECT),
          Map.entry(ModItems.BLUR_SHADES, BLUR_SHADES_POST_EFFECT),
          Map.entry(ModItems.NIGHT_VISION_SHADES, NIGHT_VISION_SHADES_POST_EFFECT),
          Map.entry(ModItems.THERMAL_SHADES, THERMAL_SHADES_POST_EFFECT),
          Map.entry(ModItems.MATRIX_SHADES, MATRIX_SHADES_POST_EFFECT),
          Map.entry(ModItems.RECEIPT_SHADES, RECEIPT_SHADES_POST_EFFECT),
          Map.entry(ModItems.HALFTONE_SHADES, HALFTONE_SHADES_POST_EFFECT),
          Map.entry(ModItems.LEGO_SHADES, LEGO_SHADES_POST_EFFECT),
          Map.entry(ModItems.FLUTED_GLASS_SHADES, FLUTED_GLASS_SHADES_POST_EFFECT),
          Map.entry(ModItems.CHROMATIC_SHADES, CHROMATIC_SHADES_POST_EFFECT),
          Map.entry(ModItems.XRAY_SHADES, XRAY_SHADES_POST_EFFECT),
          Map.entry(ModItems.FISHEYE_SHADES, FISHEYE_SHADES_POST_EFFECT),
          Map.entry(ModItems.NEON_SHADES, NEON_SHADES_POST_EFFECT),
          Map.entry(ModItems.KALEIDOSCOPE_SHADES, KALEIDOSCOPE_SHADES_POST_EFFECT),
          Map.entry(ModItems.STATIC_SHADES, STATIC_SHADES_MARKER),
          Map.entry(ModItems.SONAR_SHADES, SONAR_SHADES_MARKER),
          Map.entry(ModItems.GLITCH_SHADES, GLITCH_SHADES_MARKER),
          Map.entry(ModItems.RAIN_SHADES, RAIN_SHADES_MARKER),
          Map.entry(ModItems.CURSOR_SHADES, CURSOR_SHADES_MARKER),
          Map.entry(ModItems.VERTIGO_SHADES, VERTIGO_SHADES_MARKER),
          Map.entry(ModItems.PREDATOR_SHADES, PREDATOR_SHADES_POST_EFFECT),
          Map.entry(ModItems.FRACTAL_SHADES, FRACTAL_SHADES_POST_EFFECT),
          Map.entry(ModItems.ANIMATED_GLASS_SHADES, ANIMATED_GLASS_SHADES_MARKER),
          Map.entry(ModItems.MOLTEN_GLASS_SHADES, MOLTEN_GLASS_SHADES_MARKER));
    }

    return postEffectsByItem;
  }

  public static void doGameRender(CrossFrameResourcePool resourcePool) {

    Minecraft mc = Minecraft.getInstance();
    LocalPlayer player = mc.player;
    if (player == null) {
      return;
    }

    ItemStack headStack = player.getItemBySlot(EquipmentSlot.HEAD);
    Item headItem = headStack.getItem();

    Identifier postEffectId;
    if (headItem == ModItems.PRISM_SHADES) {

      while (CYCLE_PRISM_KEY.consumeClick()) {
        if (c2s != null) {
          c2s.accept(new CyclePrismC2SPacket());
        }
      }

      int index = headStack.getOrDefault(ModComponents.PRISM_CYCLE_INDEX, 0);
      postEffectId = PRISM_CYCLE.get(index);
    } else {
      postEffectId = postEffectsByItem().get(headItem);
    }

    if (postEffectId == null || postEffectId.equals(PLASMA_SHADES_MARKER)) {
      return;
    }

    if (postEffectId.equals(STATIC_SHADES_MARKER)) {
      ShadesLiveVision.process(resourcePool, ShadesRenderPipelines.STATIC_TV, null);
      return;
    }
    if (postEffectId.equals(SONAR_SHADES_MARKER)) {
      ShadesLiveVision.process(resourcePool, ShadesRenderPipelines.SONAR, getSonarDepthCapture(), ShadesClient::buildSonarCameraRayUniform);
      return;
    }
    if (postEffectId.equals(GLITCH_SHADES_MARKER)) {
      ShadesLiveVision.process(resourcePool, ShadesRenderPipelines.GLITCH, null);
      return;
    }
    if (postEffectId.equals(RAIN_SHADES_MARKER)) {
      ShadesLiveVision.process(resourcePool, ShadesRenderPipelines.RAIN, null);
      return;
    }
    if (postEffectId.equals(CURSOR_SHADES_MARKER)) {
      ShadesLiveVision.process(resourcePool, ShadesRenderPipelines.CURSOR, null, ShadesClient::buildCursorUniform);
      return;
    }
    if (postEffectId.equals(VERTIGO_SHADES_MARKER)) {
      ShadesLiveVision.process(resourcePool, ShadesRenderPipelines.VERTIGO, null, ShadesClient::buildMotionUniform);
      return;
    }
    if (postEffectId.equals(ANIMATED_GLASS_SHADES_MARKER)) {
      ShadesLiveVision.process(resourcePool, ShadesRenderPipelines.ANIMATED_GLASS, null);
      return;
    }
    if (postEffectId.equals(MOLTEN_GLASS_SHADES_MARKER)) {
      ShadesLiveVision.process(resourcePool, ShadesRenderPipelines.MOLTEN_GLASS, null, ShadesClient::buildGlassUniform);
      return;
    }

    PostChain postChain = mc.getShaderManager().getPostChain(postEffectId, LevelTargetBundle.MAIN_TARGETS);
    if (postChain != null) {
      postChain.process(mc.getMainRenderTarget(), resourcePool);
    }
  }

  /// persistent (not scratch-pool) copy of the real depth buffer, refreshed via
  /// GameRendererMixin#shades$captureWorldDepth right after the world/entities finish rendering -
  /// see that mixin's doc comment for why mainTarget's own depth can't be read directly by the
  /// time doGameRender runs later in the same frame
  private static RenderTarget sonarDepthCapture;

  public static void captureWorldDepth() {

    Minecraft mc = Minecraft.getInstance();
    RenderTarget mainTarget = mc.getMainRenderTarget();
    if (!mainTarget.useDepth) {
      return;
    }

    if (sonarDepthCapture == null || sonarDepthCapture.width != mainTarget.width || sonarDepthCapture.height != mainTarget.height) {
      if (sonarDepthCapture != null) {
        sonarDepthCapture.destroyBuffers();
      }
      sonarDepthCapture = new TextureTarget(null, mainTarget.width, mainTarget.height, true);
    }

    RenderSystem.getDevice().createCommandEncoder().copyTextureToTexture(
        mainTarget.getDepthTexture(), sonarDepthCapture.getDepthTexture(), 0, 0, 0, 0, 0, mainTarget.width, mainTarget.height);
  }

  private static GpuTextureView getSonarDepthCapture() {
    return sonarDepthCapture != null ? sonarDepthCapture.getDepthTextureView() : null;
  }

  /// world position the sonar ping currently expands from -> re-anchored once per ping cycle (see
  /// buildSonarCameraRayUniform) instead of sliding with the player every frame, same "propagate
  /// from a fixed point" idea orbital_railgun uses for its strike position
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

  /// smoothed 0..1 factors driving vertigo.fsh -> raw speed/turn are noisy per-frame, so each is
  /// eased toward its target every frame rather than applied directly (same "ease toward a target
  /// instead of snapping" idea as Adaptive-Armor's SprintMomentum buildup, just a plain
  /// exponential filter here since this is purely a local visual, not synced game state)
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

    // normalize against roughly sprint-speed/fast-turn baselines, clamp so the shader always gets a 0..1 range
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

  /// smoothed yaw/pitch delta driving molten_glass.fsh's blob sway - same eased-toward-target
  /// idea as vertigo's smoothedSpeed/smoothedTurn, kept as separate fields so switching between
  /// vertigo_shades and molten_glass_shades (e.g. mid-Prism-cycle) can't cause one effect's
  /// smoothing state to jump-start the other's
  private static float smoothedSwayYaw = 0.0f;
  private static float smoothedSwayPitch = 0.0f;
  private static float previousGlassYaw = Float.NaN;
  private static float previousGlassPitch = Float.NaN;

  /// pushes the real window aspect ratio (see molten_glass.fsh's GlassConfig doc) plus a
  /// smoothed screen-space sway offset opposite the direction the camera is currently swinging -
  /// the blobs lag behind a camera turn like they've got real inertia, then drift back to center
  /// as the turn eases off, same buildup/decay-by-easing idea as vertigo's buildMotionUniform
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

  /// true when plasma_shades is worn directly, OR prism_shades is worn and currently cycled to
  /// the plasma option -> used by ShadesVisorLayer/ShadesPlasmaEffect to pick the live
  /// GameTime-driven plasma RenderType instead of their normal per-item behavior.
  ///
  /// takes the real head-slot stack (not just the Item) since the cycle position now lives on
  /// [ModComponents#PRISM_CYCLE_INDEX] -> correct for other tracked players too, not just us
  public static boolean isPlasmaSelected(ItemStack headStack) {

    Item headItem = headStack.getItem();
    if (headItem == ModItems.PLASMA_SHADES) {
      return true;
    }

    if (headItem != ModItems.PRISM_SHADES) {
      return false;
    }

    int index = headStack.getOrDefault(ModComponents.PRISM_CYCLE_INDEX, 0);
    return PLASMA_SHADES_MARKER.equals(PRISM_CYCLE.get(index));
  }

  /// shows the current prism_shades effect name while worn, e.g. "Prism: Thermal"
  public static void doHudOverlay(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {

    Minecraft mc = Minecraft.getInstance();
    if (mc.options.hideGui || mc.player == null) {
      return;
    }

    ItemStack headStack = mc.player.getItemBySlot(EquipmentSlot.HEAD);
    if (headStack.getItem() != ModItems.PRISM_SHADES) {
      return;
    }

    int index = headStack.getOrDefault(ModComponents.PRISM_CYCLE_INDEX, 0);
    String effectName = PRISM_NAMES.get(index);
    graphics.text(mc.font, "Prism: " + effectName, 5, 5, 0xAAFFFFFF);
  }

  public static int prismCycleSize() {
    return PRISM_CYCLE.size();
  }
}

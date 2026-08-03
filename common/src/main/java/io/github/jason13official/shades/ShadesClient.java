package io.github.jason13official.shades;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.resource.CrossFrameResourcePool;
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
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

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

  /// sentinel value for PRISM_CYCLE/postEffectsByItem() - plasma_shades doesn't go through the
  /// PostChain/post_effect system at all (see ShadesRenderPipelines for why), so there's no real
  /// post_effect JSON behind this id. doGameRender recognizes it and skips PostChain processing;
  /// the actual visual comes from ShadesVisorLayer's lens model + ShadesPlasmaEffect's camera quad
  public static final Identifier PLASMA_SHADES_MARKER = Shades.identifier("plasma_shades");

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
      PLASMA_SHADES_MARKER);

  /// display names for PRISM_CYCLE, same order/indices - shown by doHudOverlay
  private static final List<String> PRISM_NAMES = Arrays.asList(
      "Off", "Basic", "Creeper", "Negative", "Spider", "Blurry", "Night Vision", "Thermal", "Matrix",
      "Receipt", "Halftone", "Lego", "Fluted Glass", "Chromatic", "X-Ray", "Fisheye", "Plasma");

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
          Map.entry(ModItems.FISHEYE_SHADES, FISHEYE_SHADES_POST_EFFECT));
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

    PostChain postChain = mc.getShaderManager().getPostChain(postEffectId, LevelTargetBundle.MAIN_TARGETS);
    if (postChain != null) {
      postChain.process(mc.getMainRenderTarget(), resourcePool);
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

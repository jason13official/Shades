package io.github.jason13official.shades;

import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import io.github.jason13official.shades.impl.common.registry.ModItems;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;

public class ShadesClient {

  public static final Identifier SHADES_VISOR_POST_EFFECT = Shades.identifier("shades_visor");
  public static final Identifier CREEPER_SHADES_POST_EFFECT = Shades.identifier("creeper_shades");
  public static final Identifier INVERT_SHADES_POST_EFFECT = Shades.identifier("invert_shades");
  public static final Identifier SPIDER_SHADES_POST_EFFECT = Shades.identifier("spider_shades");
  public static final Identifier BLUR_SHADES_POST_EFFECT = Shades.identifier("blur_shades");

  /// which vanilla-derived post-processing chain plays for which pair of glasses; filled in once
  /// ModItems is populated, since the fields aren't set yet at class-init time
  private static Map<Item, Identifier> postEffectsByItem;

  public static void init() {
  }

  private static Map<Item, Identifier> postEffectsByItem() {

    if (postEffectsByItem == null) {
      postEffectsByItem = Map.of(
          ModItems.BASIC_SHADES, SHADES_VISOR_POST_EFFECT,
          ModItems.CREEPER_SHADES, CREEPER_SHADES_POST_EFFECT,
          ModItems.INVERT_SHADES, INVERT_SHADES_POST_EFFECT,
          ModItems.SPIDER_SHADES, SPIDER_SHADES_POST_EFFECT,
          ModItems.BLUR_SHADES, BLUR_SHADES_POST_EFFECT);
    }

    return postEffectsByItem;
  }

  public static void doGameRender(CrossFrameResourcePool resourcePool) {

    Minecraft mc = Minecraft.getInstance();
    LocalPlayer player = mc.player;
    if (player == null) {
      return;
    }

    Item headItem = player.getItemBySlot(EquipmentSlot.HEAD).getItem();
    Identifier postEffectId = postEffectsByItem().get(headItem);
    if (postEffectId == null) {
      return;
    }

    PostChain postChain = mc.getShaderManager().getPostChain(postEffectId, LevelTargetBundle.MAIN_TARGETS);
    if (postChain != null) {
      postChain.process(mc.getMainRenderTarget(), resourcePool);
    }
  }
}

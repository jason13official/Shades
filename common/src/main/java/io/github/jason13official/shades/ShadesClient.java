package io.github.jason13official.shades;

import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import io.github.jason13official.shades.impl.common.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;

public class ShadesClient {

  public static final Identifier SHADES_VISOR_POST_EFFECT = Shades.identifier("shades_visor");

  public static void init() {
  }

  public static void doGameRender(CrossFrameResourcePool resourcePool) {

    // todo temporary disable for debugging/hot reloading
    // if (true) return;

    Minecraft mc = Minecraft.getInstance();
    LocalPlayer player = mc.player;
    if (player == null || !player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.BASIC_SHADES)) {
      return;
    }

    PostChain postChain = mc.getShaderManager().getPostChain(SHADES_VISOR_POST_EFFECT, LevelTargetBundle.MAIN_TARGETS);
    if (postChain != null) {
      postChain.process(mc.getMainRenderTarget(), resourcePool);
    }
  }
}
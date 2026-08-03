package io.github.jason13official.shades;

import io.github.jason13official.shades.impl.client.ShadesPlasmaEffect;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;

public class ShadesClientFabric implements ClientModInitializer {

  @Override
  public void onInitializeClient() {

    ShadesClient.init();
    ShadesClient.c2s = ClientPlayNetworking::send;
    KeyMappingHelper.registerKeyMapping(ShadesClient.CYCLE_PRISM_KEY);
    HudElementRegistry.addLast(Shades.identifier("prism_overlay"), ShadesClient::doHudOverlay);

    LevelRenderEvents.COLLECT_SUBMITS.register(context -> ShadesPlasmaEffect.submit(context.poseStack(), context.submitNodeCollector()));
  }
}

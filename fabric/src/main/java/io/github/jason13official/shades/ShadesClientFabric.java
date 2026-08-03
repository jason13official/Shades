package io.github.jason13official.shades;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;

public class ShadesClientFabric implements ClientModInitializer {

  @Override
  public void onInitializeClient() {

    ShadesClient.init();
    KeyMappingHelper.registerKeyMapping(ShadesClient.CYCLE_PRISM_KEY);
    HudElementRegistry.addLast(Shades.identifier("prism_overlay"), ShadesClient::doHudOverlay);
  }
}

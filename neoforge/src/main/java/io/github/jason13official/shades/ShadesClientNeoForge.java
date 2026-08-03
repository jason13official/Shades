package io.github.jason13official.shades;

import java.util.function.Consumer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

public class ShadesClientNeoForge {

  public ShadesClientNeoForge(final IEventBus modEventBus) {

    modEventBus.addListener((Consumer<FMLClientSetupEvent>) event -> ShadesClient.init());
    modEventBus.addListener((Consumer<RegisterKeyMappingsEvent>) event -> event.register(ShadesClient.CYCLE_PRISM_KEY));
    modEventBus.addListener((Consumer<RegisterGuiLayersEvent>) event -> event.registerAboveAll(Shades.identifier("prism_overlay"), ShadesClient::doHudOverlay));
  }
}

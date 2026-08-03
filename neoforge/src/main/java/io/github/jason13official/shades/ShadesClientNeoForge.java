package io.github.jason13official.shades;

import io.github.jason13official.shades.impl.client.ShadesPlasmaEffect;
import io.github.jason13official.shades.impl.client.ShadesRenderPipelines;
import java.util.function.Consumer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.NeoForge;

public class ShadesClientNeoForge {

  public ShadesClientNeoForge(final IEventBus modEventBus) {

    ShadesClient.c2s = ClientPacketDistributor::sendToServer;

    modEventBus.addListener((Consumer<FMLClientSetupEvent>) event -> ShadesClient.init());
    modEventBus.addListener((Consumer<RegisterKeyMappingsEvent>) event -> event.register(ShadesClient.CYCLE_PRISM_KEY));
    modEventBus.addListener((Consumer<RegisterGuiLayersEvent>) event -> event.registerAboveAll(Shades.identifier("prism_overlay"), ShadesClient::doHudOverlay));
    modEventBus.addListener((Consumer<RegisterRenderPipelinesEvent>) event -> {
      event.registerPipeline(ShadesRenderPipelines.PLASMA);
      event.registerPipeline(ShadesRenderPipelines.STATIC_TV);
      event.registerPipeline(ShadesRenderPipelines.SONAR);
      event.registerPipeline(ShadesRenderPipelines.GLITCH);
      event.registerPipeline(ShadesRenderPipelines.RAIN);
      event.registerPipeline(ShadesRenderPipelines.CURSOR);
      event.registerPipeline(ShadesRenderPipelines.VERTIGO);
      event.registerPipeline(ShadesRenderPipelines.ANIMATED_GLASS);
      event.registerPipeline(ShadesRenderPipelines.MOLTEN_GLASS);
      event.registerPipeline(ShadesRenderPipelines.FIRE);
    });

    NeoForge.EVENT_BUS.addListener((Consumer<SubmitCustomGeometryEvent>) event -> {
      ShadesPlasmaEffect.submit(event.getPoseStack(), event.getSubmitNodeCollector());
    });
  }
}

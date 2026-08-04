package io.github.jason13official.shades;

import io.github.jason13official.shades.impl.common.registry.ModBlocks;
import io.github.jason13official.shades.impl.common.registry.ModComponents;
import io.github.jason13official.shades.impl.common.registry.ModEntities;
import io.github.jason13official.shades.impl.common.registry.ModItems;
import io.github.jason13official.shades.impl.common.registry.ModMenus;
import io.github.jason13official.shades.impl.common.registry.ModParticles;
import io.github.jason13official.shades.impl.common.registry.ModTabs;
import io.github.jason13official.shades.impl.common.registry.ModTiles;
import io.github.jason13official.shades.impl.network.CyclePrismC2SPacket;
import io.github.jason13official.shades.platform.CuriosCompat;
import io.github.jason13official.shades.platform.Services;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(Constants.MOD_ID)
public class ShadesNeoForge {

  public static IEventBus EVENT_BUS;

  public ShadesNeoForge(final IEventBus modEventBus) {

    EVENT_BUS = modEventBus;

    Shades.init();
    setupNetworking(modEventBus);

    bind(Registries.BLOCK, ModBlocks::register);
    bind(Registries.ENTITY_TYPE, ModEntities::register);
    bind(Registries.ITEM, ModItems::register);
    bind(Registries.PARTICLE_TYPE, ModParticles::register);
    bind(Registries.BLOCK_ENTITY_TYPE, ModTiles::register);
    bind(Registries.MENU, ModMenus::register);
    bind(Registries.CREATIVE_MODE_TAB, ModTabs::register);
    bind(Registries.DATA_COMPONENT_TYPE, ModComponents::register);

    // stub for stuff that must happen after game objects are registered
    // EVENT_BUS.addListener((Consumer<FMLCommonSetupEvent>) event -> {});

    EVENT_BUS.addListener((Consumer<BuildCreativeModeTabContentsEvent>) event -> {
      if (event.getTab().equals(ModTabs.SHADES)) {
        ModTabs.addItemsToTab(event::accept);
      }
    });

    NeoForge.EVENT_BUS.addListener((Consumer<AddServerReloadListenersEvent>) event -> {
      event.addListener(Shades.identifier(Constants.MOD_ID), new ResourceReloadListener());
    });

    // optional Curios accessory-slot support -> the listener itself, and every
    // top.theillusivec4.curios.* reference inside it, only ever loads if Curios is installed
    if (Services.PLATFORM.isModLoaded("curios")) {
      EVENT_BUS.addListener((Consumer<RegisterCapabilitiesEvent>) CuriosCompat::registerCapability);
    }

    if (FMLLoader.getCurrent().getDist() == Dist.CLIENT) {
      new ShadesClientNeoForge(EVENT_BUS);
    }
  }

  private void setupNetworking(IEventBus modEventBus) {

    modEventBus.addListener((Consumer<RegisterPayloadHandlersEvent>) event -> {
      PayloadRegistrar registrar = event.registrar(Constants.MOD_ID);
      registrar.playToServer(
          CyclePrismC2SPacket.TYPE,
          CyclePrismC2SPacket.STREAM_CODEC,
          (pkt, ctx) -> CyclePrismC2SPacket.handle(pkt, (ServerPlayer) ctx.player()));
    });
  }

  public <T> void bind(ResourceKey<Registry<T>> registryKey, Consumer<BiConsumer<T, Identifier>> source) {

    EVENT_BUS.addListener((Consumer<RegisterEvent>) event -> {
      if (registryKey.equals(event.getRegistryKey())) {
        source.accept((t, rl) -> event.register(registryKey, rl, () -> t));
      }
    });
  }

  public static class ResourceReloadListener extends SimplePreparableReloadListener<Void> {

    @Override
    public String getName() {
      return Shades.identifier(Constants.MOD_ID).toString();
    }

    @Override
    protected void apply(Void unused, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
      // ModConfig.load(Services.PLATFORM.getConfigDirectory());
    }

    @Override
    protected Void prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
      return null;
    }
  }
}
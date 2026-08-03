package io.github.jason13official.shades;

import io.github.jason13official.shades.impl.common.registry.ModBlocks;
import io.github.jason13official.shades.impl.common.registry.ModEntities;
import io.github.jason13official.shades.impl.common.registry.ModItems;
import io.github.jason13official.shades.impl.common.registry.ModMenus;
import io.github.jason13official.shades.impl.common.registry.ModParticles;
import io.github.jason13official.shades.impl.common.registry.ModTabs;
import io.github.jason13official.shades.impl.common.registry.ModTiles;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

public class ShadesFabric implements ModInitializer {

  @Override
  public void onInitialize() {

    Shades.init();

    bind(BuiltInRegistries.BLOCK, ModBlocks::register);
    bind(BuiltInRegistries.ENTITY_TYPE, ModEntities::register);
    bind(BuiltInRegistries.ITEM, ModItems::register);
    bind(BuiltInRegistries.PARTICLE_TYPE, ModParticles::register);
    bind(BuiltInRegistries.BLOCK_ENTITY_TYPE, ModTiles::register);
    bind(BuiltInRegistries.MENU, ModMenus::register);
    bind(BuiltInRegistries.CREATIVE_MODE_TAB, ModTabs::register);

    CreativeModeTabEvents.MODIFY_OUTPUT_ALL.register((tab, output) -> {
      if (tab.equals(ModTabs.SHADES)) {
        ModTabs.addItemsToTab(output::accept);
      }
    });

    ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(Shades.identifier(Constants.MOD_ID), new ResourceReloadListener());
  }

  public <T> void bind(Registry<T> registry, Consumer<BiConsumer<T, Identifier>> source) {

    source.accept((t, rl) -> Registry.register(registry, rl, t));
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

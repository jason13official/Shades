package io.github.jason13official.shades.platform;

import io.github.jason13official.shades.platform.services.IPlatformHelper;
import java.nio.file.Path;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CreativeModeTab.Builder;
import net.minecraft.world.item.ItemStack;

public class FabricPlatformHelper implements IPlatformHelper {

  @Override
  public String getPlatformName() {

    return "Fabric";
  }

  @Override
  public boolean isModLoaded(String modId) {

    return FabricLoader.getInstance().isModLoaded(modId);
  }

  @Override
  public boolean isDevelopmentEnvironment() {

    return FabricLoader.getInstance().isDevelopmentEnvironment();
  }

  @Override
  public Path getGameDirectory() {

    return FabricLoader.getInstance().getGameDir();
  }

  @Override
  public Builder tabBuilder() {

    return FabricCreativeModeTab.builder();
  }

  @Override
  public ItemStack getAccessoryShadesItem(LivingEntity entity) {

    return isModLoaded("trinkets") ? TrinketsCompat.get(entity) : ItemStack.EMPTY;
  }

  @Override
  public void setAccessoryShadesItem(LivingEntity entity, ItemStack stack) {

    if (isModLoaded("trinkets")) {
      TrinketsCompat.set(entity, stack);
    }
  }
}

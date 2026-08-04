package io.github.jason13official.shades.platform;

import io.github.jason13official.shades.platform.services.IPlatformHelper;
import java.nio.file.Path;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTab.Builder;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;

public class NeoForgePlatformHelper implements IPlatformHelper {

  @Override
  public String getPlatformName() {

    return "NeoForge";
  }

  @Override
  public boolean isModLoaded(String modId) {

    return ModList.get().isLoaded(modId);
  }

  @Override
  public boolean isDevelopmentEnvironment() {

    return !FMLLoader.getCurrent().isProduction();
  }

  @Override
  public Path getGameDirectory() {

    return FMLLoader.getCurrent().getGameDir();
  }

  @Override
  public Builder tabBuilder() {

    return CreativeModeTab.builder();
  }

  @Override
  public ItemStack getAccessoryShadesItem(LivingEntity entity) {

    return isModLoaded("curios") ? CuriosCompat.get(entity) : ItemStack.EMPTY;
  }

  @Override
  public void setAccessoryShadesItem(LivingEntity entity, ItemStack stack) {

    if (isModLoaded("curios")) {
      CuriosCompat.set(entity, stack);
    }
  }
}
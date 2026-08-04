package io.github.jason13official.shades.platform.services;

import java.nio.file.Path;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public interface IPlatformHelper {

  /**
   * Gets the name of the current platform
   *
   * @return The name of the current platform.
   */
  String getPlatformName();

  /**
   * Checks if a mod with the given id is loaded.
   *
   * @param modId The mod to check if it is loaded.
   * @return True if the mod is loaded, false otherwise.
   */
  boolean isModLoaded(String modId);

  /**
   * Check if the game is currently in a development environment.
   *
   * @return True if in a development environment, false otherwise.
   */
  boolean isDevelopmentEnvironment();

  /**
   * Gets the name of the environment type as a string.
   *
   * @return The name of the environment type.
   */
  default String getEnvironmentName() {

    return isDevelopmentEnvironment() ? "development" : "production";
  }

  Path getGameDirectory();

  default Path getConfigDirectory() {

    return getGameDirectory().resolve("config");
  }

  CreativeModeTab.Builder tabBuilder();

  /**
   * The shades item currently worn in an accessory slot (Trinkets on Fabric, Curios on
   * NeoForge), independent of the real vanilla head slot. Empty if the entity has none equipped,
   * or if the relevant accessory mod isn't installed.
   */
  ItemStack getAccessoryShadesItem(LivingEntity entity);

  /**
   * Writes {@code stack} back into whichever accessory slot currently holds a shades item,
   * through that mod's own slot API (so the change syncs correctly). No-op if the entity has no
   * shades item equipped as an accessory, or if the relevant accessory mod isn't installed.
   */
  void setAccessoryShadesItem(LivingEntity entity, ItemStack stack);
}
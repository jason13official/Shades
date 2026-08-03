package io.github.jason13official.shades;

import io.github.jason13official.shades.impl.common.registry.ModItems;
import java.util.function.Consumer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ItemLike;

public class Shades {

  /// before game objects are registered
  public static void init() {
  }

  public static Identifier identifier(final String path) {
    return Identifier.fromNamespaceAndPath(Constants.MOD_ID, path);
  }

  public static void addItemsToTab(Consumer<ItemLike> consumer) {
    consumer.accept(ModItems.BASIC_SHADES);
    consumer.accept(ModItems.CREEPER_SHADES);
    consumer.accept(ModItems.INVERT_SHADES);
    consumer.accept(ModItems.SPIDER_SHADES);
    consumer.accept(ModItems.BLUR_SHADES);
    consumer.accept(ModItems.NIGHT_VISION_SHADES);
    consumer.accept(ModItems.THERMAL_SHADES);
    consumer.accept(ModItems.MATRIX_SHADES);
    consumer.accept(ModItems.PRISM_SHADES);
  }
}
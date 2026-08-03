package io.github.jason13official.shades.impl.common.registry;

import io.github.jason13official.shades.Constants;
import io.github.jason13official.shades.Shades;
import io.github.jason13official.shades.platform.Services;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class ModTabs {

  public static CreativeModeTab SHADES;

  public static void register(BiConsumer<CreativeModeTab, Identifier> consumer) {

    SHADES = Services.PLATFORM.tabBuilder()
        .icon(() -> new ItemStack(ModItems.BASIC_SHADES))
        .title(Component.translatable("itemGroup.shades"))
        .build();

    consumer.accept(SHADES, Shades.identifier(Constants.MOD_ID));
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
    consumer.accept(ModItems.RECEIPT_SHADES);
    consumer.accept(ModItems.HALFTONE_SHADES);
    consumer.accept(ModItems.LEGO_SHADES);
    consumer.accept(ModItems.FLUTED_GLASS_SHADES);
    consumer.accept(ModItems.CHROMATIC_SHADES);
    consumer.accept(ModItems.XRAY_SHADES);
    consumer.accept(ModItems.FISHEYE_SHADES);
    consumer.accept(ModItems.STATIC_SHADES);
    consumer.accept(ModItems.SONAR_SHADES);
    consumer.accept(ModItems.GLITCH_SHADES);
    consumer.accept(ModItems.PLASMA_SHADES);
  }
}

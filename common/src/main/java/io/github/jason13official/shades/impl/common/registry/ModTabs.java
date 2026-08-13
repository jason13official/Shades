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
    consumer.accept(ModItems.BRIGHTSAND);
    consumer.accept(ModItems.BRIGHTGLASS);
    consumer.accept(ModItems.SILIMATIC_EXPOSER);
    ModItems.allShades().forEach(consumer);
  }
}

package io.github.jason13official.shades.impl.common.registry;

import io.github.jason13official.shades.Shades;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;

public class ModItems {

  public static Item BASIC_SHADES;

  public static void register(BiConsumer<Item, Identifier> consumer) {

    BASIC_SHADES = register("basic_shades", consumer);
  }

  /// basic item and properties
  private static Item register(String id, BiConsumer<Item, Identifier> consumer) {

    Properties properties = new Properties();
    properties.setId(ResourceKey.create(Registries.ITEM, Shades.identifier(id)));
    Item item = new Item(properties);

    consumer.accept(item, Shades.identifier(id));
    return item;
  }

  /// basic item and custom properties
  private static Item register(String id, Properties properties, BiConsumer<Item, Identifier> consumer) {

    properties.setId(ResourceKey.create(Registries.ITEM, Shades.identifier(id)));
    Item item = new Item(properties);

    consumer.accept(item, Shades.identifier(id));
    return item;
  }

  /// custom item and basic properties
  private static Item register(String id, Function<Properties, Item> constructor, BiConsumer<Item, Identifier> consumer) {

    Properties properties = new Properties();
    properties.setId(ResourceKey.create(Registries.ITEM, Shades.identifier(id)));
    Item item = constructor.apply(properties);

    consumer.accept(item, Shades.identifier(id));
    return item;
  }

  /// custom item and custom properties
  private static Item register(String id, Function<Properties, Item> constructor, Properties properties, BiConsumer<Item, Identifier> consumer) {

    properties.setId(ResourceKey.create(Registries.ITEM, Shades.identifier(id)));
    Item item = constructor.apply(properties);

    consumer.accept(item, Shades.identifier(id));
    return item;
  }
}

package io.github.jason13official.shades.impl.common.registry;

import io.github.jason13official.shades.Shades;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.equipment.Equippable;

public class ModItems {

  public static Item BASIC_SHADES;
  public static Item CREEPER_SHADES;
  public static Item INVERT_SHADES;
  public static Item SPIDER_SHADES;
  public static Item BLUR_SHADES;
  public static Item NIGHT_VISION_SHADES;
  public static Item THERMAL_SHADES;
  public static Item MATRIX_SHADES;
  public static Item PRISM_SHADES;
  public static Item RECEIPT_SHADES;
  public static Item HALFTONE_SHADES;
  public static Item LEGO_SHADES;
  public static Item FLUTED_GLASS_SHADES;
  public static Item PLASMA_SHADES;

  public static void register(BiConsumer<Item, Identifier> consumer) {

    BASIC_SHADES = registerShades("basic_shades", consumer);
    CREEPER_SHADES = registerShades("creeper_shades", consumer);
    INVERT_SHADES = registerShades("invert_shades", consumer);
    SPIDER_SHADES = registerShades("spider_shades", consumer);
    BLUR_SHADES = registerShades("blur_shades", consumer);
    NIGHT_VISION_SHADES = registerShades("night_vision_shades", consumer);
    THERMAL_SHADES = registerShades("thermal_shades", consumer);
    MATRIX_SHADES = registerShades("matrix_shades", consumer);
    PRISM_SHADES = registerShades("prism_shades", consumer);
    RECEIPT_SHADES = registerShades("receipt_shades", consumer);
    HALFTONE_SHADES = registerShades("halftone_shades", consumer);
    LEGO_SHADES = registerShades("lego_shades", consumer);
    FLUTED_GLASS_SHADES = registerShades("fluted_glass_shades", consumer);
    PLASMA_SHADES = registerShades("plasma_shades", consumer);
  }

  /// no ArmorMaterial/asset -> own visor cosmetic (client renderer) is the only thing rendered on the head,
  /// and ShadesClient#doGameRender picks a post-processing chain per item once worn
  private static Item registerShades(String id, BiConsumer<Item, Identifier> consumer) {

    return register(id,
        new Properties()
            .stacksTo(1)
            .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD).build()),
        consumer);
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

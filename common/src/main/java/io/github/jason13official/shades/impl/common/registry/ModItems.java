package io.github.jason13official.shades.impl.common.registry;

import io.github.jason13official.shades.Shades;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
  public static Item CHROMATIC_SHADES;
  public static Item XRAY_SHADES;
  public static Item FISHEYE_SHADES;
  public static Item STATIC_SHADES;
  public static Item SONAR_SHADES;
  public static Item GLITCH_SHADES;
  public static Item NEON_SHADES;
  public static Item KALEIDOSCOPE_SHADES;
  public static Item RAIN_SHADES;
  public static Item CURSOR_SHADES;
  public static Item VERTIGO_SHADES;
  public static Item PREDATOR_SHADES;
  public static Item FRACTAL_SHADES;
  public static Item ANIMATED_GLASS_SHADES;
  public static Item MOLTEN_GLASS_SHADES;
  public static Item FIRE_SHADES;
  public static Item GRID_SHADES;
  public static Item ORB_SHADES;
  public static Item WAVEFORM_SHADES;
  public static Item FLUID_SHADES;
  public static Item COPPER_SHADES;
  public static Item MIRAGE_SHADES;
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
    CHROMATIC_SHADES = registerShades("chromatic_shades", consumer);
    XRAY_SHADES = registerShades("xray_shades", consumer);
    FISHEYE_SHADES = registerShades("fisheye_shades", consumer);
    STATIC_SHADES = registerShades("static_shades", consumer);
    SONAR_SHADES = registerShades("sonar_shades", consumer);
    GLITCH_SHADES = registerShades("glitch_shades", consumer);
    NEON_SHADES = registerShades("neon_shades", consumer);
    KALEIDOSCOPE_SHADES = registerShades("kaleidoscope_shades", consumer);
    RAIN_SHADES = registerShades("rain_shades", consumer);
    CURSOR_SHADES = registerShades("cursor_shades", consumer);
    VERTIGO_SHADES = registerShades("vertigo_shades", consumer);
    PREDATOR_SHADES = registerShades("predator_shades", consumer);
    FRACTAL_SHADES = registerShades("fractal_shades", consumer);
    ANIMATED_GLASS_SHADES = registerShades("animated_glass_shades", consumer);
    MOLTEN_GLASS_SHADES = registerShades("molten_glass_shades", consumer);
    FIRE_SHADES = registerShades("fire_shades", consumer);
    GRID_SHADES = registerShades("grid_shades", consumer);
    ORB_SHADES = registerShades("orb_shades", consumer);
    WAVEFORM_SHADES = registerShades("waveform_shades", consumer);
    FLUID_SHADES = registerShades("fluid_shades", consumer);
    COPPER_SHADES = registerShades("copper_shades", consumer);
    MIRAGE_SHADES = registerShades("mirage_shades", consumer);
    PLASMA_SHADES = registerShades("plasma_shades", consumer);
  }

  /// every item registered via [#registerShades] below, in registration order; a single choke
  /// point so ModTabs/CustomHeadLayerMixin don't need their own duplicate item list
  private static final List<Item> ALL_SHADES = new ArrayList<>();

  /// each item's own registered id string, keyed back from the Item; lets ShadesVisorLayer derive
  /// `{id}_visor.png` texture paths without a duplicate map
  private static final Map<Item, String> IDS_BY_ITEM = new LinkedHashMap<>();

  /// no ArmorMaterial/asset; the custom visor cosmetic is the only thing rendered on the head,
  /// and ShadesClient picks a post-processing chain per item once worn
  private static Item registerShades(String id, BiConsumer<Item, Identifier> consumer) {

    Item item = register(id,
        new Properties()
            .stacksTo(1)
            .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD).build()),
        consumer);

    ALL_SHADES.add(item);
    IDS_BY_ITEM.put(item, id);
    return item;
  }

  public static List<Item> allShades() {
    return ALL_SHADES;
  }

  public static String idOf(Item item) {
    return IDS_BY_ITEM.get(item);
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

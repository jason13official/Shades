package io.github.jason13official.shades.platform;

import io.github.jason13official.shades.impl.common.registry.ModItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotResult;

/// isolated from [NeoForgePlatformHelper]/[io.github.jason13official.shades.ShadesNeoForge] so
/// this class -> and with it every `top.theillusivec4.curios.*` reference -> is only ever loaded
/// from behind an `isModLoaded("curios")` check; public only so ShadesNeoForge (a different
/// package) can pass a method reference to it from behind that same guard
public final class CuriosCompat {

  private CuriosCompat() {
  }

  public static void registerCapability(RegisterCapabilitiesEvent event) {
    event.registerItem(CuriosCapability.ITEM, (stack, context) -> new ShadesCurio(stack),
        ModItems.allShades().toArray(new Item[0]));
  }

  static ItemStack get(LivingEntity entity) {

    return CuriosApi.getCuriosInventory(entity)
        .flatMap(handler -> handler.findFirstCurio(stack -> ModItems.idOf(stack.getItem()) != null))
        .map(SlotResult::stack)
        .orElse(ItemStack.EMPTY);
  }

  static void set(LivingEntity entity, ItemStack stack) {

    CuriosApi.getCuriosInventory(entity).ifPresent(handler ->
        handler.findFirstCurio(existing -> ModItems.idOf(existing.getItem()) != null).ifPresent(result ->
            handler.setEquippedCurio(result.slotContext().identifier(), result.slotContext().index(), stack)));
  }
}

package io.github.jason13official.shades.platform;

import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.TrinketsApi;
import io.github.jason13official.shades.impl.common.registry.ModItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/// isolated from [FabricPlatformHelper] so this class -> and with it every `eu.pb4.trinkets.*`
/// reference -> is only ever loaded from behind an `isModLoaded("trinkets")` check
final class TrinketsCompat {

  private TrinketsCompat() {
  }

  static ItemStack get(LivingEntity entity) {

    return TrinketsApi.getAttachment(entity)
        .findFirst(stack -> ModItems.idOf(stack.getItem()) != null)
        .map(TrinketSlotAccess::get)
        .orElse(ItemStack.EMPTY);
  }

  static void set(LivingEntity entity, ItemStack stack) {

    TrinketsApi.getAttachment(entity)
        .findFirst(existing -> ModItems.idOf(existing.getItem()) != null)
        .ifPresent(slot -> slot.set(stack));
  }
}

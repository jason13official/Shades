package io.github.jason13official.shades.platform;

import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.type.capability.ICurio;

/// bare marker curio for every shades item -> the effect itself is driven entirely off the real
/// item in the slot (see [CuriosCompat#get]), nothing item-specific needed here
final class ShadesCurio implements ICurio {

  private final ItemStack stack;

  ShadesCurio(ItemStack stack) {
    this.stack = stack;
  }

  @Override
  public ItemStack getStack() {
    return stack;
  }
}

package io.github.jason13official.shades.mixin;

import io.github.jason13official.shades.api.client.renderer.ShadesRenderStateExtension;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/// adds storage for [ShadesRenderStateExtension] to the render state
/// @see ShadesRenderStateExtension
@Mixin(AvatarRenderState.class)
public abstract class AvatarRenderStateMixin implements ShadesRenderStateExtension {

  @Unique
  private ItemStack shades$headSlotItem = ItemStack.EMPTY;

  @Override
  public ItemStack shades$getHeadSlotItem() {
    return this.shades$headSlotItem;
  }

  @Override
  public void shades$setHeadSlotItem(ItemStack item) {
    this.shades$headSlotItem = item;
  }
}

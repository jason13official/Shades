package io.github.jason13official.shades.impl.client.renderer;

import net.minecraft.world.item.ItemStack;

/// implemented on [net.minecraft.client.renderer.entity.state.AvatarRenderState] <br />
/// via [io.github.jason13official.shades.mixin.AvatarRenderStateMixin] <br />
///
/// carries the actual head-slot ItemStack, captured from the entity during extraction,
/// independent of vanilla's own `headEquipment`
/// (which is `ItemStack.EMPTY` unless `HumanoidArmorLayer.shouldRender` is true -> i.e. unless the item has an `Equippable` asset,
/// which ours deliberately doesn't have)
public interface ShadesRenderStateExtension {

  ItemStack shades$getHeadSlotItem();

  void shades$setHeadSlotItem(ItemStack item);
}

package io.github.jason13official.shades.api.client.renderer;

import net.minecraft.world.item.ItemStack;

/// implemented on [net.minecraft.client.renderer.entity.state.AvatarRenderState] via
/// [io.github.jason13official.shades.mixin.AvatarRenderStateMixin]; carries the actual head-slot
/// ItemStack, captured during extraction, independent of vanilla's own `headEquipment` (which is
/// always empty for items with no `Equippable` asset, like ours)
public interface ShadesRenderStateExtension {

  ItemStack shades$getHeadSlotItem();

  void shades$setHeadSlotItem(ItemStack item);
}

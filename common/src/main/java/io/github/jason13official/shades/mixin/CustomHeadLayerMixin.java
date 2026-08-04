package io.github.jason13official.shades.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.jason13official.shades.api.client.renderer.ShadesRenderStateExtension;
import io.github.jason13official.shades.impl.common.registry.ModItems;
import java.util.Set;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/// vanilla treats any head-slot item without an `Equippable` asset as a generic decorative head
/// item and renders its flat item icon via this layer; shades items deliberately carry no asset,
/// so without this they'd get caught by that fallback and render their inventory icon floating on
/// the head. Cancels it for our items instead, gated on [ShadesRenderStateExtension]
/// @see net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer#shouldRender(ItemStack, EquipmentSlot)
/// @see ShadesRenderStateExtension
@Mixin(CustomHeadLayer.class)
public abstract class CustomHeadLayerMixin<S extends LivingEntityRenderState, M extends EntityModel<S> & HeadedModel> {

  /// lazy since ModItems' fields aren't set yet at class-init time
  @Unique
  private static Set<Item> shades$shadesItems;

  @Unique
  private static Set<Item> shades$shadesItems() {

    if (shades$shadesItems == null) {
      shades$shadesItems = Set.copyOf(ModItems.allShades());
    }

    return shades$shadesItems;
  }

  @Inject(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;FF)V", at = @At("HEAD"), cancellable = true)
  private void shades$skipBasicShadesHeadItem(
      PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, S state, float yRot, float xRot, CallbackInfo ci) {

    if (state instanceof AvatarRenderState avatarState
        && shades$shadesItems().contains(((ShadesRenderStateExtension) avatarState).shades$getHeadSlotItem().getItem())) {
      ci.cancel();
    }
  }
}

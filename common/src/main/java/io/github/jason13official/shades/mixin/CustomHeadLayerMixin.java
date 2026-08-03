package io.github.jason13official.shades.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.jason13official.shades.impl.client.renderer.ShadesRenderStateExtension;
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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/// vanilla treats any head-slot item without an `Equippable` asset as a generic decorative head item,
/// renders its flat item icon via this layer (see LivingEntityRenderer#extractRenderState, the
/// `!HumanoidArmorLayer.shouldRender(headItem, HEAD)` branch)
///
/// [ModItems#BASIC_SHADES] deliberately carries no asset (so vanilla's HumanoidArmorLayer renders nothing)
///
/// it would otherwise get caught by that same fallback and render its inventory icon floating on the head.
/// cancel it specifically for our item instead of giving it a real (empty) equipment asset
///
/// gated on [ShadesRenderStateExtension] rather than `state.headEquipment`/`headItem`
/// @see net.minecraft.client.renderer.entity.LivingEntityRenderer
/// @see net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer#shouldRender(ItemStack, EquipmentSlot)
/// @see ModItems
/// @see ShadesRenderStateExtension
@Mixin(CustomHeadLayer.class)
public abstract class CustomHeadLayerMixin<S extends LivingEntityRenderState, M extends EntityModel<S> & HeadedModel> {

  /// lazy since ModItems' fields aren't set yet at class-init time
  private static Set<Item> shadesItems;

  private static Set<Item> shadesItems() {

    if (shadesItems == null) {
      shadesItems = Set.of(
          ModItems.BASIC_SHADES, ModItems.CREEPER_SHADES, ModItems.INVERT_SHADES, ModItems.SPIDER_SHADES, ModItems.BLUR_SHADES,
          ModItems.NIGHT_VISION_SHADES, ModItems.THERMAL_SHADES, ModItems.MATRIX_SHADES, ModItems.PRISM_SHADES);
    }

    return shadesItems;
  }

  @Inject(method = "submit", at = @At("HEAD"), cancellable = true)
  private void shades$skipBasicShadesHeadItem(
      PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, S state, float yRot, float xRot, CallbackInfo ci) {

    if (state instanceof AvatarRenderState avatarState
        && shadesItems().contains(((ShadesRenderStateExtension) avatarState).shades$getHeadSlotItem().getItem())) {
      ci.cancel();
    }
  }
}

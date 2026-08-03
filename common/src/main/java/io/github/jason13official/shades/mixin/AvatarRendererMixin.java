package io.github.jason13official.shades.mixin;

import io.github.jason13official.shades.impl.client.renderer.ShadesRenderStateExtension;
import io.github.jason13official.shades.impl.client.renderer.ShadesVisorLayer;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/// attaches [ShadesVisorLayer] to every player-like renderer; extends the real superclass
/// so `this.addLayer(...)`  is legal to call from a mixin class outside its package.
@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin<AvatarlikeEntity extends Avatar & ClientAvatarEntity>
    extends LivingEntityRenderer<AvatarlikeEntity, AvatarRenderState, PlayerModel> {

  private AvatarRendererMixin(EntityRendererProvider.Context context, PlayerModel model, float shadowRadius) {
    super(context, model, shadowRadius);
  }

  @Inject(method = "<init>", at = @At("RETURN"))
  private void shades$addVisorLayer(CallbackInfo ci) {
    this.addLayer(new ShadesVisorLayer(this));
  }

  /// capture the real head-slot item straight off the entity
  @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V", at = @At("TAIL"))
  private void shades$captureHeadSlotItem(AvatarlikeEntity entity, AvatarRenderState state, float partialTicks, CallbackInfo ci) {
    ((ShadesRenderStateExtension) state).shades$setHeadSlotItem(entity.getItemBySlot(EquipmentSlot.HEAD).copy());
  }
}

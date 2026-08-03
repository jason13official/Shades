package io.github.jason13official.shades.mixin;

import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import io.github.jason13official.shades.ShadesClient;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

  @Shadow @Final private CrossFrameResourcePool resourcePool;

  /// apply post-processing chain whenever the local player has our item equipped in the head slot,
  /// independent of vanilla's postEffectId/effectActive (creeper/spider/invert spectate shaders) so we don't fight over that single-slot field
  @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;doEntityOutline()V", shift = At.Shift.AFTER))
  private void shades$applyVisorShader(CallbackInfo ci) {

    ShadesClient.doGameRender(this.resourcePool);
  }

}

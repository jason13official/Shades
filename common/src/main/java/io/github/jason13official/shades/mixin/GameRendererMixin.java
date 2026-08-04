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

  /// snapshots real depth right after the world/entities finish rendering, but before
  /// renderItemInHand clears it to draw the hand ->  by shades$applyVisorShader's hook point (after
  /// renderLevel fully returns) that clear already wiped out everything except the hand, so
  /// anything needing real terrain depth (sonar_shades) reads from this snapshot instead
  @Inject(method = "renderLevel(Lnet/minecraft/client/DeltaTracker;)V",
      at = @At(value = "INVOKE",
          target = "Lnet/minecraft/client/renderer/LevelRenderer;renderLevel(Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;Lnet/minecraft/client/DeltaTracker;ZLnet/minecraft/client/renderer/state/level/CameraRenderState;Lorg/joml/Matrix4fc;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lorg/joml/Vector4f;ZLnet/minecraft/client/renderer/chunk/ChunkSectionsToRender;)V",
          shift = At.Shift.AFTER))
  private void shades$captureWorldDepth(CallbackInfo ci) {

    ShadesClient.captureWorldDepth();
  }
}

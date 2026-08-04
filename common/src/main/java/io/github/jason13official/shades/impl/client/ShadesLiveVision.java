package io.github.jason13official.shades.impl.client;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import com.mojang.blaze3d.resource.RenderTargetDescriptor;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.SamplerCache;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import java.util.OptionalInt;
import java.util.function.Function;
import net.minecraft.client.Minecraft;

/// hand-rolled equivalent of a single `PostPass`, for shaders that need live `GameTime`;
/// `PostChain`/`PostPass` always build from `POST_PROCESSING_SNIPPET` alone, never combined with
/// `GLOBALS_SNIPPET`. Copies the current frame into a scratch target, draws one fullscreen
/// triangle with a GLOBALS_SNIPPET-combined pipeline sampling that copy, writes back to the real target
/// @see ShadesRenderPipelines
public class ShadesLiveVision {

  public static void process(CrossFrameResourcePool resourcePool, RenderPipeline pipeline, GpuTextureView depthView) {
    process(resourcePool, pipeline, depthView, null);
  }

  /// `depthView`, if given, is bound as `InDepthSampler`; pass the real main target's depth only
  /// if it's known-fresh at this point in the frame. `extraUniforms`, if given, runs right before
  /// the draw call to bind whatever custom uniform(s) the pipeline needs, returning the GpuBuffer
  /// it created so this method can close it once the draw is done
  public static void process(CrossFrameResourcePool resourcePool, RenderPipeline pipeline, GpuTextureView depthView,
      Function<RenderPass, GpuBuffer> extraUniforms) {
    process(resourcePool, pipeline, depthView, extraUniforms, null);
  }

  /// `feedbackTarget`, if given, is bound as `PrevFrameSampler` (this effect's own previous
  /// frame's output) before the draw, then overwritten with this frame's fresh result right after -
  /// a genuine persistent ping-pong buffer, unlike `resourcePool`'s scratch targets, which are
  /// cleared on every acquire and can't carry content across frames
  public static void process(CrossFrameResourcePool resourcePool, RenderPipeline pipeline, GpuTextureView depthView,
      Function<RenderPass, GpuBuffer> extraUniforms, RenderTarget feedbackTarget) {

    Minecraft mc = Minecraft.getInstance();
    RenderTarget mainTarget = mc.getMainRenderTarget();

    // scratch copy of the current frame's color -> our input sampler; only ever read from below,
    // never written into
    RenderTargetDescriptor descriptor = new RenderTargetDescriptor(mainTarget.width, mainTarget.height, false, 0);
    RenderTarget scratch = resourcePool.acquire(descriptor);

    CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();
    commandEncoder.copyTextureToTexture(
        mainTarget.getColorTexture(), scratch.getColorTexture(), 0, 0, 0, 0, 0, mainTarget.width, mainTarget.height);

    SamplerCache samplerCache = RenderSystem.getSamplerCache();
    GpuSampler sampler = samplerCache.getClampToEdge(FilterMode.LINEAR);

    // no depth attachment; this pass never depth-tests/writes, and binding the same texture as
    // both a render-target attachment and a sampled input at once is a resource hazard
    GpuBuffer extraBuffer = null;
    try (RenderPass renderPass = commandEncoder.createRenderPass(
        () -> "Shades live vision",
        mainTarget.getColorTextureView(),
        OptionalInt.empty())) {

      renderPass.setPipeline(pipeline);
      RenderSystem.bindDefaultUniforms(renderPass);
      renderPass.bindTexture("InSampler", scratch.getColorTextureView(), sampler);
      if (depthView != null) {
        renderPass.bindTexture("InDepthSampler", depthView, sampler);
      }
      if (feedbackTarget != null) {
        renderPass.bindTexture("PrevFrameSampler", feedbackTarget.getColorTextureView(), sampler);
      }
      if (extraUniforms != null) {
        extraBuffer = extraUniforms.apply(renderPass);
      }

      renderPass.draw(0, 3);
    } finally {
      if (extraBuffer != null) {
        extraBuffer.close();
      }
    }

    // capture this frame's result for next frame's PrevFrameSampler read, after the render pass
    // closes so we're never reading and writing the same texture at once
    if (feedbackTarget != null) {
      commandEncoder.copyTextureToTexture(
          mainTarget.getColorTexture(), feedbackTarget.getColorTexture(), 0, 0, 0, 0, 0, mainTarget.width, mainTarget.height);
    }

    resourcePool.release(descriptor, scratch);
  }
}

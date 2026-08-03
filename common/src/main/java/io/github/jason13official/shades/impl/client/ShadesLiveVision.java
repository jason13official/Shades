package io.github.jason13official.shades.impl.client;

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
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.client.Minecraft;

/// hand-rolled equivalent of a single `PostPass`, for shaders that need live `GameTime`.
///
/// `PostChain`/`PostPass` always build their `RenderPipeline` from
/// `RenderPipelines.POST_PROCESSING_SNIPPET` alone (see `PostChain#createPass`) - never combined
/// with `GLOBALS_SNIPPET` - so a real `post_effect` JSON can never read live `GameTime`. Nothing
/// stops US from building that combined pipeline ourselves and driving a single pass by hand
/// though (same trick `ShadesRenderPipelines`/`ShadesPlasmaEffect` used for a 3D quad, applied
/// here to a 2D fullscreen one instead): copy the current frame into a scratch target (avoids a
/// read/write hazard on the same texture), then draw one fullscreen triangle with our own
/// GLOBALS_SNIPPET-combined pipeline sampling that copy, writing straight back into the real
/// target. No `PostChain`/`FrameGraphBuilder` involved - single fixed pass, no target graph to
/// schedule.
///
/// @see ShadesRenderPipelines
public class ShadesLiveVision {

  public static void process(CrossFrameResourcePool resourcePool, RenderPipeline pipeline, boolean bindDepth) {

    Minecraft mc = Minecraft.getInstance();
    RenderTarget mainTarget = mc.getMainRenderTarget();

    // scratch copy of the current frame's color - our input sampler. No depth needed on this one;
    // we only ever read from it in the pass below, never write into it
    RenderTargetDescriptor descriptor = new RenderTargetDescriptor(mainTarget.width, mainTarget.height, false, 0);
    RenderTarget scratch = resourcePool.acquire(descriptor);

    CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();
    commandEncoder.copyTextureToTexture(
        mainTarget.getColorTexture(), scratch.getColorTexture(), 0, 0, 0, 0, 0, mainTarget.width, mainTarget.height);

    SamplerCache samplerCache = RenderSystem.getSamplerCache();
    GpuSampler sampler = samplerCache.getClampToEdge(FilterMode.LINEAR);

    // depth isn't being written by this pass (no depth test/write in the pipeline), only ever
    // read - so unlike color, it can be bound straight off the real target, no copy needed
    try (RenderPass renderPass = commandEncoder.createRenderPass(
        () -> "Shades live vision",
        mainTarget.getColorTextureView(),
        OptionalInt.empty(),
        mainTarget.useDepth ? mainTarget.getDepthTextureView() : null,
        OptionalDouble.empty())) {

      renderPass.setPipeline(pipeline);
      RenderSystem.bindDefaultUniforms(renderPass);
      renderPass.bindTexture("InSampler", scratch.getColorTextureView(), sampler);
      if (bindDepth && mainTarget.useDepth) {
        renderPass.bindTexture("InDepthSampler", mainTarget.getDepthTextureView(), sampler);
      }

      renderPass.draw(0, 3);
    }

    resourcePool.release(descriptor, scratch);
  }
}

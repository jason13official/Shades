package io.github.jason13official.shades.impl.client;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.github.jason13official.shades.Shades;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

/// custom "core" render pipelines, as opposed to the JSON-driven post_effect ones every other
/// pair of shades used so far.
///
/// `PostPass` always builds its pipeline from `RenderPipelines.POST_PROCESSING_SNIPPET` alone,
/// never combined with `RenderPipelines.GLOBALS_SNIPPET` -> so a post_effect JSON can never read live `GameTime`,
/// only whatever's baked into it at load time.
///
/// Building our own pipeline (composing MATRICES_FOG_SNIPPET + GLOBALS_SNIPPET, same as vanilla's own
/// core shaders) sidesteps that; it's real world-space geometry and its shader gets live GameTime
/// like any other entity/particle
/// @see net.minecraft.client.renderer.ShaderManager
/// @see net.minecraft.client.renderer.PostPass
public class ShadesRenderPipelines {

  public static final RenderPipeline PLASMA = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET, RenderPipelines.GLOBALS_SNIPPET)
      .withLocation(Shades.identifier("pipeline/plasma"))
      .withVertexShader(Shades.identifier("core/plasma"))
      .withFragmentShader(Shades.identifier("core/plasma"))
      .withCull(false)
      .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
      .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
      .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
      .build();

  public static RenderType plasma() {
    return RenderType.create("shades:plasma",
        RenderSetup.builder(PLASMA)
            .setOutputTarget(OutputTarget.WEATHER_TARGET)
            .sortOnUpload()
            .createRenderSetup());
  }
}

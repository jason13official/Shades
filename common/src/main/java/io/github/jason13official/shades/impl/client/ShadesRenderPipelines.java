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
import net.minecraft.resources.Identifier;

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

  /// vanilla's own fullscreen-triangle vertex shader (see `screenquad.vsh`) - reused directly
  /// since our live shaders below are plain 2D fullscreen passes, same as every `post_effect`
  /// pass, just combined with GLOBALS_SNIPPET so the fragment shader can read live GameTime.
  /// See `ShadesLiveVision` for how these get driven by hand instead of through `PostChain`.
  private static final Identifier SCREENQUAD_VERTEX_SHADER = Identifier.withDefaultNamespace("core/screenquad");

  public static final RenderPipeline STATIC_TV = RenderPipeline.builder(RenderPipelines.POST_PROCESSING_SNIPPET, RenderPipelines.GLOBALS_SNIPPET)
      .withLocation(Shades.identifier("pipeline/static_tv"))
      .withVertexShader(SCREENQUAD_VERTEX_SHADER)
      .withFragmentShader(Shades.identifier("core/static_tv"))
      .withSampler("InSampler")
      .build();

  public static final RenderPipeline SONAR = RenderPipeline.builder(RenderPipelines.POST_PROCESSING_SNIPPET, RenderPipelines.GLOBALS_SNIPPET)
      .withLocation(Shades.identifier("pipeline/sonar"))
      .withVertexShader(SCREENQUAD_VERTEX_SHADER)
      .withFragmentShader(Shades.identifier("core/sonar"))
      .withSampler("InSampler")
      .withSampler("InDepthSampler")
      .build();

  public static final RenderPipeline GLITCH = RenderPipeline.builder(RenderPipelines.POST_PROCESSING_SNIPPET, RenderPipelines.GLOBALS_SNIPPET)
      .withLocation(Shades.identifier("pipeline/glitch"))
      .withVertexShader(SCREENQUAD_VERTEX_SHADER)
      .withFragmentShader(Shades.identifier("core/glitch"))
      .withSampler("InSampler")
      .build();
}

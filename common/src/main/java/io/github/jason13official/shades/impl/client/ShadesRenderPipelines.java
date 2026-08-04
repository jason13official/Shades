package io.github.jason13official.shades.impl.client;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.github.jason13official.shades.Shades;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

/// custom "core" render pipelines, unlike the JSON-driven post_effect ones. PostPass always
/// builds from POST_PROCESSING_SNIPPET alone, never combined with GLOBALS_SNIPPET, so a
/// post_effect JSON can never read live GameTime; composing GLOBALS_SNIPPET in here sidesteps
/// that, since this is real geometry/a real fullscreen pass instead
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

  /// vanilla's own fullscreen-triangle vertex shader, reused since every live shader below is a
  /// plain 2D fullscreen pass, just combined with GLOBALS_SNIPPET so the fragment shader can read
  /// live GameTime
  private static final Identifier SCREENQUAD_VERTEX_SHADER = Identifier.withDefaultNamespace("core/screenquad");

  public static final RenderPipeline STATIC_TV = RenderPipeline.builder(RenderPipelines.POST_PROCESSING_SNIPPET, RenderPipelines.GLOBALS_SNIPPET)
      .withLocation(Shades.identifier("pipeline/static_tv"))
      .withVertexShader(SCREENQUAD_VERTEX_SHADER)
      .withFragmentShader(Shades.identifier("core/static_tv"))
      .withSampler("InSampler")
      .build();

  /// needs a "CameraRay" uniform: a combined inverse-projection*view matrix + camera/ping-origin
  /// positions, pushed fresh each frame, to reconstruct real world-space position per pixel
  public static final RenderPipeline SONAR = RenderPipeline.builder(RenderPipelines.POST_PROCESSING_SNIPPET, RenderPipelines.GLOBALS_SNIPPET)
      .withLocation(Shades.identifier("pipeline/sonar"))
      .withVertexShader(SCREENQUAD_VERTEX_SHADER)
      .withFragmentShader(Shades.identifier("core/sonar"))
      .withSampler("InSampler")
      .withSampler("InDepthSampler")
      .withUniform("CameraRay", UniformType.UNIFORM_BUFFER)
      .build();

  public static final RenderPipeline GLITCH = RenderPipeline.builder(RenderPipelines.POST_PROCESSING_SNIPPET, RenderPipelines.GLOBALS_SNIPPET)
      .withLocation(Shades.identifier("pipeline/glitch"))
      .withVertexShader(SCREENQUAD_VERTEX_SHADER)
      .withFragmentShader(Shades.identifier("core/glitch"))
      .withSampler("InSampler")
      .build();

  /// needs a "CursorConfig" uniform (mouse UV + whether a screen is open), pushed fresh each frame
  public static final RenderPipeline CURSOR = RenderPipeline.builder(RenderPipelines.POST_PROCESSING_SNIPPET, RenderPipelines.GLOBALS_SNIPPET)
      .withLocation(Shades.identifier("pipeline/cursor"))
      .withVertexShader(SCREENQUAD_VERTEX_SHADER)
      .withFragmentShader(Shades.identifier("core/cursor"))
      .withSampler("InSampler")
      .withUniform("CursorConfig", UniformType.UNIFORM_BUFFER)
      .build();

  /// needs a "MotionConfig" uniform (smoothed speed/turn-rate factors), pushed fresh each frame
  public static final RenderPipeline VERTIGO = RenderPipeline.builder(RenderPipelines.POST_PROCESSING_SNIPPET, RenderPipelines.GLOBALS_SNIPPET)
      .withLocation(Shades.identifier("pipeline/vertigo"))
      .withVertexShader(SCREENQUAD_VERTEX_SHADER)
      .withFragmentShader(Shades.identifier("core/vertigo"))
      .withSampler("InSampler")
      .withUniform("MotionConfig", UniformType.UNIFORM_BUFFER)
      .build();

  /// fluted-glass refraction, live so the ridges can scroll/shimmer with GameTime instead of
  /// sitting at a frozen load-time phase
  public static final RenderPipeline ANIMATED_GLASS = RenderPipeline.builder(RenderPipelines.POST_PROCESSING_SNIPPET, RenderPipelines.GLOBALS_SNIPPET)
      .withLocation(Shades.identifier("pipeline/animated_glass"))
      .withVertexShader(SCREENQUAD_VERTEX_SHADER)
      .withFragmentShader(Shades.identifier("core/animated_glass"))
      .withSampler("InSampler")
      .build();

  /// lava-lamp metaball field; needs a "GlassConfig" uniform (real window aspect ratio) so the
  /// blobs render as true circles instead of stretched ellipses
  public static final RenderPipeline MOLTEN_GLASS = RenderPipeline.builder(RenderPipelines.POST_PROCESSING_SNIPPET, RenderPipelines.GLOBALS_SNIPPET)
      .withLocation(Shades.identifier("pipeline/molten_glass"))
      .withVertexShader(SCREENQUAD_VERTEX_SHADER)
      .withFragmentShader(Shades.identifier("core/molten_glass"))
      .withSampler("InSampler")
      .withUniform("GlassConfig", UniformType.UNIFORM_BUFFER)
      .build();

  /// domain-warped fractal-sine fire field; needs a "FireConfig" uniform (real window aspect
  /// ratio) so the flame pattern isn't stretched on a non-square window
  public static final RenderPipeline FIRE = RenderPipeline.builder(RenderPipelines.POST_PROCESSING_SNIPPET, RenderPipelines.GLOBALS_SNIPPET)
      .withLocation(Shades.identifier("pipeline/fire"))
      .withVertexShader(SCREENQUAD_VERTEX_SHADER)
      .withFragmentShader(Shades.identifier("core/fire"))
      .withSampler("InSampler")
      .withUniform("FireConfig", UniformType.UNIFORM_BUFFER)
      .build();

  /// real depth-reconstructed world position grid-snapped into blocks that bounce in place; needs
  /// "InDepthSampler" plus a "GridRay" uniform (combined inverse-projection*view matrix + camera
  /// position)
  public static final RenderPipeline GRID = RenderPipeline.builder(RenderPipelines.POST_PROCESSING_SNIPPET, RenderPipelines.GLOBALS_SNIPPET)
      .withLocation(Shades.identifier("pipeline/grid"))
      .withVertexShader(SCREENQUAD_VERTEX_SHADER)
      .withFragmentShader(Shades.identifier("core/grid"))
      .withSampler("InSampler")
      .withSampler("InDepthSampler")
      .withUniform("GridRay", UniformType.UNIFORM_BUFFER)
      .build();

  /// single roaming noise-churned lens, fisheye-refracting the real background within its
  /// footprint; needs an "OrbConfig" uniform (real window aspect ratio) only
  public static final RenderPipeline ORB = RenderPipeline.builder(RenderPipelines.POST_PROCESSING_SNIPPET, RenderPipelines.GLOBALS_SNIPPET)
      .withLocation(Shades.identifier("pipeline/orb"))
      .withVertexShader(SCREENQUAD_VERTEX_SHADER)
      .withFragmentShader(Shades.identifier("core/orb"))
      .withSampler("InSampler")
      .withUniform("OrbConfig", UniformType.UNIFORM_BUFFER)
      .build();

  /// contour line traced along real world height, with a fading trail plus a full-screen flash
  /// when the scan altitude crosses the camera's own eye level; needs "InDepthSampler" +
  /// "PrevFrameSampler" alongside InSampler, plus a "WaveformRay" uniform (both the inverse and
  /// non-inverted projection*view matrix, + camera position)
  public static final RenderPipeline WAVEFORM = RenderPipeline.builder(RenderPipelines.POST_PROCESSING_SNIPPET, RenderPipelines.GLOBALS_SNIPPET)
      .withLocation(Shades.identifier("pipeline/waveform"))
      .withVertexShader(SCREENQUAD_VERTEX_SHADER)
      .withFragmentShader(Shades.identifier("core/waveform"))
      .withSampler("InSampler")
      .withSampler("InDepthSampler")
      .withSampler("PrevFrameSampler")
      .withUniform("WaveformRay", UniformType.UNIFORM_BUFFER)
      .build();

  /// analytic multi-sine ripple field sampled at real world position; needs "InDepthSampler" plus
  /// a "FluidRay" uniform (combined inverse-projection*view matrix + camera position); fully
  /// deterministic, no feedback needed
  public static final RenderPipeline FLUID = RenderPipeline.builder(RenderPipelines.POST_PROCESSING_SNIPPET, RenderPipelines.GLOBALS_SNIPPET)
      .withLocation(Shades.identifier("pipeline/fluid"))
      .withVertexShader(SCREENQUAD_VERTEX_SHADER)
      .withFragmentShader(Shades.identifier("core/fluid"))
      .withSampler("InSampler")
      .withSampler("InDepthSampler")
      .withUniform("FluidRay", UniformType.UNIFORM_BUFFER)
      .build();

  /// procedurally-noised bump-mapped copper foil, refracting/tinting the real background; needs
  /// a "CopperConfig" uniform (real window aspect ratio) for the light-direction calc
  public static final RenderPipeline COPPER = RenderPipeline.builder(RenderPipelines.POST_PROCESSING_SNIPPET, RenderPipelines.GLOBALS_SNIPPET)
      .withLocation(Shades.identifier("pipeline/copper"))
      .withVertexShader(SCREENQUAD_VERTEX_SHADER)
      .withFragmentShader(Shades.identifier("core/copper"))
      .withSampler("InSampler")
      .withUniform("CopperConfig", UniformType.UNIFORM_BUFFER)
      .build();

  /// green tint + scanlines plus falling Matrix-code glyphs on top; needs a "MatrixConfig"
  /// uniform (real window aspect ratio) only
  public static final RenderPipeline MATRIX = RenderPipeline.builder(RenderPipelines.POST_PROCESSING_SNIPPET, RenderPipelines.GLOBALS_SNIPPET)
      .withLocation(Shades.identifier("pipeline/matrix"))
      .withVertexShader(SCREENQUAD_VERTEX_SHADER)
      .withFragmentShader(Shades.identifier("core/matrix"))
      .withSampler("InSampler")
      .withUniform("MatrixConfig", UniformType.UNIFORM_BUFFER)
      .build();

  /// bump-mapped "rain on glass" droplet refraction; needs a "RainConfig" uniform (real window
  /// aspect ratio) only
  public static final RenderPipeline RAIN = RenderPipeline.builder(RenderPipelines.POST_PROCESSING_SNIPPET, RenderPipelines.GLOBALS_SNIPPET)
      .withLocation(Shades.identifier("pipeline/rain"))
      .withVertexShader(SCREENQUAD_VERTEX_SHADER)
      .withFragmentShader(Shades.identifier("core/rain"))
      .withSampler("InSampler")
      .withUniform("RainConfig", UniformType.UNIFORM_BUFFER)
      .build();

  /// plasma's own sum-of-sines color field reused as a "warmth" map; its spatial gradient
  /// bulges/pinches the real background instead of the field being rendered directly. Needs a
  /// "MirageConfig" uniform (real window aspect ratio) only
  public static final RenderPipeline MIRAGE = RenderPipeline.builder(RenderPipelines.POST_PROCESSING_SNIPPET, RenderPipelines.GLOBALS_SNIPPET)
      .withLocation(Shades.identifier("pipeline/mirage"))
      .withVertexShader(SCREENQUAD_VERTEX_SHADER)
      .withFragmentShader(Shades.identifier("core/mirage"))
      .withSampler("InSampler")
      .withUniform("MirageConfig", UniformType.UNIFORM_BUFFER)
      .build();

  /// chromatic-refraction glass-blob metaball field; needs a "LensConfig" uniform (real window
  /// aspect ratio) only
  public static final RenderPipeline LENS = RenderPipeline.builder(RenderPipelines.POST_PROCESSING_SNIPPET, RenderPipelines.GLOBALS_SNIPPET)
      .withLocation(Shades.identifier("pipeline/lens"))
      .withVertexShader(SCREENQUAD_VERTEX_SHADER)
      .withFragmentShader(Shades.identifier("core/lens"))
      .withSampler("InSampler")
      .withUniform("LensConfig", UniformType.UNIFORM_BUFFER)
      .build();

  /// radial tentacle vignette multiplied over a noise-warped, desaturated real world; no custom
  /// uniform needed
  public static final RenderPipeline VORTEX = RenderPipeline.builder(RenderPipelines.POST_PROCESSING_SNIPPET, RenderPipelines.GLOBALS_SNIPPET)
      .withLocation(Shades.identifier("pipeline/vortex"))
      .withVertexShader(SCREENQUAD_VERTEX_SHADER)
      .withFragmentShader(Shades.identifier("core/vortex"))
      .withSampler("InSampler")
      .build();

  /// 9 moving points of glow, additive over the real background; needs a "WispConfig" uniform
  /// (real window aspect ratio) only
  public static final RenderPipeline WISP = RenderPipeline.builder(RenderPipelines.POST_PROCESSING_SNIPPET, RenderPipelines.GLOBALS_SNIPPET)
      .withLocation(Shades.identifier("pipeline/wisp"))
      .withVertexShader(SCREENQUAD_VERTEX_SHADER)
      .withFragmentShader(Shades.identifier("core/wisp"))
      .withSampler("InSampler")
      .withUniform("WispConfig", UniformType.UNIFORM_BUFFER)
      .build();

  /// folded-fbm color gradient reused as a tint/refraction-normal source over the real world; no
  /// custom uniform needed
  public static final RenderPipeline AURORA = RenderPipeline.builder(RenderPipelines.POST_PROCESSING_SNIPPET, RenderPipelines.GLOBALS_SNIPPET)
      .withLocation(Shades.identifier("pipeline/aurora"))
      .withVertexShader(SCREENQUAD_VERTEX_SHADER)
      .withFragmentShader(Shades.identifier("core/aurora"))
      .withSampler("InSampler")
      .build();

  /// swirling glowing strand layers, additive over the real background; needs a "CosmicConfig"
  /// uniform (real window aspect ratio) only
  public static final RenderPipeline COSMIC = RenderPipeline.builder(RenderPipelines.POST_PROCESSING_SNIPPET, RenderPipelines.GLOBALS_SNIPPET)
      .withLocation(Shades.identifier("pipeline/cosmic"))
      .withVertexShader(SCREENQUAD_VERTEX_SHADER)
      .withFragmentShader(Shades.identifier("core/cosmic"))
      .withSampler("InSampler")
      .withUniform("CosmicConfig", UniformType.UNIFORM_BUFFER)
      .build();

  /// a flat polar-grid voxel disc, colored by a per-cell interference pattern and gated by real
  /// depth (sky excluded, size/pattern shaped by real per-pixel distance). Needs "InDepthSampler"
  /// (shared worldDepthCapture) plus a "VoxelRay" uniform (inverse-projection*view matrix +
  /// aspect ratio + camera position)
  public static final RenderPipeline VOXEL = RenderPipeline.builder(RenderPipelines.POST_PROCESSING_SNIPPET, RenderPipelines.GLOBALS_SNIPPET)
      .withLocation(Shades.identifier("pipeline/voxel"))
      .withVertexShader(SCREENQUAD_VERTEX_SHADER)
      .withFragmentShader(Shades.identifier("core/voxel"))
      .withSampler("InSampler")
      .withSampler("InDepthSampler")
      .withUniform("VoxelRay", UniformType.UNIFORM_BUFFER)
      .build();
}

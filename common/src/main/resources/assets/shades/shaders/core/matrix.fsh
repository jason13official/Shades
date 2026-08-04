#version 330

// converted from a plain post_effect (green luma tint + scanlines, no live time) to a live core
// pipeline so it can add actual falling "Matrix code" glyphs on top - a post_effect JSON can never
// get live GameTime (see Key Findings), so the falling-glyph part needed this move. The glyph
// generator (rchar/matrixRain below) is a port of a shadertoy-style digital-rain sketch; the
// original just replaced the whole screen with the glyph field, which would violate this mod's
// own "modify how you see the world, don't replace it" rule (see Key Findings) - so here the
// glyphs are ADDED on top of the same green-luma-tint+scanline recolor the old post_effect did,
// real scene still fully legible underneath, tinted, with code raining over it
#moj_import <minecraft:globals.glsl>

uniform sampler2D InSampler;

// real window aspect ratio, pushed fresh each frame by ShadesClient
layout(std140) uniform MatrixConfig {
    float Aspect;
};

in vec2 texCoord;
out vec4 fragColor;

const vec3 TINT = vec3(0.1, 1.0, 0.25);
const float SCANLINE_STRENGTH = 0.35;

float randomF(float x) {
    return fract(sin(x) * 43758.5453);
}

float randomV(vec2 st) {
    return fract(sin(dot(st, vec2(12.9898, 78.233))) * 43758.5453);
}

// one "character cell": a randomized rectangular glyph block inside a margin-inset border, same
// shape as the original reference's rchar()
float rchar(vec2 outer, vec2 inner) {
    float grid = 5.0;
    vec2 margin = vec2(0.2, 0.05);
    float seed = 23.0;
    vec2 borders = step(margin, inner) * step(margin, 1.0 - inner);
    return step(0.5, randomV(outer * seed + floor(inner * grid))) * borders.x * borders.y;
}

// falling code columns: each column (ipos.x) gets its own fall speed via randomF(), the whole
// column's row index increments over time - a soft radial glow is added per-cell so the glyphs
// don't read as flat squares
vec3 matrixRain(vec2 st, float seconds) {

    float rows = 40.0;
    vec2 ipos = floor(st * rows);
    ipos += vec2(0.0, floor(seconds * 10.0 * randomF(ipos.x)));

    vec2 fpos = fract(st * rows);
    vec2 center = 0.5 - fpos;

    float pct = randomV(ipos);
    float glow = (1.0 - dot(center, center) * 3.0) * 2.0;

    vec3 color = vec3(0.15, 0.95, 0.35) * (rchar(ipos, fpos) * pct);
    color += vec3(0.02, 0.25, 0.08) * pct * glow;
    return color;
}

void main(){

    // GameTime advances 2 units/real-second (see plasma.fsh's writeup) - halve it back down to a
    // real-seconds-equivalent so the fall speed below matches the original reference's iTime-based
    // pacing
    float seconds = GameTime * 2400.0 * 0.5;

    vec4 diffuseColor = texture(InSampler, texCoord);
    float luma = dot(diffuseColor.rgb, vec3(0.299, 0.587, 0.114));
    vec3 green = vec3(luma) * TINT;

    // fixed scanline frequency rather than real pixel rows - SamplerInfo (OutSize/InSize) isn't
    // available in a live ShadesLiveVision pass, only real post_effect passes (see Key Findings)
    float scanline = fract(texCoord.y * 240.0);
    green *= (1.0 - step(scanline, 0.5) * SCANLINE_STRENGTH);

    vec2 st = texCoord;
    st.x *= Aspect;
    vec3 glyphs = matrixRain(st, seconds);

    vec3 outColor = green + glyphs;
    fragColor = vec4(outColor, 1.0);
}

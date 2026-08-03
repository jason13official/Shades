#version 330

// port of "Another variant of Simple fire effect" by guil (https://shadertoy.com/view/msyGRm) -
// a domain-warped fractal sum of sines: each octave both adds to the accumulator `r` AND feeds
// back into the next octave's sample position (via the mat2 shear + `r*0.4`), which is what
// gives it that layered, licking-flame look instead of plain turbulence. Ported into normalized
// texCoord space (a virtual reference height stands in for the original's iResolution.y) and
// GameTime-driven instead of iTime, with the iteration growth slowed slightly (1.13x instead of
// 1.05x, ~30 steps instead of ~76) since this runs full-screen every frame with no early-out
#moj_import <minecraft:globals.glsl>

uniform sampler2D InSampler;

// real window aspect ratio, pushed fresh each frame by ShadesClient - the fire field is sampled
// in a virtual square-ish space, so x needs this correction or the flame pattern stretches on any
// non-square window
layout(std140) uniform FireConfig {
    float Aspect;
};

in vec2 texCoord;
out vec4 fragColor;

const float VIRTUAL_RES = 480.0;

void main(){

    // GameTime advances 2 units/real-second (see plasma.fsh's writeup) - halve it back down so
    // the phase terms below animate at the same real-world speed the original iTime-driven
    // shader did
    float seconds = GameTime * 2400.0 * 0.5;

    vec2 u = vec2(texCoord.x * Aspect, texCoord.y) * VIRTUAL_RES;
    vec2 R = vec2(0.0, VIRTUAL_RES + sin(seconds / 25.0) * 8.24);
    vec2 p = 5.0 * (u + u + vec2(800.0, cos((seconds + 5.0) / 10.0) * 4000.0) - R) / R.y;
    vec2 r = vec2(0.0);

    for (float f = 1.0; f < 40.0; f *= 1.13) {
        r += sin(p * f + seconds * 0.85) / f;
        p = p * mat2(8.0, 6.0, -8.0, 6.0) * 0.1 + r * 0.4;
    }

    float l = length(r);
    vec3 fireColor = vec3(l * 0.29, l * l * 0.024, l * l * l * 0.0016);

    // transparent rather than a flat opaque overlay - the real scene stays visible everywhere,
    // more so where the flame is faint, capped below full opacity even at its brightest
    float alpha = clamp(l * 0.55, 0.0, 0.85);

    // a cheap lens/heat-haze distortion of the background, pulling the sample toward screen
    // center in proportion to local flame intensity - the world visibly warps through the fire
    // instead of the flame just sitting flatly on top of an undistorted scene
    vec2 centerOffset = texCoord - vec2(0.5);
    float bulge = clamp(l * 0.15, 0.0, 0.12);
    vec2 distortedUV = texCoord - centerOffset * bulge;

    vec3 scene = texture(InSampler, distortedUV).rgb;
    vec3 outColor = mix(scene, fireColor, alpha);

    fragColor = vec4(outColor, 1.0);
}

#version 330

// live GameTime, same trick as plasma.fsh; a real post_effect shader can't get this, since
// PostChain always builds its pipeline without GLOBALS_SNIPPET
#moj_import <minecraft:globals.glsl>

uniform sampler2D InSampler; // copy of the current frame's color, taken before this pass runs

in vec2 texCoord; // from vanilla's core/screenquad.vsh
out vec4 fragColor;

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453123);
}

void main(){

    // turns the slow 0..1 GameTime loop back into a fast-moving clock
    float t = GameTime * 2400.0;

    // occasional whole-frame vertical jump: near-zero most of the time, briefly kicking in right
    // around a "glitch beat" a couple of times per loop
    float beat = fract(t * 0.15);
    float jump = step(0.97, beat) * (hash(vec2(floor(t * 0.15), 0.0)) - 0.5) * 0.1;

    // per-scanline horizontal jitter: a handful of rows get sampled slightly sideways each beat,
    // most rows untouched; reads as analog signal noise rather than one clean wave
    float rowSeed = floor(texCoord.y * 90.0);
    float rowNoise = hash(vec2(rowSeed, floor(t * 8.0)));
    float jitter = (rowNoise - 0.5) * step(0.92, rowNoise) * 0.05;

    vec2 uv = texCoord + vec2(jitter, jump);
    vec3 color = texture(InSampler, uv).rgb;

    // sprinkle in white noise on top, re-rolled every beat via the time term inside hash()
    float grain = hash(texCoord * vec2(800.0, 600.0) + t);
    color += (grain - 0.5) * 0.12;

    fragColor = vec4(color, 1.0);
}

#version 330

// the pasted shadertoy script ("flockaroo"'s single-pass CFD) was only the DISPLAY pass of a
// multi-buffer setup - it reads iChannel0 as an already-simulated velocity/height field that a
// separate simulation-update pass (not given here) writes every frame, plus a cubemap for
// environment reflection. Neither is available in this engine's single-pass live pipeline, so this
// port keeps the display pass's real technique (normal-from-local-gradient + reflective shading)
// but substitutes a minimal self-contained single-pass wave update (4-tap neighbor blur + damping
// + a slow periodic perturbation) for the missing simulation pass, and a procedural sky gradient
// for the missing cubemap. The wave height is smuggled through the output alpha channel (packed to
// 0..1) rather than the visible RGB, using PrevFrameSampler as its own previous frame's state
// exactly like waveform_shades does for its trail - see ShadesClient's fluidFeedback
#moj_import <minecraft:globals.glsl>

uniform sampler2D InSampler;
uniform sampler2D PrevFrameSampler;

// real window aspect ratio, pushed fresh each frame by ShadesClient
layout(std140) uniform FluidConfig {
    float Aspect;
};

in vec2 texCoord;
out vec4 fragColor;

float readHeight(vec2 uv) {
    return texture(PrevFrameSampler, uv).a * 2.0 - 1.0;
}

vec3 sky(vec3 dir) {
    float h = dir.y * 0.5 + 0.5;
    return mix(vec3(0.05, 0.08, 0.12), vec3(0.55, 0.75, 0.95), h);
}

void main(){

    float t = GameTime * 2400.0 * 0.5;
    vec2 texel = 1.0 / vec2(textureSize(PrevFrameSampler, 0));

    // minimal single-pass wave update: average the four neighbors of our own last frame, damp it
    // slightly so it doesn't blow up, and re-inject a slow moving perturbation so the field never
    // fully settles - a cheap stand-in for the real simulation pass this port doesn't have
    float up = readHeight(texCoord + vec2(0.0, texel.y));
    float down = readHeight(texCoord - vec2(0.0, texel.y));
    float left = readHeight(texCoord - vec2(texel.x, 0.0));
    float right = readHeight(texCoord + vec2(texel.x, 0.0));
    float self = readHeight(texCoord);

    float h = (up + down + left + right) * 0.25;
    h = mix(self, h, 0.5) * 0.995;

    vec2 pertPos = vec2(0.5 + sin(t * 0.23) * 0.3, 0.5 + cos(t * 0.19) * 0.3);
    float pertDist = length((texCoord - pertPos) * vec2(Aspect, 1.0));
    h += smoothstep(0.05, 0.0, pertDist) * 0.02 * sin(t * 3.0);
    h = clamp(h, -1.0, 1.0);

    vec2 grad = vec2(right - left, up - down) / max(texel.x, texel.y) * 0.02;
    vec3 n = normalize(vec3(-grad, 1.0));

    vec2 sc = (texCoord - 0.5) * vec2(Aspect, 1.0);
    vec3 dir = normalize(vec3(sc, -1.0));
    vec3 r = reflect(dir, n);
    vec3 refl = sky(r);

    vec3 scene = texture(InSampler, texCoord).rgb;
    vec3 col = mix(scene, refl, 0.6) + refl * 0.15 * n.z;

    fragColor = vec4(col, h * 0.5 + 0.5);
}

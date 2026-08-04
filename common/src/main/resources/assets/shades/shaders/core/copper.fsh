#version 330

// port of a bump-mapped "copper foil" shadertoy sketch - two overlapping noise samples at
// different scales/scroll speeds combine into a mottled height field. The original samples a
// precomputed noise texture (iChannel0); this engine's live pass has no spare texture channel for
// that, so a 2D value-noise function (same smoothstep-interpolated hash-lattice shape) stands in
// for it instead. The height field drives a refraction + tint + specular pass over the real scene
// (same recipe as fluted_glass_vision/animated_glass), not a fully synthetic lit render - the
// point is to look at the world through wavy tinted copper foil, not to replace the world with foil
#moj_import <minecraft:globals.glsl>

uniform sampler2D InSampler;

// real window aspect ratio, pushed fresh each frame by ShadesClient
layout(std140) uniform CopperConfig {
    float Aspect;
};

in vec2 texCoord;
out vec4 fragColor;

float hash2(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

float noise2(vec2 x) {
    vec2 i = floor(x);
    vec2 f = fract(x);
    f = f * f * (3.0 - 2.0 * f);
    return mix(
        mix(hash2(i + vec2(0.0, 0.0)), hash2(i + vec2(1.0, 0.0)), f.x),
        mix(hash2(i + vec2(0.0, 1.0)), hash2(i + vec2(1.0, 1.0)), f.x),
        f.y
    );
}

float getCopper(vec2 uv, float t) {
    uv.x *= 3.0;
    float t0 = noise2(uv * 0.4 * 8.0 + vec2(0.0, t) * 0.05 * 8.0);
    float t1 = noise2(uv * 0.5 * 8.0 + vec2(0.0, t) * 0.01 * 8.0) * 0.2;
    return smoothstep(0.15, 0.25, t0 * t1 * 2.0);
}

const vec3 COPPER_TINT = vec3(1.0, 0.7, 0.5);

void main(){

    float t = GameTime * 2400.0 * 0.5;
    vec2 uv = texCoord;
    float eps = 0.005;

    float p0 = getCopper(uv, t);
    float p1 = getCopper(uv + vec2(0.0, eps), t);
    float p2 = getCopper(uv + vec2(eps, eps), t);
    vec3 normal = normalize(vec3(p0 - p1, p2 - p1, 0.5));

    // bend the real background sample through the foil's own bumps - same "looking through wavy
    // glass" trick fluted_glass_vision uses, just driven by this noise height field instead of a
    // sine ridge
    vec2 distortedUV = texCoord + normal.xy * 0.02;
    vec3 scene = texture(InSampler, distortedUV).rgb;

    vec3 lightDir = normalize(vec3(0.25, 0.75, 0.2) - vec3((uv - 0.5) * vec2(Aspect, 1.0) * 2.0, 0.0));
    float diffuse = max(dot(lightDir, normal), 0.0);
    vec3 viewDir = vec3(0.0, 0.0, 1.0);
    vec3 halfVec = normalize(lightDir + viewDir);
    float specular = pow(max(dot(normal, halfVec), 0.0), 30.0);

    vec3 outColor = scene * mix(vec3(1.0), COPPER_TINT, 0.55) * (0.6 + diffuse * 0.5) + specular * COPPER_TINT * 0.5;

    fragColor = vec4(outColor, 1.0);
}

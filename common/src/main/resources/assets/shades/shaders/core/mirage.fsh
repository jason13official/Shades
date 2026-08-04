#version 330

// reuses plasma.fsh's sum-of-sines recipe as a "warmth" field instead of rendering the pattern
// directly: warm (red-dominant) spots bulge the world outward like a heat-mirage lens, cool
// (blue-dominant) spots pinch/dent it inward, using the field's own spatial gradient as a fake
// surface normal, same "derivative as normal" trick fluted_glass_vision/copper.fsh use
#moj_import <minecraft:globals.glsl>

uniform sampler2D InSampler;

// real window aspect ratio, pushed fresh each frame by ShadesClient
layout(std140) uniform MirageConfig {
    float Aspect;
};

in vec2 texCoord;
out vec4 fragColor;

// identical recipe to plasma.fsh's main() body
vec3 plasmaColor(vec2 uv, float t) {

    vec2 p = uv * 6.0;

    float v = 0.0;
    v += sin(p.x + t);
    v += sin((p.y + t) * 0.7);
    v += sin((p.x + p.y + t) * 0.5);
    vec2 swirl = p + 0.5 * vec2(sin(t * 0.3), cos(t * 0.4));
    v += sin(length(swirl) * 2.0 - t);
    v *= 0.25;

    return vec3(
        sin(v * 3.14159 + 0.0),
        sin(v * 3.14159 + 2.094),
        sin(v * 3.14159 + 4.189)
    ) * 0.5 + 0.5;
}

// red minus blue, roughly -1..1: positive = warm (bulge), negative = cool (pinch)
float warmthAt(vec2 uv, float t) {
    vec3 c = plasmaColor(uv, t);
    return c.r - c.b;
}

void main(){

    float t = GameTime * 2400.0;
    vec2 uv = texCoord;
    uv.x *= Aspect;

    vec3 plasma = plasmaColor(uv, t);
    float warmth = plasma.r - plasma.b;

    float eps = 0.01;
    float wL = warmthAt(uv - vec2(eps, 0.0), t);
    float wR = warmthAt(uv + vec2(eps, 0.0), t);
    float wD = warmthAt(uv - vec2(0.0, eps), t);
    float wU = warmthAt(uv + vec2(0.0, eps), t);
    vec2 gradient = vec2(wR - wL, wU - wD) / (2.0 * eps);

    // bulge outward from warm spots, pinch inward toward cool spots, same spherical-lens
    // compression spirit molten_glass.fsh/orb.fsh use for their blobs; clamped so a steep
    // gradient can't sample wildly off-position
    vec2 distortOffset = clamp(gradient * warmth * 0.0025, vec2(-0.03), vec2(0.03));
    vec2 distortedUV = texCoord + distortOffset;

    vec3 scene = texture(InSampler, distortedUV).rgb;

    // faint plasma tint riding along with the distortion, strongest at the warmest/coolest
    // extremes, so the lensing reads as heat-haze rather than plain clear-glass refraction
    vec3 outColor = mix(scene, scene * (vec3(1.0) + plasma * 0.3), abs(warmth) * 0.5);

    fragColor = vec4(outColor, 1.0);
}

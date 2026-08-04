#version 330

// a single roaming translucent lens over the real world, reusing molten_glass's
// fisheye-through-a-blob technique (one blob instead of eight); noise3 (a procedural hash-lattice
// value-noise) drives an organic churning silhouette instead of molten_glass's per-angle radius
// wobble
#moj_import <minecraft:globals.glsl>

uniform sampler2D InSampler;

// real window aspect ratio, pushed fresh each frame by ShadesClient
layout(std140) uniform OrbConfig {
    float Aspect;
};

in vec2 texCoord;
out vec4 fragColor;

const float FISHEYE_STRENGTH = 0.5;
const float BASE_RADIUS = 0.22;

float hash3(vec3 p) {
    p = fract(p * 0.3183099 + 0.1);
    p *= 17.0;
    return fract(p.x * p.y * p.z * (p.x + p.y + p.z));
}

float noise3(vec3 x) {
    vec3 i = floor(x);
    vec3 f = fract(x);
    f = f * f * (3.0 - 2.0 * f);

    return mix(
        mix(
            mix(hash3(i + vec3(0.0, 0.0, 0.0)), hash3(i + vec3(1.0, 0.0, 0.0)), f.x),
            mix(hash3(i + vec3(0.0, 1.0, 0.0)), hash3(i + vec3(1.0, 1.0, 0.0)), f.x),
            f.y
        ),
        mix(
            mix(hash3(i + vec3(0.0, 0.0, 1.0)), hash3(i + vec3(1.0, 0.0, 1.0)), f.x),
            mix(hash3(i + vec3(0.0, 1.0, 1.0)), hash3(i + vec3(1.0, 1.0, 1.0)), f.x),
            f.y
        ),
        f.z
    );
}

void main(){

    float t = GameTime * 2400.0 * 0.5;

    // slow Lissajous drift, kept well inside frame so the blob never sits right at the edge
    vec2 center = vec2(0.5 + sin(t * 0.13) * 0.28, 0.5 + cos(t * 0.19) * 0.22);

    vec2 uv = vec2(texCoord.x * Aspect, texCoord.y);
    vec2 centerA = vec2(center.x * Aspect, center.y);
    vec2 d = uv - centerA;

    // noise-churned radius, so the silhouette boils/breathes instead of staying a perfect circle
    float wobble = noise3(vec3(normalize(d + 1e-5) * 3.0, t * 0.3)) * 2.0 - 1.0;
    float radius = BASE_RADIUS * (1.0 + wobble * 0.25);

    float distSq = dot(d, d);
    float falloff = clamp(1.0 - distSq / (radius * radius), 0.0, 1.0);
    float influence = falloff * falloff * falloff;

    // pull the sample toward the blob's center, same bendFactor trick molten_glass.fsh uses, so
    // the world visibly bulges/magnifies through the orb instead of just tinting flatly on top
    vec2 toCenter = texCoord - center;
    float bendFactor = mix(1.0, 1.0 - FISHEYE_STRENGTH, influence);
    vec2 distortedUV = center + toCenter * bendFactor;

    vec3 scene = texture(InSampler, distortedUV).rgb;

    // a little inner churn so the tint itself isn't flat, reusing this file's own noise3 instead
    // of pulling in plasma.fsh's sine recipe like molten_glass does
    float churn = noise3(vec3(distortedUV * 5.0, t * 0.5));
    vec3 tint = mix(vec3(0.1, 0.55, 0.5), vec3(0.5, 1.0, 0.85), churn);

    // capped well below 1 so even the orb's densest core stays translucent; real world always
    // shows through, more so toward the blob's edge
    vec3 blended = mix(scene, tint, influence * 0.7);

    // approximate lens normal, radially outward from the blob's center; same cheap shortcut
    // molten_glass.fsh uses instead of a real finite-difference gradient
    vec2 aspectToCenter = toCenter;
    aspectToCenter.x *= Aspect;
    vec3 normal = normalize(vec3(-(aspectToCenter / max(radius, 0.001)) * influence * 0.6, 1.0));
    vec3 lightDir = normalize(vec3(sin(t * 0.2) * 0.5, 0.6, 0.7));
    float specular = pow(max(dot(normal, lightDir), 0.0), 24.0) * influence;

    vec3 outColor = blended + specular * 0.4;

    fragColor = vec4(outColor, 1.0);
}

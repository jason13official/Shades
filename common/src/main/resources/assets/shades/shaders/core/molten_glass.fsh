#version 330

// round, soft lava-lamp blobs (Wyvill-style bounded falloff), each also a genuine fisheye/lens
// bulge: wherever a blob dominates a pixel, the sample position gets pulled toward that blob's
// center (a spherical-lens compression), so looking at one visibly warps/magnifies the world
// behind it. Color follows a clear -> orange -> red -> white-hot ramp with plasma churn mixed in
#moj_import <minecraft:globals.glsl>

uniform sampler2D InSampler;

// real window aspect ratio + a smoothed screen-space sway opposite the camera's current yaw/pitch
// swing; the whole blob field lags behind a camera turn like it has real weight, then eases back
// as the turn settles
layout(std140) uniform GlassConfig {
    float Aspect;
    float SwayX;
    float SwayY;
};

in vec2 texCoord;
out vec4 fragColor;

const int BLOB_COUNT = 8;
const float FISHEYE_STRENGTH = 0.45;
const float NORMAL_STRENGTH = 0.6;

// same sum-of-sines recipe plasma.fsh uses, reused here as a subtle churning inner light inside
// the molten blobs, so they don't read as a flat color ramp
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

// clear -> orange -> red -> white-hot. s=0 isn't used directly as a color (main() blends it in at
// zero opacity there), it only matters from just-above-0 upward
vec3 hotRamp(float s) {

    vec3 orange = vec3(1.0, 0.45, 0.05);
    vec3 red = vec3(0.95, 0.12, 0.04);
    vec3 white = vec3(1.0, 0.95, 0.85);

    if (s < 0.5) {
        return mix(orange, red, s / 0.5);
    }
    return mix(red, white, (s - 0.5) / 0.5);
}

// each blob's base position/radius, driven only by its index and time (plus the camera-sway
// offset); a single source of truth so every pass below agrees on where a blob actually is
vec2 blobPos(int i, float t, out float radius) {

    float fi = float(i);
    // an irrational-ish per-blob offset so blobs don't rise/sway/pulse in lockstep
    float seed = fi * 2.399963;

    // loops from just below the screen to just above it and wraps; a one-way loop reads close
    // enough to a real lava lamp's rise-and-cool cycle and needs no state
    float riseSpeed = 0.02 + 0.005 * fi;
    float riseT = fract(t * riseSpeed + seed * 0.61803);
    float y = mix(-0.3, 1.3, riseT);

    float sway = sin(t * 0.02 + seed) * 0.10;
    float x = fract((fi + 0.5) / float(BLOB_COUNT) + sway);
    radius = 0.13 + 0.045 * (0.5 + 0.5 * sin(seed)) + 0.02 * sin(t * 0.05 + seed * 1.7);

    return vec2(x + SwayX, y + SwayY);
}

// classic metaball field using the smooth Wyvill/Blinn bounded falloff (x^3, 1 at the blob's
// center fading to 0 at its edge) instead of a raw inverse-square, for genuinely soft edges. Also
// tracks the single most-dominant blob at this pixel; that blob is what the fisheye bulge centers on
void fieldAt(vec2 uv, float t, out float heat, out vec2 dominantPos, out float dominantRadius, out float dominantInfluence) {

    heat = 0.0;
    dominantInfluence = 0.0;
    dominantPos = uv;
    dominantRadius = 0.001;

    for (int i = 0; i < BLOB_COUNT; i++) {

        float baseRadius;
        vec2 pos = blobPos(i, t, baseRadius);
        float seed = float(i) * 2.399963;

        // aspect-correct BEFORE measuring distance/angle, so the blob is round in real screen
        // space instead of stretched to match the window's UV aspect
        vec2 d = uv - pos;
        d.x *= Aspect;

        // bend the radius per-angle; two overlapping wobble frequencies, slowly writhing over
        // time, turn what would otherwise be a perfect circle into a lumpy blob silhouette
        float angle = atan(d.y, d.x);
        float wobble = 1.0
            + 0.16 * sin(angle * 3.0 + seed * 5.1 + t * 0.08)
            + 0.09 * sin(angle * 5.0 - seed * 2.3 + t * 0.05);
        float effectiveRadius = baseRadius * wobble;

        float distSq = dot(d, d);
        float falloff = clamp(1.0 - distSq / (effectiveRadius * effectiveRadius), 0.0, 1.0);
        float influence = falloff * falloff * falloff;

        heat += influence;

        if (influence > dominantInfluence) {
            dominantInfluence = influence;
            dominantPos = pos;
            dominantRadius = effectiveRadius;
        }
    }
}

void main(){

    float t = GameTime * 2400.0;

    float heatCenter, dominantRadius, dominantInfluence;
    vec2 dominantPos;
    fieldAt(texCoord, t, heatCenter, dominantPos, dominantRadius, dominantInfluence);

    // pull the sample position toward the dominant blob's center; strongest exactly at the
    // center and easing to none past the blob's edge, so the world visibly bulges/magnifies
    // through each blob instead of just refracting at its rim
    vec2 toCenter = texCoord - dominantPos;
    float bendFactor = mix(1.0, 1.0 - FISHEYE_STRENGTH, dominantInfluence);
    vec2 distortedUV = dominantPos + toCenter * bendFactor;

    vec3 scene = texture(InSampler, distortedUV).rgb;

    // re-evaluate at the bent position too, so the visible molten shape itself warps through its
    // own lens instead of just the background behind it
    float heatDistorted, dominantRadius2, dominantInfluence2;
    vec2 dominantPos2;
    fieldAt(distortedUV, t, heatDistorted, dominantPos2, dominantRadius2, dominantInfluence2);

    float heat = smoothstep(0.10, 0.55, heatDistorted);

    vec3 hot = hotRamp(heat);
    vec3 churn = plasmaColor(distortedUV * 4.0, t * 1.3);
    hot = mix(hot, hot * (0.6 + churn * 0.8), 0.3 * heat);

    // capped below 1.0 so even a blob's own white-hot core stays translucent instead of a flat
    // opaque cutout; heat itself (uncapped) still drives the color ramp, just not the opacity
    vec3 blended = mix(scene, hot, heat * 0.8);

    // an approximate lens normal, pointing away from the dominant blob's center, scaled by how
    // dominant it is; good enough for a specular glint without a real analytic gradient
    vec2 aspectToCenter = toCenter;
    aspectToCenter.x *= Aspect;
    vec3 normal = normalize(vec3(-(aspectToCenter / max(dominantRadius, 0.001)) * dominantInfluence * NORMAL_STRENGTH, 1.0));

    // light slowly orbits, so the specular glint sweeps over time instead of pinning to one
    // direction; gated mostly (not entirely) by heat, so clear glass still shows a faint shine
    vec3 lightDir = normalize(vec3(sin(t * 0.15) * 0.6 - 0.2, 0.5, 0.75));
    float diffuse = max(dot(normal, lightDir), 0.0);
    vec3 viewDir = vec3(0.0, 0.0, 1.0);
    vec3 halfVec = normalize(lightDir + viewDir);
    float specular = pow(max(dot(normal, halfVec), 0.0), 30.0) * (0.3 + 0.7 * heat);

    vec3 outColor = blended * (0.6 + diffuse * 0.5) + specular * 0.5;

    fragColor = vec4(outColor, 1.0);
}

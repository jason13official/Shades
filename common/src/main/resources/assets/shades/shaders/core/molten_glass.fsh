#version 330

// same derivative-as-normal refraction idea as fluted_glass_vision, but instead of a fixed
// vertical ridge pattern across the whole screen, the "glass surface" IS the lava-lamp metaball
// field itself - its spatial gradient (how fast the field rises toward a blob's center) becomes
// the surface normal, so the bending only happens right around each blob's rim, like a lens
// sitting over a glowing blob, and is flat (no distortion) anywhere the liquid is empty.
//
// on top of that base idea: each blob's interior churns with plasma.fsh's sum-of-sines instead of
// a flat two-tone gradient, the rim cycles an iridescent oil-slick hue instead of one fixed color,
// and nearby blobs occasionally arc a flickering bolt of energy between them - "lava lamp" as a
// connected, living system instead of five independent static blobs
#moj_import <minecraft:globals.glsl>

uniform sampler2D InSampler;

// real window aspect ratio, pushed fresh each frame by ShadesClient - UV space is 0..1 in both
// axes regardless of the window's real pixel aspect, so blob distances need this correction or
// every blob renders as a wide ellipse instead of a circle.
//
// SwayX/SwayY are a smoothed screen-space offset opposite the camera's current yaw/pitch swing
// (see ShadesClient#buildGlassUniform) - the whole blob field visibly lags behind a camera turn
// like it has real inertia, then eases back to center as the turn settles
layout(std140) uniform GlassConfig {
    float Aspect;
    float SwayX;
    float SwayY;
};

in vec2 texCoord;
out vec4 fragColor;

const float PI = 3.14159265;

const float DISTORTION_AMOUNT = 0.025;
const float NORMAL_STRENGTH = 0.4;
const float GRADIENT_EPSILON = 0.004;

const int BLOB_COUNT = 10;
const vec3 LIQUID_COLOR = vec3(0.08, 0.02, 0.08);
const vec3 BLOB_CORE_COLOR = vec3(1.0, 0.7, 0.25);
const vec3 ARC_COLOR = vec3(0.55, 0.85, 1.0);

// each blob's base position/radius, driven only by its index and time - shared by fieldAt() and
// arcGlow() so both agree on where a blob actually is without duplicating the math
vec2 blobPos(int i, float t, out float radius) {

    float fi = float(i);
    // an irrational-ish per-blob offset so the blobs don't all rise/sway/pulse in lockstep
    float seed = fi * 2.399963;

    // loops from just below the screen to just above it and wraps - real lava lamp blobs sink
    // again after cooling at the top, but a one-way loop reads close enough and needs no state
    float riseSpeed = 0.025 + 0.006 * fi;
    float riseT = fract(t * riseSpeed + seed * 0.61803);
    float y = mix(-0.25, 1.25, riseT);

    float sway = sin(t * 0.02 + seed) * 0.10;
    float x = fract((fi + 0.5) / float(BLOB_COUNT) + sway);
    radius = 0.09 + 0.03 * (0.5 + 0.5 * sin(seed)) + 0.015 * sin(t * 0.05 + seed * 1.7);

    // camera-inertia offset applied AFTER the wrap, so it's a real screen-space shift of the
    // whole field rather than something that folds back into the per-blob column position
    return vec2(x + SwayX, y + SwayY);
}

// classic metaball field, using the smooth Wyvill/Blinn bounded falloff (t^3, t = 1 at the blob's
// center fading smoothly to 0 at its edge) instead of a raw inverse-square - inverse-square spikes
// so hard near the center that any reasonable smoothstep width still reads as a near-hard edge;
// this kernel is gentle and bounded everywhere, which is what actually makes the blob soft.
// coreField sums the same per-blob term squared, so it only climbs fast where a blob's contribution
// is already high (its own center, or where several overlap), giving a hotter inner color for free
void fieldAt(vec2 uv, float t, out float field, out float coreField) {

    field = 0.0;
    coreField = 0.0;

    for (int i = 0; i < BLOB_COUNT; i++) {

        float baseRadius;
        vec2 pos = blobPos(i, t, baseRadius);
        float seed = float(i) * 2.399963;

        // aspect-correct BEFORE measuring distance/angle, so the blob is round in real screen
        // space instead of stretched to match the window's UV aspect
        vec2 d = uv - pos;
        d.x *= Aspect;

        // bend the radius per-angle instead of using it as-is - two overlapping wobble frequencies,
        // slowly writhing over time, turn what would otherwise be a perfect circle into an
        // amorphous, slightly lumpy blob silhouette, like a real glob of wax
        float angle = atan(d.y, d.x);
        float wobble = 1.0
            + 0.18 * sin(angle * 3.0 + seed * 5.1 + t * 0.08)
            + 0.10 * sin(angle * 5.0 - seed * 2.3 + t * 0.05);
        float effectiveRadius = baseRadius * wobble;

        float distSq = dot(d, d);
        float falloff = clamp(1.0 - distSq / (effectiveRadius * effectiveRadius), 0.0, 1.0);
        float influence = falloff * falloff * falloff;

        field += influence;
        coreField += influence * influence;
    }
}

// identical recipe to plasma.fsh's main() body - see that file for the full per-line writeup.
// used here as the churning interior color instead of plasma's own lens, masked to only show up
// inside a blob
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

// cheap deterministic 0..1 "random" from a float seed, used to flicker arcs on/off per second
// instead of leaving them constantly on
float hash(float n) {
    return fract(sin(n) * 43758.5453);
}

vec3 hsv2rgb(vec3 c) {
    vec4 k = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + k.xyz) * 6.0 - k.www);
    return c.z * mix(k.xxx, clamp(p - k.xxx, 0.0, 1.0), c.y);
}

// checks every pair of blobs and, when two are close enough, draws a thin jittering bolt between
// their centers - a straight line perturbed sideways by a couple of sine octaves, tapered to zero
// at both endpoints so it visibly anchors to each blob instead of floating free
float arcGlow(vec2 uv, float t) {

    vec2 p = uv;
    p.x *= Aspect;

    float glow = 0.0;

    for (int i = 0; i < BLOB_COUNT - 1; i++) {
        float radiusA;
        vec2 a = blobPos(i, t, radiusA);
        a.x *= Aspect;

        for (int j = i + 1; j < BLOB_COUNT; j++) {
            float radiusB;
            vec2 b = blobPos(j, t, radiusB);
            b.x *= Aspect;

            float pairDist = distance(a, b);
            float proximityFade = 1.0 - smoothstep(0.22, 0.5, pairDist);
            if (proximityFade <= 0.0) {
                continue;
            }

            float seedPair = float(i) * 7.13 + float(j) * 3.71;

            // flickers on/off roughly once a second instead of arcing continuously
            float flicker = step(0.6, hash(seedPair * 12.9898 + floor(t * 1.5)));
            if (flicker <= 0.0) {
                continue;
            }

            vec2 ab = b - a;
            float abLen = max(length(ab), 0.0001);
            vec2 abDir = ab / abLen;
            float along = clamp(dot(p - a, abDir), 0.0, abLen) / abLen;
            vec2 pointOnLine = a + abDir * (along * abLen);

            vec2 perp = vec2(-abDir.y, abDir.x);
            float taper = sin(along * PI);
            float jitter = (sin(along * 24.0 + t * 4.0 + seedPair) * 0.6
                + sin(along * 53.0 - t * 6.0 + seedPair * 1.7) * 0.4) * 0.025 * taper;

            vec2 jitteredPoint = pointOnLine + perp * jitter;
            float d = length(p - jitteredPoint);
            float line = smoothstep(0.006, 0.0, d);

            glow = max(glow, line * proximityFade);
        }
    }

    return glow;
}

void main(){

    float t = GameTime * 2400.0;

    // sample the (undistorted) field at the pixel and two tiny steps away, so the difference
    // approximates how fast it's rising here - exactly the "slope of the glass" fluted_glass_vision
    // gets from a cosine, just read off the blob shapes themselves instead of a fixed wave
    float fieldCenter, coreCenter;
    float fieldX, coreX;
    float fieldY, coreY;
    fieldAt(texCoord, t, fieldCenter, coreCenter);
    fieldAt(texCoord + vec2(GRADIENT_EPSILON, 0.0), t, fieldX, coreX);
    fieldAt(texCoord + vec2(0.0, GRADIENT_EPSILON), t, fieldY, coreY);

    vec2 gradient = vec2(fieldX - fieldCenter, fieldY - fieldCenter) / GRADIENT_EPSILON;
    vec3 normal = normalize(vec3(-gradient * NORMAL_STRENGTH, 1.0));

    // flat everywhere the liquid is empty (gradient ~0, normal ~straight at the camera), bending
    // hardest right at a blob's rim where the field climbs fastest
    vec2 distortedUV = texCoord + normal.xy * DISTORTION_AMOUNT;

    vec3 scene = texture(InSampler, distortedUV).rgb;

    // re-sample the field at the bent position too, so the visible blob shapes themselves warp
    // through the "lens" instead of just the background behind them
    float fieldDistorted, coreDistorted;
    fieldAt(distortedUV, t, fieldDistorted, coreDistorted);

    // wide-but-lower smoothstep range than a first pass would suggest - the bounded falloff kernel
    // peaks at 1.0 only exactly at a blob's center, so a high floor here left nearly the whole body
    // translucent and only the center readable; starting low keeps most of the interior solid
    // while the true edge still fades out smoothly
    float blobMask = smoothstep(0.10, 0.55, fieldDistorted);
    float coreMask = smoothstep(0.30, 0.85, coreDistorted);

    // interior churns with the plasma field instead of a flat gradient, with a hot-core color
    // biased in on top where coreMask is high
    vec3 interior = plasmaColor(distortedUV * 4.0, t * 1.3);
    vec3 blobColor = mix(interior, BLOB_CORE_COLOR, coreMask * 0.6);

    // the rim - the band where the field is transitioning, not fully in or out - gets an
    // iridescent, slowly hue-cycling sheen instead of one fixed color, like light catching an
    // oil-slick skin around the blob
    float rimBand = blobMask * (1.0 - smoothstep(0.55, 0.85, fieldDistorted));
    float hue = fract(t * 0.05 + fieldDistorted * 0.4);
    vec3 iridescent = hsv2rgb(vec3(hue, 0.75, 1.0));
    blobColor = mix(blobColor, iridescent, rimBand * 0.8);

    // only tint toward the dark "liquid" a little where there's no blob at all, instead of
    // dumping the whole screen 85% of the way to near-black - the real scene should stay readable
    // everywhere except right where a blob actually is
    vec3 ambient = mix(scene, LIQUID_COLOR, 0.15);
    vec3 blended = mix(ambient, blobColor, blobMask * 0.92);

    // light slowly orbits, so the specular rim glinting off each blob sweeps over time instead of
    // pinning to one fixed direction
    vec3 lightDir = normalize(vec3(sin(t * 0.15) * 0.6 - 0.2, 0.5, 0.75));
    float diffuse = max(dot(normal, lightDir), 0.0);
    vec3 viewDir = vec3(0.0, 0.0, 1.0);
    vec3 halfVec = normalize(lightDir + viewDir);
    float specular = pow(max(dot(normal, halfVec), 0.0), 30.0);

    vec3 outColor = blended * (0.6 + diffuse * 0.5) + specular * 0.5;

    // electric arcs added last, on top of the lit glass, so they read as their own glowing energy
    // rather than something the glass shading dims
    outColor += ARC_COLOR * arcGlow(texCoord, t);

    fragColor = vec4(outColor, 1.0);
}

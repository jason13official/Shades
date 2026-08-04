#version 330

// reworked shape: v1/v2 built the pattern from a reflect+spherical-inversion warp, which reads as
// a twisted, organic knot, trippy but unlike anything else's silhouette in this mod. This version
// keeps the same interference-pattern coloring but lays it out as an actual flat disc instead: a
// polar grid of voxel cells (radius/angle bins, same idea as kaleidoscope_vision's polar fold),
// each cell pseudo-randomly offset in "height" and shaded from that offset (same fake-lighting
// idea lego_vision's studs use), with thin dark seams between cells (receipt/halftone/lego's
// cell-edge trick). A single per-cell evaluation replaces the old 80-step raymarch entirely, same
// visual family, far cheaper, and a much flatter, more geometric read
#moj_import <minecraft:globals.glsl>

uniform sampler2D InSampler;
uniform sampler2D InDepthSampler;

// InverseTransformMatrix + CameraPosition: combined inverse-projection*view matrix + camera
// position, for worldPos() (screen -> world). Aspect: real window aspect ratio, placed before the
// vec3 so Std140Builder's padding matches GLSL's own (scalars must precede vectors)
layout(std140) uniform VoxelRay {
    mat4 InverseTransformMatrix;
    float Aspect;
    vec3 CameraPosition;
};

in vec2 texCoord;
out vec4 fragColor;

#define PI 3.14159265

float hash2(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453123);
}

float sum3(vec3 x) {
    return dot(x, vec3(1.0));
}

vec3 worldPos(vec3 screenPoint) {
    vec3 ndc = screenPoint * 2.0 - 1.0;
    vec4 homPos = InverseTransformMatrix * vec4(ndc, 1.0);
    return homPos.xyz / homPos.w + CameraPosition;
}

// the same 6-wave interference recipe v1/v2 used, evaluated once per voxel cell instead of
// accumulated over 80 raymarch steps
vec3 cellColor(vec2 cellId, float phase) {
    vec3 p = vec3(cellId * 0.5, phase);
    vec3 u = vec3(p.y + p.z, p.x + p.y, p.z + p.x);
    vec3 w = vec3(p.z - p.y, p.y - p.x, p.x - p.z);
    float c = sum3(cos(u)) + sum3(cos(w));
    float s = sum3(sin(u)) + sum3(sin(w));
    return 0.5 + 0.5 * sin(atan(s, c) + vec3(0.0, 2.0, 4.0));
}

// disc color+coverage at a given screen point, premultiplied by alpha (so the small blur in
// main() below can average several taps without black fringing where taps land outside the disc)
vec4 discAt(vec2 tc, float seconds, float realDist, float distFade) {

    vec2 uv = (tc * 2.0 - 1.0) * vec2(Aspect, 1.0);
    float radius = length(uv);
    float angle = atan(uv.y, uv.x);

    // screen corners sit at radius sqrt(Aspect^2+1) in this aspect-corrected space; derive from
    // the real Aspect (with a small margin) instead of a fixed guess, so every corner is always
    // covered regardless of window shape/ultrawide monitors, not just the common 16:9 case
    float discRadius = sqrt(Aspect * Aspect + 1.0) * 1.08 + sin(seconds * 0.3) * 0.05;

    if (radius > discRadius || distFade <= 0.0) {
        return vec4(0.0);
    }

    // bumped up from the original 8/28 to keep voxel granularity consistent now that the disc
    // covers a much bigger footprint
    const float RADIAL_CELLS = 12.0;
    const float ANGULAR_CELLS = 36.0;

    float rCellF = (radius / discRadius) * RADIAL_CELLS;
    float rCell = floor(rCellF);

    // slow rotation over time
    float aNorm = fract((angle + PI) / (2.0 * PI) + seconds * 0.05);
    float aCellF = aNorm * ANGULAR_CELLS;
    float aCell = floor(aCellF);

    // pseudo-random per-cell "height"; real distance nudges it too, so what's actually behind
    // the disc subtly shapes its own pattern instead of the disc being purely screen-space
    float cellOffset = hash2(vec2(rCell, aCell) + floor(realDist));

    vec3 col = cellColor(vec2(rCell, aCell), seconds * 0.4 + cellOffset * 3.0);
    col *= 0.6 + cellOffset * 0.6;

    // thin dark seams between cells
    vec2 cellUV = vec2(fract(rCellF), fract(aCellF));
    float seam = 1.0 - smoothstep(0.0, 0.05, min(min(cellUV.x, 1.0 - cellUV.x), min(cellUV.y, 1.0 - cellUV.y)));
    col = mix(col, vec3(0.0), seam * 0.5);

    // soft fade right at the disc's outer edge instead of a hard circular cutoff
    float edgeFade = 1.0 - smoothstep(discRadius * 0.85, discRadius, radius);

    float alpha = edgeFade * distFade * 0.85;
    return vec4(col * alpha, alpha);
}

void main(){

    float rawDepth = texture(InDepthSampler, texCoord).r;
    vec3 scene = texture(InSampler, texCoord).rgb;

    float seconds = GameTime * 2400.0 * 0.5;

    // sky reads as "very far" rather than being excluded outright; previously a hard cutoff, so
    // the disc looked like a full circle when aimed at the ground but vanished completely aimed
    // at the sky; treating sky as a large-but-finite distance instead lets the SAME fade already
    // used near the horizon taper it into a faint ghost over open sky too, instead of a binary
    // full-circle-or-nothing jump
    float realDist;
    if (rawDepth >= 0.9999) {
        realDist = 40.0;
    } else {
        vec3 surfacePos = worldPos(vec3(texCoord, rawDepth));
        realDist = length(surfacePos - CameraPosition);
    }

    // fades out with real range, so the disc reads as anchored near real surfaces rather than a
    // fixed screen-space sticker; wider than the disc's old radius since a much bigger disc now
    // reaches farther-away ground (and now sky) that a tight fade would just kill again
    float distFade = 1.0 - smoothstep(0.0, 56.0, realDist);

    // a very small 5-tap box blur, softening the cell seams and cell-to-cell color jumps that
    // otherwise read as a bit harsh/vector-crisp next to this mod's softer effects
    const float R = 0.0025;
    vec4 acc = discAt(texCoord, seconds, realDist, distFade);
    acc += discAt(texCoord + vec2(R, 0.0), seconds, realDist, distFade);
    acc += discAt(texCoord - vec2(R, 0.0), seconds, realDist, distFade);
    acc += discAt(texCoord + vec2(0.0, R), seconds, realDist, distFade);
    acc += discAt(texCoord - vec2(0.0, R), seconds, realDist, distFade);
    acc *= 0.2;

    float alpha = acc.a;
    vec3 patternColor = acc.rgb / max(alpha, 0.0001);
    vec3 outColor = mix(scene, patternColor, alpha);

    fragColor = vec4(outColor, 1.0);
}

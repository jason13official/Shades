#version 330

// This version is fully per-pixel instead: every pixel reconstructs its OWN real world position
// (same worldPos() technique grid_shades/fluid_shades/sonar_shades use, no fixed row involved at
// all) and lights up if that real point sits near a slowly-oscillating target elevation around eye
// level - a literal glowing contour line sweeping up and down through the real terrain, like a
// depth-scan readout. Fully local/per-pixel, so it can't break from camera orientation the way a
// fixed-row sample could
#moj_import <minecraft:globals.glsl>

uniform sampler2D InSampler;
uniform sampler2D InDepthSampler;
uniform sampler2D PrevFrameSampler;

// InverseTransformMatrix: combined inverse-projection*view matrix, for worldPos() (screen -> world),
// same as sonar.fsh/grid.fsh. ForwardTransformMatrix: the same matrix WITHOUT inverting, for going
// the other direction (world -> screen) - used to find which real screen row the scan altitude's
// crossing plane actually projects to, so the full-screen flash below can track it instead of
// sitting at a hardcoded row. Both pushed fresh each frame by ShadesClient
layout(std140) uniform WaveformRay {
    mat4 InverseTransformMatrix;
    mat4 ForwardTransformMatrix;
    vec3 CameraPosition;
};

in vec2 texCoord;
out vec4 fragColor;

vec3 hsv2rgb(vec3 c) {
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

vec3 worldPos(vec3 screenPoint) {
    vec3 ndc = screenPoint * 2.0 - 1.0;
    vec4 homPos = InverseTransformMatrix * vec4(ndc, 1.0);
    return homPos.xyz / homPos.w + CameraPosition;
}

// world -> screen, the inverse of worldPos() above. `valid` is false for points behind the camera
// (or right on the eye), where a naive perspective divide would blow up/flip
vec2 screenPos(vec3 point, out bool valid) {
    vec4 clip = ForwardTransformMatrix * vec4(point - CameraPosition, 1.0);
    valid = clip.w > 0.001;
    if (!valid) {
        return vec2(-10.0);
    }
    vec3 ndc = clip.xyz / clip.w;
    return ndc.xy * 0.5 + 0.5;
}

void main(){

    float t = GameTime * 2400.0 * 0.5;
    vec3 scene = texture(InSampler, texCoord).rgb;

    // the scan altitude oscillates around eye level; `phase` reaching 0 is the instant the sweep
    // is exactly AT the camera's own real height
    float phase = sin(t * 0.12);
    float target = CameraPosition.y + phase * 4.0;

    // real camera forward direction (accounts for actual pitch+yaw), from unprojecting two points
    // straight ahead on-screen at different depths - then find where a point 20 blocks ahead,
    // height-overridden to the current scan altitude, actually lands on screen. This is what lets
    // the full-screen crossing flash below scroll/move with the real camera and the real scan
    // altitude instead of sitting frozen at a hardcoded row
    vec3 aheadNear = worldPos(vec3(0.5, 0.5, 0.0));
    vec3 aheadFar = worldPos(vec3(0.5, 0.5, 1.0));
    vec3 forwardDir = normalize(aheadFar - aheadNear);

    vec3 targetPoint = CameraPosition + forwardDir * 20.0;
    targetPoint.y = target;

    bool flashValid;
    vec2 flashScreenPos = screenPos(targetPoint, flashValid);

    // Partial throwback to the very first version of this shader (a flat full-width screen-space
    // band, see SUMMARY.md): right at the crossing instant, punch a real full-screen horizontal
    // band across the whole view (sky included, not gated by depth at all) on top of the normal
    // per-pixel world contour below, so "the band passing over you" reads as a genuine full-screen
    // event instead of only ever a thin line traced onto real geometry far away.
    //
    // the band's screen-space THICKNESS scales with crossIntensity too, not just its alpha - a
    // fixed-thickness line never reads as "washing over the camera" no matter how bright it gets,
    // since the world band is only ~0.3 blocks thick and the camera is a single point: being
    // "inside" a thin plane at your own eye height should fill most of your view, not draw a
    // hairline. Thin/subtle far from the crossing, growing toward covering most of the screen
    // right as the scan passes through the camera's real position
    float crossIntensity = 1.0 - smoothstep(0.0, 0.08, abs(phase));
    float bandThickness = mix(0.015, 0.5, crossIntensity);
    float screenBand = flashValid ? 1.0 - smoothstep(0.0, bandThickness, abs(texCoord.y - flashScreenPos.y)) : 0.0;
    vec3 flashColor = hsv2rgb(vec3(fract(t / 6.0), 0.85, 1.0));
    float flashAlpha = clamp(screenBand * crossIntensity * 0.75, 0.0, 0.75);

    float rawDepth = texture(InDepthSampler, texCoord).r;

    // sky/no real geometry here -> nothing to contour, but the full-screen flash can still show
    if (rawDepth >= 0.9999) {
        fragColor = vec4(mix(scene, flashColor, flashAlpha), 1.0);
        return;
    }

    vec3 surfacePos = worldPos(vec3(texCoord, rawDepth));
    float diff = surfacePos.y - target;

    float core = 1.0 - smoothstep(0.0, 0.3, abs(diff));
    float glow = exp(-abs(diff) * 8.0) * 0.3;

    vec3 traceColor = hsv2rgb(vec3(fract(t / 6.0 + surfacePos.x * 0.01 + surfacePos.z * 0.01), 0.75, 1.0));
    vec3 trace = traceColor * (core + glow);

    // fading trail: real terrain doesn't move, but the scan altitude does, so a given surface
    // point only lights up while the sweep passes near its real height
    vec3 prev = texture(PrevFrameSampler, texCoord).rgb;
    vec3 accumulated = clamp(trace + prev * 0.85, 0.0, 1.0);

    // real background stays at full brightness everywhere, and the trace is always translucent
    // (capped alpha, never a hard opaque overwrite) rather than a flat max() - close-up geometry
    // (e.g. the contour crossing right at your feet) would otherwise read as a harsh saturated
    // edge since near objects cover far more screen pixels at full core intensity
    float alpha = clamp(max(max(accumulated.r, accumulated.g), accumulated.b) * 0.7, 0.0, 0.65);
    vec3 outColor = mix(scene, accumulated, alpha);

    // layer the full-screen crossing flash on top of the normal per-pixel contour result
    outColor = mix(outColor, flashColor, flashAlpha);

    fragColor = vec4(outColor, 1.0);
}

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

// combined inverse-projection*view matrix + camera position, pushed fresh each frame by
// ShadesClient - same worldPos() reconstruction sonar.fsh/grid.fsh use
layout(std140) uniform WaveformRay {
    mat4 InverseTransformMatrix;
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

void main(){

    float t = GameTime * 2400.0 * 0.5;
    float rawDepth = texture(InDepthSampler, texCoord).r;

    vec3 scene = texture(InSampler, texCoord).rgb;

    // sky/no real geometry here -> nothing to contour, leave untouched
    if (rawDepth >= 0.9999) {
        fragColor = vec4(scene, 1.0);
        return;
    }

    vec3 surfacePos = worldPos(vec3(texCoord, rawDepth));

    // the "scan altitude" slowly oscillates around eye level, so the contour sweeps up and down
    // through real terrain/buildings over time instead of sitting at one fixed height
    float target = CameraPosition.y + sin(t * 0.3) * 4.0;
    float diff = surfacePos.y - target;

    float core = 1.0 - smoothstep(0.0, 0.12, abs(diff));
    float glow = exp(-abs(diff) * 8.0) * 0.3;

    vec3 traceColor = hsv2rgb(vec3(fract(t / 6.0 + surfacePos.x * 0.01 + surfacePos.z * 0.01), 0.75, 1.0));
    vec3 trace = traceColor * (core + glow);

    // fading trail: real terrain doesn't move, but the scan altitude does, so a given surface
    // point only lights up while the sweep passes near its real height
    vec3 prev = texture(PrevFrameSampler, texCoord).rgb;
    vec3 accumulated = clamp(trace + prev * 0.85, 0.0, 1.0);

    // real background stays at full brightness everywhere - the trace only ever brightens on top
    // of it, never dims it
    vec3 outColor = max(scene, accumulated);
    fragColor = vec4(outColor, 1.0);
}

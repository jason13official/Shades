#version 330

// reworked twice: v1 was a pure screen-space rotating/scaling/translating sine clip (an IQ-style
// implicit-SDF trace) - all three motions changed too slowly to move the visible segment
// noticeably frame-to-frame, so it read as one frozen thin streak. v2 replaced that with an
// explicit full-width scrolling sine curve, which animated fine but had no tie to the real world.
// This version reads real per-column world data instead of a canned formula: for each screen
// column, sample real depth along a fixed center row to find how far above/below eye level
// whatever's really there is, and use THAT as the signal driving the traced curve - a genuine
// oscilloscope reading of the real world's silhouette, not a synthetic waveform laid over it.
// Same worldPos() reconstruction grid_shades/fluid_shades/sonar_shades all use. Turning your head
// visibly reshapes the trace since it's tracking real geometry; standing still lets the trail
// saturate into a bright, still hue-cycling outline instead of fading - same "CRT phosphor stays
// lit on an unchanging signal" behavior a real scope would show
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

    // one real-world sample per column, always along a fixed center row -> the value that drives
    // the traced curve at this x, regardless of which row we're actually shading right now
    float refDepth = texture(InDepthSampler, vec2(texCoord.x, 0.5)).r;

    float signal = 0.0;
    if (refDepth < 0.9999) {
        vec3 refPos = worldPos(vec3(texCoord.x, 0.5, refDepth));
        // real height relative to eye level, normalized into a soft -1..1 range
        signal = clamp((refPos.y - CameraPosition.y) / 10.0, -1.0, 1.0);
    }

    float waveY = 0.5 + signal * 0.3;
    float dist = abs(texCoord.y - waveY);
    float core = 1.0 - smoothstep(0.0, 0.0035, dist);
    float glow = exp(-dist * 60.0) * 0.5;

    vec3 traceColor = hsv2rgb(vec3(fract(t / 6.0 + texCoord.x * 0.2), 0.75, 1.0));
    vec3 trace = traceColor * (core + glow);

    vec3 prev = texture(PrevFrameSampler, texCoord).rgb;
    vec3 accumulated = clamp(trace + prev * 0.85, 0.0, 1.0);

    vec3 scene = texture(InSampler, texCoord).rgb;
    vec3 outColor = max(scene * 0.4, accumulated);

    fragColor = vec4(outColor, 1.0);
}

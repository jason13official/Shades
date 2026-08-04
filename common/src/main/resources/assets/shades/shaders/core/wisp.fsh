#version 330

// 9 points of warm light tracing sine-wave paths, glow accumulated via inverse-distance falloff.
// Fully synthetic in the original (a flat black background); composited here as an additive glow
// on top of the real world instead, so it reads as floating wisps of light rather than a replace
#moj_import <minecraft:globals.glsl>

uniform sampler2D InSampler;

// real window aspect ratio, pushed fresh each frame by ShadesClient
layout(std140) uniform WispConfig {
    float Aspect;
};

in vec2 texCoord;
out vec4 fragColor;

const int POINT_COUNT = 9;
const float FREQ = 20.0;
const float SPEED = 1.6;
const float PHASE_SPREAD = 9.0;
const vec3 WISP_COLOR = vec3(251.0, 255.0, 0.0) / 255.0;

void main(){

    float seconds = GameTime * 2400.0 * 0.5;
    vec2 uv = (texCoord * 2.0 - 1.0) * vec2(Aspect, 1.0);

    vec3 glow = vec3(0.0);
    for (int i = 0; i < POINT_COUNT; i++) {
        float wavepos = seconds / 10.0 + float(i) / (SPEED * PHASE_SPREAD);
        vec2 point = vec2(
            -sin(FREQ * wavepos + 1.0 / FREQ) * 0.9,
            -sin(4.0 * wavepos * 3.14159265) * 0.9
        );

        // small epsilon on the distance so a point passing directly under a pixel doesn't spike
        // to an unbounded value
        glow += WISP_COLOR / (distance(point, uv) + 0.05);
    }

    vec3 scene = texture(InSampler, texCoord).rgb;
    vec3 outColor = scene + clamp(glow * 0.02, 0.0, 0.85);

    fragColor = vec4(outColor, 1.0);
}

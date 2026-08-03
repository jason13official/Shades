#version 330

// live GameTime not needed here -> Speed/Turn already carry the live signal, but this
// stays a "core" pipeline (GLOBALS_SNIPPET) rather than a post_effect JSON so ShadesClient can
// push a custom per-frame uniform, same as sonar/cursor
#moj_import <minecraft:globals.glsl>

uniform sampler2D InSampler;

// pushed fresh each frame by ShadesClient -> smoothed 0..1 factors derived from the player's real
// horizontal movement speed and yaw turn rate (see buildMotionUniform's buildup/decay smoothing)
layout(std140) uniform MotionConfig {
    float Speed;
    float Turn;
};

in vec2 texCoord;
out vec4 fragColor;

const vec2 CENTER = vec2(0.5);
const int SAMPLES = 5;

void main(){

    vec2 delta = texCoord - CENTER;
    float dist = length(delta);

    // swirl: rotate around center, more twist toward the edges, driven by turn rate
    float angle = Turn * dist * 2.0;
    float s = sin(angle);
    float c = cos(angle);
    vec2 swirled = CENTER + mat2(c, -s, s, c) * delta;

    // radial zoom blur toward center, driven by speed
    vec2 toCenter = CENTER - swirled;
    vec3 color = vec3(0.0);
    for (int i = 0; i < SAMPLES; i++) {
        float w = float(i) / float(SAMPLES - 1);
        color += texture(InSampler, swirled + toCenter * Speed * 0.04 * w).rgb;
    }
    color /= float(SAMPLES);

    float intensity = clamp(Speed + Turn, 0.0, 1.0);
    float vignette = smoothstep(0.4, 0.95, dist) * intensity * 0.4;
    color *= (1.0 - vignette);

    fragColor = vec4(color, 1.0);
}

#version 330

// heartbeat vision: a real "lub-dub" double-thump vignette + center zoom-pulse driven by the
// player's actual health, sped up and reddened the lower it gets
#moj_import <minecraft:globals.glsl>

uniform sampler2D InSampler;

// real window aspect ratio, current health fraction (0 dead, 1 full), and an accumulated beat
// phase whose rate speeds up as health drops -> pushed fresh each frame by ShadesClient
layout(std140) uniform PulseConfig {
    float Aspect;
    float HealthFactor;
    float BeatPhase;
};

in vec2 texCoord;
out vec4 fragColor;

void main(){

    // two sharp thumps per beat cycle ("lub-dub"), each a narrow raised-cosine pulse rather than
    // a single smooth sine
    float lub = pow(max(sin(BeatPhase), 0.0), 24.0);
    float dub = pow(max(sin(BeatPhase - 0.9), 0.0), 24.0);
    float thump = max(lub, dub * 0.7);

    vec2 uv = texCoord * 2.0 - 1.0;
    uv.x *= Aspect;
    float radius = length(uv);

    // the weaker the player's health, the stronger both the zoom-punch and the vignette reach
    float severity = 1.0 - HealthFactor;
    float zoom = 1.0 - thump * severity * 0.04;
    vec2 zoomedUv = (texCoord - 0.5) * zoom + 0.5;

    vec3 scene = texture(InSampler, clamp(zoomedUv, 0.0, 1.0)).rgb;

    float vignette = smoothstep(0.4, 1.3, radius) * (0.35 + thump * 0.5) * (0.3 + severity * 0.7);
    vec3 outColor = mix(scene, vec3(0.5, 0.0, 0.0), vignette);

    fragColor = vec4(outColor, 1.0);
}

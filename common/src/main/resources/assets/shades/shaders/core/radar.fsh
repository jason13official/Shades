#version 330

// edge-of-screen radar ring: a rotating sweep line plus a single blip pointing toward the nearest
// real entity, brighter/bigger the closer it is; a warning flash kicks in at very close range
#moj_import <minecraft:globals.glsl>

uniform sampler2D InSampler;

// real window aspect ratio; Distance/Angle/Found describe the nearest tracked entity (Found is
// 0 or 1, Angle is a bearing in radians relative to the player's own forward view direction),
// pushed fresh each frame by ShadesClient
layout(std140) uniform RadarConfig {
    float Aspect;
    float Distance;
    float Angle;
    float Found;
};

in vec2 texCoord;
out vec4 fragColor;

#define PI 3.14159265
#define RING_RADIUS 0.85
#define RING_WIDTH 0.02

void main(){

    vec3 scene = texture(InSampler, texCoord).rgb;

    vec2 uv = (texCoord * 2.0 - 1.0) * vec2(Aspect, 1.0);
    float radius = length(uv);
    float angle = atan(uv.y, uv.x);

    float seconds = GameTime * 2400.0 * 0.5;

    vec3 glow = vec3(0.0);

    // faint ring outline, always visible as the radar's own "screen"
    float ring = 1.0 - smoothstep(RING_WIDTH, RING_WIDTH * 2.0, abs(radius - RING_RADIUS));
    glow += vec3(0.1, 0.6, 0.4) * ring * 0.25;

    // sweep line rotating around the ring
    float sweepAngle = mod(seconds * 0.8, 2.0 * PI) - PI;
    float sweepDelta = abs(mod(angle - sweepAngle + PI, 2.0 * PI) - PI);
    float sweep = (1.0 - smoothstep(0.0, 0.5, sweepDelta)) * ring;
    glow += vec3(0.2, 1.0, 0.7) * sweep * 0.6;

    if (Found > 0.5) {
        float proximity = 1.0 - clamp(Distance / 32.0, 0.0, 1.0);
        float blipDelta = abs(mod(angle - Angle + PI, 2.0 * PI) - PI);
        float blip = (1.0 - smoothstep(0.0, 0.12, blipDelta)) * ring;
        glow += vec3(1.0, 0.3, 0.2) * blip * (0.5 + proximity * 0.8);

        // danger flash across the whole view once something gets very close
        float danger = smoothstep(4.0, 1.5, Distance) * (sin(seconds * 6.0) * 0.5 + 0.5);
        glow += vec3(0.6, 0.05, 0.05) * danger * 0.3;
    }

    vec3 outColor = scene + glow;

    fragColor = vec4(outColor, 1.0);
}

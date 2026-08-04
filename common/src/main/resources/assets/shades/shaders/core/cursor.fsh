#version 330

// live GameTime, same trick as plasma.fsh
#moj_import <minecraft:globals.glsl>

uniform sampler2D InSampler;

// real cursor position when a screen (chat/inventory/etc.) is open; the level+PostChain pass runs
// before the GUI pass, so the effect stays visible behind a translucent screen
layout(std140) uniform CursorConfig {
    float ScreenOpen;
    vec2 CursorUv;
};

in vec2 texCoord;
out vec4 fragColor;

const vec3 RIPPLE_COLOR = vec3(1.0, 0.75, 0.2);

void main(){

    float t = GameTime * 2400.0;
    vec3 color = texture(InSampler, texCoord).rgb;

    if (ScreenOpen > 0.5) {
        // ripple looping outward from the real cursor position
        float dist = length(texCoord - CursorUv);
        float ping = mod(t * 0.4, 1.0) * 0.6;
        float ring = clamp(0.02 / (abs(dist - ping) + 0.01) - 0.5, 0.0, 2.0);

        vec2 distortedUv = texCoord + normalize(texCoord - CursorUv + 1e-6) * ring * 0.01;
        color = texture(InSampler, distortedUv).rgb;
        color += RIPPLE_COLOR * ring * 0.6;
    } else {
        // idle: a slow breathing vignette so the item still reads as "live" with no cursor to ripple from
        float breathe = sin(t * 0.5) * 0.5 + 0.5;
        float edgeDist = length(texCoord - vec2(0.5));
        float vignette = smoothstep(0.3, 0.9, edgeDist) * (0.15 + breathe * 0.1);
        color *= (1.0 - vignette);
    }

    fragColor = vec4(color, 1.0);
}

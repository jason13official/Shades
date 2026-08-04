#version 330

// tinted-lens look: a gentle grade toward the real biome color everywhere, strengthening into a
// vignette toward the screen edges - a genuine screen-space lens tint rather than anything tied
// to real depth/distance, so it reads consistently on ground, sky, and clouds alike
#moj_import <minecraft:globals.glsl>

uniform sampler2D InSampler;

// real window aspect ratio, and the real biome-blended grass/foliage color at the player's
// position (BiomeColors), pushed fresh each frame by ShadesClient
layout(std140) uniform BiomeConfig {
    float Aspect;
    vec3 GroundTint;
};

in vec2 texCoord;
out vec4 fragColor;

void main(){

    vec3 scene = texture(InSampler, texCoord).rgb;

    vec2 uv = (texCoord * 2.0 - 1.0) * vec2(Aspect, 1.0);
    float edge = smoothstep(0.4, 1.2, length(uv));

    float amount = 0.22 + edge * 0.35;
    vec3 outColor = mix(scene, scene * (GroundTint * 1.3 + 0.15), amount);

    fragColor = vec4(outColor, 1.0);
}

#version 330

uniform sampler2D InSampler;

in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

out vec4 fragColor;

/// direct port of 1.20.1's outline_soft.fsh (the "Pencil" debug shader): a quantized, gamma-
/// boosted grayscale ramp multiplied by a soft edge mask built from an 8-tap luma difference sum
const float LUMA_RAMP = 2.2;
const float LUMA_LEVEL = 6.0;
const vec4 GRAY = vec4(0.3, 0.59, 0.11, 0.0);

void main() {

    vec2 oneTexel = 1.0 / InSize;
    vec4 center = texture(InSampler, texCoord);
    vec4 up = texture(InSampler, texCoord + vec2(0.0, -oneTexel.y));
    vec4 up2 = texture(InSampler, texCoord + vec2(0.0, -oneTexel.y) * 2.0);
    vec4 down = texture(InSampler, texCoord + vec2(oneTexel.x, 0.0));
    vec4 down2 = texture(InSampler, texCoord + vec2(oneTexel.x, 0.0) * 2.0);
    vec4 left = texture(InSampler, texCoord + vec2(-oneTexel.x, 0.0));
    vec4 left2 = texture(InSampler, texCoord + vec2(-oneTexel.x, 0.0) * 2.0);
    vec4 right = texture(InSampler, texCoord + vec2(0.0, oneTexel.y));
    vec4 right2 = texture(InSampler, texCoord + vec2(0.0, oneTexel.y) * 2.0);

    vec4 sum = abs(center - up) + abs(center - down) + abs(center - left) + abs(center - right)
        + abs(center - up2) + abs(center - down2) + abs(center - left2) + abs(center - right2);
    float sumLuma = 1.0 - dot(clamp(sum, 0.0, 1.0), GRAY);

    float centerLuma = dot(center + (center - pow(center, vec4(LUMA_RAMP))), GRAY);
    centerLuma = centerLuma - fract(centerLuma * LUMA_LEVEL) / LUMA_LEVEL;
    centerLuma = centerLuma * (LUMA_LEVEL / (LUMA_LEVEL - 1.0));
    centerLuma = centerLuma * sumLuma;

    fragColor = vec4(vec3(centerLuma), 1.0);
}

#version 330

uniform sampler2D InSampler;

in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

out vec4 fragColor;

/// direct port of 1.20.1's outline.fsh: sums the 4-neighbor color difference from center, so flat
/// areas go black and only real edges show through
void main() {

    vec2 oneTexel = 1.0 / InSize;
    vec4 center = texture(InSampler, texCoord);
    vec4 left = texture(InSampler, texCoord - vec2(oneTexel.x, 0.0));
    vec4 right = texture(InSampler, texCoord + vec2(oneTexel.x, 0.0));
    vec4 up = texture(InSampler, texCoord - vec2(0.0, oneTexel.y));
    vec4 down = texture(InSampler, texCoord + vec2(0.0, oneTexel.y));

    vec4 total = clamp((center - left) + (center - right) + (center - up) + (center - down), 0.0, 1.0);
    fragColor = vec4(total.rgb, 1.0);
}

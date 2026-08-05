#version 330

uniform sampler2D InSampler;

in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

out vec4 fragColor;

/// ported from 1.20.1's notch.fsh (a nod to Notch's other games' half-res dithered look); the
/// original read a real noise texture (DitherSampler) we don't ship, so the per-cell dither
/// offset is a plain hash of the cell coordinate instead of a sampled noise pattern
float hash(vec2 p) {
    return fract(sin(dot(p, vec2(12.9898, 78.233))) * 43758.5453123);
}

void main() {

    vec2 halfSize = InSize * 0.5;
    vec2 cell = floor(texCoord * halfSize);
    vec2 steppedCoord = cell / halfSize;

    float noise = hash(cell);
    vec3 color = texture(InSampler, steppedCoord).rgb + (noise - 0.5) * vec3(1.0 / 12.0, 1.0 / 12.0, 1.0 / 6.0);

    float r = floor(color.r * 8.0) / 8.0;
    float g = floor(color.g * 8.0) / 8.0;
    float b = floor(color.b * 4.0) / 4.0;

    fragColor = vec4(r, g, b, 1.0);
}

#version 330

uniform sampler2D InSampler;

in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

out vec4 fragColor;

/// direct port of 1.20.1's blobs2.fsh (a soft dilate/max filter over a square neighborhood,
/// flattening detail into soft blobs); Radius trimmed from the original's 20 to 6 since this
/// runs full-screen every frame instead of once
const float RADIUS = 6.0;

void main() {

    vec2 oneTexel = 1.0 / InSize;
    vec4 c = texture(InSampler, texCoord);
    vec4 maxVal = c;

    for (float u = 0.0; u <= RADIUS; u += 1.0) {
        for (float v = 0.0; v <= RADIUS; v += 1.0) {
            float weight = (sqrt(u * u + v * v) / RADIUS) > 1.0 ? 0.0 : 1.0;

            vec4 s0 = texture(InSampler, texCoord + vec2(-u, -v) * oneTexel);
            vec4 s1 = texture(InSampler, texCoord + vec2(u, v) * oneTexel);
            vec4 s2 = texture(InSampler, texCoord + vec2(-u, v) * oneTexel);
            vec4 s3 = texture(InSampler, texCoord + vec2(u, -v) * oneTexel);

            vec4 tempMax = max(max(s0, s1), max(s2, s3));
            maxVal = mix(maxVal, max(maxVal, tempMax), weight);
        }
    }

    fragColor = vec4(maxVal.rgb, 1.0);
}

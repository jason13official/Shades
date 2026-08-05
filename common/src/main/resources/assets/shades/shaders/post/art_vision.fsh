#version 330

uniform sampler2D InSampler;

in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

out vec4 fragColor;

/// simplified single-pass stand-in for 1.20.1's 6-pass "Art" chain (blobs2 -> outline_watercolor
/// -> a wide two-direction blur -> outline_combine -> blit): flattens detail into soft blobs
/// (small-radius max filter, same idea as blobs2_vision but cheaper) then adds a glowing edge
/// highlight sampled at a much wider spacing (standing in for the original's real 20px blur of
/// its edge mask) on top -> moving detail reads as a soft painterly smear with glowing outlines
/// twinkling around it
const vec4 GRAY = vec4(0.3, 0.59, 0.11, 0.0);

vec4 flatten(vec2 uv, vec2 oneTexel) {
    vec4 c = texture(InSampler, uv);
    vec4 maxVal = c;
    for (float u = -3.0; u <= 3.0; u += 1.0) {
        for (float v = -3.0; v <= 3.0; v += 1.0) {
            maxVal = max(maxVal, texture(InSampler, uv + vec2(u, v) * oneTexel));
        }
    }
    return maxVal;
}

void main() {

    vec2 oneTexel = 1.0 / InSize;
    vec4 flat0 = flatten(texCoord, oneTexel);

    // wide-spaced taps stand in for a real blur of the edge mask; cheap approximation of the
    // original's dedicated 20px two-pass box blur
    vec2 wide = oneTexel * 6.0;
    vec4 center = texture(InSampler, texCoord);
    float edge = 0.0;
    edge += dot(abs(center - texture(InSampler, texCoord + vec2(wide.x, 0.0))), GRAY);
    edge += dot(abs(center - texture(InSampler, texCoord - vec2(wide.x, 0.0))), GRAY);
    edge += dot(abs(center - texture(InSampler, texCoord + vec2(0.0, wide.y))), GRAY);
    edge += dot(abs(center - texture(InSampler, texCoord - vec2(0.0, wide.y))), GRAY);
    edge = clamp(edge, 0.0, 1.0);

    vec3 color = flat0.rgb + flat0.rgb * edge * 0.75;
    fragColor = vec4(color, 1.0);
}

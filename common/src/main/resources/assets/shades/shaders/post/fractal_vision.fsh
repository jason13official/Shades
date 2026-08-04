#version 330

// kaleidoscope_vision's polar wedge-fold, sampled once into a scratch buffer by the JSON's first
// pass; this pass runs neon_vision's Sobel edge glow over that already-folded image, so the glow
// tracks the repeated wedges instead of the original unfolded scene

uniform sampler2D InSampler;

in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

out vec4 fragColor;

const vec3 GLOW_COLOR = vec3(0.6, 0.25, 1.0);

float luma(vec2 uv) {
    return dot(texture(InSampler, uv).rgb, vec3(0.299, 0.587, 0.114));
}

void main(){

    vec2 texel = 1.0 / InSize;

    // same 3x3 Sobel edge magnitude neon_vision.fsh uses
    float tl = luma(texCoord + texel * vec2(-1.0,  1.0));
    float  t = luma(texCoord + texel * vec2( 0.0,  1.0));
    float tr = luma(texCoord + texel * vec2( 1.0,  1.0));
    float  l = luma(texCoord + texel * vec2(-1.0,  0.0));
    float  r = luma(texCoord + texel * vec2( 1.0,  0.0));
    float bl = luma(texCoord + texel * vec2(-1.0, -1.0));
    float  b = luma(texCoord + texel * vec2( 0.0, -1.0));
    float br = luma(texCoord + texel * vec2( 1.0, -1.0));

    float gx = (tr + 2.0 * r + br) - (tl + 2.0 * l + bl);
    float gy = (bl + 2.0 * b + br) - (tl + 2.0 * t + tr);
    float edge = clamp(length(vec2(gx, gy)), 0.0, 1.0);

    float centerLuma = luma(texCoord);
    float glow = edge;
    for (int i = 0; i < 8; i++) {
        float angle = 6.28318 * float(i) / 8.0;
        vec2 uv = texCoord + vec2(cos(angle), sin(angle)) * texel * 3.0;
        glow = max(glow, abs(luma(uv) - centerLuma) * 1.5);
    }

    vec3 dimmed = texture(InSampler, texCoord).rgb * 0.2;
    fragColor = vec4(dimmed + GLOW_COLOR * glow, 1.0);
}

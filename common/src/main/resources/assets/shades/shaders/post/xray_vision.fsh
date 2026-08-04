#version 330

uniform sampler2D InSampler; // input texture

in vec2 texCoord; // uv coords

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

out vec4 fragColor; // final pixel color

// scanner-blue tint for the outlined result
const vec3 TINT = vec3(0.25, 0.85, 1.0);

float luma(vec2 uv) {
    return dot(texture(InSampler, uv).rgb, vec3(0.299, 0.587, 0.114));
}

void main(){

    vec2 texel = 1.0 / InSize;

    // classic 3x3 Sobel kernel: two taps of neighbouring luminance, one sensitive to
    // horizontal edges (gx) and one to vertical edges (gy)
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

    // everything that isn't an edge reads as near-black, edges glow in TINT, for a scanner/X-ray look
    fragColor = vec4(TINT * edge, 1.0);
}

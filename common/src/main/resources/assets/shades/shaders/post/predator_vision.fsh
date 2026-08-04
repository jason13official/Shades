#version 330

// thermal_vision's heat ramp + neon_vision's Sobel edge glow, combined; the heat ramp reads the
// scene, the edge pass outlines silhouettes on top of it, for a "predator thermal" look

uniform sampler2D InSampler;

in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

out vec4 fragColor;

const vec3 OUTLINE_COLOR = vec3(0.5, 1.0, 0.4);

// same 5-band ramp thermal_vision.fsh uses
vec3 heatRamp(float t) {

    vec3 c0 = vec3(0.0, 0.0, 0.05);
    vec3 c1 = vec3(0.25, 0.0, 0.35);
    vec3 c2 = vec3(0.65, 0.0, 0.2);
    vec3 c3 = vec3(0.9, 0.35, 0.0);
    vec3 c4 = vec3(1.0, 0.85, 0.1);
    vec3 c5 = vec3(1.0, 1.0, 0.9);

    float s = t * 5.0;
    if (s < 1.0) return mix(c0, c1, s);
    if (s < 2.0) return mix(c1, c2, s - 1.0);
    if (s < 3.0) return mix(c2, c3, s - 2.0);
    if (s < 4.0) return mix(c3, c4, s - 3.0);
    return mix(c4, c5, min(s - 4.0, 1.0));
}

float luma(vec2 uv) {
    return dot(texture(InSampler, uv).rgb, vec3(0.299, 0.587, 0.114));
}

void main(){

    // same 3x3 Sobel edge magnitude neon_vision.fsh uses
    vec2 texel = 1.0 / InSize;

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

    vec3 thermal = heatRamp(luma(texCoord));
    vec3 outColor = mix(thermal, OUTLINE_COLOR, edge * 0.85);

    fragColor = vec4(outColor, 1.0);
}

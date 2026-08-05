#version 330

uniform sampler2D InSampler;

in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

out vec4 fragColor;

const float REDUCE_MIN = 1.0 / 128.0;
const float REDUCE_MUL = 1.0 / 8.0;
const float SPAN_MAX = 8.0;

/// ported from 1.20.1's fxaa.fsh; the original vertex shader precomputed `posPos` (center +
/// half-texel-shifted sample point) per vertex, folded here into one texCoord since we're always
/// vanilla's core/screenquad
void main() {

    vec2 rcpFrame = 1.0 / InSize;
    vec2 nwCoord = texCoord - rcpFrame * 0.5;

    vec3 rgbNW = texture(InSampler, nwCoord).rgb;
    vec3 rgbNE = textureOffset(InSampler, nwCoord, ivec2(1, 0)).rgb;
    vec3 rgbSW = textureOffset(InSampler, nwCoord, ivec2(0, 1)).rgb;
    vec3 rgbSE = textureOffset(InSampler, nwCoord, ivec2(1, 1)).rgb;
    vec3 rgbM = texture(InSampler, texCoord).rgb;

    vec3 luma = vec3(0.299, 0.587, 0.114);
    float lumaNW = dot(rgbNW, luma);
    float lumaNE = dot(rgbNE, luma);
    float lumaSW = dot(rgbSW, luma);
    float lumaSE = dot(rgbSE, luma);
    float lumaM = dot(rgbM, luma);

    float lumaMin = min(lumaM, min(min(lumaNW, lumaNE), min(lumaSW, lumaSE)));
    float lumaMax = max(lumaM, max(max(lumaNW, lumaNE), max(lumaSW, lumaSE)));

    vec2 dir;
    dir.x = -((lumaNW + lumaNE) - (lumaSW + lumaSE));
    dir.y = (lumaNW + lumaSW) - (lumaNE + lumaSE);

    float dirReduce = max((lumaNW + lumaNE + lumaSW + lumaSE) * (0.25 * REDUCE_MUL), REDUCE_MIN);
    float rcpDirMin = 1.0 / (min(abs(dir.x), abs(dir.y)) + dirReduce);
    dir = min(vec2(SPAN_MAX), max(vec2(-SPAN_MAX), dir * rcpDirMin)) * rcpFrame;

    vec3 rgbA = 0.5 * (
        texture(InSampler, texCoord + dir * (1.0 / 3.0 - 0.5)).rgb +
        texture(InSampler, texCoord + dir * (2.0 / 3.0 - 0.5)).rgb);
    vec3 rgbB = rgbA * 0.5 + 0.25 * (
        texture(InSampler, texCoord + dir * (0.0 / 3.0 - 0.5)).rgb +
        texture(InSampler, texCoord + dir * (3.0 / 3.0 - 0.5)).rgb);

    float lumaB = dot(rgbB, luma);
    vec3 color = (lumaB < lumaMin || lumaB > lumaMax) ? rgbA : rgbB;

    fragColor = vec4(color, 1.0);
}

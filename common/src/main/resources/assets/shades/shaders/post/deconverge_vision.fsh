#version 330

uniform sampler2D InSampler;

in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

out vec4 fragColor;

/// direct port of 1.20.1's deconverge.fsh with fixed constants standing in for the original's
/// JSON-configurable uniforms: a small flat per-channel texel shift (no radial component), like
/// a CRT with its three electron guns slightly out of alignment
const vec3 CONVERGE_X = vec3(-2.0, 0.0, 2.0);
const vec3 CONVERGE_Y = vec3(0.0, 0.0, 0.0);

void main() {

    vec2 oneTexel = 1.0 / InSize;

    vec3 coordX = vec3(texCoord.x) + CONVERGE_X * oneTexel.x;
    vec3 coordY = vec3(texCoord.y) + CONVERGE_Y * oneTexel.y;

    float redValue = texture(InSampler, vec2(coordX.x, coordY.x)).r;
    float greenValue = texture(InSampler, vec2(coordX.y, coordY.y)).g;
    float blueValue = texture(InSampler, vec2(coordX.z, coordY.z)).b;

    fragColor = vec4(redValue, greenValue, blueValue, 1.0);
}

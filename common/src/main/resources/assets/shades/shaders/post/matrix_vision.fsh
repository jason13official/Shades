#version 330

uniform sampler2D InSampler; // input texture

in vec2 texCoord; // uv coords

// screen size in real pixels -> only pulled in here to size the scanlines below in actual
// pixels rather than screen-fraction;
// optional(-ish), only declare it if a shader actually reads OutSize/InSize
// (see the vanilla color_convolve.fsh for an example that uses it)
layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

out vec4 fragColor; // final pixel color

// matrix/CRT phosphor-green tint
const vec3 TINT = vec3(0.1, 1.0, 0.25);

// how much darker every other scanline gets
const float SCANLINE_STRENGTH = 0.35;

void main(){

    // sample the color at the given pixel coord
    vec4 diffuseColor = texture(InSampler, texCoord);

    // convert to grayscale brightness (luma), then tint green like an old CRT phosphor
    float luma = dot(diffuseColor.rgb, vec3(0.299, 0.587, 0.114));
    vec3 green = vec3(luma) * TINT;

    // turn the pixel's screen-space Y into a repeating 0..1 ramp every 2 real pixels tall,
    // then darken the bottom half of each ramp -> draws alternating light/dark horizontal rows
    float scanline = fract(texCoord.y * OutSize.y * 0.5);
    float scanDarken = step(scanline, 0.5) * SCANLINE_STRENGTH;
    green *= (1.0 - scanDarken);

    // output (alpha forced to 1)
    fragColor = vec4(green, 1.0);
}

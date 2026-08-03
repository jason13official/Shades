#version 330

uniform sampler2D InSampler; // input texture

in vec2 texCoord; // uv coords
out vec4 fragColor; // final pixel color

// sepia filter (warm brown/orange)
const vec3 TINT = vec3(0.65, 0.4, 0.18);

// 0.0 no darkness, 1.0 full black
const float DARKNESS = 0.55;

// start, end, max amount
const float VIGNETTE_INNER = 0.3;
const float VIGNETTE_OUTER = 0.85;
const float VIGNETTE_STRENGTH = 0.6;

void main() {

    // sample the color at the given pixel coord
    vec4 diffuseColor = texture(InSampler, texCoord);

    // apply sepia tint and dim slightly
    vec3 tinted = diffuseColor.rgb * TINT;
    vec3 darkened = tinted * (1.0 - DARKNESS);

    // get distance from screen center
    float edgeDistance = length(texCoord - vec2(0.5));
    // convert to a smooth vignette value
    float vignette = smoothstep(VIGNETTE_INNER, VIGNETTE_OUTER, edgeDistance) * VIGNETTE_STRENGTH;
    // apply
    vec3 outColor = darkened * (1.0 - vignette);

    // output (alpha forced to 1)
    fragColor = vec4(outColor, 1.0);
}

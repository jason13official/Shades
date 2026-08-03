#version 330

uniform sampler2D InSampler; // input texture

in vec2 texCoord; // uv coords
out vec4 fragColor; // final pixel color

// green night-vision tint
const vec3 TINT = vec3(0.35, 1.0, 0.4);

// how much to boost brightness before tinting
const float BRIGHTNESS = 1.6;

// start, end of the edge darkening (like looking through a scope)
const float VIGNETTE_INNER = 0.25;
const float VIGNETTE_OUTER = 0.75;

// cheap pseudo-random value per pixel, used for grain below -> always the same for a given
// texCoord (no live "Time" uniform is available(?) to post shaders here, so this can't flicker
// frame to frame -> see note in shades_visor.fsh, same limitation)
float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453123);
}

void main(){

    // sample the color at the given pixel coord
    vec4 diffuseColor = texture(InSampler, texCoord);

    // convert to grayscale brightness (luma), amplify it, then tint green
    float luma = dot(diffuseColor.rgb, vec3(0.299, 0.587, 0.114));
    vec3 amplified = vec3(luma * BRIGHTNESS) * TINT;

    // get distance from screen center
    float edgeDistance = length(texCoord - vec2(0.5));
    // convert to a smooth vignette value, darken the edges
    float vignette = smoothstep(VIGNETTE_INNER, VIGNETTE_OUTER, edgeDistance);
    amplified *= (1.0 - vignette * 0.85);

    // sprinkle in some static grain for a grainy goggle-cam look
    float grain = (hash(texCoord * 512.0) - 0.5) * 0.06;
    amplified += grain;

    // output (alpha forced to 1)
    fragColor = vec4(clamp(amplified, 0.0, 1.0), 1.0);
}

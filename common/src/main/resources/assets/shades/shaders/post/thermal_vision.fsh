#version 330

uniform sampler2D InSampler; // input texture

in vec2 texCoord; // uv coords
out vec4 fragColor; // final pixel color

// maps a 0..1 brightness value to a thermal-camera color, cold to hot:
// black -> purple -> red -> orange -> yellow -> white
vec3 heatRamp(float t) {

    vec3 c0 = vec3(0.0, 0.0, 0.05);  // near-black (coldest)
    vec3 c1 = vec3(0.25, 0.0, 0.35); // purple
    vec3 c2 = vec3(0.65, 0.0, 0.2);  // red
    vec3 c3 = vec3(0.9, 0.35, 0.0);  // orange
    vec3 c4 = vec3(1.0, 0.85, 0.1);  // yellow
    vec3 c5 = vec3(1.0, 1.0, 0.9);   // white (hottest)

    // split t into 5 even bands and blend between each pair of neighbouring colors
    float s = t * 5.0;
    if (s < 1.0) return mix(c0, c1, s);
    if (s < 2.0) return mix(c1, c2, s - 1.0);
    if (s < 3.0) return mix(c2, c3, s - 2.0);
    if (s < 4.0) return mix(c3, c4, s - 3.0);
    return mix(c4, c5, min(s - 4.0, 1.0));
}

void main(){

    // sample the color at the given pixel coord
    vec4 diffuseColor = texture(InSampler, texCoord);

    // use brightness (luma) as the "temperature" driving the color ramp - bright things
    // (sky, lava, glowstone) read as hot, dark things read as cold
    float luma = dot(diffuseColor.rgb, vec3(0.299, 0.587, 0.114));

    // output (alpha forced to 1)
    fragColor = vec4(heatRamp(luma), 1.0);
}

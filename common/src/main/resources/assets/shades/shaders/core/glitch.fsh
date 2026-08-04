#version 330

// live GameTime, same trick as plasma.fsh, driven by hand instead of through PostChain
#moj_import <minecraft:globals.glsl>

uniform sampler2D InSampler;

in vec2 texCoord;
out vec4 fragColor;

float hash(float x) {
    return fract(sin(x) * 43758.5453123);
}

void main(){

    float t = GameTime * 2400.0;

    // chop the screen into a handful of horizontal bands that re-roll on every "beat"; each band
    // gets its own random sideways offset, most stay put, a few slide hard, reading as a signal
    // tear instead of one continuous wave
    float bandCount = 12.0;
    float band = floor(texCoord.y * bandCount);
    float beat = floor(t * 2.0);
    float seed = hash(band * 13.7 + beat);

    float shift = step(0.8, seed) * (hash(seed * 7.0) - 0.5) * 0.15;

    vec2 uv = vec2(texCoord.x + shift, texCoord.y);
    fragColor = vec4(texture(InSampler, uv).rgb, 1.0);
}

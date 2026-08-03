#version 330

uniform sampler2D InSampler;

in vec2 texCoord;
out vec4 fragColor;

const float PI = 3.14159265;
const float SEGMENTS = 6.0;

void main(){

    vec2 fromCenter = texCoord - vec2(0.5);
    float radius = length(fromCenter);
    float angle = atan(fromCenter.y, fromCenter.x);

    // fold the angle into one wedge, then mirror the second half back onto the first - repeating
    // that single wedge around the circle is what makes it read as a kaleidoscope
    float wedge = 2.0 * PI / SEGMENTS;
    float folded = mod(angle, wedge);
    if (folded > wedge * 0.5) {
        folded = wedge - folded;
    }

    vec2 sampleUv = vec2(0.5) + vec2(cos(folded), sin(folded)) * radius;
    fragColor = texture(InSampler, clamp(sampleUv, 0.0, 1.0));
}

#version 330

uniform sampler2D InSampler; // input texture

in vec2 texCoord; // uv coords
out vec4 fragColor; // final pixel color

// how far the red/blue channels split from green, growing outward from screen center so the
// middle of view stays clean and the fringing shows up toward the edges like a cheap lens
const float SHIFT_STRENGTH = 0.02;

void main(){

    vec2 fromCenter = texCoord - vec2(0.5);

    // classic chromatic-aberration fringing:
    // push red outward, pull blue inward, leave green where the eye expects it
    float r = texture(InSampler, texCoord + fromCenter * SHIFT_STRENGTH).r;
    float g = texture(InSampler, texCoord).g;
    float b = texture(InSampler, texCoord - fromCenter * SHIFT_STRENGTH).b;

    // output (alpha forced to 1)
    fragColor = vec4(r, g, b, 1.0);
}

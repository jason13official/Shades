#version 330

// live GameTime for the hue cycle and wobble phase; a real post_effect shader can't get this
#moj_import <minecraft:globals.glsl>

uniform sampler2D InSampler;

in vec2 texCoord;
out vec4 fragColor;

const vec2 FREQUENCY = vec2(18.0, 22.0);
const vec2 WOBBLE_AMOUNT = vec2(0.006, 0.006);

/// ported from 1.20.1's wobble.fsh: samples through a sine-displaced UV (screen-space only, the
/// underlying geometry is untouched; a still crosshair sitting at screen center reveals the
/// pixels moving underneath it, same as the original) then rotates every pixel's hue by the same
/// amount every frame
vec3 hue(float h) {
    float r = abs(h * 6.0 - 3.0) - 1.0;
    float g = 2.0 - abs(h * 6.0 - 2.0);
    float b = 2.0 - abs(h * 6.0 - 4.0);
    return clamp(vec3(r, g, b), 0.0, 1.0);
}

vec3 hsvToRgb(vec3 hsv) {
    return ((hue(hsv.x) - 1.0) * hsv.y + 1.0) * hsv.z;
}

vec3 rgbToHsv(vec3 rgb) {
    vec3 hsv = vec3(0.0);
    hsv.z = max(rgb.r, max(rgb.g, rgb.b));
    float minc = min(rgb.r, min(rgb.g, rgb.b));
    float c = hsv.z - minc;

    if (c != 0.0) {
        hsv.y = c / hsv.z;
        vec3 delta = (hsv.z - rgb) / c;
        delta.rgb -= delta.brg;
        delta.rg += vec2(2.0, 4.0);
        if (rgb.r >= hsv.z) {
            hsv.x = delta.b;
        } else if (rgb.g >= hsv.z) {
            hsv.x = delta.r;
        } else {
            hsv.x = delta.g;
        }
        hsv.x = fract(hsv.x / 6.0);
    }
    return hsv;
}

void main() {

    float t = GameTime * 1200.0;

    float xOffset = sin(texCoord.y * FREQUENCY.x + t * 2.0 * 3.14159) * WOBBLE_AMOUNT.x;
    float yOffset = cos(texCoord.x * FREQUENCY.y + t * 2.0 * 3.14159) * WOBBLE_AMOUNT.y;

    vec3 rgb = texture(InSampler, texCoord + vec2(xOffset, yOffset)).rgb;
    vec3 hsv = rgbToHsv(rgb);
    hsv.x = fract(hsv.x + t * 0.05);

    fragColor = vec4(hsvToRgb(hsv), 1.0);
}

#version 330

// a radial polar-coordinate "tentacle" wedge pattern (per-angular-segment, noise-perturbed V
// shapes) multiplied over a subtly noise-warped, partially desaturated version of the real world -
// looking at the world through a swirling radial vortex vignette rather than replacing it. No
// aspect correction, matching the original's raw-UV polar remap
#moj_import <minecraft:globals.glsl>

uniform sampler2D InSampler;

in vec2 texCoord;
out vec4 fragColor;

#define PI 3.14159

float hash13(vec3 p3) {
    p3 = fract(p3 * 0.1031);
    p3 += dot(p3, p3.zyx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

vec2 gradient(vec3 p) {
    float angle = hash13(p) * 2.0 * PI;
    return vec2(cos(angle), sin(angle));
}

float noise(vec3 p) {
    vec3 fl = floor(p);
    vec2 fr = fract(p.xy);
    float r1 = dot(fr, gradient(fl));
    float r2 = dot(fr - vec2(0.0, 1.0), gradient(fl + vec3(0.0, 1.0, 0.0)));
    float r3 = dot(fr - vec2(1.0, 0.0), gradient(fl + vec3(1.0, 0.0, 0.0)));
    float r4 = dot(fr - vec2(1.0, 1.0), gradient(fl + vec3(1.0, 1.0, 0.0)));

    fr = smoothstep(0.0, 1.0, fr);
    return mix(mix(r1, r2, fr.y), mix(r3, r4, fr.y), fr.x);
}

// one radial "tentacle" wedge, repeated 10 times around the circle; each wedge's edge wobbles via
// the noise field so it reads as writhing instead of a rigid pinwheel
vec3 tentacle(vec2 uv, float seconds) {
    uv.x *= 10.0;
    float id = floor(uv.x);
    uv.x = fract(uv.x);

    uv.x += uv.y * 0.15 * noise(vec3(uv.y * 13.0, seconds, id * 2.2));
    uv.x -= 0.5;
    uv.x = abs(uv.x);
    uv.x *= 2.0;

    return vec3(smoothstep(0.9, 1.0, uv.x + uv.y));
}

void main(){

    float seconds = GameTime * 2400.0 * 0.5;

    vec2 uv = texCoord - vec2(0.5);
    uv = vec2(atan(uv.y, uv.x), length(uv));
    float vignette = max(0.0, min(1.0, (0.6 - uv.y) * 2.2));
    uv.y = 1.3 - uv.y;
    uv.x = uv.x / PI * 0.5 + 0.5;
    vec3 col = tentacle(uv, seconds) * vignette;

    // subtle noise-driven UV drift, so the world underneath shimmers instead of sitting rigid
    float r1 = 0.003 * noise(vec3(texCoord * 40.0, seconds));
    float r2 = 0.003 * noise(vec3(texCoord * 40.0, seconds + 1.0));
    vec2 driftUV = texCoord + mix(r1, r2, fract(seconds));

    vec3 texColor = texture(InSampler, driftUV).rgb;
    float grayScale = dot(texColor, vec3(0.2, 0.7, 0.1));
    texColor = mix(vec3(grayScale), texColor, 0.4);
    texColor *= col;

    fragColor = vec4(texColor, 1.0);
}

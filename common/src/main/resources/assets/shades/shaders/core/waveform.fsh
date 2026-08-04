#version 330

// port of an oscilloscope-line shadertoy sketch: a slowly rotating/scaling/translating sine wave
// traced as a thin glowing line (Inigo Quilez's gradient-based signed-distance-to-curve trick),
// colored by a cycling hue, accumulating into a fading trail. The trail needs to read back its own
// previous frame's output - unlike every other live item here, so this one goes through
// ShadesLiveVision's PrevFrameSampler feedback path instead of just InSampler (see ShadesClient's
// waveformFeedback persistent target). Dropped the original's iFrame-based frame-skipping (a
// shadertoy perf hack tuned for a fixed 60fps target - GameTime doesn't give a comparable frame
// count, and drawing every real frame reads fine at any framerate)
#moj_import <minecraft:globals.glsl>

uniform sampler2D InSampler;
uniform sampler2D PrevFrameSampler;

// real window aspect ratio, pushed fresh each frame by ShadesClient
layout(std140) uniform WaveformConfig {
    float Aspect;
};

in vec2 texCoord;
out vec4 fragColor;

#define PI 3.14159265359

float sinNorm(float x) {
    return sin(x) * 0.5 + 0.5;
}

float rand(float seed) {
    return fract(sin(dot(vec2(seed, seed / PI), vec2(12.9898, 78.233))) * 43758.5453);
}

float smoothVal(float x, float maxX) {
    return clamp(smoothstep(0.0, 1.0, x / maxX) * (1.0 - smoothstep(0.0, 1.0, x / maxX)) * 4.0, 0.0, 1.0);
}

vec3 hsv2rgb(vec3 c) {
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

float smoothRand(float t, float interval, float seed) {
    float next = rand(1.0 + floor(t / interval) + seed);
    float curr = rand(floor(t / interval) + seed);
    return mix(curr, next, fract(t / interval));
}

float waveF(vec2 point, float t) {
    return sin(point.x * 2.0 + t * 1.275) + point.y;
}

vec2 waveGrad(vec2 point, float t) {
    vec2 h = vec2(0.01, 0.0);
    return vec2(
        waveF(point + h.xy, t) - waveF(point - h.xy, t),
        waveF(point + h.yx, t) - waveF(point - h.yx, t)
    ) / (2.0 * h.x);
}

float lineColor(vec2 point, float t, float lineWidthPx) {
    float v = waveF(point, t);
    vec2 g = waveGrad(point, t);
    float de = abs(v) / length(g);
    float normalizedLineRadius = lineWidthPx * 0.5;
    return 1.0 - clamp(smoothstep(0.0, normalizedLineRadius, de), 0.0, 1.0);
}

void main(){

    float t = GameTime * 2400.0 * 0.5;

    vec2 point = (texCoord - 0.5) * 2.0;
    point.x *= Aspect;

    // scale/rotate/translate the sample space smoothly over time - this is what makes the traced
    // line drift/breathe instead of sitting static
    float z = mix(0.5, 1.5, smoothRand(t, 2.0, 0.0));
    point /= z;

    float rot = smoothRand(t, 0.5, 354.856) * PI;
    point = vec2(cos(rot) * point.x + sin(rot) * point.y, -sin(rot) * point.x + cos(rot) * point.y);

    point.x += smoothRand(t, 1.0, 842.546) * 2.0 - 1.0;

    float lineLength = 0.25 + smoothRand(t, 4.0, 0.846) * 0.25 + 0.25;
    float linePoint = (point.x + lineLength * 0.5) / lineLength;
    float lineWidth = mix(0.01, 0.05, smoothVal(linePoint * 100.0, 100.0)) / max(z, 0.001);

    vec3 trace = vec3(0.0);
    if (point.x >= -lineLength * 0.5 && point.x <= lineLength * 0.5) {
        trace = vec3(lineColor(point, t, lineWidth));
    }

    trace *= hsv2rgb(vec3(fract(t / 7.0), sinNorm(t * 0.73) * 0.4 + 0.6, 1.0));
    trace += pow((trace.r + trace.g + trace.b) / 3.0 + 0.25, 3.0) - pow(0.25, 3.0);

    float decay = sinNorm(t * 0.789) * 0.5 + 0.25;
    vec3 prev = texture(PrevFrameSampler, texCoord).rgb;
    vec3 accumulated = clamp(trace + prev * decay, 0.0, 1.0);

    vec3 scene = texture(InSampler, texCoord).rgb;
    vec3 outColor = max(scene * 0.35, accumulated);

    fragColor = vec4(outColor, 1.0);
}

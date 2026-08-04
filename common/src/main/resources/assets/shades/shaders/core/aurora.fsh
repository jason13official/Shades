#version 330

// a psychedelic two-tone color field driven by a folded/bounced fbm noise (gyroid-based), fully
// synthetic in the original. Reused here as both a tint AND a refraction-normal source (same
// "derivative as normal" trick mirage.fsh/copper.fsh use) instead of a flat replace, so it reads
// as an aurora-like shimmer over the real world rather than an unrelated picture
#moj_import <minecraft:globals.glsl>

uniform sampler2D InSampler;

in vec2 texCoord;
out vec4 fragColor;

float gyroid(vec3 seed) {
    return dot(sin(seed), cos(seed.yzx));
}

float fbm(vec3 seed) {
    float result = 0.0, a = 0.5;
    for (int i = 0; i < 6; ++i) {
        seed.z += result * 0.5;
        result += abs(gyroid(seed / a)) * a;
        a /= 2.0;
    }
    return result;
}

float noise(vec2 p, float seconds) {
    vec3 seed = vec3(p, length(p) - seconds * 0.025);
    return sin(fbm(seed) * 7.0) * 0.5 + 0.5;
}

float noise2(vec2 p, float i) {
    vec3 seed = vec3(p, i);
    return sin(fbm(seed) * 6.0) * 0.5 + 0.5;
}

vec3 gradient(float t) {
    float t2 = t * t;
    float t3 = t2 * t;
    float r = 0.2 + 1.5 * t - 0.6 * t2 + 0.1 * t3;
    float g = 0.05 - 0.4 * t + 1.8 * t2 - 0.7 * t3;
    float b = 0.5 - 1.7 * t + 1.9 * t2 - 0.6 * t3;
    return clamp(vec3(r, g, b), 0.0, 1.0);
}

vec3 gradient2(float t) {
    float t2 = t * t;
    float t3 = t2 * t;
    float r = -0.05 + 1.5 * t - 0.4 * t2 - 0.1 * t3;
    float g = -0.1 + 0.2 * t + 1.3 * t2 - 0.5 * t3;
    float b = 0.1 - 0.3 * t + 0.5 * t2 + 0.1 * t3;
    return clamp(vec3(r, g, b), 0.0, 1.0);
}

void main(){

    float seconds = GameTime * 2400.0 * 0.5;

    float n = noise(texCoord, seconds);
    float m = noise2(texCoord, n);
    vec3 col = gradient2(m) * gradient(n);

    // finite-difference gradient of the underlying scalar field, used as a fake surface normal
    float eps = 0.003;
    float nL = noise(texCoord - vec2(eps, 0.0), seconds);
    float nR = noise(texCoord + vec2(eps, 0.0), seconds);
    float nD = noise(texCoord - vec2(0.0, eps), seconds);
    float nU = noise(texCoord + vec2(0.0, eps), seconds);
    vec2 normalXY = vec2(nR - nL, nU - nD) / (2.0 * eps);

    vec2 distortedUV = texCoord + clamp(normalXY * 0.0006, vec2(-0.02), vec2(0.02));
    vec3 scene = texture(InSampler, distortedUV).rgb;

    vec3 outColor = mix(scene, scene * (vec3(1.0) + col), 0.35);

    fragColor = vec4(outColor, 1.0);
}

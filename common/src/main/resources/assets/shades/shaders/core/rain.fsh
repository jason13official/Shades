#version 330

// rain-on-glass: same bump-mapped noise-height technique as copper_shades' getCopper() (two
// overlapping noise samples at different scales/scroll speeds), retuned for bigger, slower,
// downward-scrolling droplet-like blobs and a cool/clear tint instead of copper's warm one.
// Refracts the real background, looking at the world through a rain-speckled pane
#moj_import <minecraft:globals.glsl>

uniform sampler2D InSampler;

// real window aspect ratio, pushed fresh each frame by ShadesClient
layout(std140) uniform RainConfig {
    float Aspect;
};

in vec2 texCoord;
out vec4 fragColor;

float hash2(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

float noise2(vec2 x) {
    vec2 i = floor(x);
    vec2 f = fract(x);
    f = f * f * (3.0 - 2.0 * f);
    return mix(
        mix(hash2(i + vec2(0.0, 0.0)), hash2(i + vec2(1.0, 0.0)), f.x),
        mix(hash2(i + vec2(0.0, 1.0)), hash2(i + vec2(1.0, 1.0)), f.x),
        f.y
    );
}

// same shape as copper_shades' getCopper(), two overlapping noise samples at different scales,
// scrolling straight DOWN over time (only the y offset moves) so droplets read as streaking
float getDroplets(vec2 uv, float t) {
    uv.x *= 2.0;
    float t0 = noise2(uv * 2.0 - vec2(0.0, t) * 0.3);
    float t1 = noise2(uv * 3.0 - vec2(0.0, t) * 0.6) * 0.5;
    return smoothstep(0.35, 0.55, t0 * t1 * 2.0);
}

void main(){

    float t = GameTime * 2400.0 * 0.5;
    vec2 uv = texCoord;
    uv.x *= Aspect;
    float eps = 0.004;

    float p0 = getDroplets(uv, t);
    float p1 = getDroplets(uv + vec2(0.0, eps), t);
    float p2 = getDroplets(uv + vec2(eps, eps), t);
    vec3 normal = normalize(vec3(p0 - p1, p2 - p1, 0.5));

    // bend the real background sample through the droplets' own bumps, same "looking through
    // wavy glass" trick copper_shades/fluted_glass_vision use
    vec2 distortedUV = texCoord + normal.xy * 0.02;
    vec3 scene = texture(InSampler, distortedUV).rgb;

    vec3 lightDir = normalize(vec3(0.25, 0.75, 0.2) - vec3((uv - 0.5) * 2.0, 0.0));
    float diffuse = max(dot(lightDir, normal), 0.0);
    vec3 viewDir = vec3(0.0, 0.0, 1.0);
    vec3 halfVec = normalize(lightDir + viewDir);
    float specular = pow(max(dot(normal, halfVec), 0.0), 40.0);

    // mostly clear, just a faint cool tint; real rain-on-glass barely colors what's behind it,
    // unlike copper_shades' much stronger warm tint
    vec3 rainTint = vec3(0.75, 0.85, 1.0);
    vec3 outColor = scene * mix(vec3(1.0), rainTint, 0.2) * (0.8 + diffuse * 0.3) + specular * 0.5;

    fragColor = vec4(outColor, 1.0);
}

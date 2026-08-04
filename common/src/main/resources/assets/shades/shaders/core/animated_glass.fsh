#version 330

// fluted_glass_vision's derivative-as-normal refraction trick, live so the ridges can actually
// scroll and the light can sweep over real time instead of being frozen at load-time phase
#moj_import <minecraft:globals.glsl>

uniform sampler2D InSampler;

in vec2 texCoord;
out vec4 fragColor;

const float PI = 3.14159265;
const float FLUTE_COUNT = 40.0;
const float DISTORTION_AMOUNT = 0.012;

void main(){

    float t = GameTime * 2400.0;

    // same ridge shape as fluted_glass_vision, but the phase drifts sideways over time so the
    // ridges themselves crawl across the screen instead of sitting still
    float flutePosition = fract(texCoord.x * FLUTE_COUNT + t * 0.4);
    float slope = cos(flutePosition * PI * 2.0) * PI;

    // a slow vertical shimmer term on top of the ridge slope, so the glass reads as gently
    // rippling instead of just sliding sideways as a rigid pattern
    float shimmer = sin(t * 0.4 + texCoord.y * 10.0) * 0.05;
    vec3 normal = normalize(vec3(slope * 0.15, shimmer, 1.0));

    vec2 distortedUV = texCoord + normal.xy * DISTORTION_AMOUNT;
    vec4 diffuseColor = texture(InSampler, distortedUV);

    // the light itself slowly orbits instead of sitting fixed, so the specular highlight sweeps
    // across the ridges over time rather than pinning to one static band
    vec3 lightDir = normalize(vec3(sin(t * 0.15) * 0.6 - 0.2, 0.5, 0.75));

    float diffuse = max(dot(normal, lightDir), 0.0);
    vec3 viewDir = vec3(0.0, 0.0, 1.0);
    vec3 halfVec = normalize(lightDir + viewDir);
    float specular = pow(max(dot(normal, halfVec), 0.0), 40.0);

    vec3 outColor = diffuseColor.rgb * (0.6 + diffuse * 0.5) + specular * 0.5;

    fragColor = vec4(outColor, 1.0);
}

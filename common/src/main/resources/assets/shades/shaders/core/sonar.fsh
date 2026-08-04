#version 330

// live GameTime, same trick as plasma.fsh
#moj_import <minecraft:globals.glsl>

uniform sampler2D InSampler;
uniform sampler2D InDepthSampler;

// combined inverse(ProjMat * ViewRotationMat) + camera position + ping origin, pushed fresh each
// frame by ShadesClient (real GameRenderState fields, not ambient/stale bound uniforms.
// Matches orbital_railgun's strike.fsh worldPos() technique
layout(std140) uniform CameraRay {
    mat4 InverseTransformMatrix;
    vec3 CameraPosition;
    vec3 PingOrigin;
};

in vec2 texCoord;
out vec4 fragColor;

const vec3 PING_COLOR = vec3(0.3, 0.9, 1.0);
const float SONAR_RANGE = 48.0; // blocks

vec3 worldPos(vec3 screenPoint) {
    vec3 ndc = screenPoint * 2.0 - 1.0;
    vec4 homPos = InverseTransformMatrix * vec4(ndc, 1.0);
    return homPos.xyz / homPos.w + CameraPosition;
}

void main(){

    float t = GameTime * 2400.0;
    float rawDepth = texture(InDepthSampler, texCoord).r;

    vec3 surfacePos = worldPos(vec3(texCoord, rawDepth));
    float distanceFromOrigin = length(surfacePos - PingOrigin);

    // an expanding ring sweeping outward from PingOrigin and looping, in genuine blocks
    float pingFraction = mod(t * 0.06, 1.0);
    float ping = pingFraction * SONAR_RANGE;

    // inverse-distance glow: bright, naturally-falling-off ring instead of a flat band
    float shellDist = abs(distanceFromOrigin - ping);
    float ring = clamp(1.0 / (shellDist + 0.3) - 0.23, 0.0, 3.0);

    // sustain-then-fade: stays bright most of the sweep, only dims near the end
    float fade = 1.0 - smoothstep(0.6, 1.0, pingFraction);
    ring *= fade;

    vec3 color = texture(InSampler, texCoord).rgb;
    color += PING_COLOR * ring;

    fragColor = vec4(color, 1.0);
}

#version 330

// reworked: the previous version was a from-scratch single-pass wave-sim substitute (the pasted
// shadertoy script was only the DISPLAY pass of a multi-buffer CFD setup - the real simulation-
// update pass was never given), reading/writing its own previous frame's height field through
// PrevFrameSampler. In practice that home-grown sim produced unstable flat-shard artifacts rather
// than a convincing ripple. Replaced with an analytic multi-sine ripple field sampled at each
// pixel's real depth-reconstructed world position - same worldPos() technique grid_shades/
// waveform_shades/sonar_shades all use, no feedback/simulation state needed at all, fully
// deterministic. Same "look at the world through something" refraction recipe fluted_glass_vision/
// molten_glass use, just driven by real world XZ instead of a fixed screen-space ridge pattern
#moj_import <minecraft:globals.glsl>

uniform sampler2D InSampler;
uniform sampler2D InDepthSampler;

// combined inverse-projection*view matrix + camera position, pushed fresh each frame by
// ShadesClient - same worldPos() reconstruction sonar.fsh/grid.fsh use
layout(std140) uniform FluidRay {
    mat4 InverseTransformMatrix;
    vec3 CameraPosition;
};

in vec2 texCoord;
out vec4 fragColor;

vec3 worldPos(vec3 screenPoint) {
    vec3 ndc = screenPoint * 2.0 - 1.0;
    vec4 homPos = InverseTransformMatrix * vec4(ndc, 1.0);
    return homPos.xyz / homPos.w + CameraPosition;
}

// three overlapping traveling ripples at different frequencies/speeds/directions, summed - the
// same "layer a few sine waves" idea plasma.fsh/molten_glass.fsh's churn use, just sampled in
// real world XZ instead of screen UV
float heightAt(vec2 xz, float t) {
    float h = 0.0;
    h += sin(xz.x * 0.5 + xz.y * 0.3 + t * 1.2) * 0.5;
    h += sin(xz.x * 0.3 - xz.y * 0.6 + t * 0.8) * 0.3;
    h += sin(xz.x * 0.9 + xz.y * 0.9 - t * 1.6) * 0.2;
    return h;
}

void main(){

    float t = GameTime * 2400.0 * 0.5;
    float rawDepth = texture(InDepthSampler, texCoord).r;

    // sky/no real geometry here -> nothing to ripple, leave the pixel untouched
    if (rawDepth >= 0.9999) {
        fragColor = texture(InSampler, texCoord);
        return;
    }

    vec3 surfacePos = worldPos(vec3(texCoord, rawDepth));
    vec2 xz = surfacePos.xz;

    // finite-difference normal from the ripple field, same approximate-gradient shortcut
    // fluted_glass_vision/molten_glass.fsh use rather than an analytic derivative
    float eps = 0.15;
    float hL = heightAt(xz - vec2(eps, 0.0), t);
    float hR = heightAt(xz + vec2(eps, 0.0), t);
    float hD = heightAt(xz - vec2(0.0, eps), t);
    float hU = heightAt(xz + vec2(0.0, eps), t);
    vec3 normal = normalize(vec3(hL - hR, hD - hU, eps * 6.0));

    // bend the real background sample through the ripple's own bumps
    vec2 distortedUV = texCoord + normal.xy * 0.015;
    vec3 scene = texture(InSampler, distortedUV).rgb;

    vec3 lightDir = normalize(vec3(0.3, 0.6, 0.5));
    float diffuse = max(dot(normal, lightDir), 0.0);
    vec3 halfVec = normalize(lightDir + vec3(0.0, 0.0, 1.0));
    float specular = pow(max(dot(normal, halfVec), 0.0), 60.0);

    vec3 waterTint = vec3(0.55, 0.8, 0.95);
    vec3 outColor = mix(scene, scene * waterTint, 0.3) * (0.7 + diffuse * 0.4) + specular * 0.6;

    fragColor = vec4(outColor, 1.0);
}

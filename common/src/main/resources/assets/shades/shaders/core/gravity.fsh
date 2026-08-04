#version 330

// stretches the view downward while falling (speed-scaled), then snaps into an expanding
// shockwave ring the instant the player lands
#moj_import <minecraft:globals.glsl>

uniform sampler2D InSampler;

// real window aspect ratio, smoothed 0..1 fall-speed factor, and seconds since the last landing
// (large sentinel when no recent landing), pushed fresh each frame by ShadesClient
layout(std140) uniform GravityConfig {
    float Aspect;
    float FallFactor;
    float ShockAge;
};

in vec2 texCoord;
out vec4 fragColor;

void main(){

    // pulls samples downward from the current pixel, proportional to fall speed, so the world
    // reads as streaking upward past the camera while falling
    vec2 stretchUv = texCoord;
    stretchUv.y -= (texCoord.y - 0.5) * FallFactor * 0.15;

    vec3 scene = texture(InSampler, clamp(stretchUv, 0.0, 1.0)).rgb;
    scene = mix(scene, scene * vec3(0.85, 0.9, 1.0), FallFactor * 0.5);

    vec2 uv = (texCoord * 2.0 - 1.0) * vec2(Aspect, 1.0);
    float radius = length(uv);

    // ring expands outward from the impact, fading out with age
    float ringRadius = ShockAge * 4.0;
    float ring = 1.0 - smoothstep(0.0, 0.15, abs(radius - ringRadius));
    float fade = 1.0 - smoothstep(0.0, 0.6, ShockAge);
    vec3 outColor = scene + vec3(0.9, 0.85, 0.6) * ring * fade * 0.6;

    fragColor = vec4(outColor, 1.0);
}

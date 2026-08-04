#version 330

// silvery night vignette that strengthens with real sky darkness, colored/brightened by the real
// current moon phase, plus a scattering of twinkling stars that only show up at night
#moj_import <minecraft:globals.glsl>

uniform sampler2D InSampler;

// real window aspect ratio, current moon phase as 0 (full) .. 1 (new, via MoonPhase.index()/7),
// and real sky darkness 0 (day) .. 1 (night), pushed fresh each frame by ShadesClient
layout(std140) uniform LunarConfig {
    float Aspect;
    float PhaseFraction;
    float NightFactor;
};

in vec2 texCoord;
out vec4 fragColor;

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453123);
}

void main(){

    vec3 scene = texture(InSampler, texCoord).rgb;

    // brightest/whitest at full moon (PhaseFraction 0), dim and slightly warm at new moon
    float moonStrength = 1.0 - PhaseFraction;
    vec3 moonTint = mix(vec3(0.6, 0.5, 0.45), vec3(0.65, 0.75, 1.0), moonStrength);

    float amount = NightFactor * (0.25 + moonStrength * 0.35);
    vec3 graded = mix(scene, scene * moonTint * 1.3, amount);

    // sparse twinkling star field, screen-space, only visible once it's dark enough; floored so
    // moonStrength hitting exactly 0 at the dimmest phase can't fully zero the stars out
    vec2 cell = floor(texCoord * vec2(120.0 * Aspect, 120.0));
    float starChance = hash(cell);
    float seconds = GameTime * 2400.0 * 0.5;
    float twinkle = sin(seconds * 3.0 + starChance * 40.0) * 0.5 + 0.5;
    float starVisibility = mix(0.6, 1.0, moonStrength);
    float star = step(0.995, starChance) * twinkle * NightFactor * starVisibility;

    vec3 outColor = graded + vec3(star);

    fragColor = vec4(outColor, 1.0);
}

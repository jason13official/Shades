#version 330

// "Cosmic Strands" by Noztol: 7 layers of swirling, pulsing glowing strands, built by repeatedly
// rotating and warping the sample space. Fully synthetic in the original (a flat starfield
// background); composited here as a Reinhard-tonemapped additive glow over the real world instead
#moj_import <minecraft:globals.glsl>

uniform sampler2D InSampler;

// real window aspect ratio, pushed fresh each frame by ShadesClient
layout(std140) uniform CosmicConfig {
    float Aspect;
};

in vec2 texCoord;
out vec4 fragColor;

void main(){

    float seconds = GameTime * 2400.0 * 0.5;
    vec2 uv = (texCoord * 2.0 - 1.0) * vec2(Aspect, 1.0) * 1.5;

    vec3 glow = vec3(0.0);
    float t = seconds * 0.2;

    float angle = 0.5;
    mat2 rot = mat2(cos(angle), -sin(angle), sin(angle), cos(angle));

    for (float i = 0.0; i < 7.0; i++) {
        uv *= rot;
        uv.y += sin(uv.x * (1.5 + i * 0.2) + t * (1.2 + i * 0.5)) * 0.35;
        uv.x += cos(uv.y * (1.2 + i * 0.3) - t * (0.8 + i * 0.4)) * 0.35;

        float strand = 0.015 / (abs(uv.y) + 0.01);

        // fade each strand layer in and out over time instead of leaving all 7 on constantly
        float pulse = sin(t * 6.0 + i * 2.14) * 0.5 + 0.5;
        pulse = smoothstep(0.1, 0.9, pulse);
        strand *= pulse;

        vec3 color = 0.5 + 0.5 * cos(seconds * 0.3 + uv.xyx * 2.0 + vec3(0.0, 2.0, 4.0) + i * 0.8);

        glow += strand * color;
    }

    glow = glow / (1.0 + glow);
    glow = pow(glow, vec3(0.7));

    vec3 scene = texture(InSampler, texCoord).rgb;
    vec3 outColor = scene + glow * 0.6;

    fragColor = vec4(outColor, 1.0);
}

#version 330

uniform sampler2D InSampler;

in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

out vec4 fragColor;

/// simplified single-pass stand-in for 1.20.1's real NTSC chain (ntsc_decode -> color_convolve ->
/// deconverge -> phosphor -> scan_pincushion -> ntsc_encode -> blur, 8 passes with real cross-
/// frame phosphor persistence): a flat CRT-in-one-pass look combining the same ingredients this
/// mod already ported standalone -> deconverge's channel split (deconverge_vision), scan_pincushion's
/// scanline darkening/curved clip (scan_pincushion_vision); minus phosphor's temporal trail,
/// since a plain post_effect pass can't carry state across frames (see phosphor_shades, which
/// needs the live pipeline for exactly that reason)
const vec3 CONVERGE_X = vec3(-1.5, 0.0, 1.5);
const float PINCUSHION_AMOUNT = 0.015;
const float SCANLINE_AMOUNT = 0.7;
const vec3 FLOOR = vec3(0.04);

void main() {

    vec2 oneTexel = 1.0 / InSize;

    vec2 pinUnitCoord = texCoord * 2.0 - 1.0;
    float pincushionR2 = pow(length(pinUnitCoord), 2.0);
    vec2 scanCoord = texCoord * (1.0 - PINCUSHION_AMOUNT * 0.2) + PINCUSHION_AMOUNT * 0.1
        + pinUnitCoord * PINCUSHION_AMOUNT * pincushionR2;

    if (scanCoord.x < 0.0 || scanCoord.y < 0.0 || scanCoord.x > 1.0 || scanCoord.y > 1.0) {
        fragColor = vec4(0.0, 0.0, 0.0, 1.0);
        return;
    }

    // channel deconvergence, sampled around the pincushion-warped coordinate
    float redValue = texture(InSampler, scanCoord + vec2(CONVERGE_X.x * oneTexel.x, 0.0)).r;
    float greenValue = texture(InSampler, scanCoord + vec2(CONVERGE_X.y * oneTexel.x, 0.0)).g;
    float blueValue = texture(InSampler, scanCoord + vec2(CONVERGE_X.z * oneTexel.x, 0.0)).b;
    vec3 color = vec3(redValue, greenValue, blueValue);

    float scanBrightMod = sin(scanCoord.y * InSize.y * 0.25 * 3.1415926535);
    float scanBrightness = mix(1.0, (scanBrightMod * scanBrightMod + 1.0) * 0.5, SCANLINE_AMOUNT);
    color *= scanBrightness;
    color = FLOOR + (1.0 - FLOOR) * color;

    fragColor = vec4(color, 1.0);
}

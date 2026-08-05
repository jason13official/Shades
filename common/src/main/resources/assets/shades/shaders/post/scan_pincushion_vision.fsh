#version 330

uniform sampler2D InSampler;

in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

out vec4 fragColor;

/// direct port of 1.20.1's scan_pincushion.fsh: a curved screen-edge clip plus CRT scanline
/// darkening and a lifted-black/gamma color compression. The original `discard`ed pixels outside
/// the pincushion curve, leaving whatever the target already held; we paint them solid black
/// instead, matching the "black borders" the wiki describes for shaders built on this pass
const float PINCUSHION_AMOUNT = 0.02;
const float SCANLINE_AMOUNT = 0.8;
const vec3 FLOOR = vec3(0.05);
const vec3 POWER = vec3(0.8);

void main() {

    vec2 pinUnitCoord = texCoord * 2.0 - 1.0;
    float pincushionR2 = pow(length(pinUnitCoord), 2.0);
    vec2 pincushionCurve = pinUnitCoord * PINCUSHION_AMOUNT * pincushionR2;

    vec2 scanCoord = texCoord * (1.0 - PINCUSHION_AMOUNT * 0.2) + PINCUSHION_AMOUNT * 0.1 + pincushionCurve;

    if (scanCoord.x < 0.0 || scanCoord.y < 0.0 || scanCoord.x > 1.0 || scanCoord.y > 1.0) {
        fragColor = vec4(0.0, 0.0, 0.0, 1.0);
        return;
    }

    vec4 inTexel = texture(InSampler, texCoord);

    float innerSine = scanCoord.y * InSize.y * 0.25;
    float scanBrightMod = sin(innerSine * 3.1415926535);
    float scanBrightness = mix(1.0, (scanBrightMod * scanBrightMod + 1.0) * 0.5, SCANLINE_AMOUNT);

    vec3 scanlineTexel = inTexel.rgb * scanBrightness;
    scanlineTexel = FLOOR + (1.0 - FLOOR) * scanlineTexel;
    scanlineTexel = pow(scanlineTexel, POWER);

    fragColor = vec4(scanlineTexel, 1.0);
}

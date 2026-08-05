#version 330

uniform sampler2D InSampler;

in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

out vec4 fragColor;

/// direct port of 1.20.1's bumpy.fsh: treats each sampled color as a vector and compares
/// neighbor directions via dot product to fake a lit bump-map look from color differences alone
void main() {

    vec2 oneTexel = 1.0 / InSize;
    vec4 c = texture(InSampler, texCoord);
    vec4 u = texture(InSampler, texCoord + vec2(0.0, -oneTexel.y));
    vec4 d = texture(InSampler, texCoord + vec2(0.0, oneTexel.y));
    vec4 l = texture(InSampler, texCoord + vec2(-oneTexel.x, 0.0));
    vec4 r = texture(InSampler, texCoord + vec2(oneTexel.x, 0.0));

    vec4 nc = normalize(c);
    vec4 nu = normalize(u);
    vec4 nd = normalize(d);
    vec4 nl = normalize(l);
    vec4 nr = normalize(r);

    float i = 64.0;
    float f = 1.0;
    f += (dot(nc, nu) * i) - (dot(nc, nd) * i);
    f += (dot(nc, nr) * i) - (dot(nc, nl) * i);

    vec3 color = c.rgb * clamp(f, 0.5, 2.0);
    fragColor = vec4(color, 1.0);
}

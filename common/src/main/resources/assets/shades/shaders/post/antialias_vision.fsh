#version 330

uniform sampler2D InSampler;

in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

out vec4 fragColor;

/// direct port of 1.20.1's antialias.fsh: a 12-tap min-erosion across two ring sizes (the same
/// family as sobel_vision's kernel, but without the final min-against-original that turns it into
/// an edge-only look); just softens/smears jagged edges instead of isolating them
void main() {

    vec2 oneTexel = 1.0 / InSize;

    vec4 u = texture(InSampler, texCoord + vec2(0.0, -oneTexel.y));
    vec4 d = texture(InSampler, texCoord + vec2(0.0, oneTexel.y));
    vec4 l = texture(InSampler, texCoord + vec2(-oneTexel.x, 0.0));
    vec4 r = texture(InSampler, texCoord + vec2(oneTexel.x, 0.0));
    vec4 v3 = min(min(l, r), min(u, d));

    vec4 ul = texture(InSampler, texCoord + vec2(-oneTexel.x, -oneTexel.y));
    vec4 dr = texture(InSampler, texCoord + vec2(oneTexel.x, oneTexel.y));
    vec4 dl = texture(InSampler, texCoord + vec2(-oneTexel.x, oneTexel.y));
    vec4 ur = texture(InSampler, texCoord + vec2(oneTexel.x, -oneTexel.y));
    vec4 v6 = min(min(ul, dr), min(ur, dl));

    vec4 v7 = min(v3, v6);

    vec4 uu = texture(InSampler, texCoord + vec2(0.0, -oneTexel.y) * 2.0);
    vec4 dd = texture(InSampler, texCoord + vec2(0.0, oneTexel.y) * 2.0);
    vec4 ll = texture(InSampler, texCoord + vec2(-oneTexel.x, 0.0) * 2.0);
    vec4 rr = texture(InSampler, texCoord + vec2(oneTexel.x, 0.0) * 2.0);
    vec4 v10 = min(min(uu, dd), min(ll, rr));

    vec4 v11 = min(v7, v10);
    vec4 c = texture(InSampler, texCoord);

    fragColor = vec4(min(c, v11).rgb, 1.0);
}
